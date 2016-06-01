package org.roysin.wechathelper;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.IActivityController;
import android.app.IActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import org.roysin.wechathelper.Model.BehaviourRecorder;
import org.roysin.wechathelper.Model.FloatIconController;
import org.roysin.wechathelper.Model.IconShownCondition;
import org.roysin.wechathelper.Model.MmCondition;
import org.roysin.wechathelper.Utils.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


/**
 * Created by Administrator on 2016/3/19.
 */
public class BackToReadingService extends Service {

    private static final String TAG = "BackToReadingService";
    private static final int MSG_ACTIVITY_RESUMING = 0x02;
    private static final long DELAY_GET_TASKS = 600;


    private IActivityManager mAm;
    private IActivityController mMonitor;
    private WindowManager mWm;
    private List<BehaviourRecorder> mRecorders = new ArrayList<>();
    private Handler mHandler;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");
        flags |= START_STICKY;
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate");
        if(mHandler == null) mHandler = new Handler(BackToReadingService.this.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case MSG_ACTIVITY_RESUMING:
                        handleActvityResuming();
                }
            }
        };

        if(mMonitor == null){
            mMonitor = new IntentMonitor();
        }
        try{
            mAm = ActivityManagerNative.getDefault();
            mAm.setActivityController(mMonitor);
        }catch (RemoteException e){
            Log.d(TAG,e.toString());
            mAm = null;
        }


        //tencent wechat(micromsg) recorder.
        final BehaviourRecorder mmRecorder = new BehaviourRecorder(Constants.WEIXIN_PKGNAME,BackToReadingService.this);
        final String [] mmRecordablePages = new String[]{Constants.READING_PAGE_CLASS};
        mmRecorder.setRecordablePages(mmRecordablePages);


        String [] mmHiddenPages = new String[]{
                Constants.GALLERY_UI_CLASS,
                Constants.SNS_TIMELINE_CLASS
        };
        final IconShownCondition mmCondition = new MmCondition(mmRecorder);
        mmCondition.setHiddenPages(mmHiddenPages);

        final FloatIconController mmIconController = new FloatIconController(BackToReadingService.this);
        mmIconController.bindCondition(mmCondition);
        mmIconController.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mmRecorder.resumeTopActivity();
                mmIconController.remove();
            }
        });
        mmIconController.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mmIconController.remove();
                return false;
            }
        });
        // add your recorders here.
        mRecorders.add(mmRecorder);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind");
        return null;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        try{
            mAm.setActivityController(null);
        }catch (RemoteException e){
            Log.d(TAG,e.toString());
            mAm = null;
        }
        super.onDestroy();
    }

    protected class StartTask implements Runnable {
        private String mPkg;
        private Intent mIntent;
        public void setParams(Intent intent, String pkg){
            mPkg = pkg;
            mIntent = intent;
        }
        @Override
        public void run() {
            for (BehaviourRecorder r : mRecorders) {
                if (mPkg != null && mPkg.equals(r.getPackageName())) {
                    if (!r.isEnabled()) {
                        r.enable();
                    } else {
                        r.recordBehaviour(mIntent);
                    }
                } else {
                    r.disable();
                }

            }
        }
    }

    protected class IntentMonitor extends IActivityController.Stub {
        StartTask startingTask = null;
        public void IntentMonitor() {
            Log.i(TAG, "IntentMonitor created");
        }
        public boolean activityStarting(final Intent intent, final String pkg) {
            Log.i(TAG, "activityStarting with intent " + intent);
            if(startingTask == null ){
                startingTask = new StartTask();
            }
            startingTask.setParams(intent,pkg);
            mHandler.post(startingTask);
            return true;
        }

        @Override
        public boolean activityResuming(String pkg) {
            Log.i(TAG, "activityResuming pkg = " +pkg);
            //we can't get resuming activity as pkg was given only. check tasks
            //to get top Activity after some time, because tasks will not update immediately.
            mHandler.sendEmptyMessageDelayed(MSG_ACTIVITY_RESUMING,DELAY_GET_TASKS);
            return true;
        }


        @Override
        public boolean appCrashed(String processName, int pid, String shortMsg, String longMsg,
                                  long timeMillis, String stackTrace) {
            Log.i(TAG, "appCrashed() processName : " + processName + " pid : " + pid);
            return true;
        }

        @Override
        public int appEarlyNotResponding(String processName, int pid, String annotation) {
            Log.d(TAG, "appEarlyNotResponding() processName : " + processName + " pid : " + pid);
            return 0;
        }


        @Override
        public int appNotResponding(String processName, int pid, String processStats) {
            Log.d(TAG, "appNotResponding() processName : " + processName);
            return 0;
        }


        @Override
        public int systemNotResponding(String msg) {
            Log.d(TAG, "appNotResponding() msg : " + msg);
            return 0;
        }


    }


    private void handleActvityResuming() {
        List<ActivityManager.RunningTaskInfo> taskInfo = null;
        ComponentName component = null;
        try{
            component = mAm.getTasks(1,0).get(0).topActivity;
        }catch (RemoteException e){
            Log.d(TAG,e.toString());
        }
        Log.d(TAG, "real topActivity is " +component.getClassName());

        String pkg = component.getPackageName();
        for(BehaviourRecorder r :mRecorders)
            if(pkg != null && pkg.equals(r.getPackageName())){
                if(!r.isEnabled()){
                    r.enable();
                }else{
                    r.recordBehaviour(component);
                }
            }else {
                r.disable();
            }
    }

    public static void startSelf(Context context){
        Intent service = new Intent();
        service.setClassName(context.getPackageName(), "org.roysin.wechathelper.BackToReadingService");
        context.startService(service);
        Toast.makeText(context, "微信助手服务已启动", Toast.LENGTH_SHORT).show();
    }

    public static void stop(Context context){
        Intent service = new Intent();
        service.setClassName(context.getPackageName(),"org.roysin.wechathelper.BackToReadingService");
        context.stopService(service);
        Toast.makeText(context, "微信助手服务已关闭", Toast.LENGTH_SHORT).show();
    }
}
