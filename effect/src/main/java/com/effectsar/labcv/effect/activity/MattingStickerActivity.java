package com.effectsar.labcv.effect.activity;

import static com.effectsar.labcv.effect.manager.EffectDataManager.TYPE_CLOSE;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.effectsar.labcv.common.adapter.ItemViewRVAdapter;
import com.effectsar.labcv.common.fragment.ItemViewPageFragment;
import com.effectsar.labcv.common.imgsrc.video.SimplePlayer;
import com.effectsar.labcv.common.imgsrc.video.VideoSourceImpl;
import com.effectsar.labcv.common.model.CaptureResult;
import com.effectsar.labcv.common.utils.BitmapUtils;
import com.effectsar.labcv.common.utils.FileUtils;
import com.effectsar.labcv.common.utils.PlatformUtils;
import com.effectsar.labcv.common.utils.ToastUtils;
import com.effectsar.labcv.common.view.bubble.BubbleWindowManager;
import com.effectsar.labcv.core.effect.EffectResourceHelper;
import com.effectsar.labcv.core.util.ImageUtil;
import com.effectsar.labcv.core.util.LogUtils;
import com.effectsar.labcv.effect.R;
import com.effectsar.labcv.effect.adapter.EffectRVAdapter;
import com.effectsar.labcv.effect.config.StickerConfig;
import com.effectsar.labcv.effect.fragment.MattingStickerFragment;
import com.effectsar.labcv.effect.model.EffectButtonItem;
import com.effectsar.labcv.effect.view.ProgressBar;
import com.effectsar.labcv.effectsdk.EffectsSDKEffectConstants;
import com.effectsar.platform.struct.CategoryData;
import com.effectsar.platform.struct.Material;
import com.google.gson.Gson;
import com.volcengine.ck.LocalAlbumActivityContract;
import com.volcengine.ck.album.AlbumEntrance;
import com.volcengine.ck.album.base.AlbumConfig;
import com.volcengine.ck.album.utils.AlbumExtKt;
import com.volcengine.effectone.permission.Scene;
import com.volcengine.effectone.singleton.AppSingleton;
import com.volcengine.effectone.utils.EOUtils;

import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/** {zh}
 * Created  on 2021/5/24 4:01 下午
 */
/** {en}
 * Created on 2021/5/24 4:01 pm
 */

public class MattingStickerActivity extends BaseEffectActivity implements MattingStickerFragment.MattingStickerCallback, ItemViewRVAdapter.OnItemClickListener<EffectButtonItem> {
    private MattingStickerFragment mFragment = null;
    public static final String EFFECT_TAG = "effect_board_tag";
    public static final int TYPE_NO_MATTING = 0;
    public static final int TYPE_UPLOAD_MATTING = 1;
    protected String mBgPath;
    private final String BgKey = "BCCustomBackground";
    private final String BgValueDefault = "matting_bg/GE/generalEffect/resource1/background.png";
    private final String BgValueChromaDefault = "chroma_matting_bg/AmazingFeature/image/background.png";
    private final String MaterialPath = "matting_bg";
    private final String chromaMaterialPath = "chroma_matting_bg";
    private EffectResourceHelper mResourceProvider;

    private VideoSourceImpl videoSource = null;
    private ImageUtil newUtil = null;

    private EffectRVAdapter mAdapter;
    private StickerConfig mStickerConfig;

    private final LocalAlbumActivityContract albumActivityContract = new LocalAlbumActivityContract();
    private ActivityResultLauncher<AlbumConfig> albumLauncher;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mStickerConfig = parseStickerConfig(getIntent());
        mResourceProvider = new EffectResourceHelper(this);

