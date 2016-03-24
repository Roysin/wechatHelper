package org.roysin.wechathelper;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.IActivityController;
import android.app.IActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Toast;

import java.util.List;
import java.util.Set;


/**
 * Created by Administrator on 2016/3/19.
 */
public class BackToReadingService extends Service {

    private static final String TAG = "BackToReadingService";
    private static final int MSG_SHOW_FLOAT_BTN = 0x01;
    private static final int MSG_REMOVE_FLOAT_BTN = 0x02;
    private static final int MSG_ACTIVITY_RESUMING = 0x03;
    private static final long DELAY_GET_TASKS = 600;


    private IActivityManager mAm;
    private IActivityController mMonitor;
    private WindowManager mWm;


    private PageStatus mStatus;
    private boolean mShowing = false;
    private Handler mHandler;
    private NotificationListenerService ntfListener;


    private View mFloatBtn;
    private WindowManager.LayoutParams mFloatParams;
    private Animation mShowingAni = null;

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

        if(mMonitor == null){
            mMonitor = new IntentMonitor();
        }

        mWm = (WindowManager) getSystemService(WINDOW_SERVICE);

        try{
            mAm = ActivityManagerNative.getDefault();
            mAm.setActivityController(mMonitor);
        }catch (RemoteException e){
            Log.d(TAG,e.toString());
            mAm = null;
        }
        ntfListener = new NotificationListenerService() {
            @Override
            public void onNotificationPosted(StatusBarNotification sbn) {
                super.onNotificationPosted(sbn);
                if(Constants.WEIXIN_PKGNAME.equals(sbn.getPackageName())){

                }
            }

            @Override
            public void onNotificationRemoved(StatusBarNotification sbn) {
                super.onNotificationRemoved(sbn);
            }
        };


