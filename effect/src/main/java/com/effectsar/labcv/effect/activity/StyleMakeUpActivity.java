package com.effectsar.labcv.effect.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.effectsar.labcv.common.imgsrc.bitmap.BitmapSourceImpl;
import com.effectsar.labcv.common.imgsrc.camera.CameraSourceImpl;
import com.effectsar.labcv.common.model.EffectType;
import com.effectsar.labcv.common.utils.LocaleUtils;
import com.effectsar.labcv.common.utils.PlatformUtils;
import com.effectsar.labcv.common.utils.ToastUtils;
import com.effectsar.labcv.common.view.bubble.BubbleWindowManager;
import com.effectsar.labcv.core.Config;
import com.effectsar.labcv.core.effect.EffectResourceHelper;
import com.effectsar.labcv.core.util.LogUtils;
import com.effectsar.labcv.effect.config.EffectConfig;
import com.effectsar.labcv.effect.R;
import com.effectsar.labcv.effect.fragment.StyleMakeUpFragment;
import com.effectsar.labcv.effect.fragment.TabStyleMakeUpFragment;
import com.effectsar.labcv.effect.manager.EffectDataManager;
import com.effectsar.labcv.effect.manager.LocalParamDataManager;
import com.effectsar.labcv.effect.model.ComposerNode;
import com.effectsar.labcv.effect.model.EffectButtonItem;
import com.effectsar.labcv.resource.MaterialResource;
import com.effectsar.platform.struct.CategoryData;
import com.google.gson.Gson;
import com.effectsar.platform.EffectsARPlatform;
import com.effectsar.platform.api.MaterialDownloadListener;
import com.effectsar.platform.struct.Material;
import com.effectsar.platform.struct.PlatformError;

import static com.effectsar.labcv.common.model.EffectType.LITE_ASIA;
import static com.effectsar.labcv.effect.manager.EffectDataManager.TYPE_CLOSE;
import static com.effectsar.labcv.effect.manager.EffectDataManager.TYPE_STYLE_MAKEUP;
import static com.effectsar.labcv.effect.manager.EffectDataManager.TYPE_STYLE_MAKEUP_2D;
import static com.effectsar.labcv.effect.manager.EffectDataManager.TYPE_STYLE_MAKEUP_3D;
import static com.effectsar.labcv.effect.manager.EffectDataManager.getDefaultIntensity;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;


/** {zh} 
 * Created  on 2021/5/24 7:58 下午
 */
/** {en} 
 * Created on 2021/5/24 7:58 pm
 */

public class StyleMakeUpActivity extends BaseEffectActivity implements TabStyleMakeUpFragment.IMakeUpCallback {
    private TabStyleMakeUpFragment mFragment;
    public static final String EFFECT_TAG = "style_board_tag";


    private EffectConfig mEffectConfig;

