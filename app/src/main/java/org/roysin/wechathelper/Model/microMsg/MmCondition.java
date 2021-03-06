package org.roysin.wechathelper.Model.microMsg;

import android.content.Intent;

import org.roysin.wechathelper.Model.BehaviourRecorder;
import org.roysin.wechathelper.Model.IconShownCondition;
import org.roysin.wechathelper.Utils.LogUtil;

/**
 * Created by Zangyakui on 2016/5/31.
 */
public class MmCondition extends IconShownCondition {

    private static final String TAG = "MmCondition";

    public MmCondition(BehaviourRecorder recorder) {
        super(recorder);
    }
    @Override
    protected boolean onDecideToShow(Intent intent) {
        boolean result = false;
        if(intent != null){
            String value = null;
            try{
                //ClassNotFoundException may throw.
                value = intent.getStringExtra("nofification_type");
            }catch (Exception e){
                value = null;
            }
            if("new_msg_nofification".equals(value)){
                result = true;
            }
        }
        LogUtil.d(TAG," onCompareAdditionalCondition result: "+ result);
        return result;
    }
}
