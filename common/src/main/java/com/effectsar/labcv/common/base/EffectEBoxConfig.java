package com.effectsar.labcv.common.base;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.volcengine.effectone.singleton.AppSingleton;
import com.volcengine.effectone.utils.EOUtils;

import java.io.File;

/**
 * Author: gaojin.ivy
 * Time: 2025/6/30 17:48
 */

public class EffectEBoxConfig {
    //判断是否使用EBox统一使用这个方法
    public static boolean useEbox() {
        SharedPreferences manager = PreferenceManager.getDefaultSharedPreferences(AppSingleton.instance);
        String eboxType = manager.getString("eo_ebox_enable_type", "follow_system");
        if ("open".equals(eboxType)) {
            return true;
        } else if ("close".equals(eboxType)) {
            return false;
        } else {
            return "zh".equals(EOUtils.INSTANCE.getLocaleUtil().getCurrentLocale().getLanguage());
        }
    }

    public static File eboxModelDir() {
        return EOUtils.INSTANCE.getPathUtil().internalResource("model");
    }
}
