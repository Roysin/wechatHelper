package org.roysin.wechathelper.Utils;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.util.Set;

/**
 * Created by Administrator on 2016/5/31.
 */
public class LogUtil {

    private static final String TAG ="BackToReadingDebug" ;

    public static void log(String msg){
        Log.d(TAG,msg);
    }
    public static void d(String TAG, String msg ){
        Log.d(TAG,msg);
    }
    public static void printLog(Intent intent) {
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
}
