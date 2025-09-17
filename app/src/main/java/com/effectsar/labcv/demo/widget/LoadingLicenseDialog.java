package com.effectsar.labcv.demo.widget;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatDialog;

import com.effectsar.labcv.demo.R;

/**
 * Author: gaojin.ivy
 * Time: 2025/5/29 11:45
 */

public class LoadingLicenseDialog extends AppCompatDialog {

    private TextView loadingText;
    private String content = "";

    private boolean created = false;

    public LoadingLicenseDialog(Context context) {
        super(context, R.style.LicenseLoading);
    }

    public LoadingLicenseDialog(Context context, int theme) {
        super(context, theme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.license_loading_layout);
        loadingText = findViewById(R.id.load_textview);
        if (!TextUtils.isEmpty(content)) {
            loadingText.setText(content);
        }
        setCanceledOnTouchOutside(false);
        setCancelable(false);

        created = true;
    }

    public void setLoadingText(String text) {
        this.content = text;
        if (created) {
            loadingText.setText(text);
        }
    }
}
