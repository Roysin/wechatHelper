package org.roysin.wechathelper;

import android.content.ComponentName;
import android.content.Intent;

/**
 * Created by Administrator on 2016/3/19.
 */
public class PageStatus {
    public static final int DEFAULT_PAGE= 0;
    public static final int READING_PAGE = 1;
    public static final int CHATTING_PAGE = READING_PAGE + 1;
    public static final int SNS_TIMELINE_PAGE = READING_PAGE + 2;
    public static final int GALLERY_PAGE = READING_PAGE + 3;

    public static final int REASON_DEFAULT = 0x11;
    public static final int REASON_SWITCH_APP = REASON_DEFAULT + 1;
    public static final int REASON_LONG_PRESS = REASON_DEFAULT + 2;
    public static final int REASON_READING = REASON_DEFAULT + 3;
    public static final int REASON_GALLERY = REASON_DEFAULT + 4;


    public int currentPage;
    public int lastPage;
    public boolean firstStart;
    public Intent lastReadingPageIntent;
    public int disapperReason;

    public void PageStatus(){
        reset();
    }

    public void update(Intent intent, boolean firstStart){
        update(intent.getComponent(),firstStart);
        if (this.currentPage == PageStatus.READING_PAGE){
            this.lastReadingPageIntent = new Intent(intent);
        }
    }
    public void update(ComponentName component, boolean firstStart){

        if (component != null){
            String cls = component.getShortClassName();
            this.lastPage = this.currentPage;
            if(Constants.READING_PAGE_CLASS.equals(cls)){
                this.currentPage = PageStatus.READING_PAGE;
            }
            else if(Constants.CHATTING_UI_CLASS.equals(cls)){
                this.currentPage = PageStatus.CHATTING_PAGE;
            }
            else if(Constants.SNS_TIMELINE_CLASS.equals(cls)){
                this.currentPage = PageStatus.SNS_TIMELINE_PAGE;
            }
            else if(Constants.GALLERY_UI_CLASS.equals(cls)){
                this.currentPage = PageStatus.GALLERY_PAGE;
            }
            else {
                this.currentPage = PageStatus.DEFAULT_PAGE;
            }
        }
        this.firstStart = firstStart;
    }
    public void reset(){
        currentPage =  DEFAULT_PAGE;
        lastPage = DEFAULT_PAGE;
        firstStart = true;
        lastReadingPageIntent = null;
        disapperReason = REASON_DEFAULT;
    }
}
