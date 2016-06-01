package org.roysin.wechathelper.Model.QQ;

import android.content.Intent;

import org.roysin.wechathelper.Model.BehaviourRecorder;
import org.roysin.wechathelper.Model.IconShownCondition;
import org.roysin.wechathelper.Utils.LogUtil;

/**
 * Created by Administrator on 2016/6/1.
 */
public class QQCondition extends IconShownCondition {
    public QQCondition(BehaviourRecorder qqRecorder) {
        super(qqRecorder);
    }

    @Override
    protected boolean onDecideToShow(Intent intent) {
        LogUtil.printLog(intent);
        boolean result = false;
        if(intent != null){
            try{
                if("notifcation".equals(intent.getStringExtra("KEY_FROM")))
                    result = true;
            }catch (Exception e){
                e.printStackTrace();
                result = false;
            }
        }
        return result;
    }
}
