package com.pie.demo.view.weiget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * 自定义无法滑动的ViewPager
 */
public class NoScrollViewPager extends ViewPager {

    public NoScrollViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NoScrollViewPager(Context context) {
        super(context);
    }

    /**
     * 表示不对事件进行拦截, 从而可以使嵌套在ViewPager内部的ViewPager可以响应滑动动作
     *
     * @param arg0
     * @return
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent arg0) {
        return false;
    }

    /**
     * 拦截ViewPager的触摸事件, 不做任何处理
     */
    @Override
    public boolean onTouchEvent(MotionEvent arg0) {
        return false;
    }

}