    private volatile EffectButtonItem mCurrentItem;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mEffectConfig = parseEffectConfig(getIntent());
        // init root path for style makeup items
        showBoardFragment();
        LogUtils.e(this + "StyleMakeUpActivity ======== onCreate ");
        PlatformUtils.fetchCategoryDataWithCache("feature_style_makeup", categoryData -> {
            runOnUiThread(() -> {
                EffectButtonItem styleMakeup2DItem = mEffectDataManager.getSubItem(TYPE_STYLE_MAKEUP_2D);
                EffectButtonItem styleMakeup3DItem = mEffectDataManager.getSubItem(TYPE_STYLE_MAKEUP_3D);
                mFragment.setData(new EffectButtonItem[]{
                        styleMakeup2DItem,
//                            styleMakeup3DItem
                }, categoryData);
            });
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        showBoardFragment();

        onFragmentInitialized();
    }

    @Override
    public void onPause() {
        super.onPause();
        synchronized (this) {
            if (mCurrentItem != null) {
                mCurrentItem.setSelected(false);
                mCurrentItem.setSelectedRelation(false);
                mCurrentItem = null;
            }
        }
    }

    private EffectConfig parseEffectConfig(Intent intent) {
        String sAlgorithmConfig = intent.getStringExtra(EffectConfig.EffectConfigKey);
        if (sAlgorithmConfig == null) {
            return null;
        }

        return new Gson().fromJson(sAlgorithmConfig, EffectConfig.class);
    }

    private TabStyleMakeUpFragment generateStickerFragment() {
        if (mFragment != null) {
            return mFragment;
        }
        mFragment = new TabStyleMakeUpFragment(mContext).setCallback(this);

        // Check if style_makeup/local/ contains style makeup resources.
        // If so, load style_makeup/local/ for local resources test,
        // or enter the normal initializing process.

//        EffectButtonItem localTestItems = loadTestItems();
//        if (localTestItems != null) {
//            mFragment.setData(new EffectButtonItem[]{localTestItems}, this);
//        } else {
//            // enter the normal initializing process.
//            EffectButtonItem styleMakeup2DItem = mEffectDataManager.getSubItem(TYPE_STYLE_MAKEUP_2D);
//            EffectButtonItem styleMakeup3DItem = mEffectDataManager.getSubItem(EffectDataManager.TYPE_STYLE_MAKEUP_3D);
//            mFragment.setData(new EffectButtonItem[]{
//                    styleMakeup2DItem,
//                    styleMakeup3DItem
//            }, this);
//        }

        return mFragment;

    }

    public void onFragmentInitialized(){
        mFragment.updateLocalParam(getEffectType(), isFirstLaunch());
        // get local stored selected item
        Set<EffectButtonItem> localStoredSet = mEffectDataManager.getLocalStoredItems(0);
        if (!localStoredSet.isEmpty()) {
            for (EffectButtonItem item : localStoredSet) {
                if (item.getNode() != null &&
                        item.getNode().getPath()!= null && item.getNode().getPath().startsWith("style_makeup")
                ) {
                    LocalParamDataManager.load(item,mEffectDataManager.getEffectType());
                    if (item.isSelected() &&
                            (item.getId() == TYPE_STYLE_MAKEUP_2D || item.getId() == TYPE_STYLE_MAKEUP_3D)
                    ) {
                        mCurrentItem = item;
                    }
                }
            }
        }

        // Init toast of the selected item to index which resource is select last time.
        if (mCurrentItem != null && mBubbleTipManager != null) {
            if (mCurrentItem.getId() != TYPE_CLOSE) {
                mBubbleTipManager.show(mCurrentItem.getTitleId(),0);
            }
            if (mCurrentItem.getDesc()>0)
            {
                ToastUtils.show(getResources().getString(mCurrentItem.getDesc()));
            }
        }


        // select the chosen item
        mFragment.setSelectItem(mCurrentItem, false);
//        didSelectItem(mCurrentItem);

        // refresh focus of tabs & recyclerview
        mImgBack.post(()->{mFragment.refreshItemFocus();});
        /*if (mCurrentItem != null) {
            mImgBack.post(()->{mFragment.refreshItemFocus();});
        }*/
    }

    public EffectDataManager getEffectDataManager(){
        return mEffectDataManager;
    }

    public boolean isEnableDefaultBeauty(){
        return mBubbleConfig.isEnableBeauty();
    }

    @Override
    public void updateComposerNodeIntensity(String node, String key, float value) {
        if (mSurfaceView != null) {
            mSurfaceView.queueEvent(() -> {
                mEffectManager.updateComposerNodeIntensity(node, key, value);
            });

        }

    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        int id = view.getId();
        if (id == R.id.img_open) {
            showBoardFragment();
        } else if (id == R.id.img_default_activity) {
            resetDefault();
        } else if (view.getId() == R.id.img_setting) {
            mBubbleWindowManager.hideResolutionOption(view, 480);

            ArrayList<BubbleWindowManager.ITEM_TYPE> item_types = new ArrayList<>();

            if (mImageSourceProvider instanceof CameraSourceImpl) {
                boolean contains = false;
                List<int[]> supportedSizes = ((CameraSourceImpl)mImageSourceProvider).getSupportedPreviewSizes();
                if (supportedSizes != null && supportedSizes.size() > 0) {
                    for (int[] size : supportedSizes) {
                        if (size!=null && size.length == 2 && size[0] == 1920 && size[1] == 1080) {
                            contains = true;
                        }
                    }
                }
                if (contains) {
                    item_types.add(BubbleWindowManager.ITEM_TYPE.RESOLUTION);
                }
            }

            item_types.add(BubbleWindowManager.ITEM_TYPE.BEAUTY);
            item_types.add(BubbleWindowManager.ITEM_TYPE.PERFORMANCE);
            if (mImageSourceProvider instanceof BitmapSourceImpl) {
                item_types.add(BubbleWindowManager.ITEM_TYPE.PICTURE_MODE);
            }
            mBubbleWindowManager.show(view, mBubbleCallback, item_types.toArray(new BubbleWindowManager.ITEM_TYPE[item_types.size()]));
        }
    }

    @Override
    protected void resetDefault() {
        //   {zh} 移除风格妆节点       {en} Remove style makeup node  
        if (mSurfaceView != null) {
            //   {zh} 恢复默认美颜，如果有       {en} Restore default beauty, if any
            super.resetDefault();
            mSurfaceView.queueEvent(() -> {
                synchronized (this) {
                    if (mCurrentItem != null && mCurrentItem.getId() != TYPE_CLOSE) {
                        if (mCurrentItem.getNode() != null) {
                            String nodePath = mCurrentItem.getNode().getPath();
                            mEffectManager.removeComposeNodes(new String[]{nodePath});
                        }
                    }
                }
            });
        }

        runOnUiThread(()->{
            //   {zh} 重置选择框及进度条       {en} Reset the selection box and progress bar  
            if (null != mFragment) {
                mFragment.resetDefault();

            }
        });
        synchronized (this) {
            //  {zh} 重置强度值  {en} Reset strength value
            mEffectDataManager.resetItem(mCurrentItem);
            mCurrentItem = null;
        }
    }

    @Override
    public void onClickEvent(View view) {
        if (view.getId() == R.id.iv_close_board) {
            hideBoardFragment(mFragment);

        } else if (view.getId() == R.id.iv_record_board) {
            takePic();
        } else if (view.getId() == R.id.img_default) {
            resetDefault();
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
    public boolean showBoardFragment() {
        if (null == mFragment) {
            mFragment = generateStickerFragment();
        }
        showBoardFragment(mFragment, mBoardFragmentTargetId, EFFECT_TAG, true);
        return true;
    }


    @Override
    public void onMakeUpItemSelect(EffectButtonItem item, int tabIndex, int contentIndex) {
        didSelectItem(item);
    }

    public void setupMakeup(EffectButtonItem item){
        if (item == null) {
            return;
        }
        if (mSurfaceView != null) {
            mSurfaceView.queueEvent(() -> {
                if (mCurrentItem!= null && mCurrentItem.getId() != TYPE_CLOSE) {
                    if (mCurrentItem.getNode() != null) {
                        mEffectManager.removeComposeNodes(new String[]{mCurrentItem.getNode().getPath()});
                        if (item.getId() == TYPE_CLOSE){
                            //  {zh} 重置强度值  {en} Reset strength value
                            mEffectDataManager.resetItem(mCurrentItem);
                        }

                    }
                }
                mCurrentItem = item;

                if (mCurrentItem.getId() == TYPE_CLOSE) {
                    return;
                }
                if (mCurrentItem.getNode() == null) {
                    return;
                }
                mEffectManager.appendComposeNodes(new String[]{mCurrentItem.getNode().getPath()});
                if (item.getNode() != null) {
                    for (int i = 0; i < mCurrentItem.getNode().getKeyArray().length; i++) {
                        mEffectManager.updateComposerNodeIntensity(mCurrentItem.getNode().getPath(),
                                item.getNode().getKeyArray()[i], mCurrentItem.getIntensityArray()[i]);
                    }
                }
            });
        }
    }


    private void didSelectItem(EffectButtonItem item) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // show tips & title
                if (item!= null && mBubbleTipManager != null) {
                    mBubbleTipManager.show(item.getTitleId(),0);
                    if (!TextUtils.isEmpty(item.getTips())) {
                        ToastUtils.show(item.getTips());
                    }
                }
                // change fragment ui
                mFragment.setSelectItem(item, true);
            }
        });
        setupMakeup(item);
    }

    private void refreshResourceUI(Material material) {
        mFragment.refreshItem(material);
    }

    public EffectType getEffectType() {
        return mEffectConfig.getEffectType();
    }

    public EffectButtonItem loadTestItems(){
        String localRootPath = new EffectResourceHelper(this).getComposePath() + File.separator + "style_makeup"+ File.separator +"local";

        ArrayList<EffectButtonItem> localItems = new ArrayList<EffectButtonItem>(){
            {
                File[] files = new File(localRootPath).listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File pathname) {
                        if (pathname.isDirectory() && !pathname.isHidden()) {
                            return true;
                        }
                        return false;
                    }
                });

                if (files != null) {
                    for (File file : files) {
                        // Style makeup resources use "Internal_Filter", "Internal_Makeup" as keys in test
                        // Remember to check the difference in the development of each version.
                        add(new EffectButtonItem(TYPE_STYLE_MAKEUP_2D, R.drawable.ic_qinglv_0, file.getName(), new ComposerNode("style_makeup"+File.separator+"local"+File.separator+file.getName(), new String[]{"Internal_Filter", "Internal_Makeup"},getDefaultIntensity(TYPE_STYLE_MAKEUP, LITE_ASIA))));
                    }
                }
            }
        };

        if (localItems.size() <= 0) {
            return null;
        } else {
            localItems.add(0,new EffectButtonItem(TYPE_CLOSE, R.drawable.clear, R.string.close));
            return new EffectButtonItem(
                    TYPE_STYLE_MAKEUP_2D,
                    R.drawable.clear,
                    R.string.tab_style_makeup,
                    localItems.toArray(new EffectButtonItem[localItems.size()]),
                    false
            );
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

}
