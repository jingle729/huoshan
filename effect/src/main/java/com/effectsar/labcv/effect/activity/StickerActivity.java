package com.effectsar.labcv.effect.activity;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.effectsar.labcv.common.model.CaptureResult;
import com.effectsar.labcv.common.task.SavePicTask;
import com.effectsar.labcv.common.utils.BitmapUtils;
import com.effectsar.labcv.common.utils.DensityUtils;
import com.effectsar.labcv.common.utils.LocaleUtils;
import com.effectsar.labcv.common.utils.PlatformUtils;
import com.effectsar.labcv.common.utils.ToastUtils;
import com.effectsar.labcv.common.view.bubble.BubbleWindowManager;
import com.effectsar.labcv.core.Config;
import com.effectsar.labcv.core.effect.EffectManager;
import com.effectsar.labcv.core.effect.EffectResourceHelper;
import com.effectsar.labcv.core.util.LogUtils;
import com.effectsar.labcv.effect.R;
import com.effectsar.labcv.effect.config.StickerConfig;
import com.effectsar.labcv.effect.fragment.SelectUploadFragment;
import com.effectsar.labcv.effect.fragment.TabStickerFragment;
import com.effectsar.labcv.effect.model.SelectUploadItem;
import com.effectsar.labcv.effectsdk.EffectsSDKEffectConstants;
import com.effectsar.labcv.resource.MaterialResource;
import com.effectsar.platform.struct.Material;
import com.effectsar.platform.struct.PlatformError;
import com.google.gson.Gson;
import com.volcengine.ck.album.AlbumEntrance;
import com.volcengine.ck.album.base.AlbumConfig;
import com.volcengine.ck.album.utils.AlbumExtKt;
import com.volcengine.effectone.permission.Scene;
import com.volcengine.effectone.singleton.AppSingleton;
import com.volcengine.effectone.utils.EOUtils;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * {zh}
 * 贴纸活动页
 */

/**
 * {en}
 * Sticker activity page
 */


public class StickerActivity extends BaseEffectActivity implements TabStickerFragment.OnTabStickerFramentCallback, SelectUploadFragment.ISelectUploadCallback {
    private TabStickerFragment mFragment = null;
    public static final String EFFECT_TAG = "effect_board_tag";
    public static final String FRAGMENT_SELECT_UPLOAD = "fragment_select_upload";
    public static final int ANIMATION_DURATION = 400;

    private StickerConfig mStickerConfig;
    private MaterialResource mSelectedItem;
    private HashMap<Material, ResourceIndex> mResourceIndexMap = new HashMap<>();

//    private StickerFetch mStickerFetch;
//    private ResourceManager mResourceManager;

    //  {zh} 上传贴纸所用成员  {en} Members used to upload stickers
    private SelectUploadFragment mSelectUploadFragment;
    public static final int SELECT_UPLOAD = R.drawable.ic_select_upload;
    public static final int DEFAULT_UPLOAD_1 = R.drawable.ruining;
    public static final int DEFAULT_UPLOAD_2 = R.drawable.shiwen;
    public static final int DEFAULT_UPLOAD_3 = R.drawable.yangfan;
    protected static final int REQUEST_SELECT_UPLOAD_PICKER = 13;
//    private FaceVerifyThreadHandler mThreadHandler;
    private Handler mHandler;
    private Messenger mMessenger;
    private String mSelectedItemKey = null;
    private String mSelectedItemType = null;
    private ActivityResultLauncher<AlbumConfig> uploadSelectedLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        mEffectManager.addMessageListener(this);

        LayoutInflater.from(this)
                .inflate(R.layout.layout_select_upload,findViewById(R.id.fl_effect_fragment), true);

        mStickerConfig = parseStickerConfig(getIntent());

        //   {zh} 默认弹出底部面板       {en} Bottom panel pops up by default
        showBoardFragment();
        if (null == mStickerConfig) return;

        ArrayList<MaterialResource> localMaterialList = fetchLocalMaterialList();
        if (localMaterialList != null) {
            mFragment.setData(localMaterialList);
        } else {
            PlatformUtils.fetchCategoryDataWithCache(mStickerConfig.getType(), categoryData -> {
                if (categoryData == null) {
                    return;
                }
                if (!mFragment.hasValidData()) {
                    runOnUiThread(() -> {
                        mFragment.setData(categoryData);
                    });
                }
            });
        }