        mStatus = new PageStatus();
        if(mHandler == null) mHandler = new Handler(BackToReadingService.this.getMainLooper()) {

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case MSG_SHOW_FLOAT_BTN:
                        showFloatBtn();
                        break;
                    case MSG_REMOVE_FLOAT_BTN:
                        mHandler.removeMessages(MSG_SHOW_FLOAT_BTN);
                        removeFloatBtn();
                        break;
                    case MSG_ACTIVITY_RESUMING:
                        handleActvityResuming();
                }
            }
        };


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
        if(component == null || (!"com.tencent.mm".equals(component.getPackageName()))){
            if(mShowing) {
                mStatus.disapperReason = PageStatus.REASON_SWITCH_APP;
                mHandler.sendEmptyMessage(MSG_REMOVE_FLOAT_BTN);
            }
            return;
        }
        mStatus.update(component,false);

        Log.i(TAG, "status : currentPage: " + mStatus.currentPage
                + " lastPage: " + mStatus.lastPage
                + " lastIntent: " + mStatus.lastReadingPageIntent
                + " firstStart: " + mStatus.firstStart);

        if(mStatus.currentPage == PageStatus.READING_PAGE){
            if(mShowing){
                mStatus.disapperReason = PageStatus.REASON_READING;
            }
            mHandler.sendEmptyMessage(MSG_REMOVE_FLOAT_BTN);
        }else if((mStatus.disapperReason == PageStatus.REASON_SWITCH_APP
                || mStatus.disapperReason == PageStatus.REASON_GALLERY)
                && mStatus.lastReadingPageIntent != null){
            mHandler.sendEmptyMessage(MSG_SHOW_FLOAT_BTN);
        }

    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind");
        return null;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        super.onDestroy();
    }

    protected class IntentMonitor extends IActivityController.Stub {


        public void IntentMonitor() {
            Log.i(TAG, "IntentMonitor created");
        }

        @Override
        public boolean activityStarting(Intent intent, String pkg) {
            Log.i(TAG, "activityStarting with intent " + intent);
            if(intent != null && ("com.tencent.mm".equals(pkg))){
//                printLog(intent);
                mStatus.update(intent, true);
                Log.i(TAG, "status : currentPage: " + mStatus.currentPage
                        + " lastPage: " + mStatus.lastPage
                        + " lastIntent: " + mStatus.lastReadingPageIntent
                        + " firstStart: " + mStatus.firstStart);

                if (mStatus.currentPage == PageStatus.CHATTING_PAGE
                        && mStatus.lastPage == PageStatus.READING_PAGE
                        && mStatus.lastReadingPageIntent != null) {
                    if ("new_msg_nofification".equals(intent.getStringExtra("nofification_type"))) {
                        mHandler.sendEmptyMessageDelayed(MSG_SHOW_FLOAT_BTN, DELAY_GET_TASKS);
                    }

                }else if (mStatus.currentPage == PageStatus.READING_PAGE) {
                    if(mShowing){
                        mStatus.disapperReason = PageStatus.READING_PAGE;
                    }
                    mHandler.sendEmptyMessage(MSG_REMOVE_FLOAT_BTN);
                }else if (mStatus.currentPage == PageStatus.GALLERY_PAGE){
                    if(mShowing){
                        mStatus.disapperReason = PageStatus.REASON_GALLERY;
                    }
                    mHandler.sendEmptyMessage(MSG_REMOVE_FLOAT_BTN);
                }
                return true;
            }
            if(mShowing){
                mStatus.disapperReason = PageStatus.REASON_SWITCH_APP;
            }
            mHandler.sendEmptyMessage(MSG_REMOVE_FLOAT_BTN);
            return true;
        }

        @Override
        public boolean activityResuming(String pkg) {
            Log.i(TAG, "activityResuming pkg = " +pkg);
            if("com.tencent.mm".equals(pkg)){
                //we can't get resuming activity as pkg was given only. check tasks
                //to get top Activity after some time, because tasks will not update immediately.
                mHandler.sendEmptyMessageDelayed(MSG_ACTIVITY_RESUMING,DELAY_GET_TASKS);
            }else {
                if(mShowing){
                    mStatus.disapperReason = PageStatus.REASON_SWITCH_APP;
                }
                mHandler.sendEmptyMessage(MSG_REMOVE_FLOAT_BTN);
            }

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

    private void removeFloatBtn() {
        Log.i(TAG,"removeFloatBtn ");
        if(mWm != null && mFloatBtn != null && mShowing){
            mWm.removeViewImmediate(mFloatBtn);
            mShowing = false;
        }
    }

    private void showFloatBtn() {

        Log.i(TAG,"showFloatBtn ");

        if(mFloatBtn == null){
            LayoutInflater inflater =  LayoutInflater.from(BackToReadingService.this);
            mFloatBtn = inflater.inflate(R.layout.float_button_layout,null);

            mFloatBtn.findViewById(R.id.start_activity).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(TAG, "onClick, startActivity now " );
                    if(mStatus != null && mStatus.lastReadingPageIntent != null){
                        int flags = mStatus.lastReadingPageIntent.getFlags();
                        flags |= Intent.FLAG_ACTIVITY_NEW_TASK;
                        mStatus.lastReadingPageIntent.setFlags(flags);
                        startActivity(mStatus.lastReadingPageIntent);
                    }
                }
            });
            mFloatBtn.findViewById(R.id.start_activity).setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    removeFloatBtn();
                    if(mStatus != null){
                        mStatus.reset();
                    }
                    return true;
                }
            });
        }
        if(mFloatParams == null){
            mFloatParams = new WindowManager.LayoutParams();
            mFloatParams.gravity = Gravity.TOP | Gravity.LEFT;
            mFloatParams.height = getResources().getDimensionPixelSize(R.dimen.float_height);
            mFloatParams.width = getResources().getDimensionPixelSize(R.dimen.float_height);
            mFloatParams.x = getResources().getDimensionPixelSize(R.dimen.float_x);
            mFloatParams.y = getResources().getDimensionPixelSize(R.dimen.float_y);
            mFloatParams.type = WindowManager.LayoutParams.TYPE_PRIORITY_PHONE;
            mFloatParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            ;
            mFloatParams.format = PixelFormat.TRANSPARENT;
        }
        if(mShowingAni == null){
            mShowingAni = new AlphaAnimation(0.0f,1.0f);
            mShowingAni.setFillAfter(true);
            mShowingAni.setDuration(300);
        }else {
            mShowingAni.cancel();
        }
        if(mWm != null && !mShowing) {
            Log.i(TAG, "now we are going to showFloatBtn ");
            mWm.addView(mFloatBtn, mFloatParams);
            mFloatBtn.findViewById(R.id.start_activity).startAnimation(mShowingAni);
            mShowing = true;
        }
    }

    private void printLog(Intent intent) {
        Log.i(TAG, "========================== beigin");
        Log.i(TAG, "intent: " +intent);
        if(intent != null){
            Bundle extras = intent.getExtras();
            if(extras != null) {
                Set<String> keyset = extras.keySet();
                for (String key : keyset) {
                    Log.i(TAG, "key: " + key + " value: " + extras.get(key));
                }
            }

            ComponentName component = intent.getComponent();

            if (component != null) {
                Log.i(TAG, "packageName: " + component.getPackageName()
                        + " , className: " + component.getShortClassName());


            }
        }
        Log.i(TAG, "========================== end");
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
