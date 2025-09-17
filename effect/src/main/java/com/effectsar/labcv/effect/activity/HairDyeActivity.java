package com.effectsar.labcv.effect.activity;

import static com.effectsar.labcv.effect.manager.EffectDataManager.DESC_HAIR_DYE_HIGHLIGHT_PART_A;
import static com.effectsar.labcv.effect.manager.EffectDataManager.DESC_HAIR_DYE_HIGHLIGHT_PART_B;
import static com.effectsar.labcv.effect.manager.EffectDataManager.DESC_HAIR_DYE_HIGHLIGHT_PART_C;
import static com.effectsar.labcv.effect.manager.EffectDataManager.DESC_HAIR_DYE_HIGHLIGHT_PART_D;
import static com.effectsar.labcv.effect.manager.EffectDataManager.TYPE_BEAUTY_BODY;
import static com.effectsar.labcv.effect.manager.EffectDataManager.TYPE_BEAUTY_FACE;
import static com.effectsar.labcv.effect.manager.EffectDataManager.TYPE_BEAUTY_RESHAPE;
import static com.effectsar.labcv.effect.manager.EffectDataManager.TYPE_BLACK_TECHNOLOGY;
import static com.effectsar.labcv.effect.manager.EffectDataManager.TYPE_FILTER;
import static com.effectsar.labcv.effect.manager.EffectDataManager.TYPE_FILTER_AMG;
import static com.effectsar.labcv.effect.manager.EffectDataManager.TYPE_HAIR_DYE_FULL;
import static com.effectsar.labcv.effect.manager.EffectDataManager.TYPE_HAIR_DYE_HIGHLIGHT;
import static com.effectsar.labcv.effect.manager.EffectDataManager.TYPE_LIPSTICK_GLOSSY;
import static com.effectsar.labcv.effect.manager.EffectDataManager.TYPE_LIPSTICK_MATTE;
import static com.effectsar.labcv.effect.manager.EffectDataManager.TYPE_MAKEUP;
import static com.effectsar.labcv.effect.manager.EffectDataManager.TYPE_PALETTE;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.effectsar.labcv.common.imgsrc.camera.CameraSourceImpl;
import com.effectsar.labcv.common.model.EffectType;
import com.effectsar.labcv.common.utils.PlatformUtils;
import com.effectsar.labcv.common.view.bubble.BubbleWindowManager;
import com.effectsar.labcv.core.Config;
import com.effectsar.labcv.core.effect.EffectResourceHelper;
import com.effectsar.labcv.core.util.LogUtils;
import com.effectsar.labcv.effect.R;
import com.effectsar.labcv.effect.config.EffectConfig;
import com.effectsar.labcv.effect.fragment.EffectFragment;
import com.effectsar.labcv.effect.model.ColorItem;
import com.effectsar.labcv.effect.model.EffectButtonItem;
import com.effectsar.platform.struct.CategoryData;
import com.google.gson.Gson;
import com.effectsar.platform.EffectsARPlatform;
import com.effectsar.platform.api.MaterialDownloadListener;
import com.effectsar.platform.struct.CategoryTabItem;
import com.effectsar.platform.struct.Material;
import com.effectsar.platform.struct.PlatformError;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;


/** {zh} 
 * 美颜美型
 */

/** {en}
 * Beauty beauty
 */

public class HairDyeActivity extends BaseEffectActivity implements EffectFragment.IEffectCallback{
    private EffectFragment mEffectFragment = null;
    private String mFeature = "";
    public static final String EFFECT_TAG = "effect_board_tag";
    public static final String FEATURE_AR_LIPSTICK = "feature_ar_lipstick";
//    public static final String FEATURE_AR_HAIR_DYE = "feature_ar_hair_dye";
    private volatile int materialTotal = 0;
    private volatile int materialCount = 0;
    private EffectResourceHelper mResourceHelper;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String feature = "";
        String sEffectConfig = getIntent().getStringExtra(EffectConfig.EffectConfigKey);
        if (sEffectConfig != null) {
            EffectConfig effectConfig = new Gson().fromJson(sEffectConfig, EffectConfig.class);
            if (effectConfig != null) {
                feature = effectConfig.getFeature();
            }
        }
        mFeature = feature;
        mResourceHelper = new EffectResourceHelper(this);

//        PlatformUtils.fetchCategoryMaterial(FEATURE_AR_HAIR_DYE, new PlatformUtils.CategoryMaterialFetchListener() {
//            @Override
//            public void onStart() {
//
//            }
//
//            @Override
//            public void onSuccess(CategoryData categoryData) {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        showBoardFragment();
//                        if (mEffectFragment != null) {
//                            mEffectFragment.updateLocalParam(isFirstLaunch());
//                        }
//                    }
//                });
//            }
//
//            @Override
//            public void onMaterialFetchSuccess(@NonNull Material material, @NonNull String path) {
//
//            }
//
//            @Override
//            public void onProgress(int i) {
//
//            }
//
//            @Override
//            public void onFailed() {
//
//            }
//        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private EffectFragment generateEffectFragment() {
        if (mEffectFragment != null) return mEffectFragment;

