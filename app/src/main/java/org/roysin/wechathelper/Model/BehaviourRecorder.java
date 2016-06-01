package org.roysin.wechathelper.Model;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

/**
 * Created by Administrator on 2016/5/30.
 */
public class BehaviourRecorder {
    private  Context mContext;
    protected String mPkgName;
    protected String mCurrentPage;
    protected String mLastPage;
    protected Stack<Intent> mIntents;
    private BehaviourChangedListener mBehaviourChangedListener;
    private List<String> mRecordablePages;
    private boolean mEnable;

    public BehaviourRecorder(String packageName,Context ctx){
        if(packageName == null){
            throw new IllegalArgumentException("packageName cannot be null");
        }
        this.mContext = ctx;
        this.mPkgName = packageName;
        this.mIntents = new Stack<>();
    }

    public void recordBehaviour(Intent intent){
        if(!mEnable){
            return;
        }
        recordBehaviour(intent,intent.getComponent());
    }

    public void recordBehaviour(ComponentName component ){
        if(!mEnable){
            return;
        }
        recordBehaviour(null,component);
    }
    protected void recordBehaviour(Intent intent,ComponentName component){
        if (component != null){
            String cls = component.getShortClassName();
            this.mLastPage= this.mCurrentPage;
            this.mCurrentPage = cls;
            innerRecord(intent,this.mLastPage,this.mCurrentPage);
        }
    }
    protected void innerRecord(Intent intent,String lastPage,String newPage) {
        if (intent != null && newPage != null && mRecordablePages.contains(newPage)) {
            this.mIntents.push(intent);
        }
        if (this.mBehaviourChangedListener != null) {
            mBehaviourChangedListener.onPageChanged(intent,lastPage, newPage);
        }
    }

    public void disable(){
        if(!mEnable) return;
        mEnable = false;
        if (this.mBehaviourChangedListener != null) {
            mBehaviourChangedListener.onExitingPackage();
        }
    }
    public void enable(){
        if(mEnable) return;
        mEnable = true;
        if (this.mBehaviourChangedListener != null) {
            mBehaviourChangedListener.onEnteringPackage();
        }
    }
    public boolean isEnabled(){
        return mEnable;
    }
    public void setRecordablePages(String [] pageList){
        mRecordablePages = Arrays.asList(pageList);
    }

    public String getPackageName(){
        return mPkgName;
    }

    public void setBehaviourChangedListener(BehaviourChangedListener behaviourChangedListener) {
        this.mBehaviourChangedListener = behaviourChangedListener;
    }

    public boolean isRecordablePage(String prevPage) {
        if(mRecordablePages!= null){
            return mRecordablePages.contains(prevPage);
        }
        return false;
    }

    public String getCurrentPage() {
        return mCurrentPage;
    }

    public void resumeTopActivity() {
        Intent intent = mIntents.pop();
        if(intent != null){
            int flag = intent.getFlags();
            flag |= Intent.FLAG_ACTIVITY_NEW_TASK;
            intent.setFlags(flag);
            mContext.startActivity(intent);
        }
    }

    public interface BehaviourChangedListener{
        void onEnteringPackage();
        void onPageChanged(Intent intent, String previousPage, String newPage);
        void onExitingPackage();
    }
}