        mHandler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (setRenderCacheTexture((Bitmap) msg.obj)) {
                    //  {zh} RenderCacheTexture 设置成功  {en} RenderCacheTexture set successfully
                    hideBoardFragment(mFragment);
                }
//                 {zh} 下方为上传图片前检测是否存在人脸。452去掉该块逻辑                 {en} Below is to detect whether there is a face before uploading the picture. 452 Remove the block logic
//                int what = msg.what;
//                switch (what) {
//                    case FaceVerifyThreadHandler.FACE_DETECT:
//                        //   {zh} 上传的图片中有且仅有一张才开始设置渲染缓存       {en} Only one of the uploaded images was detected
//                        // start detect when only one of the uploaded images is detected
//                        if (msg.arg1 == 1){
////                             {zh} ToastUtils.show("人脸检测完成！开始setRenderCache");                             {en} ToastU tils.show ("face detection complete! start setRenderCache");
//                            if (setRenderCacheTexture((CaptureResult) msg.obj)) {
//                                //  {zh} RenderCacheTexture 设置成功  {en} RenderCacheTexture set successfully
//                                hideBoardFragment(mFragment);
//                            }
//
//                        }else {
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    ToastUtils.show(getResources().getString(R.string.no_face_detected));
//
//                                }
//                            });
//                            LogUtils.e("the bitmap uploaded contains no face or more than one face!!");
//                        }
//                        break;
//                }
            }
        };

        uploadSelectedLauncher = registerForActivityResult(albumActivityContract, result -> {
            if (!result.isEmpty()) {
                String mediaPath = AlbumExtKt.getAbsolutePath(result.get(0));
                checkFaceContained(mediaPath);
            }
        });
    }

    @Override
    protected void onDestroy() {
        mEffectManager.removeMessageListener(this);
        super.onDestroy();
        mSelectedItem = null;
//        mResourceManager.clearLoadingResource();
        if(mHandler != null) {
            mHandler.removeCallbacks(null);
            mHandler = null;
        }
//        if(mThreadHandler != null) {
//            mThreadHandler.removeCallbacks(null);
//            mThreadHandler = null;
//        }
        if (mFragment != null) {
            mFragment.destroy();
            mFragment = null;
        }
        if (mSelectUploadFragment != null) {
            mSelectUploadFragment.onDestroy();
            mSelectUploadFragment = null;
        }
        if (mResourceIndexMap != null) {
            mResourceIndexMap.clear();
            mResourceIndexMap=null;
        }
    }

    private StickerConfig parseStickerConfig(Intent intent) {
        String sAlgorithmConfig = intent.getStringExtra(StickerConfig.StickerConfigKey);
        if (sAlgorithmConfig == null) {
            return null;
        }

        return new Gson().fromJson(sAlgorithmConfig, StickerConfig.class);
    }


    private TabStickerFragment generateStickerFragment() {
        if (mFragment != null) return mFragment;
        mFragment = new TabStickerFragment();
        mFragment.setCallback(this);
        return mFragment;
    }
    public boolean isActivityValid(){
        if(this == null || isDestroyed() || isFinishing()){
            return false;
        }
        return true;
    }
    @Override
    public void onStickerSelected(MaterialResource item, int tabIndex, int contentIndex) {

        if (item == null || item.isLocal()) {
            mSelectedItem = null;
            String path = item == null ? "" : item.getPath();
            mEffectManager.setStickerAbs(path);
            didSelectItem(item, tabIndex, contentIndex);
        } else {
            willSelectItem(item, tabIndex, contentIndex);
            PlatformUtils.fetchMaterial(item.getRemoteMaterial(), new PlatformUtils.MaterialFetchListener() {
                @Override
                public void onStart(@NonNull Material material) {
                    refreshResourceUI(material);
                }

                @Override
                public void onSuccess(@NonNull Material material, @NonNull String path) {
                    if(!isActivityValid())return;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mSelectedItem != null && material == mSelectedItem.getRemoteMaterial()) {
                                didSelectItem(mSelectedItem);
                                mSelectedItem = null;

                                if (null != mSurfaceView) {
                                    mSurfaceView.queueEvent(new Runnable() {
                                        @Override
                                        public void run() {
                                            mEffectManager.setStickerAbs(path);
                                        }
                                    });
                                }

                                if (mSelectedItemType != null && mSelectedItemType.contains("hideboard")) {
                                    hideBoardFragment(mFragment);
                                }
                                if (mSelectedItemType != null && mSelectedItemType.contains("msgcap")) {
                                    setImgCompareViewVisibility(View.GONE);
                                } else {
                                    setImgCompareViewVisibility(View.VISIBLE);
                                }

                                String watermark = LocaleUtils.getCurrentLocale(mContext).getLanguage().equals("zh")? "shuiyin" : "shuiyin_en";
                                if (mSelectedItemType != null && mSelectedItemType.contains("GAN")) {
                                    mEffectManager.appendComposeNodes(new String[]{watermark});
                                    mEffectManager.updateComposerNodeIntensity(watermark,"save",0);
                                } else {
                                    mEffectManager.removeComposeNodes(new String[]{watermark});
                                }
                            }
                            refreshResourceUI(material);
                            mResourceIndexMap.remove(material);
                        }
                    });
                }

                @Override
                public void onProgress(@NonNull Material material, int i) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            refreshResourceUI(material);
                        }
                    });
                }

                @Override
                public void onFailed(@NonNull Material material, @NonNull Exception e, @NonNull PlatformError platformError) {
                    if(!isActivityValid())return;
                    runOnUiThread(() -> {
                        ToastUtils.show(platformError.name());
                        refreshResourceUI(material);
                    });
                    mResourceIndexMap.remove(material);
                }
            });
        }
    }


    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.img_record) {
            if (mSelectedItemType != null) {
                if (mSelectedItemType.contains("msgcap")) {
                    mEffectManager.sendCaptureMessage();
                    return;
                }
            }
        }
        super.onClick(view);
        if (id == R.id.img_open) {
            showBoardFragment();
        } else if (id == R.id.img_default_activity) {
            resetDefault();
            mSelectedItem = null;
            didSelectItem(null, 0, 0);
        } else if (view.getId() == R.id.img_setting) {
            mBubbleWindowManager.hideResolutionOption(view,480);
            mBubbleWindowManager.show(view,
                    mBubbleCallback,
                    BubbleWindowManager.ITEM_TYPE.BEAUTY,
                    BubbleWindowManager.ITEM_TYPE.PERFORMANCE,
                    BubbleWindowManager.ITEM_TYPE.RESOLUTION
            );
        }

    }

    @Override
    public void onClickEvent(View view) {
        if (view.getId() == R.id.iv_close_board) {
            hideBoardFragment(mFragment);

        } else if (view.getId() == R.id.iv_record_board) {
            if (mSelectedItemType != null) {
                if (mSelectedItemType.contains("msgcap")) {
                    mEffectManager.sendCaptureMessage();
                    return;
                }
            }
            takePic();
        } else if (view.getId() == R.id.img_default) {
            resetDefault();
            mSelectedItem = null;
            didSelectItem(null, 0, 0);
        }
    }

    public void setSelectUploadFragmentHeight(float y, int duration) {
        int boardLayoutID = getResources().getIdentifier("fl_select_upload", "id", getPackageName());
        View selectUploadView = getWindow().findViewById(boardLayoutID);
        selectUploadView.animate().y(y).setDuration(duration).start();
    }

    @Override
    public void onEffectInitialized() {
        super.onEffectInitialized();
        if (mStickerConfig != null && !TextUtils.isEmpty(mStickerConfig.getStickerPath())) {
            mEffectManager.setStickerAbs(mStickerConfig.getStickerPath());
        }

    }


    @Override
    public boolean closeBoardFragment() {
        if (mFragment != null && mFragment.isVisible()) {
            hideBoardFragment(mFragment);
            return true;
        }
        return false;

    }
    @Override
    protected void hideBoardFragment(Fragment fragment) {
        // change SelectUpdateFragment position height
        float height = getResources().getDisplayMetrics().heightPixels
                 - getResources().getDimensionPixelSize(R.dimen.height_board_bottom)
                 - DensityUtils.dp2px(mContext,64+6);
        setSelectUploadFragmentHeight(height ,ANIMATION_DURATION);
        super.hideBoardFragment(fragment);
    }

    @Override
    public boolean showBoardFragment() {
        if (null == mFragment) {
            mFragment = generateStickerFragment();
        }
        showBoardFragment(mFragment, mBoardFragmentTargetId, EFFECT_TAG, true);

        // change SelectUpdateFragment position height
        float height = getResources().getDisplayMetrics().heightPixels
                - getResources().getDimensionPixelSize(R.dimen.height_board_total)
                - DensityUtils.dp2px(mContext,64+6);
        setSelectUploadFragmentHeight(height ,ANIMATION_DURATION);

        return true;
    }

    @Override
    protected boolean isAutoTest() {
        return mStickerConfig != null && !TextUtils.isEmpty(mStickerConfig.getStickerPath());
    }

    @Override
    protected void setEffectByConfig() {
        if (mEffectManager == null || mStickerConfig == null) return;
        mEffectManager.setSticker(mStickerConfig.getStickerPath());
    }

    private void willSelectItem(MaterialResource stickerItem, int tabIndex, int contentIndex) {
        mSelectedItem = stickerItem;
        mResourceIndexMap.put(stickerItem.getRemoteMaterial(), new ResourceIndex(tabIndex, contentIndex));
    }

    private void didSelectItem(MaterialResource stickerItem) {
        ResourceIndex index = mResourceIndexMap.get(stickerItem.getRemoteMaterial());
        if (index == null) {
            didSelectItem(stickerItem, 0, 0);
        } else {
            didSelectItem(stickerItem, index.tabIndex, index.contentIndex);
        }
    }

    private void didSelectItem(MaterialResource stickerItem, int tabIndex, int contentIndex) {
        mFragment.selectItem(tabIndex, contentIndex);

        if (stickerItem != null && mBubbleTipManager != null) {
            mBubbleTipManager.show(stickerItem.getTitle(), null);
            if (!TextUtils.isEmpty(stickerItem.getTips())) {
                ToastUtils.show(stickerItem.getTips());
            }
        }

         //  {zh} TODO: 选择弹窗  {en} TODO: Select popup
        if (mSelectedItem != null) {
            if (!TextUtils.isEmpty(mSelectedItem.getRemoteMaterial().getExtra().getKey())) {
                // 1. display SelectUploadFragment 2. start face verify thread
                mSelectedItemKey = mSelectedItem.getRemoteMaterial().getExtra().getKey();
                showSelectUploadFragment();
            } else {
                // 1. clear render cache 2. close face verify thread 3. hide SelectUploadFragment
                closeSelectUploadFragment();
            }
            mSelectedItemType = mSelectedItem.getRemoteMaterial().getExtra().getType();
        } else {
            closeSelectUploadFragment();
            mSelectedItemKey = null;
            mSelectedItemType = null;
        }

    }

    private void refreshResourceUI(Material material) {
        if (mResourceIndexMap == null) return;
        ResourceIndex index = mResourceIndexMap.get(material);
//        assert index != null;
        if (index != null) {
            mFragment.refreshItem(index.tabIndex, index.contentIndex);
        }
    }

    @Override
    public void onUploadSelected(SelectUploadItem buttonItem, int position) {
        if (buttonItem.getIcon() == SELECT_UPLOAD) {
            EOUtils.INSTANCE.getPermission().checkPermissions(this, Scene.ALBUM, () -> {
                AlbumConfig config = new AlbumConfig();
                config.setAllEnable(false);
                config.setVideoEnable(false);
                config.setImageEnable(false);
                config.setMaxSelectCount(1);
                config.setShowGif(false);
                uploadSelectedLauncher.launch(config);
                return null;
            }, strings -> {
                AlbumEntrance.INSTANCE.showAlbumPermissionTips(StickerActivity.this);
                return null;
            });
        } else {
            if (setRenderCacheTexture(buttonItem.getIcon())) {
                hideBoardFragment(mFragment);
            }
        }
    }

    /** {zh}
     * 设置选择的底图
     * Set the selected image to compare
     *
     * @param imagePath
     */
    /** {en}
     * Set the selected base map
     * Set the selected image to compare
     *
     * @param imagePath
     */

    public void checkFaceContained(String imagePath) {
        Bitmap bitmap = BitmapUtils.decodeBitmapFromFile(imagePath, 800, 800);
        if (bitmap != null && !bitmap.isRecycled()) {
            Message msg = mHandler.obtainMessage(1, bitmap);
            msg.replyTo = mMessenger;
            msg.sendToTarget();
        } else {
            ToastUtils.show("failed to get image");
        }
    }

    @Override
    public void onMessageReceived(int i, int i1, int i2, String s) {
        super.onMessageReceived(i, i1, i2, s);
        ByteBuffer buf = null;
        if(i == EffectManager.MSG_ID_CAPTURE_IMAGE_RESULT && s != null && !s.isEmpty())
        {
            buf = mEffectManager.getCapturedImageByteBufferWithKey(s,mTextureWidth,mTextureHeight);
            final CaptureResult captureResult = new CaptureResult(buf, mTextureWidth, mTextureHeight);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (null == captureResult || captureResult.getWidth() == 0 || captureResult.getHeight() == 0 || null == captureResult.getByteBuffer()) {
                        ToastUtils.show(getString(com.effectsar.labcv.common.R.string.capture_fail));
                    } else {
                        LogUtils.d("takePic return success");
                        SavePicTask task = new SavePicTask(new SavePicTask.SavePicDelegate() {
                            @Override
                            public ContentResolver getContentResolver() {
                                return mContext.getContentResolver();
                            }

                            @Override
                            public void onSavePicFinished(boolean success, String path) {
                                if (success) {
                                    ToastUtils.show(getString(com.effectsar.labcv.common.R.string.capture_ok));
                                } else {
                                    ToastUtils.show(getString(com.effectsar.labcv.common.R.string.capture_fail));
                                }


                            }
                        });
                        task.execute(captureResult);
                    }
                }
            });
        }
    }

    private static class ResourceIndex {
        int tabIndex;
        int contentIndex;

        public ResourceIndex(int tabIndex, int contentIndex) {
            this.tabIndex = tabIndex;
            this.contentIndex = contentIndex;
        }
    }

    private void showSelectUploadFragment(){
        // create SelectUploadFragment, ThreadHandler, Messenger if empty

        List<SelectUploadItem> list = new ArrayList<SelectUploadItem>(){
            {
                add(new SelectUploadItem(SELECT_UPLOAD));
                switch (mSelectedItemKey) {
                    case "pixelLoopInput":
                        add(new SelectUploadItem(DEFAULT_UPLOAD_1));
                        add(new SelectUploadItem(DEFAULT_UPLOAD_2));
                        add(new SelectUploadItem(DEFAULT_UPLOAD_3));
                        break;
                    case "swappermeInput":
                        add(new SelectUploadItem(R.drawable.ic_qinglv_0));
                        add(new SelectUploadItem(R.drawable.ic_qinglv_1));
                        add(new SelectUploadItem(R.drawable.ic_qinglv_2));
                        break;
                }
            }
        };
        if (mSelectUploadFragment == null) {
            mSelectUploadFragment = SelectUploadFragment.newInstance(list).setUploadSelectedCallback(this);
        } else {
            mSelectUploadFragment.updateItem(list);
        }

        // set SelectUpload position height
        float height = getResources().getDisplayMetrics().heightPixels
                - getResources().getDimensionPixelSize(R.dimen.height_board_total)
                - DensityUtils.dp2px(mContext,64+6);
        setSelectUploadFragmentHeight(height ,ANIMATION_DURATION);

        showFragment(mSelectUploadFragment,R.id.fl_select_upload,FRAGMENT_SELECT_UPLOAD);

//        if (mThreadHandler == null){
//            mThreadHandler = FaceVerifyThreadHandler.createFaceVerifyHandlerThread(mContext);
//        }
        if (mMessenger == null) {
            mMessenger = new Messenger(mHandler);
        }
//        mThreadHandler.resume();
    }

    private void hideSelectUploadFragment(){
        hideFragment(mSelectUploadFragment);
    }

    private void closeSelectUploadFragment(){
        if (mSelectedItemKey != null) {
            clearRenderCacheTexture(mSelectedItemKey);
        }
//        if (mThreadHandler != null) {
//            mThreadHandler.quit();
//            mThreadHandler = null;
//        }

        if (mMessenger != null) {
            mMessenger = null;
        }
        hideFragment(mSelectUploadFragment);
    }

    private ArrayList<MaterialResource> fetchLocalMaterialList(){
        ArrayList<MaterialResource> localMaterialList;
        String tempRootPath = "/data/local/tmp/EffectsARSDK/stickers/local";
        localMaterialList = fetchMaterialList(tempRootPath);
        if (localMaterialList == null) {
            String sdcardRootPath = new EffectResourceHelper(getApplicationContext()).getStickerPath("local");
            if (Config.ENABLE_ASSETS_SYNC) {
                sdcardRootPath = new EffectResourceHelper(getApplicationContext()).getStickerPath();
            }
            localMaterialList = fetchMaterialList(sdcardRootPath);
        }
        return localMaterialList;
    }

    private ArrayList<MaterialResource> fetchMaterialList(String path){
        ArrayList<MaterialResource> materialList = new ArrayList<>();
        File[] files = new File(path).listFiles(pathname -> {
            if (pathname.isDirectory() && !pathname.isHidden()) {
                return true;
            }
            return false;
        });
        if (files != null) {
            for (File file : files) {
                MaterialResource materialResource = new MaterialResource();
                materialResource.setTitle(file.getName());
                materialResource.setPath(new File(path, file.getName()).getAbsolutePath());
                materialList.add(materialResource);
            }
        }
        if (materialList.size() > 0) {
            return materialList;
        }
        return null;
    }

    public boolean setRenderCacheTexture(@DrawableRes int id){
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), id);
        if (bitmap != null) {
            ByteBuffer buffer = BitmapUtils.bitmap2ByteBuffer(bitmap);
            if (!mEffectManager.setRenderCacheTexture(
                    mSelectedItemKey,
                    buffer,
                    bitmap.getWidth(),
                    bitmap.getHeight(),
                    4*bitmap.getWidth(),
                    EffectsSDKEffectConstants.PixlFormat.RGBA8888,
                    EffectsSDKEffectConstants.Rotation.CLOCKWISE_ROTATE_0)){
                LogUtils.e("setRenderCacheTexture fail!!");
                return false;
            }
            return true;
        }
        return false;
    }

    public boolean setRenderCacheTexture(Bitmap bitmap){
        if (bitmap != null) {
            ByteBuffer buffer = BitmapUtils.bitmap2ByteBuffer(bitmap);
            if (!mEffectManager.setRenderCacheTexture(
                    mSelectedItemKey,
                    buffer,
                    bitmap.getWidth(),
                    bitmap.getHeight(),
                    4*bitmap.getWidth(),
                    EffectsSDKEffectConstants.PixlFormat.RGBA8888,
                    EffectsSDKEffectConstants.Rotation.CLOCKWISE_ROTATE_0)){
                LogUtils.e("setRenderCacheTexture fail!!");
                return false;
            }
            return true;
        }
        return false;
    }

    public boolean setRenderCacheTexture(CaptureResult captureResult){
        if (captureResult == null){
            LogUtils.e("decodeByteBuffer return null!!");
            return false;
        }
        if (!mEffectManager.setRenderCacheTexture(
                mSelectedItemKey,
                captureResult.getByteBuffer(),
                captureResult.getWidth(),
                captureResult.getHeight(),
                4*captureResult.getWidth(),
                EffectsSDKEffectConstants.PixlFormat.RGBA8888,
                EffectsSDKEffectConstants.Rotation.CLOCKWISE_ROTATE_0)){
            LogUtils.e("setRenderCacheTexture fail!!");
            return false;
        }
        return true;
    }

    public void clearRenderCacheTexture(String key){
        if (key != null) {
            ByteBuffer buffer = ByteBuffer.allocateDirect(0);
            if (!mEffectManager.setRenderCacheTexture(
                    mSelectedItemKey,
                    buffer,
                    0,
                    0,
                    0,
                    EffectsSDKEffectConstants.PixlFormat.RGBA8888,
                    EffectsSDKEffectConstants.Rotation.CLOCKWISE_ROTATE_0)){
                LogUtils.e("setRenderCacheTexture fail!!");
            }
        }
    }

}