        EffectFragment effectFragment = new EffectFragment();
        mEffectManager.setSyncLoadResource(true);
        effectFragment.setColorListPosition(EffectFragment.BOARD_FRAGMENT_HEAD_ABOVE).useProgressBar(false);
        effectFragment.setData(mContext,mEffectDataManager, mFilterDatamanager, getHairDyeTabItems(),mEffectConfig.getEffectType(), isFirstLaunch());
        effectFragment.setCallback(this);
        return effectFragment;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public List<EffectFragment.TabItem> getTabItems() {
        return new ArrayList<EffectFragment.TabItem>(){
            {
                add(new EffectFragment.TabItem(TYPE_BEAUTY_FACE, R.string.tab_face_beautification));
                add(new EffectFragment.TabItem(TYPE_BEAUTY_RESHAPE, R.string.tab_face_beauty_reshape));
                add(new EffectFragment.TabItem(TYPE_BEAUTY_BODY, R.string.tab_face_beauty_body));
                add(new EffectFragment.TabItem(TYPE_MAKEUP, R.string.tab_face_makeup));
                add(new EffectFragment.TabItem(TYPE_FILTER_AMG, R.string.tab_filter_amg));
                add(new EffectFragment.TabItem(TYPE_FILTER, R.string.tab_filter));
                if (!getVersionName().contains("lite")) {
                    add(new EffectFragment.TabItem(TYPE_PALETTE, R.string.tab_palette));
                }
                add(new EffectFragment.TabItem(TYPE_BLACK_TECHNOLOGY, R.string.tab_black_technology));
            }
        };
    }

    private String getVersionName() {
        try {
            return "V" + this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "";
        }
    }

    public List<EffectFragment.TabItem> getLipstickTabItems() {
        return Arrays.asList(
                new EffectFragment.TabItem(TYPE_LIPSTICK_GLOSSY, R.string.tab_lipstick_glossy),
                new EffectFragment.TabItem(TYPE_LIPSTICK_MATTE, R.string.tab_lipstick_matte)
        );
    }

    public List<EffectFragment.TabItem> getHairDyeTabItems() {
        return Arrays.asList(
                new EffectFragment.TabItem(TYPE_HAIR_DYE_FULL, R.string.tab_hair_dye_full),
                new EffectFragment.TabItem(TYPE_HAIR_DYE_HIGHLIGHT, R.string.tab_hair_dye_highlight)
        );
    }


    @Override
    public void updateComposeNodes(Set<EffectButtonItem> effectButtonItems) {
        if (mSurfaceView != null) {
            mSurfaceView.queueEvent(new Runnable() {
                @Override
                public void run() {
                    String[][] nodesAndTags = mEffectDataManager.generateComposerNodesAndTags(effectButtonItems);
                    mEffectManager.setComposeNodes(nodesAndTags[0]);
                    StringBuilder sb = new StringBuilder();
                    for (String item : nodesAndTags[0]) {
                        sb.append(item);
                        sb.append(" ");
                    }
                    LogUtils.d("nodes =" + sb.toString());


                }
            });
        }

    }



    @Override
    public void updateComposerNodeIntensity(EffectButtonItem item) {

        if (mSurfaceView != null) {
            mSurfaceView.queueEvent(new Runnable() {
                @Override
                public void run() {
                    if (item.getNode() == null)return;
                    for (int i = 0; i < item.getNode().getKeyArray().length; i++) {
                            mEffectManager.updateComposerNodeIntensity(item.getNode().getPath(),
                                    item.getNode().getKeyArray()[i], item.getIntensityArray()[i]);

                        LogUtils.d("updateComposerNodeIntensity +"+item.getNode().getPath() + "  "+item.getNode().getKeyArray()[i]+" "+item.getIntensityArray()[i]);

                    }
                }
            });
        }

    }

    @Override
    public void updateComposerNodeIntensity(Set<EffectButtonItem> effectButtonItems) {

    }

    @Override
    public void onFilterSelected(String filter) {
        if (null != mSurfaceView) {
            mSurfaceView.queueEvent(new Runnable() {
                @Override
                public void run() {
                    mEffectManager.setFilter(filter != null ? filter : "");

                }
            });
        }

    }

    @Override
    public void onFilterValueChanged(float cur) {
        if (null != mSurfaceView) {
            mSurfaceView.queueEvent(new Runnable() {
                @Override
                public void run() {
                    mEffectManager.updateFilterIntensity(cur);
                }
            });
        }

    }

    @Override
    public void showTip(String title, String desc) {
        if (mBubbleTipManager != null){
            mBubbleTipManager.show(title, desc);
        }

    }

    @Override
    public void setImgCompareHeightBy(float y, int duration) {
        setImgCompareViewHeightBy(y, duration);
    }

