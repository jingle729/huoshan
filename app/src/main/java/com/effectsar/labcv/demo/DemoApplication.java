package com.effectsar.labcv.demo;

import android.app.Application;
import android.content.Context;
//vehook_add_package don't delete this line
import com.effectsar.labcv.common.DefaultImageLoader;
import com.effectsar.labcv.common.utils.PlatformUtils;
import com.effectsar.labcv.core.effect.EffectManager;
import com.effectsar.labcv.ebox.EBoxSDKHelper;
import com.effectsar.labcv.resource.database.DatabaseManager;
import com.effectsar.labcv.common.utils.ToastUtils;
import com.effectsar.labcv.core.util.LogUtils;
import com.volcengine.effectone.EffectOneSdk;
import com.volcengine.effectone.singleton.AppSingleton;

import java.lang.ref.WeakReference;
// leakcannry_add don't delete this line

public class DemoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        DatabaseManager.init(this);
        ToastUtils.init(this);
        com.effectsar.labcv.common.utils.ToastUtils.init(this);
        LogUtils.syncIsDebug(getApplicationContext());
        PlatformUtils.init(getApplicationContext());
//        leakcannry_upload don't delete this line
        if (EffectManager.USE_MODEL_FROM_ASSET) {
            EffectManager.setAssetManager(this);
        }
        AppSingleton.INSTANCE.bindInstance(this);
        EffectOneSdk.INSTANCE.setImageLoader(new DefaultImageLoader());
        EBoxSDKHelper.INSTANCE.init();
    }
}
