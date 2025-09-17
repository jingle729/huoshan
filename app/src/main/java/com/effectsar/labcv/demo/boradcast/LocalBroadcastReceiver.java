package com.effectsar.labcv.demo.boradcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.effectsar.labcv.common.utils.LocaleUtils;
import com.effectsar.labcv.common.utils.ToastUtils;
import com.effectsar.labcv.core.util.LogUtils;
import com.volcengine.effectone.singleton.AppSingleton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class LocalBroadcastReceiver extends BroadcastReceiver {
    public static final String ACTION = "com.effectsar.labcv.core.check_result:action";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getStringExtra("msg") != null) {
            ToastUtils.show(convertLocalLog(intent.getStringExtra("msg")));
        }
    }

    private String convertLocalLog(String log) {
        List<String[]> splitLogs = splitLog(log);
        if (splitLogs.size() <= 1) {
            return log;
        }
        Locale locale = LocaleUtils.getCurrentLocale(AppSingleton.instance);
        String msg = extractLog(locale.getLanguage(), splitLogs);
        if (msg != null) {
            return msg;
        }
        msg = extractLog("en", splitLogs);
        if (msg != null) {
            return msg;
        }
        return splitLogs.get(0)[1];
    }

    private List<String[]> splitLog(String log) {
        String[] s1 = log.split("\\{");
        if (s1.length <= 1) {
            LogUtils.e("could not split log " + log + ", return directly");
            return Collections.emptyList();
        }

        List<String[]> result = new ArrayList<>();
        for (String s2 : s1) {
            if (s2.equals("")) {
                continue;
            }
            String[] s3 = s2.split("\\}");
            if (s3.length != 2) {
                LogUtils.e("could not parse single language " + s2 + ", skip");
                continue;
            }
            result.add(new String[]{s3[0], s3[1]});
        }
        return result;
    }

    private String extractLog(String language, List<String[]> arr) {
        for (String[] ar : arr) {
            if (ar[0].equals(language)) {
                return ar[1];
            }
        }
        return null;
    }
}
