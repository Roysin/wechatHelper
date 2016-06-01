package org.roysin.wechathelper.Model.QQ;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import org.roysin.wechathelper.Model.FloatIconController;
import org.roysin.wechathelper.R;

/**
 * Created by Administrator on 2016/6/1.
 */
public class QQController extends FloatIconController {
    public QQController(Context ctx) {
        super(ctx);
    }

    @Override
    protected ViewGroup getView(ViewGroup convertView){
        if(convertView == null){
            LayoutInflater inflater =  LayoutInflater.from(mCtx);
            convertView = (ViewGroup) inflater.inflate(R.layout.float_text_layout,null);
            convertView.findViewById(R.id.start_activity).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getBindedCondition().getRecorder().resumeTopActivity();
                    remove();
                }
            });
            convertView.findViewById(R.id.start_activity).setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    remove();
                    getBindedCondition().resetStatus();
                    getBindedCondition().getRecorder().resetStatus();
                    return false;
                }
            });
            convertView.findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    remove();
                    getBindedCondition().resetStatus();
                    getBindedCondition().getRecorder().resetStatus();
                }
            });
        }
        return convertView;
    }

    @Override
    protected WindowManager.LayoutParams getFloatParams() {
        WindowManager.LayoutParams floatParams = new WindowManager.LayoutParams();
        floatParams.gravity = Gravity.TOP | Gravity.LEFT;
//        DisplayMetrics metrics = new DisplayMetrics();
//        floatParams.width = metrics.widthPixels;
        floatParams.height = mCtx.getResources().getDimensionPixelSize(R.dimen.float_height);
        floatParams.width = mCtx.getResources().getDimensionPixelSize(R.dimen.float_width);
        floatParams.x = mCtx.getResources().getDimensionPixelSize(R.dimen.float_x);
        floatParams.y = mCtx.getResources().getDimensionPixelSize(R.dimen.float_y);
        floatParams.type = WindowManager.LayoutParams.TYPE_PRIORITY_PHONE;
        floatParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        ;
        floatParams.format = PixelFormat.TRANSPARENT;
        return floatParams;
    }

    @Override
    protected Animation getShowingAnimation() {
        AlphaAnimation ani = new AlphaAnimation(0.0f,1.0f);
        ani.setFillAfter(true);
        ani.setDuration(300);
        return ani;
    }

}