        albumLauncher = registerForActivityResult(albumActivityContract, result -> {
            if (!result.isEmpty()) {
                mBgPath = AlbumExtKt.getAbsolutePath(result.get(0));
            }
        });
    }

    private StickerConfig parseStickerConfig(Intent intent) {
        String sAlgorithmConfig = intent.getStringExtra(StickerConfig.StickerConfigKey);
        if (sAlgorithmConfig == null) {
            return null;
        }

        return new Gson().fromJson(sAlgorithmConfig, StickerConfig.class);
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        super.onSurfaceCreated(gl10, eglConfig);
        newUtil = new ImageUtil();
    }

    @Override
    public void onPause() {
        mSurfaceView.queueEvent(new Runnable() {
            @Override
            public void run() {
                if (videoSource != null) {
                    videoSource.close();
                    videoSource = null;
                }

                if (newUtil != null) {
                    newUtil.release();
                    newUtil = null;
                }
            }
        });
        super.onPause();
    }

    private MattingStickerFragment generateStickerFragment(){
        if (mFragment != null) return mFragment;

        ArrayList<Fragment> fragments = new ArrayList<Fragment>(){
            {
                ArrayList<EffectButtonItem> items = new ArrayList<EffectButtonItem>(){
                    {
//                        add(new EffectButtonItem(TYPE_NO_MATTING,R.drawable.clear,R.string.close));
                        EffectButtonItem itemClose = new EffectButtonItem(TYPE_CLOSE, R.drawable.clear_no_border, R.string.close);
                        EffectButtonItem mItem = new EffectButtonItem(TYPE_UPLOAD_MATTING,R.drawable.ic_upload_photo,R.string.upload_title);
                        mItem.setParent(mItem);
                        itemClose.setParent(itemClose);
                        itemClose.setSelected(isOpenVirtualBg);
                        mItem.setSelectChild(itemClose);
                        add(itemClose);
                        add(mItem);
                    }
                };
                mAdapter = new EffectRVAdapter(items,MattingStickerActivity.this);
                ItemViewPageFragment<EffectRVAdapter> fragment = new ItemViewPageFragment<>();
                fragment.setAdapter(mAdapter);
                add(fragment);
            }
        };
        ArrayList<String> titles = new ArrayList<String>(){
            {
                add(getString(mStickerConfig.getType().equals("feature_matting_sticker") ? R.string.tab_matting : R.string.tab_chroma_matting));
            }
        };

        mFragment = new MattingStickerFragment(fragments,titles);
        mFragment.setMattingStickerCallback(this);
        if (!mStickerConfig.getType().equals("feature_matting_sticker")){
            ToastUtils.show(getString(R.string.matting_sticker_tips));
        }

        return mFragment;

    }

    @Override
    public void onClickEvent(View view) {
        if (view.getId() == R.id.iv_close_board) {
            hideBoardFragment(mFragment);
        } else if (view.getId() == R.id.iv_record_board) {
            takePic();
        } else if (view.getId() == R.id.img_default) {
            mBgPath = null;
            mEffectManager.removeRenderCache(BgKey);
            mSurfaceView.queueEvent(() -> {
                if (videoSource != null) {
                    videoSource.close();
                    videoSource = null;
                }
            });

            checkAndSetRenderCache();
        }
    }

    @Override
    public void onProgressChanged(ProgressBar progressBar, float progress, boolean isFormUser) {

    }

    @Override
    public void onProgressEnd(ProgressBar progressBar, float progress, boolean isFormUser) {

    }

    boolean isOpenVirtualBg = true;
    @Override
    public void onItemClick(EffectButtonItem item, int position) {
        if(item.getId() == TYPE_CLOSE){
            boolean selected = item.isSelected();
            isOpenVirtualBg = !selected;

            if(isOpenVirtualBg){
                item.getParent().setSelectChild(null);
                onEffectInitialized();
            }else{
                item.getParent().setSelectChild(item);
                removeVirtualBg();
            }

            item.setSelected(isOpenVirtualBg);
            mAdapter.notifyItemChanged(0);
        }else if (item.getId() == TYPE_NO_MATTING){
            removeVirtualBg();
        }else if (item.getId() == TYPE_UPLOAD_MATTING){
            if(!isOpenVirtualBg){
                ToastUtils.show("Please turn on virtual background first");
                return;
            }
            mSurfaceView.queueEvent(() -> {
                if (videoSource != null) {
                    videoSource.close();
                    videoSource = null;
                }
            });
            startChoosePic();
        }
    }

    protected void startChoosePic() {
        EOUtils.INSTANCE.getPermission().checkPermissions(this, Scene.ALBUM, () -> {
            AlbumConfig config = new AlbumConfig();
            config.setAllEnable(false);
            config.setVideoEnable(true);
            config.setImageEnable(true);
            config.setMaxSelectCount(1);
            config.setShowGif(false);
            albumLauncher.launch(config);
            return null;
        }, strings -> {
            AlbumEntrance.INSTANCE.showAlbumPermissionTips(this);
            return null;
        });
    }

    private void removeVirtualBg(){
        if (null == mSurfaceView){
            return;
        }
        mSurfaceView.queueEvent(()->{
            mEffectManager.setSticker("");
        });
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        int id = view.getId();
        if (id == R.id.img_open) {
            showBoardFragment();
        } else if (id == R.id.img_default_activity) {
            mBgPath = null;
            mSurfaceView.queueEvent(() -> {
                if (videoSource!= null) {
                    videoSource.close();
                    videoSource = null;
                }
            });
            checkAndSetRenderCache();
        } else if (view.getId() == R.id.img_setting) {
            mBubbleWindowManager.hideResolutionOption(view,480);
            mBubbleWindowManager.show(
                    view,
                    mBubbleCallback,
                    BubbleWindowManager.ITEM_TYPE.BEAUTY,
                    BubbleWindowManager.ITEM_TYPE.PERFORMANCE,
                    BubbleWindowManager.ITEM_TYPE.RESOLUTION
            );
        }
    }

    private void setRenderCacheFromPicture() {
        if (mBgPath == null) {
            return;
        }

        CaptureResult captureResult = decodeByteBuffer(mBgPath);
        if (captureResult == null){
            LogUtils.e("decodeByteBuffer return null!!");
            return;
        }
        boolean result = mEffectManager.setRenderCacheTexture(
                BgKey,
                captureResult.getByteBuffer(),
                captureResult.getWidth(),
                captureResult.getHeight(),
                4 * captureResult.getWidth(),
                EffectsSDKEffectConstants.PixlFormat.RGBA8888,
                EffectsSDKEffectConstants.Rotation.CLOCKWISE_ROTATE_0);
        if (!result){
            LogUtils.e("setRenderCacheTexture fail!!");
        }
    }

    private void setRenderCacheFromVideo() {
        if (mBgPath == null) {
            return;
        }
        LogUtils.i("setRenderCacheTexture from video");

        mSurfaceView.queueEvent(()-> {
            if (videoSource != null) {
                videoSource.close();
                videoSource = null;
            }

            videoSource = new VideoSourceImpl(null, new SimplePlayer.IPlayStateListener() {
                @Override
                public void videoAspect(int width, int height, int videoRotation) {

                }

                @Override
                public void frameRate(int rate) {

                }

                @Override
                public void onVideoEnd() {

                }
            }, null);

            videoSource.setSimplerAudioOn(false);

            videoSource.open(mBgPath, surfaceTexture -> mSurfaceView.queueEvent(new Runnable() {
                @Override
                public void run() {
                    if (videoSource == null) return ;
                    if (!videoSource.isReady()) return ;

                    videoSource.update();
                    int texture = videoSource.getTexture();
                    int rotation = videoSource.getOrientation();
                    int width = rotation % 180 == 0 ? videoSource.getWidth() : videoSource.getHeight();
                    int height = rotation % 180 == 0 ? videoSource.getHeight() : videoSource.getWidth();

                    int curTexture = newUtil.transferTextureToTexture(texture,
                            EffectsSDKEffectConstants.TextureFormat.Texture_Oes, EffectsSDKEffectConstants.TextureFormat.Texure2D,
                            width, height, new ImageUtil.Transition().rotate(rotation));
                    // Bitmap bmp = mImageUtil.transferTextureToBitmap(curTexture, EffectsSDKEffectConstants.TextureFormat.Texure2D, width, height);
                    mEffectManager.setRenderCacheTexture(BgKey, curTexture, width, height);
                }
            }));
        });
    }

    protected void checkAndSetRenderCache() {
        //   {zh} Android 中选择图片会导致 SDK 销毁 & 重建       {en} Selecting a picture in Android causes the SDK to be destroyed & rebuilt  
        //   {zh} 直接在 onActivityResult 中设置图片不可用，       {en} Set the picture unavailable directly in onActivityResult,  
        //   {zh} 需要等待 SDK 重新初始化完成后再设置       {en} You need to wait for the SDK to reinitialize before setting  
        if (!TextUtils.isEmpty(mBgPath)) {
            mEffectManager.setSticker(isChromaMatting());
            if (FileUtils.isImageFile(mBgPath)) {
                setRenderCacheFromPicture();
            } else if (FileUtils.isVideoFile(mBgPath)) {
                setRenderCacheFromVideo();
            } else {
                LogUtils.e("file format not support: " + mBgPath);
            }
        } else {
            mEffectManager.setSticker(isChromaMatting());
            mEffectManager.setRenderCacheTexture(BgKey, mResourceProvider.getMaterialPath(isChromaTexturePath()));
        }
    }

    @Override
    protected void resetDefault() {

    }

    private String isChromaMatting() {
        return mStickerConfig.getType().equals("feature_matting_sticker") ? MaterialPath : chromaMaterialPath;
    }

    private String isChromaTexturePath() {
        return mStickerConfig.getType().equals("feature_matting_sticker") ? BgValueDefault : BgValueChromaDefault;
    }

    private CaptureResult decodeByteBuffer(String path){
        Bitmap bitmap = BitmapUtils.decodeBitmapFromFile(path,mMaxTextureSize,mMaxTextureSize);
        if (bitmap == null )return null;
        return new CaptureResult(BitmapUtils.bitmap2ByteBuffer(bitmap), bitmap.getWidth(),bitmap.getHeight());

    }

    @Override
    public void onEffectInitialized() {
        super.onEffectInitialized();

        PlatformUtils.fetchCategoryMaterial(mStickerConfig.getType(), new PlatformUtils.CategoryMaterialFetchListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void onSuccess(CategoryData categoryData) {
                checkAndSetRenderCache();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showBoardFragment();
                    }
                });
            }

            @Override
            public void onMaterialFetchSuccess(@NonNull Material material, @NonNull String path) {

            }

            @Override
            public void onProgress(int i) {

            }

            @Override
            public void onFailed() {

            }
        });
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
    public boolean showBoardFragment() {
        if (null == mFragment){
            mFragment = generateStickerFragment();
        }

        showBoardFragment(mFragment, mBoardFragmentTargetId, EFFECT_TAG, true);
        return true;
    }
}
