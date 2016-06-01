package org.roysin.wechathelper.Model;
import android.content.Context;
import android.util.Log;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;

import org.roysin.wechathelper.R;

/**
 * Created by Zangyakui on 2016/5/30.
 */
public abstract class FloatIconController implements IconShownCondition.Callback {
    private static final String TAG = "FloatIconController";
    protected final Context mCtx;
    private WindowManager mWm;
    private boolean mIsShowing;
    protected ViewGroup mFloatView;
    private WindowManager.LayoutParams mFloatParams;
    private Animation mShowingAni;
    private IconShownCondition mBoundCondition;

    public FloatIconController(Context ctx){
        if(ctx == null){
            throw new IllegalArgumentException("context cannot be null");
        }
        this.mCtx = ctx;
        this.mWm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
    }
    private  boolean isShowing(){
        return mIsShowing;
    }
    public  boolean show(){
        showWithDelay(0);
        return mIsShowing;
    }

    protected IconShownCondition getBindedCondition(){
        return mBoundCondition;
    }
    private void showFloatView(long delay) {

        Log.i(TAG,"showFloatView ");
        mFloatView = getView(mFloatView);
        mFloatParams = getFloatParams();
        if(mShowingAni == null){
            mShowingAni = getShowingAnimation();
        }else {
            mShowingAni.cancel();
        }
        if(mWm != null && !mIsShowing) {
            Log.i(TAG, "now we are going to showFloatView ");
            mWm.addView(mFloatView, mFloatParams);
            mShowingAni.setStartOffset(delay);
            mFloatView.findViewById(R.id.float_view_layout).startAnimation(mShowingAni);
            mIsShowing = true;
        }
    }

    protected abstract ViewGroup getView(ViewGroup convertView);
    protected abstract WindowManager.LayoutParams getFloatParams();
    protected abstract Animation getShowingAnimation();


    public  void remove(){
        removeFloatView();
    }

    private void removeFloatView() {
        Log.i(TAG,"removeFloatView ");
        if(mWm != null && mFloatView != null && mIsShowing){
            mWm.removeViewImmediate(mFloatView);
            mIsShowing = false;
        }
    }
    @Override
    public void onConditionDismatched(int reasonLeavePackage, int lastStatus, int currentStatus) {
        if(isShowing()){
            remove();
        }
    }

    @Override
    public void onConditionFitted(int reason,int lastStatus) {
        if(!isShowing()) {
            if(lastStatus == IconShownCondition.STATUS_REMOVED){
                showWithDelay(600);
            }else if(reason == IconShownCondition.REASON_ENTER_PACKAGE){
                showWithDelay(600);
            }else {
                show();
            }
        }
    }

    private void showWithDelay(long delay) {
        showFloatView(delay);
    }

    public void bindCondition(IconShownCondition condition) {
        mBoundCondition = condition;
        mBoundCondition.registerCallback(this);
    }
}
