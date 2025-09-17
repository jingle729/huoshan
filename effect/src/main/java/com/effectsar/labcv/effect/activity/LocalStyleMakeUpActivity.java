package com.effectsar.labcv.effect.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.effectsar.labcv.common.imgsrc.camera.CameraSourceImpl;
import com.effectsar.labcv.common.model.EffectType;
import com.effectsar.labcv.common.utils.ToastUtils;
import com.effectsar.labcv.common.view.bubble.BubbleWindowManager;
import com.effectsar.labcv.core.effect.EffectResourceHelper;
import com.effectsar.labcv.effect.config.EffectConfig;
import com.effectsar.labcv.effect.R;
import com.effectsar.labcv.effect.fragment.StyleMakeUpFragment;
import com.effectsar.labcv.effect.manager.EffectDataManager;
import com.effectsar.labcv.effect.manager.LocalParamDataManager;
import com.effectsar.labcv.effect.model.ComposerNode;
import com.effectsar.labcv.effect.model.EffectButtonItem;
import com.google.gson.Gson;

import static com.effectsar.labcv.common.model.EffectType.LITE_ASIA;
import static com.effectsar.labcv.effect.manager.EffectDataManager.TYPE_CLOSE;
import static com.effectsar.labcv.effect.manager.EffectDataManager.TYPE_LOCAL_STYLE_MAKEUP_2D;
import static com.effectsar.labcv.effect.manager.EffectDataManager.TYPE_LOCAL_STYLE_MAKEUP_3D;
import static com.effectsar.labcv.effect.manager.EffectDataManager.TYPE_STYLE_MAKEUP;
import static com.effectsar.labcv.effect.manager.EffectDataManager.TYPE_STYLE_MAKEUP_2D;
import static com.effectsar.labcv.effect.manager.EffectDataManager.getDefaultIntensity;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


/** {zh}
 * Created  on 2021/5/24 7:58 下午
 */
/** {en}
 * Created on 2021/5/24 7:58 pm
 */

public class LocalStyleMakeUpActivity extends BaseEffectActivity implements StyleMakeUpFragment.IMakeUpCallback {
    private StyleMakeUpFragment mFragment;
    public static final String EFFECT_TAG = "style_board_tag";


    private EffectConfig mEffectConfig;

    private EffectButtonItem mSelected;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mEffectConfig = parseEffectConfig(getIntent());
        showBoardFragment();
        storedItemStatus = 1;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus) {
            Set<EffectButtonItem> localStoredSet = mEffectDataManager.getLocalStoredItems(1);
            if (!localStoredSet.isEmpty()) {
                for (EffectButtonItem item : localStoredSet) {
                    if (item.getNode() != null &&
                            item.getNode().getPath() != null &&
                            item.getNode().getPath().contains("style_makeup")
                    ) {
                        LocalParamDataManager.load(item, mEffectDataManager.getEffectType());
                        if (item.shouldHighLight()) {
                            mSelected = item;
                        }
                    }
                }
            }
            // Init toast of the selected item to index which resource is select last time.
            if (mSelected != null && mBubbleTipManager != null) {
                mBubbleTipManager.show(mSelected.getTitleId(),0);
                if (mSelected.getDesc()>0)
                {
                    ToastUtils.show(getResources().getString(mSelected.getDesc()));
                }
            }

            if (mFragment != null) {
                mFragment.setSelectItem(mSelected);
                mFragment.updateLocalParam(isFirstLaunch());
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

    private StyleMakeUpFragment generateStickerFragment() {
        if (mFragment != null) return mFragment;
        mFragment = new StyleMakeUpFragment(mContext);

        // Check if style_makeup/local/ contains style makeup resources.
        // If so, load style_makeup/local/ for local resources test,
        // or enter the normal initializing process.

        EffectButtonItem localTestItems = loadTestItems();
        if (localTestItems != null) {
            mFragment.setData(new EffectButtonItem[]{localTestItems}, this);
        } else {
            // enter the normal initializing process.
            EffectButtonItem styleMakeup2DItem = mEffectDataManager.getSubItem(TYPE_LOCAL_STYLE_MAKEUP_2D);
            EffectButtonItem styleMakeup3DItem = mEffectDataManager.getSubItem(TYPE_LOCAL_STYLE_MAKEUP_3D);
            mFragment.setData(new EffectButtonItem[]{
                    styleMakeup2DItem,
                    styleMakeup3DItem
            }, this);
        }

        return mFragment;

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
            mBubbleWindowManager.show(view, mBubbleCallback, item_types.toArray(new BubbleWindowManager.ITEM_TYPE[item_types.size()]));
        }
    }

    @Override
    protected void resetDefault() {
        //   {zh} 移除风格妆节点       {en} Remove style makeup node  
        if (mSurfaceView != null) {
            mSurfaceView.queueEvent(() -> {
                //   {zh} 恢复默认美颜，如果有       {en} Restore default beauty, if any  
                super.resetDefault();
                if (mSelected != null && mSelected.getId() != TYPE_CLOSE) {
                    if (mSelected.getNode() != null) {
                        mEffectManager.removeComposeNodes(new String[]{mSelected.getNode().getPath()});
                    }
                    //  {zh} 重置强度值  {en} Reset strength value
                    mEffectDataManager.resetItem(mSelected);


                }
            });
        }
        runOnUiThread(()->{
            //   {zh} 重置选择框及进度条       {en} Reset the selection box and progress bar  
            if (null != mFragment) {
                mFragment.resetDefault();

            }
        });


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
    public void onMakeUpItemSelect(EffectButtonItem item) {
        if (item != null && mBubbleTipManager != null) {
            mBubbleTipManager.show(item.getTitleId(),0);
            if (item.getDesc()>0)
            {
                ToastUtils.show(getResources().getString(item.getDesc()));
            }
        }

        if (mSurfaceView != null) {
            mSurfaceView.queueEvent(() -> {

                if (mSelected!= null && mSelected.getId() != TYPE_CLOSE) {
                    if (mSelected.getNode() != null) {
                        mEffectManager.removeComposeNodes(new String[]{mSelected.getNode().getPath()});
                        if (item.getId() == TYPE_CLOSE){
                            //  {zh} 重置强度值  {en} Reset strength value
                            if (!LocalParamDataManager.useLocalParamStorage()) {
                                mEffectDataManager.resetItem(mSelected);
                            }
                        }

                    }


                }
                mSelected = item;

                if (mSelected.getId() == TYPE_CLOSE) {
                    return;
                }
                if (mSelected.getNode() == null) {
                    return;
                }
                mEffectManager.appendComposeNodes(new String[]{mSelected.getNode().getPath()});
                if (item.getNode() != null) {
                    for (int i = 0; i < mSelected.getNode().getKeyArray().length; i++) {
                        mEffectManager.updateComposerNodeIntensity(mSelected.getNode().getPath(),
                                item.getNode().getKeyArray()[i], mSelected.getIntensityArray()[i]);
                    }
                }
            });
        }

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

}
