package com.effectsar.labcv.demo.activity;

import static com.effectsar.labcv.common.config.ImageSourceConfig.ImageSourceType.TYPE_CAMERA;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ConfigurationInfo;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import com.effectsar.labcv.algorithm.config.AlgorithmConfig;
import com.effectsar.labcv.common.base.EffectEBoxConfig;
import com.effectsar.labcv.common.config.ImageSourceConfig;
import com.effectsar.labcv.common.config.UIConfig;
import com.effectsar.labcv.common.customfeatureswitch.CustomFeatureSwitch;
import com.effectsar.labcv.common.database.LocalParamManager;
import com.effectsar.labcv.common.utils.CommonUtils;
import com.effectsar.labcv.common.utils.PlatformUtils;
import com.effectsar.labcv.common.utils.ToastUtils;
import com.effectsar.labcv.core.Config;
import com.effectsar.labcv.core.effect.EffectResourceHelper;
import com.effectsar.labcv.core.license.EffectLicenseHelper;
import com.effectsar.labcv.core.license.EffectLicenseProvider;
import com.effectsar.labcv.core.util.LogUtils;
import com.effectsar.labcv.demo.R;
import com.effectsar.labcv.demo.adapter.MainTabRVAdapter;
import com.effectsar.labcv.demo.adapter.decoration.GridDividerItemDecoration;
import com.effectsar.labcv.demo.boradcast.LocalBroadcastReceiver;
import com.effectsar.labcv.demo.fragment.LicenseInfoFragment;
import com.effectsar.labcv.demo.model.FeatureConfig;
import com.effectsar.labcv.demo.model.FeatureTab;
import com.effectsar.labcv.demo.model.FeatureTabItem;
import com.effectsar.labcv.demo.model.MainDataManager;
import com.effectsar.labcv.demo.model.UserData;
import com.effectsar.labcv.demo.task.RequestLicenseTask;
import com.effectsar.labcv.demo.task.UnzipTask;
import com.effectsar.labcv.demo.widget.LoadingLicenseDialog;
import com.effectsar.labcv.ebox.MainPageConfig;
import com.effectsar.labcv.ebox.PageDetail;
import com.effectsar.labcv.ebox.ParseUtils;
import com.effectsar.labcv.effect.config.EffectConfig;
import com.effectsar.labcv.effect.config.StickerConfig;
import com.effectsar.labcv.effectsdk.RenderManager;
import com.effectsar.labcv.lens.config.ImageQualityConfig;
import com.effectsar.labcv.licenselibrary.EffectsSDKLicenseInterface;
import com.effectsar.platform.struct.CategoryData;
import com.effectsar.platform.struct.Material;
import com.effectsar.platform.utils.NetworkType;
import com.effectsar.platform.utils.NetworkUtils;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.gyf.immersionbar.ImmersionBar;
import com.volcengine.ebox.loader.EBoxSDKManager;
import com.volcengine.effectone.permission.Scene;
import com.volcengine.effectone.utils.EOUtils;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import kotlin.Unit;


