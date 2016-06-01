package org.roysin.wechathelper.Model;
import android.content.Context;
import android.graphics.PixelFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import org.roysin.wechathelper.R;

/**
 * Created by Administrator on 2016/5/30.
 */
public class FloatIconController implements IconShownCondition.Callback {
    private static final String TAG = "FloatIconController";
    private final Context mCtx;
    private WindowManager mWm;
    private boolean mIsShowing;
    private View mFloatBtn;
    private View.OnClickListener mOnClickListener;
    private View.OnLongClickListener mOnLongClickListener;
    private WindowManager.LayoutParams mFloatParams;
    private AlphaAnimation mShowingAni;
    private Runnable showingRunnable;

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

    private void showFloatBtn(long delay) {

        Log.i(TAG,"showFloatBtn ");

        if(mFloatBtn == null){
            LayoutInflater inflater =  LayoutInflater.from(mCtx);
            mFloatBtn = inflater.inflate(R.layout.float_button_layout,null);

            mFloatBtn.findViewById(R.id.start_activity).setOnClickListener(mOnClickListener);
            mFloatBtn.findViewById(R.id.start_activity).setOnLongClickListener(mOnLongClickListener);
        }

        if(mFloatParams == null){
            mFloatParams = new WindowManager.LayoutParams();
            mFloatParams.gravity = Gravity.TOP | Gravity.LEFT;
            mFloatParams.height = mCtx.getResources().getDimensionPixelSize(R.dimen.float_height);
            mFloatParams.width = mCtx.getResources().getDimensionPixelSize(R.dimen.float_height);
            mFloatParams.x = mCtx.getResources().getDimensionPixelSize(R.dimen.float_x);
            mFloatParams.y = mCtx.getResources().getDimensionPixelSize(R.dimen.float_y);
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
        if(mWm != null && !mIsShowing) {
            Log.i(TAG, "now we are going to showFloatBtn ");
            mWm.addView(mFloatBtn, mFloatParams);
            mShowingAni.setStartOffset(delay);
            mFloatBtn.findViewById(R.id.start_activity).startAnimation(mShowingAni);
            mIsShowing = true;
        }
    }

    public  void remove(){
        removeFloatBtn();
    }

    private void removeFloatBtn() {
        Log.i(TAG,"removeFloatBtn ");
        if(mWm != null && mFloatBtn != null && mIsShowing){
            mWm.removeViewImmediate(mFloatBtn);
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
        showFloatBtn(delay);
    }

    public void setOnClickListener(View.OnClickListener listener){
        mOnClickListener = listener;
        if(mFloatBtn != null){
            mFloatBtn.findViewById(R.id.start_activity).setOnClickListener(mOnClickListener);
        }
    }
    public void setOnLongClickListener(View.OnLongClickListener listener){
        mOnLongClickListener = listener;
        if(mFloatBtn != null){
            mFloatBtn.findViewById(R.id.start_activity).setOnLongClickListener(mOnLongClickListener);
        }
    }

    public void bindCondition(IconShownCondition conditon) {
        conditon.registerCallback(this);
    }
}
