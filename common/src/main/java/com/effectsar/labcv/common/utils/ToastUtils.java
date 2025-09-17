package com.effectsar.labcv.common.utils;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;

import com.effectsar.labcv.common.R;
import com.effectsar.labcv.core.util.LogUtils;


public class ToastUtils {
    private static Context mAppContext = null;
    private static Toast mToast;
    private static TextView mTextView;

    private static ImageView mImageView;


    public static void init(Context context) {
        mAppContext = context;
        if (mAppContext != null) {
            mToast = new Toast(mAppContext);
            View layout = View.inflate(mAppContext, R.layout.layout_toast, null);
            mTextView = layout.findViewById(R.id.tv_toast);
            mImageView = layout.findViewById(R.id.iv_image);
            mImageView.setVisibility(View.GONE);
            mToast.setView(layout);
            mToast.setDuration(Toast.LENGTH_SHORT);
            mToast.setGravity(Gravity.CENTER,0,0);
        }
    }


    public static void show(String msg) {
        if (null == mAppContext) {
            LogUtils.d("ToastUtils not inited with Context");
            return;
        }
//        Toast.makeText(mAppContext, msg, Toast.LENGTH_SHORT).show();
        if (mToast == null || mTextView == null) {
            mToast = new Toast(mAppContext);
            View layout = View.inflate(mAppContext, R.layout.layout_toast, null);
            mTextView = layout.findViewById(R.id.tv_toast);
            mImageView = layout.findViewById(R.id.iv_image);
            mImageView.setVisibility(View.GONE);
            mToast.setView(layout);
            mToast.setDuration(Toast.LENGTH_SHORT);
            mToast.setGravity(Gravity.CENTER,0,0);
        }
        mImageView.setVisibility(View.GONE);
        mTextView.setText(msg);
        mToast.show();

    }

    public static void show(String msg, int resId) {
        if (null == mAppContext) {
            LogUtils.d("ToastUtils not inited with Context");
        }
        if (mToast == null || mTextView == null || mImageView == null) {
            mToast = new Toast(mAppContext);
            View layout = View.inflate(mAppContext, R.layout.layout_toast, null);
            mTextView = layout.findViewById(R.id.tv_toast);
            mImageView = layout.findViewById(R.id.iv_image);
            mToast.setView(layout);
            mToast.setDuration(Toast.LENGTH_SHORT);
            mToast.setGravity(Gravity.CENTER,0,0);
        }
        mImageView.setVisibility(View.VISIBLE);
        mImageView.setImageResource(resId);
        mTextView.setText(msg);
        mToast.show();
    }

    public static Toast makeToast(String msg) {
        if (null == mAppContext) {
            LogUtils.d("ToastUtils not inited with Context");
            return null;
        }
        Toast t = Toast.makeText(mAppContext, msg, Toast.LENGTH_LONG);
        t.show();
        return t;
    }
}
