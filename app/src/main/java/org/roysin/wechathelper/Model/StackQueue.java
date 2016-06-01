package org.roysin.wechathelper.Model;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016/6/1.
 */
public class StackQueue <E> {
    private final long maxSize;
    private int index;
    ArrayList<E> mElements;
    public StackQueue (long maxSize){
        this.maxSize = maxSize;
        index = -1;
        mElements = new ArrayList<E>();
    }

    public void push(E element){
        index ++;
        if(index < maxSize){
            mElements.add(index,element);
        }else {
            mElements.remove(0);
            index = (int) maxSize -1;
            mElements.add(index,element);
        }
    }

    public E pop(){
        E result = null;
        if(index > -1 && index < maxSize){
            result = mElements.get(index);
            mElements.remove(index);
            index --;
        }
        return result;
    }

    public E peek(){
        E result = null;
        if(index > -1 && index < maxSize){
            result = mElements.get(index);
        }
        return result;
    }

    public long size(){
        return index+1;
    }
}
