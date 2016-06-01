package org.roysin.wechathelper.Model;

import android.content.Intent;

import org.roysin.wechathelper.Utils.LogUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Zangyakui on 2016/5/30.
 */
public abstract class IconShownCondition implements BehaviourRecorder.BehaviourChangedListener{
    private static final String TAG = "IconShownCondition";
    public static final int REASON_PAGE_CHANGED = 0x01;
    public static final int REASON_ENTER_PACKAGE = 0x02;
    public static final int REASON_LEAVE_PACKAGE = 0x03;


    public static final int STATUS_SHOWING = 0x01;
    public static final int STATUS_HIDING = 0x02;
    public static final int STATUS_REMOVED = 0x03;


    private BehaviourRecorder mRecorder;
    private List<String> mHiddenPages;
    private int status;
    ArrayList<Callback> mCallbacks;


    public interface Callback{
        void onConditionDismatched(int reasonLeavePackage, int lastStatus, int currentStatus);
        void onConditionFitted(int reason,int lastStatus);
    }
    public IconShownCondition(BehaviourRecorder recorder){
        if(recorder == null){
            throw new IllegalArgumentException("recorder cannot be null");
        }
        this.mRecorder = recorder;
        mRecorder.setBehaviourChangedListener(this);
        status = STATUS_REMOVED;
    }



    public void resetStatus() {
        status = STATUS_REMOVED;
    }
    public void setHiddenPages(String [] hiddenPages){
        if(hiddenPages == null){
            throw new IllegalArgumentException("hidden pages cannot be null");
        }
        this.mHiddenPages =Arrays.asList(hiddenPages);
    }

    public void registerCallback(IconShownCondition.Callback callback){

        if(callback == null){
            throw new IllegalArgumentException("callback cannot be null");
        }
        if(mCallbacks == null){
            mCallbacks = new ArrayList<>();
        }
        if(mCallbacks.contains(callback)){
            return;
        }
        mCallbacks.add(callback);
    }

    protected void compareCondition(Intent intent, String prevPage, String newPage){
        int lastStatus = status;
        status = onCompareCondition(status,intent,prevPage,newPage);
        boolean fit = status == STATUS_SHOWING;
        LogUtil.d(TAG, "compareCondition fit: " + fit);
        if(fit){
            for(Callback cb : mCallbacks){
                cb.onConditionFitted(REASON_PAGE_CHANGED,lastStatus);
            }
        }else {
            for(Callback cb : mCallbacks){
                cb.onConditionDismatched(REASON_PAGE_CHANGED,lastStatus,status);
            }
        }
    }

    /**
     *
     * @param enterOrLeave true means enter,otherwise leave.
     */
    protected void compareCondition(boolean enterOrLeave){
        int lastStatus = status;
        status = onCompareAdditionalCondition(status,enterOrLeave);
        boolean fit = status == STATUS_SHOWING;
        LogUtil.d(TAG, "compareCondition fit: " + fit);
        if(mCallbacks != null){
            if(fit){
                for (Callback cb :mCallbacks){
                    cb.onConditionFitted(REASON_ENTER_PACKAGE,lastStatus);
                }
            }else {
                for (Callback cb :mCallbacks){
                    cb.onConditionDismatched(REASON_LEAVE_PACKAGE,lastStatus,status);
                }
            }
        }
    }

    protected int onCompareAdditionalCondition(int lastStatus, boolean enterOrLeave) {
        int result = lastStatus;
        if(enterOrLeave){ // entering the package.
            switch (lastStatus){
                case STATUS_HIDING:
                    // icon was hidden, we need to bring it back
                    // if current page is not a hidden page.
                    if(mHiddenPages == null ||
                            !mHiddenPages.contains(mRecorder.getCurrentPage())){
                        result = STATUS_SHOWING;
                    }
                    break;
            }//end switch
        }else { // leaving the package.
            switch (lastStatus){
                case STATUS_SHOWING:
                    result = STATUS_HIDING;
                    break;
            }
        }
        LogUtil.d(TAG," onCompareAdditionalCondition result: "+ result
                +" lastStatus: "+ lastStatus
                + " enterOrLeave:"+ enterOrLeave);
        return result;
    }


    protected int onCompareCondition(int lastStatus, Intent intent, String prevPage, String newPage) {

        int result = lastStatus;
        switch (lastStatus){
            case STATUS_REMOVED:
                if(mRecorder.isRecordablePage(prevPage) && !mRecorder.isRecordablePage(newPage)){
                    result = STATUS_SHOWING;
                    boolean ok = onDecideToShow(intent);
                    if(!ok){
                        result = lastStatus;
                    }
                }
                break;
            case STATUS_SHOWING:
                if(mHiddenPages!= null && mHiddenPages.contains(newPage)){
                    result = STATUS_HIDING;
                }
                if(mRecorder.isRecordablePage(newPage)) {
                    result = STATUS_REMOVED;
                }
                break;
            case STATUS_HIDING:
                if(mHiddenPages== null || !mHiddenPages.contains(newPage)){
                    result = STATUS_SHOWING;
                }
                if(mRecorder.isRecordablePage(newPage)) {
                    result = STATUS_REMOVED;
                }
                break;
        }
        LogUtil.d(TAG," onCompareCondition result: "+ result
                +" lastStatus: "+ lastStatus
                + " intent:" + intent
                +" prevPage: "+ prevPage
                +" newPage: " + newPage);
        return result;
    }

    /**
     * this method will only be invoked when status changed from STATUS_REMOVED to STATUS_SHOWING.
     * @param intent
     * @return
     */
    protected abstract boolean onDecideToShow(Intent intent);

    public BehaviourRecorder getRecorder(){
        return mRecorder;
    }

    @Override
    public void onEnteringPackage() {
        boolean enter = true;
        compareCondition(enter);
    }

    @Override
    public void onPageChanged(Intent intent, String previousPage, String newPage) {
        compareCondition(intent,previousPage,newPage);
    }

    @Override
    public void onExitingPackage() {
        boolean enter = false;
        compareCondition(enter);
    }
}