    @Override
    public void onHairDyeSelected(int part, ColorItem colorItem) {

        // TODO:
        String str_part = "";
        switch (part) {
            case DESC_HAIR_DYE_HIGHLIGHT_PART_A:
                str_part = "msg: part A, (";
                break;
            case DESC_HAIR_DYE_HIGHLIGHT_PART_B:
                str_part = "msg: part B, (";
                break;
            case DESC_HAIR_DYE_HIGHLIGHT_PART_C:
                str_part = "msg: part C, (";
                break;
            case DESC_HAIR_DYE_HIGHLIGHT_PART_D:
                str_part = "msg: part D, (";
                break;
            default:
                str_part = "msg: part full, (";
        }

        String str_color = "";
        str_color =  colorItem.getR() +", "+ colorItem.getG() +", "+ colorItem.getB() + ")";
        LogUtils.e(str_part + str_color);

        // color set message should be sent in GL thread, so that color-setting message can be sent after the resources are loaded
        mSurfaceView.queueEvent(new Runnable() {
            @Override
            public void run() {
                mEffectManager.setHairColorByPart(part,colorItem.getR(),colorItem.getG(),colorItem.getB(),colorItem.getA());
            }
        });
    }

    @Override
    public void updateComposerNodeIntensity(String node, String key, float value) {
        if (mSurfaceView != null) {
            mSurfaceView.queueEvent(new Runnable() {
                @Override
                public void run() {
                    mEffectManager.updateComposerNodeIntensity(node, key,  value);

                }
            });
        }
    }

    @Override
    public void onClickEvent(View view) {
        if (null == view) return;
        if (view.getId() == R.id.iv_close_board) {
            hideBoardFragment(mEffectFragment);

        } else if (view.getId() == R.id.iv_record_board) {
            takePic();
        } else if (view.getId() == R.id.img_default) {
            //  set to default
            resetDefault();
            if (mEffectFragment != null) {
                mEffectFragment.resetToDefault();
            }

        }
    }

    @Override
    public void onPause() {
//        mEffectFragment.saveStatus();
        super.onPause();
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        int id = view.getId();
        if (id == R.id.img_open) {
            showBoardFragment();
        }else if (id == R.id.img_default_activity) {
            resetDefault();
            if (mEffectFragment != null) {
                mEffectFragment.resetToDefault();
            }

        }  else if (view.getId() == R.id.img_setting) {
            mBubbleWindowManager.hideResolutionOption(view,480);
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
//                if ( contains && !mFeature.equals(FEATURE_AR_LIPSTICK) && !mFeature.equals(FEATURE_AR_HAIR_DYE) ) {
                if ( contains && !mFeature.equals(FEATURE_AR_LIPSTICK)) {
                    item_types.add(BubbleWindowManager.ITEM_TYPE.RESOLUTION);
                }
            }
//            if ( (mFeature != null) && (mFeature.equals(FEATURE_AR_LIPSTICK) || mFeature.equals(FEATURE_AR_HAIR_DYE)) ) {
            if ( (mFeature != null) && (mFeature.equals(FEATURE_AR_LIPSTICK))) {
                item_types.add(BubbleWindowManager.ITEM_TYPE.BEAUTY);
            }

            item_types.add(BubbleWindowManager.ITEM_TYPE.PERFORMANCE);

            mBubbleWindowManager.show(view, mBubbleCallback, item_types.toArray(new BubbleWindowManager.ITEM_TYPE[item_types.size()]));
        }
    }



    @Override
    public void onEffectInitialized() {
        super.onEffectInitialized();

    }

    @Override
    public boolean closeBoardFragment() {
        if (mEffectFragment != null && mEffectFragment.isVisible()) {
            hideBoardFragment(mEffectFragment);
            return true;
        }
        return false;

    }

    @Override
    public boolean showBoardFragment() {
        if(null == mEffectFragment){
            mEffectFragment = generateEffectFragment();
        }
        showBoardFragment(mEffectFragment, mBoardFragmentTargetId, EFFECT_TAG, true);
        return true;
    }

    @Override
    public EffectType getEffectType() {
        return mEffectConfig.getEffectType();
    }


    @Override
    protected void setBeautyDefault() {
        if (mEffectFragment != null) {
            Set<EffectButtonItem> mSelected = mEffectFragment.getSelectNodes();
            Set<EffectButtonItem> defaults = mEffectDataManager.getDefaultItems();
            for (EffectButtonItem it : mSelected) {
                if (defaults.contains(it) ) {
                    continue;
                }
                for (int i = 0; i < it.getNode().getKeyArray().length; i++) {
                    mEffectManager.updateComposerNodeIntensity(it.getNode().getPath(),
                            it.getNode().getKeyArray()[i], it.isEnableNegative()?0.5f:0f);
                }
            }
        }
        super.setBeautyDefault();

    }

}