public class MainActivity extends FragmentActivity
        implements UnzipTask.IUnzipViewCallback, View.OnClickListener, LicenseInfoFragment.ILicenseInfoCallback {
    private TabLayout tabLayout;
    private RecyclerView recyclerView;

    /*  {zh} 素材下发进度条相关    {en} Material delivery progress bar related */
    private LinearLayout loadLLContainer;
    private ProgressBar pbLoad;
    private TextView tvLoad;
    private AlertDialog.Builder dialog;
    private AlertDialog icpTipDialog;
    private FeatureConfig mConfig;
    private boolean mUpdatedBeforeIntent = false;

    private GridLayoutManager mLayoutManager;
    private boolean mSkipRVScrollListen;
    private List<FeatureTab> mTabs = new ArrayList<>();
    private final Point mDefaultPreviewSize = new Point(1280, 720);

    private LicenseInfoFragment mLicenseInfoFragment = null;

    public static final String LICENSE_INFO_TAG = "license_info_tag";

    private static final String CATEGORY_KEY = "feature_all_model_resource";

    private boolean isCheckResource = false;
    private boolean isEditingMode = false;

    private LoadingLicenseDialog loadingLicenseDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ImmersionBar.with(this)
                .statusBarColor(R.color.colorWhite)
                .statusBarDarkFont(true)
                .navigationBarColor(R.color.colorWhite)
                .init();

        setContentView(R.layout.activity_main);

        tabLayout = findViewById(R.id.tl_main);
        recyclerView = findViewById(R.id.rv_main);
        //  {zh} 素材下发加载进度条  {en} Material delivery and loading progress bar
        pbLoad = findViewById(R.id.load_bar);
        tvLoad = findViewById(R.id.tv_load);
        loadLLContainer = findViewById(R.id.loadLlContainer);
        loadLLContainer.setVisibility(View.GONE);
        loadingLicenseDialog = new LoadingLicenseDialog(MainActivity.this);

        resetRemoteMaterialFlagIfNeeded();

        SharedPreferences sharedPreferences = this.getSharedPreferences(LICENSE_INFO_TAG, 0);
        EffectLicenseHelper.Online_or_offline_model(this);
        if (EffectLicenseHelper.getInstance(getApplicationContext()) != null) {
            com.effectsar.labcv.licenselibrary.EffectsSDKLicenseWrapper.nativeSetDeviceIdType(sharedPreferences.getInt("deviceIdType", 0));
        }
        isEditingMode = sharedPreferences.getBoolean("isEditingMode", false);
        if (isEditingMode) {
            showLicenseInfoFragment();
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(new LocalBroadcastReceiver(), new IntentFilter(LocalBroadcastReceiver.ACTION));
        LocalParamManager.init(getApplicationContext());
        // for debug only
        LocalParamManager.getInstance();

        boolean useEbox = EffectEBoxConfig.useEbox();
        // 非中文版本不支持ebox
        if (useEbox) {
            loadingLicenseDialog.show();
            EBoxSDKManager.INSTANCE.getPageConfig(result -> {
                loadingLicenseDialog.dismiss();
                MainPageConfig mainPageConfig = null;
                if (!TextUtils.isEmpty(result)) {
                    mainPageConfig = ParseUtils.INSTANCE.parse(result);
                }
                initView(mainPageConfig);
                return Unit.INSTANCE;
            });
        } else {
            initView(null);
        }
    }

    private void resetRemoteMaterialFlagIfNeeded() {
        int savedVersionCode = UserData.getInstance(this).getVersion();
        int currentVersionCode = getVersionCode();
        if (savedVersionCode != currentVersionCode) {
            UserData.getInstance(getApplicationContext()).setModelDownloaded(false);
        }
    }

    private void initView(MainPageConfig mainPageConfig) {
        MainDataManager mDataManager = new MainDataManager();
        mTabs = mDataManager.getFeatureTabs(mainPageConfig);
        initRV(mTabs);
        initTab(mTabs);
        initDialog();
        initICPTipDialog();
        findViewById(R.id.img_banner).setOnLongClickListener(v -> {
            MainActivity.this.startActivity(new Intent(MainActivity.this, SettingActivity.class));
            return true;
        });
    }

    private void initTab(List<FeatureTab> tabs) {
        for (FeatureTab tab : tabs) {
            tabLayout.addTab(tabLayout.newTab().setText(tab.getTitle()));
        }
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = getChildPosition(tab.getPosition());
                RecyclerView.SmoothScroller scroller = new LinearSmoothScroller(getContext()) {
                    @Override
                    protected int getVerticalSnapPreference() {
                        return LinearSmoothScroller.SNAP_TO_START;
                    }
                };
                scroller.setTargetPosition(position);
                mLayoutManager.startSmoothScroll(scroller);
                mSkipRVScrollListen = true;
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private void initRV(List<FeatureTab> tabs) {
        MainTabRVAdapter adapter = new MainTabRVAdapter(tabs);
        adapter.setOnItemClickListener(new MainTabRVAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(FeatureTabItem item) {
                onMainItemClick(item);
            }

            @Override
            public void onICPClick() {
                icpTipDialog.show();
            }
        });
        mLayoutManager = new GridLayoutManager(getContext(), 2);
        mLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                int viewType = adapter.getItemViewType(position);
                if (viewType == MainTabRVAdapter.TYPE_TITLE || viewType == MainTabRVAdapter.TYPE_FOOTER) {
                    return 2;
                }
                return 1;
            }
        });
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new GridDividerItemDecoration(this));
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (mSkipRVScrollListen) {
                    //   {zh} 如果是 tabLayout 导致的滑动，就跳过这一步，       {en} If the slide is caused by tabLayout, skip this step,  
                    //   {zh} 否则会引起循环调用       {en} Otherwise it will cause a loop call  
                    return;
                }
                tabLayout.setScrollPosition(getParentPosition(mLayoutManager.findFirstVisibleItemPosition()), 0, true);
                tabLayout.selectTab(null);
            }

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    //   {zh} 滑动状态变为 SCROLL_STATE_DRAGGING 时意味着手动滑动       {en} When the sliding state changes to SCROLL_STATE_DRAGGING, it means manual sliding  
                    //   {zh} 此时关闭跳过       {en} Close Skip at this time  
                    mSkipRVScrollListen = false;
                }
            }
        });
    }

    private void initDialog() {
        dialog = new AlertDialog.Builder(this);
        dialog.setMessage(R.string.load_or_not);
        dialog.setCancelable(true);
        dialog.setPositiveButton(R.string.confirm, (dialog, which) -> fetchRemoteResource());
        dialog.setNegativeButton(R.string.cancel, (dialog, which) -> {
        });
    }

    private void initICPTipDialog() {
        AlertDialog.Builder icpTipDialogBuilder = new AlertDialog.Builder(this);
        icpTipDialogBuilder.setMessage("即将打开网站https://beian.miit.gov.cn");
        icpTipDialogBuilder.setCancelable(true);

        icpTipDialogBuilder.setPositiveButton(R.string.confirm, (dialog, which) -> {
            String url = "https://beian.miit.gov.cn";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        });
        icpTipDialogBuilder.setNegativeButton(R.string.cancel, (dialog, which) -> icpTipDialog.dismiss());
        icpTipDialog = icpTipDialogBuilder.create();
    }

    @Override
    public void onClick(View v) {
        if (CommonUtils.isFastClick()) {
            LogUtils.e("too fast click");
            return;
        }
        if (!isCheckResource && !isEditingMode) {
            checkResourceReady();
        }
    }

    private void onMainItemClick(FeatureTabItem item) {
        if (!isCheckResource && !isEditingMode) {
            checkResourceReady();
            return;
        }

        FeatureConfig config = item.getConfig();

        if (item.getId().equals(MainDataManager.FEATURE_AR_WATCH)) {
            CustomFeatureSwitch.getInstance().setFeature_watch(true);
        } else if (item.getId().equals(MainDataManager.FEATURE_AR_BRACELET)) {
            CustomFeatureSwitch.getInstance().setFeature_bracelet(true);
        }

        if (Config.ENABLE_ASSETS_SYNC) {
            startActivity(config);
            return;
        }

        if (!Config.IS_ONLINE_MODEL) {
            startActivity(config);
            return;
        }

        /** {zh}
         * 算法模块/健身助手/扫一扫/画质打分/脏镜头检测/视频插帧 需要获取在线模型
         */
        /** {en}
         * Algorithm module/fitness assistant/sweep/image quality scoring/dirty shot detection/video frame insertion needs to obtain online model
         */
        boolean isZh = Locale.getDefault().getLanguage() == "zh";
        if ((config.getAlgorithmConfig() != null) ||
                config.getFeatureCategory().equals(MainDataManager.FEATURE_SPORT_ASSISTANCE) ||
                (config.getFeatureCategory().equals(MainDataManager.FEATURE_QR_SCAN) && !isZh) ||
                config.getFeatureCategory().equals(MainDataManager.FEATURE_VFI) ||
                config.getFeatureCategory().equals(MainDataManager.FEATURE_VIDA) ||
                config.getFeatureCategory().equals(MainDataManager.FEATURE_TAINT_DETECT) ||
                checkLocalMaterialExist()) {

            if (PlatformUtils.isDownloading(CATEGORY_KEY)) {
                return;
            }

            mConfig = config;
            boolean mResourceReady = UserData.getInstance(getApplicationContext()).hasModelDownloaded();
            if (mResourceReady) {
                startActivity(config);
                return;
            }

            NetworkType networkType = NetworkUtils.INSTANCE.getNetworkType(this);
            if (networkType == null) {
                return;
            }
            if (!networkType.isAvailable()) {
                ToastUtils.show(getString(R.string.network_needed));
                return;
            }
            if (!networkType.isWifi()) {
                dialog.show();
            } else {
                fetchRemoteResource();
            }
        } else {
            startActivity(config);
        }
    }

    private void fetchRemoteResource() {
        if (PlatformUtils.isDownloading(CATEGORY_KEY)) {
            ToastUtils.show(getText(R.string.resource_loading).toString());
            return;
        }
        PlatformUtils.fetchCategoryMaterial(CATEGORY_KEY, new PlatformUtils.CategoryMaterialFetchListener() {
            @Override
            public void onStart() {
                runOnUiThread(new Runnable() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void run() {
                        loadLLContainer.setVisibility(View.VISIBLE);
                        tvLoad.setText(getText(R.string.resource_loading) + " 0%");
                        pbLoad.setProgress(0);
                        mUpdatedBeforeIntent = true;
                    }
                });
            }

            @Override
            public void onSuccess(CategoryData categoryData) {
                runOnUiThread(new Runnable() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void run() {
                        pbLoad.setProgress(100);
                        tvLoad.setText(getText(R.string.load_complete) + " " + 100 + "%");
                        UserData.getInstance(getApplicationContext()).setModelDownloaded(true);
                        loadLLContainer.setVisibility(View.GONE);
                        if (!mUpdatedBeforeIntent) {
                            startActivity(mConfig);
                        }
                    }
                });
            }

            @Override
            public void onMaterialFetchSuccess(@NonNull Material material, @NonNull String path) {

            }

            @Override
            public void onProgress(int i) {
                runOnUiThread(new Runnable() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void run() {
                        pbLoad.setProgress(i);
                        tvLoad.setText(getText(R.string.resource_loading) + " " + i + "%");
                    }
                });
            }

            @Override
            public void onFailed() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadLLContainer.setVisibility(View.GONE);
                        ToastUtils.show(getString(R.string.load_failed));
                    }
                });
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void startActivity(FeatureConfig config) {
        ImageSourceConfig imageSourceConfig;
        if (config.getImageSourceConfig() != null) {
            imageSourceConfig = config.getImageSourceConfig();
        } else {
            /** {zh}
             * 默认设置视频源为：前置相机，支持视频录制，默认分辨为mDefaultPreviewSize
             */
            /** {en}
             * Default setting video source is: front camera, support video recording, default resolution is mDefaultPreviewSize
             */
            imageSourceConfig = new ImageSourceConfig(TYPE_CAMERA, String.valueOf(Camera.CameraInfo.CAMERA_FACING_FRONT));
        }
        imageSourceConfig.setRecordable(false);
        imageSourceConfig.setRequestWidth(mDefaultPreviewSize.x);
        imageSourceConfig.setRequestHeight(mDefaultPreviewSize.y);
        imageSourceConfig.setFrameRate(String.valueOf(LocalParamManager.getInstance().getFrameRate()));

        //扫一扫使用1080p,提高识别率
        if (config.getFeatureCategory().equals(MainDataManager.FEATURE_QR_SCAN)) {
            imageSourceConfig.setRequestWidth(1920);
            imageSourceConfig.setRequestHeight(1080);
        }

        Class<?> clz;
        try {
            if (config.getActivityClassName() == null) {
                throw new ClassNotFoundException();
            }
            clz = Class.forName(config.getActivityClassName());
        } catch (ClassNotFoundException e) {
            ToastUtils.show("class " + config.getActivityClassName() + " not found," +
                    " ensure your config for this item");
            return;
        }

        Intent intent = new Intent(this, clz);
        intent.putExtra(ImageSourceConfig.IMAGE_SOURCE_CONFIG_KEY, new Gson().toJson(imageSourceConfig));
        // in sake of speed
        if (config.getAlgorithmConfig() != null) {
            intent.putExtra(AlgorithmConfig.ALGORITHM_CONFIG_KEY, new Gson().toJson(config.getAlgorithmConfig()));
        }
        if (config.getImageQualityConfig() != null) {
            intent.putExtra(ImageQualityConfig.IMAGE_QUALITY_KEY, new Gson().toJson(config.getImageQualityConfig()));

        }
        if (config.getStickerConfig() != null) {
            intent.putExtra(StickerConfig.StickerConfigKey, new Gson().toJson(config.getStickerConfig()));

        }
        if (config.getEffectConfig() != null) {
            intent.putExtra(EffectConfig.EffectConfigKey, new Gson().toJson(config.getEffectConfig()));
        }

        if (config.getUiConfig() != null) {
            intent.putExtra(UIConfig.KEY, new Gson().toJson(config.getUiConfig()));
        }

        if (config.getPageDetail() != null) {
            intent.putExtra(PageDetail.PAGE_DETAIL_KEY, config.getPageDetail());
        }

        List<Scene> scenes = new ArrayList<>();
        scenes.add(Scene.RECORDER);
        EOUtils.INSTANCE.getPermission().checkScenesPermissions(
                MainActivity.this,
                scenes,
                () -> {
                    try {
                        startActivity(intent);
                    } catch (Exception e) {
                        //do nothing
                    }
                    return null;
                }, strings -> null);
    }

    public int getChildPosition(int parentPosition) {
        int childPosition = 0;
        for (int i = 0; i < parentPosition; i++) {
            childPosition += mTabs.get(i).getChildren().size() + 1;
        }
        return childPosition;
    }

    public int getParentPosition(int childPosition) {
        for (int i = 0; i < mTabs.size(); i++) {
            if (childPosition < mTabs.get(i).getChildren().size() + 1) {
                return i;
            }
            childPosition -= mTabs.get(i).getChildren().size() + 1;
        }
        return 0;
    }

    public void checkResourceReady() {
        int savedVersionCode = UserData.getInstance(this).getVersion();
        int currentVersionCode = getVersionCode();
        File assetPath = this.getContext().getExternalFilesDir("assets");
        File dstFile = new File(assetPath, UnzipTask.DIR);
        if (savedVersionCode < currentVersionCode || !dstFile.exists()) {
            UnzipTask task = new UnzipTask(this);
            task.execute(UnzipTask.DIR);
        } else {
            checkLicenseReady();
        }
    }

    /// try to set use buildin sensor as soon as possible in order to make it work.
    private void tryToSetUseBuildInSensorASAP() {
        try {
            EffectLicenseHelper licenseHelper = EffectLicenseHelper.getInstance(this.getContext());
            ActivityManager am = (ActivityManager) this.getContext().getSystemService(this.getContext().ACTIVITY_SERVICE);
            ConfigurationInfo ci = am.getDeviceConfigurationInfo();
            String filePath = licenseHelper.getLicensePath(EffectsSDKLicenseInterface.LICENSE_FUNCTION_NAME.EFFECT);

            RenderManager renderManager = new RenderManager();
            int ret = renderManager.checkLicenseBase(filePath, licenseHelper.getLicenseMode() == EffectLicenseProvider.LICENSE_MODE_ENUM.ONLINE_LICENSE);
            if (ret == 0) {
                ret = renderManager.useBuiltinSensor(true);
                Log.e("", "useBuiltinSensor:" + ret);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void checkLicenseReady() {
        RequestLicenseTask task = new RequestLicenseTask(new RequestLicenseTask.ILicenseViewCallback() {

            @Override
            public Context getContext() {
                return getApplicationContext();
            }

            @Override
            public void onStartTask() {
                loadingLicenseDialog.setLoadingText(getString(R.string.wait_license));
                loadingLicenseDialog.show();
            }

            @Override
            public void onEndTask(boolean result) {
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    //{zh} 增加1s延迟, 防止二次校验太快造成弹窗闪烁体验问题  {en} Add a 1-second delay to prevent the experience of pop-up flashing caused by too fast secondary verification
                    UserData.getInstance(getApplicationContext()).setVersion(getVersionCode());
                    loadingLicenseDialog.dismiss();
                    tryToSetUseBuildInSensorASAP();
                    isCheckResource = true;
                }, 1000);
            }
        });
        task.execute();
    }

    private int getVersionCode() {
        Context context = getApplicationContext();
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public Context getContext() {
        return getApplicationContext();
    }

    @Override
    public void onStartTask() {
        loadingLicenseDialog.show();
    }

    @Override
    public void onEndTask(boolean result) {
        if (!result) {
            ToastUtils.show("fail to copy resource, check your resource and re-open");
        } else {
            loadingLicenseDialog.dismiss();
            checkLicenseReady();
        }
    }

    public void showLicenseInfoFragment() {
        // load local license info
        SharedPreferences sharedPreferences = this.getSharedPreferences(LICENSE_INFO_TAG, 0);
        String key = sharedPreferences.getString("key", null);
        String secret = sharedPreferences.getString("secret", null);

        if (null == mLicenseInfoFragment) {
            mLicenseInfoFragment = LicenseInfoFragment.newInstance(key, secret);
            mLicenseInfoFragment.setCallback(this);
        }
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        Fragment existFragment = fm.findFragmentByTag(LICENSE_INFO_TAG);

        if (existFragment == null) {
            ft.add(R.id.fl_input_license_info, mLicenseInfoFragment, LICENSE_INFO_TAG).show(mLicenseInfoFragment).commitNow();
        } else {
            ft.show(mLicenseInfoFragment).commitNow();
        }
    }

    public void hideLicenseInfoFragment() {
        if (mLicenseInfoFragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.hide(mLicenseInfoFragment).commitNow();
        }
    }

    @Override
    public void onLicenseInfoSaved(String key, String secret) {
        EffectLicenseHelper.setKey(key);
        EffectLicenseHelper.setSecret(secret);
        checkResourceReady();
    }

    private boolean checkLocalMaterialExist() {
        boolean result = false;
        String tempRootPath = "/storage/emulated/0/Android/data/com.effectsar.labcv.demo/files/assets/resource/material/stickers";
        String sdcardRootPath = new EffectResourceHelper(getApplicationContext()).getStickerPath("local");
        FileFilter fileFilter = new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if (pathname.isDirectory() && !pathname.isHidden()) {
                    return true;
                }
                return false;
            }
        };
        File[] tempFiles = new File(tempRootPath).listFiles(fileFilter);
        File[] sdcardFiles = new File(sdcardRootPath).listFiles(fileFilter);

        if (tempFiles != null && tempFiles.length > 0) {
            result = true;
        }
        if (sdcardFiles != null && sdcardFiles.length > 0) {
            result = true;
        }
        return result;
    }

}
