package com.effectsar.labcv.demo.activity;

import static com.effectsar.labcv.demo.activity.MainActivity.LICENSE_INFO_TAG;
import static com.effectsar.labcv.demo.activity.PermissionsActivity.PERMISSION_READ_PHONE_STATE;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.effectsar.labcv.common.database.LocalParamManager;
import com.effectsar.labcv.common.utils.ToastUtils;
import com.effectsar.labcv.core.license.EffectLicenseHelper;
import com.effectsar.labcv.demo.R;
import com.effectsar.labcv.demo.adapter.SettingRecyclerAdapter;
import com.effectsar.labcv.demo.fragment.EBoxConfigPreferenceFragment;
import com.effectsar.labcv.demo.model.Constants;
import com.effectsar.labcv.demo.model.SettingModel;
import com.effectsar.labcv.demo.model.UserData;
import com.effectsar.labcv.effect.resource.StickerFetch;
import com.effectsar.labcv.effect.task.DownloadResourceTask;
import com.effectsar.labcv.resource.database.DatabaseManager;
import com.volcengine.effectone.singleton.AppSingleton;

import java.util.ArrayList;
import java.util.List;

public class SettingActivity extends Activity implements SettingRecyclerAdapter.ItemConfirmListener {

    private RecyclerView rcvContainer;
    private SettingRecyclerAdapter mRcvAdapter;
    private List<SettingModel> settingModels = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        setupData();
        setupUI();
    }

    private void setupUI() {
        rcvContainer = findViewById(R.id.rcvContainer);
        rcvContainer.setLayoutManager(new LinearLayoutManager(this));
        mRcvAdapter = new SettingRecyclerAdapter(settingModels);
        mRcvAdapter.setOnItemConfirmListener(this);
        rcvContainer.setAdapter(mRcvAdapter);

    }

    private void setupData() {
        // 准备数据
        settingModels.add(new SettingModel(SettingModel.SettingItemShowType.ST_INPUT, SettingModel.SettingItemTitleEnum.SI_FPS, "输入帧率", LocalParamManager.getInstance().getFrameRate() + ""));
//        settingModels.add(new SettingModel(SettingModel.SettingItemShowType.ST_SWITCH, SettingModel.SettingItemEnum.SI_BOE, "", "0"));
        settingModels.add(new SettingModel(SettingModel.SettingItemShowType.ST_SHOW, SettingModel.SettingItemTitleEnum.SI_DEVICEID, "", Constants.DEVICE_ID));
        settingModels.add(new SettingModel(SettingModel.SettingItemShowType.ST_SHOW, SettingModel.SettingItemTitleEnum.SI_CHANNELID, "", Constants.CHANNEL_ID));
        settingModels.add(new SettingModel(SettingModel.SettingItemShowType.ST_TRIGGER, SettingModel.SettingItemTitleEnum.SI_DELLICCACHE, "删除 License 缓存", ""));
        settingModels.add(new SettingModel(SettingModel.SettingItemShowType.ST_TRIGGER, SettingModel.SettingItemTitleEnum.SI_CHANGEDID, "切换Lic使用的DeviceId", ""));
        settingModels.add(new SettingModel(SettingModel.SettingItemShowType.ST_TRIGGER, SettingModel.SettingItemTitleEnum.SI_AUTHORIZE, "在线授权/离线授权", ""));
        settingModels.add(new SettingModel(SettingModel.SettingItemShowType.ST_TRIGGER, SettingModel.SettingItemTitleEnum.SI_MIDPLATTYPE, "生产环境/BOE", ""));
        settingModels.add(new SettingModel(SettingModel.SettingItemShowType.ST_TRIGGER, SettingModel.SettingItemTitleEnum.SI_EDITMODE, "手动输入Secret&Key", ""));
        settingModels.add(new SettingModel(SettingModel.SettingItemShowType.ST_TRIGGER, SettingModel.SettingItemTitleEnum.SI_AMGFILTER, "是否显示Amazing滤镜", ""));
        settingModels.add(new SettingModel(SettingModel.SettingItemShowType.ST_TRIGGER, SettingModel.SettingItemTitleEnum.SI_EBOX_CONFIG, "EBox配置", ""));
    }

    @Override
    public void onInputConfirm(SettingModel model, String content) {
        model.setContent(content);
        if (model.getEnumType() == SettingModel.SettingItemTitleEnum.SI_FPS) {
            doFps(model);
        } else if (model.getEnumType() == SettingModel.SettingItemTitleEnum.SI_DELLICCACHE) {
            deleteLicenseCache();
        } else if (model.getEnumType() == SettingModel.SettingItemTitleEnum.SI_CHANGEDID) {
            changeLicDid();
        } else if (model.getEnumType() == SettingModel.SettingItemTitleEnum.SI_AUTHORIZE) {
            changeAuthorizeType();
        } else if (model.getEnumType() == SettingModel.SettingItemTitleEnum.SI_MIDPLATTYPE) {
            changeMidPlatformType();
        } else if (model.getEnumType() == SettingModel.SettingItemTitleEnum.SI_EDITMODE) {
            changeEditingMode();
        } else if (model.getEnumType() == SettingModel.SettingItemTitleEnum.SI_AMGFILTER) {
            changeNewFilterShowStatus();
        } else if (model.getEnumType() == SettingModel.SettingItemTitleEnum.SI_EBOX_CONFIG) {
            PreferenceDebugActivity.Companion.start(
                    this,
                    "Ebox Config",
                    EBoxConfigPreferenceFragment.class
            );
        }
    }

    @Override
    public void onSwtichToggle(SettingModel model, boolean on) {
        model.setContent(on ? "1" : "0");
    }

    private void doFps(SettingModel model) {
        int fps = 0;
        try {
            fps = Integer.parseInt(model.getContent());
        } catch (Exception e) {
            ToastUtils.show("Input must be integer");
            return;
        }

        if (fps < 15 || fps > 30) {
            ToastUtils.show("Invalid FPS, must between 15 ~ 30");
            return;
        }
        LocalParamManager.getInstance().setFrameRate(fps);
        ToastUtils.show("FPS saved: " + fps);
    }

    private void deleteLicenseCache() {
        boolean suc = EffectLicenseHelper.getInstance(getApplicationContext()).deleteCacheFile();
        ToastUtils.show(suc ? "删除授权缓存成功": "删除授权缓存失败");
    }

    private void changeLicDid() {
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("license_info_tag", 0);
        int deviceIdType = sharedPreferences.getInt("deviceIdType",0);
        deviceIdType = (deviceIdType + 1) % 2;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("deviceIdType", deviceIdType);
        editor.commit();

        com.effectsar.labcv.licenselibrary.EffectsSDKLicenseWrapper.nativeSetDeviceIdType(deviceIdType);
        if (deviceIdType == 1) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (checkSelfPermission(PERMISSION_READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                            Intent intent = new Intent();
                            intent.setClass(getApplicationContext(), PermissionsActivity.class);
                            intent.putExtra(PermissionsActivity.PERMISSION_REQUEST_LIST, PERMISSION_READ_PHONE_STATE);
                            startActivity(intent);
                        }
                    }
                });
            }
        }
        ToastUtils.show("设备id类型切换成功, 设置为: " + deviceIdType);
    }

    private void changeAuthorizeType() {
        SharedPreferences sharedPreferences = AppSingleton.instance.getSharedPreferences("online_model",0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (sharedPreferences.getString("online_model_key", "").equals("OFFLINE_LICENSE")){
            editor.putString("online_model_key", "ONLINE_LICENSE");
            ToastUtils.show("已切换到在线授权，请重启APP");
        }
        else if (sharedPreferences.getString("online_model_key", "").equals("ONLINE_LICENSE")){
            editor.putString("online_model_key", "OFFLINE_LICENSE");
            ToastUtils.show("已切换到离线授权，请重启APP");
        }
        else {
            editor.putString("online_model_key", "ONLINE_LICENSE");
        }
        editor.commit();
    }

    private void changeMidPlatformType() {
        String info = "URL:";
        if (!UserData.getInstance(AppSingleton.instance).isBoe()) {
            UserData.getInstance(AppSingleton.instance).setBoe(true);
            DownloadResourceTask.setServerType(DownloadResourceTask.SERVER_TYPE_BOE);
            StickerFetch.BASE_URL = StickerFetch.BASE_URL_BOE;
            info = info + getString(R.string.tip_url_link_boe);
            EffectLicenseHelper.LICENSE_URL = EffectLicenseHelper.LICENSE_URL_BOE;
        } else {
            UserData.getInstance(AppSingleton.instance).setBoe(false);
            DownloadResourceTask.setServerType(DownloadResourceTask.SERVER_TYPE_PROD);
            StickerFetch.BASE_URL = StickerFetch.BASE_URL_PROD;
            info = info + getString(R.string.tip_url_link_prod);
            EffectLicenseHelper.LICENSE_URL = EffectLicenseHelper.LICENSE_URL_OFFICIAL;
        }
        DatabaseManager.getInstance().removeAllResourceItem();
        ToastUtils.show(info);
    }

    private void changeEditingMode() {
        SharedPreferences sharedPreferences = this.getSharedPreferences(LICENSE_INFO_TAG, 0);
        boolean isEditingMode = sharedPreferences.getBoolean("isEditingMode", false);
        if (isEditingMode) {
            ToastUtils.show("Exiting editing mode");
        }else {
            ToastUtils.show("Entering editing mode. Please relaunch APP.");
        }
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isEditingMode", !isEditingMode);
        editor.commit();
    }

    private void changeNewFilterShowStatus() {
        SharedPreferences sharedPreferences = this.getSharedPreferences(LICENSE_INFO_TAG, 0);
        boolean showOldFilter = sharedPreferences.getBoolean("newFilterShowStatus", false);
        if (showOldFilter) {
            ToastUtils.show("Stop Showing Amazing Filter");
        }else {
            ToastUtils.show("Start to Show Amazing Filter");
        }
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("newFilterShowStatus", !showOldFilter);
        editor.commit();
    }
}
