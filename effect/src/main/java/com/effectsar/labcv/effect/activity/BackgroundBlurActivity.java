package com.effectsar.labcv.effect.activity;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.View;

import com.effectsar.labcv.common.adapter.ItemViewRVAdapter;
import com.effectsar.labcv.common.fragment.ItemViewPageFragment;
import com.effectsar.labcv.common.utils.DensityUtils;
import com.effectsar.labcv.common.utils.PlatformUtils;
import com.effectsar.labcv.common.view.bubble.BubbleWindowManager;
import com.effectsar.labcv.core.Config;
import com.effectsar.labcv.core.effect.EffectResourceHelper;
import com.effectsar.labcv.effect.R;
import com.effectsar.labcv.effect.adapter.MattingStickerRVAdapter;
import com.effectsar.labcv.effect.fragment.MattingStickerFragment;
import com.effectsar.labcv.effect.manager.EffectDataManager;
import com.effectsar.labcv.effect.model.EffectButtonItem;

import com.effectsar.labcv.effect.view.ProgressBar;
import com.effectsar.labcv.resource.MaterialResource;
import com.effectsar.platform.EffectsARPlatform;
import com.effectsar.platform.api.MaterialDownloadListener;
import com.effectsar.platform.struct.CategoryData;
import com.effectsar.platform.struct.CategoryTabItem;
import com.effectsar.platform.struct.Material;
import com.effectsar.platform.struct.PlatformError;


import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

/** {zh}
 * Created  on 2021/5/24 4:01 下午
 */

/** {en}
 * Created on 2021/5/24 4:01 pm
 */

public class BackgroundBlurActivity extends BaseEffectActivity implements MattingStickerFragment.MattingStickerCallback, ItemViewRVAdapter.OnItemClickListener<EffectButtonItem>  {
    private MattingStickerFragment mFragment = null;
    public static final String EFFECT_TAG = "effect_board_tag";

