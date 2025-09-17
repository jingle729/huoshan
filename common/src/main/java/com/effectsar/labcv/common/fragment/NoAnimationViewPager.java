package com.effectsar.labcv.common.fragment;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class NoAnimationViewPager extends ViewPager {
    public NoAnimationViewPager(@NonNull Context context) {
        super(context);
    }

    public NoAnimationViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setCurrentItem(int item, boolean smoothScroll) {
        super.setCurrentItem(item, smoothScroll);

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return false;
    }

    @Override
    public void setCurrentItem(int item) {
       super.setCurrentItem(item, false);
    }

}