    public static final String FEATURE_BACKGROUND_BLUR = "feature_background_blur";
    private boolean mSwitch = true;
    private String TYPE_DEFAULT_MATTING = "";
    private EffectButtonItem defaultItem;
    private EffectResourceHelper mResourceHelper;
    private volatile int materialTotal = 0;
    private volatile int materialCount = 0;
    private boolean initLoading = true;
    private String oldPath = "";

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mResourceHelper = new EffectResourceHelper(this);
        mFragment = generateStickerFragment();
    }


    private MattingStickerFragment generateStickerFragment(){
        if (mFragment != null) return mFragment;
        ArrayList<Fragment> fragments = new ArrayList<Fragment>(){
            {
                ArrayList<EffectButtonItem> items = new ArrayList<EffectButtonItem>(){
                    {
                        EffectButtonItem mManagerItem  = mEffectDataManager.getItem(EffectDataManager.TYPE_BACK_GROUND);
                        for (int i = 0; i < mManagerItem.getChildren().length; i++) {
                            EffectButtonItem mItem =  mManagerItem.getChildren()[i];
                            if (i==0){
                                mItem.setSelected(true).setSelectedRelation(true);
                                TYPE_DEFAULT_MATTING = mItem.getNode().getPath();
                            }
                            String materialPath = mItem.getNode().getPath();
                            if (!TextUtils.isEmpty(materialPath) && materialPath.equals("background_blur_in")) {
                                mItem.getNode().setPath(mResourceHelper.getStickerPath(materialPath));
                            }
                            add(mItem);
                        }
                    }
                };
                MattingStickerRVAdapter adapter = new MattingStickerRVAdapter(items,BackgroundBlurActivity.this);
                ItemViewPageFragment<MattingStickerRVAdapter> fragment = new ItemViewPageFragment<>();
                fragment.setAdapter(adapter);
                add(fragment);
            }
        };
        ArrayList<String> titles = new ArrayList<String>(){
            {
                add(getString(R.string.tab_background_blur));
            }
        };

        mFragment = new MattingStickerFragment(fragments,titles);

        mFragment.setMattingStickerCallback(this);

        return mFragment;

    }

    @Override
    public void onClickEvent(View view) {
        if (view.getId() == R.id.iv_close_board) {
            hideBoardFragment(mFragment);
        } else if (view.getId() == R.id.img_default) {
            mSwitch = true;
            mFragment.useProgressBar(false);
            ItemViewPageFragment fragment = (ItemViewPageFragment)mFragment.getCurrentFragment();
            for (int i = 0; i < mEffectDataManager.getItem(EffectDataManager.TYPE_BACK_GROUND).getChildren().length; i++) {
                EffectButtonItem item = mEffectDataManager.getItem(EffectDataManager.TYPE_BACK_GROUND).getChildren()[i];
                if (i==0) {
                    item.setSelected(true).setSelectedRelation(true);
                }
                else {
                    item.setSelected(false).setSelectedRelation(false);
                    if (item.isEnableNegative() == true) {
                        mEffectManager.resourcePath = "sticker";
                    }
                    mEffectManager.removeComposeNodes(new String[]{item.getNode().getPath()});
                }
            }
            fragment.refreshUI();
            if (null == mSurfaceView){
                return;
            }
            mSurfaceView.queueEvent(()->{
                TYPE_DEFAULT_MATTING =mEffectDataManager.getItem(EffectDataManager.TYPE_BACK_GROUND).getChildren()[0].getNode().getPath();
                mEffectManager.setSticker(mSwitch ? mEffectDataManager.getItem(EffectDataManager.TYPE_BACK_GROUND).getChildren()[0].getNode().getPath() : "");
            });
        } else if (view.getId() == R.id.iv_record_board) {
            takePic();
        }
    }

    @Override
    public void onProgressChanged(ProgressBar progressBar, float progress, boolean isFormUser) {
        if (defaultItem != null && defaultItem.getNode() != null) {
            for (int i = 0; i < defaultItem.getNode().getKeyArray().length; i++) {
                mEffectManager.updateComposerNodeIntensity(defaultItem.getNode().getPath(),
                        defaultItem.getNode().getKeyArray()[i], progress);
            }
        }
    }

    @Override
    public void onProgressEnd(ProgressBar progressBar, float progress, boolean isFormUser) {

    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        int id = view.getId();
        if (id == R.id.img_open) {
            showBoardFragment();
        } else if (id == R.id.img_default_activity) {
            mSwitch = true;
            mFragment.useProgressBar(false);
            ItemViewPageFragment fragment = (ItemViewPageFragment)mFragment.getCurrentFragment();
            for (int i = 0; i < mEffectDataManager.getItem(EffectDataManager.TYPE_BACK_GROUND).getChildren().length; i++) {
                EffectButtonItem item = mEffectDataManager.getItem(EffectDataManager.TYPE_BACK_GROUND).getChildren()[i];
                if (i==0) {
                    item.setSelected(true).setSelectedRelation(true);
                }
                else {
                    item.setSelected(false).setSelectedRelation(false);
                    if (item.isEnableNegative() == true) {
                        mEffectManager.resourcePath = "sticker";
                        mEffectManager.removeComposeNodes(new String[]{item.getNode().getPath()});
                    }
                }
            }

            if (fragment != null) {
                fragment.refreshUI();
            }

            if (null == mSurfaceView){
                return;
            }
            mSurfaceView.queueEvent(()->{
                TYPE_DEFAULT_MATTING =mEffectDataManager.getItem(EffectDataManager.TYPE_BACK_GROUND).getChildren()[0].getNode().getPath();
                mEffectManager.setSticker(mSwitch ? mEffectDataManager.getItem(EffectDataManager.TYPE_BACK_GROUND).getChildren()[0].getNode().getPath() : "");
            });
        }  else if (view.getId() == R.id.img_setting) {
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


    @Override
    public void onEffectInitialized() {
        super.onEffectInitialized();
        if (initLoading){
            EffectButtonItem item = mEffectDataManager.getItem(EffectDataManager.TYPE_BACK_GROUND).getChildren()[0];
            mEffectManager.setSticker(item.getNode().getPath());
            initLoading = false;
        }
        PlatformUtils.fetchCategoryMaterial(FEATURE_BACKGROUND_BLUR, new PlatformUtils.CategoryMaterialFetchListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void onSuccess(CategoryData categoryData) {
                if (mSurfaceView == null) return;
                mSurfaceView.queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        EffectButtonItem item = mEffectDataManager.getItem(EffectDataManager.TYPE_BACK_GROUND).getChildren()[0];
                        mEffectManager.setSticker(item.getNode().getPath());
                        item.setSelected(true);
                    }
                });

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

    @Override
    public void onItemClick(EffectButtonItem item, int position) {
        mBubbleTipManager.show(item.getTitleId(),0);
        for (int i = 0; i < mEffectDataManager.getItem(EffectDataManager.TYPE_BACK_GROUND).getChildren().length; i++) {
            EffectButtonItem item1 = mEffectDataManager.getItem(EffectDataManager.TYPE_BACK_GROUND).getChildren()[i];
            if (item.getNode().getPath().equals(item1.getNode().getPath())) {
                if (mSwitch == true){
                    if (!TYPE_DEFAULT_MATTING.equals(item.getNode().getPath())){
                        mSwitch = false;
                        if (item1.isEnableNegative() == true) {
                            mEffectManager.resourcePath = "sticker";
                            mEffectManager.removeComposeNodes(new String[]{item1.getNode().getPath()});
                        }
                    }
                    else {
                        item1.setSelected(false).setSelectedRelation(false);
                        mEffectManager.removeComposeNodes(new String[]{item1.getNode().getPath()});
                    }

                }
                else {
                    item1.setSelected(true).setSelectedRelation(true);
                    mSwitch = false;
                }

            }
            else {
                if (item1.isEnableNegative() == true) {
                    mEffectManager.resourcePath = "sticker";
                    mEffectManager.removeComposeNodes(new String[]{item1.getNode().getPath()});
                }
                item1.setSelected(false).setSelectedRelation(false);;
                mEffectManager.removeComposeNodes(new String[]{item1.getNode().getPath()});
            }
        }
        TYPE_DEFAULT_MATTING = item.getNode().getPath();
        defaultItem = item;
        if (item.isEnableNegative() == true){
            oldPath = item.getNode().getPath();
            mSwitch = !mSwitch;
            mFragment.useProgressBar(mSwitch);
            item.setSelected(mSwitch).setSelectedRelation(mSwitch);
            ((ItemViewPageFragment)mFragment.getCurrentFragment()).refreshUI();
            if (null == mSurfaceView){
                return;
            }
            mSurfaceView.queueEvent(()->{
                mEffectManager.setSticker("");
                mEffectManager.resourcePath = "sticker";
                if (mSwitch == false) {
                    mEffectManager.removeComposeNodes(new String[]{item.getNode().getPath()});
                }
                else {
                    mEffectManager.appendComposeNodes(new String[]{item.getNode().getPath()});
                    if (item.getNode() != null) {
                        for (int i = 0; i < item.getNode().getKeyArray().length; i++) {
                            mEffectManager.updateComposerNodeIntensity(item.getNode().getPath(),
                                    item.getNode().getKeyArray()[i], 0.8f);
                        }
                    }
                }
            });
        }
        else {
            mSwitch = !mSwitch;
            mFragment.useProgressBar(false);
            item.setSelected(mSwitch).setSelectedRelation(mSwitch);
            ((ItemViewPageFragment)mFragment.getCurrentFragment()).refreshUI();
            if (null == mSurfaceView){
                return;
            }
            mSurfaceView.queueEvent(()->{
                mEffectManager.resourcePath = "";
                mEffectManager.setSticker(mSwitch ? item.getNode().getPath() : "");
               if (oldPath != ""){
                   mEffectManager.removeComposeNodes(new String[]{oldPath});
               }
               if (!mSwitch) {
                   mEffectManager.removeComposeNodes(new String[]{item.getNode().getPath()});
               }
            });
        }
    }
}
