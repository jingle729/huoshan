package com.effectsar.labcv.effect.fragment;

import static com.effectsar.labcv.effect.manager.EffectDataManager.TYPE_CLOSE;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.effectsar.labcv.common.fragment.TabBoardFragment;
import com.effectsar.labcv.common.model.EffectType;
import com.effectsar.labcv.common.utils.PlatformUtils;
import com.effectsar.labcv.common.utils.ToastUtils;
import com.effectsar.labcv.core.util.LogUtils;
import com.effectsar.labcv.effect.R;
import com.effectsar.labcv.effect.activity.StyleMakeUpActivity;
import com.effectsar.labcv.effect.manager.LocalParamDataManager;
import com.effectsar.labcv.effect.model.ComposerNode;
import com.effectsar.labcv.effect.model.EffectButtonItem;
import com.effectsar.labcv.effect.view.ProgressBar;
import com.effectsar.labcv.effect.view.RadioTextView;
import com.effectsar.platform.struct.CategoryData;
import com.effectsar.platform.struct.CategoryTabItem;
import com.effectsar.platform.struct.Material;
import com.effectsar.platform.struct.PlatformError;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

;

public class TabStyleMakeUpFragment extends TabBoardFragment implements View.OnClickListener, ProgressBar.OnProgressChangedListener {

    private RadioTextView mRtFilter;
    private RadioTextView mRtMakeUp;
    private ProgressBar mProgressbar;
    private EffectButtonItem[] mEffectItemGroup;
    private volatile EffectButtonItem mCurrentItem;
    private IMakeUpCallback mCallback;
    private float[] mItemIntensity;
    private Context mContext;

    private HashMap<Material, ResourceIndex> mMaterialIndexMap = new HashMap<>();
    private HashMap<String, Material> mFileNameMaterialMap = new HashMap<>();

    private int mSelectedTab = 0;
    private int mSelectedIndex = 0;
    private int mCurrentHoverTab = 0;

    public TabStyleMakeUpFragment(Context mContext) {
        this.mContext = mContext;
    }

    public interface IMakeUpCallback {
        void onMakeUpItemSelect(EffectButtonItem item, int tabIndex, int contentIndex);

        /** {zh}
         * @param node  特效名称
         * @param key   功能 key
         * @param value 强度值
         * @brief 更新特效强度
         */
        /** {en}
         * @param node   Effect name
         * @param key    Function key
         * @param value  Strength value
         * @brief  Update effect strength
         */

        void updateComposerNodeIntensity(String node, String key, float value);

        /** {zh}
         * 回调Fragment内部点击事件
         */
        /** {en}
         * Callback Fragment Internal Click Event
         */

        void onClickEvent(View view);
    }


    // Reinflate the layout, since slide bar is added and new layout is applied.
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_style_makeup, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view,savedInstanceState);
        mRtFilter = view.findViewById(R.id.rt_filter);
        mRtFilter.setOnClickListener(this);
        mRtMakeUp = view.findViewById(R.id.rt_makeup);
        mRtMakeUp.setOnClickListener(this);

        mProgressbar = view.findViewById(R.id.pb_makeup);
        mProgressbar.setOnProgressChangedListener(this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        int layoutID = this.getResources().getIdentifier("fl_effect_board", "id", getActivity().getPackageName());
        View view = getActivity().getWindow().findViewById(layoutID);
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams)view.getLayoutParams();
        int pbHeight = getResources().getDimensionPixelSize(R.dimen.height_progress_bar);
        lp.height = lp.height + pbHeight;
        view.setLayoutParams(lp);

    }

    @Override
    public void onViewPagerSelected(int position) {
        mCurrentHoverTab = position;
    }

    @Override
    public void onClickEvent(View view) {
        if (mCallback == null) {
            LogUtils.e("mEffectCallback == null!!");

            return;
        }
        mCallback.onClickEvent(view);
    }

    @Override
    public void setData() {

    }

    public TabStyleMakeUpFragment setData(EffectButtonItem[] effectItemGroup, CategoryData categoryData) {

        if (categoryData == null) {
            return this;
        }

        mEffectItemGroup = effectItemGroup;

        // binding mEffectItemGroup with downloaded materials.
        for (CategoryTabItem tab : categoryData.getTabs()) {
            for (Material material : tab.getItems()) {
                String fileName = material.getFileName();
                if (!TextUtils.isEmpty(fileName)) {
                    String fileNameSplit = fileName.split("\\.")[0];
                    if (!TextUtils.isEmpty(fileNameSplit)) {
                        mFileNameMaterialMap.put(fileNameSplit,material);
                    }
                }
            }
        }

        // constructs fragments & titles
        ArrayList<Fragment> fragments = new ArrayList<>();
        ArrayList<String> titles = new ArrayList<>();

        for (int i = 0; i < effectItemGroup.length; i++) {
            int tabIndex = i;
            EffectButtonItem itemGroup = effectItemGroup[tabIndex];
            if (itemGroup.getChildren() != null) {
                for (int j = 0; j < itemGroup.getChildren().length; j++) {
                    int contentIndex = j;
                    EffectButtonItem item = itemGroup.getChildren()[contentIndex];
                    if (item.getNode() != null) {
                        String folderName = item.getPath();
                        item.setMaterial(mFileNameMaterialMap.get(folderName));
                        mMaterialIndexMap.put(item.getRemoteMaterial(), new ResourceIndex(tabIndex, contentIndex));
                    }
                }
                fragments.add(new EffectMaterialFragment()
                        .setData(new ArrayList<>(Arrays.asList(itemGroup.getChildren())))
                        .setCallback(new EffectMaterialFragment.MaterialFragmentCallback() {
                            @Override
                            public void onItemLoadSuccess(EffectButtonItem item, int position) {
                                if (mCallback != null) {
                                    if (item == null) {
                                        return;
                                    }
                                    mCallback.onMakeUpItemSelect(item, tabIndex, position);
                                }
                            }
                        }));
                titles.add(mContext.getString(itemGroup.getTitleId()));
            }
        }

        // set fragments & titles
        refreshTabPageAdapterData(fragments,titles);

        return this;
    }

    public TabStyleMakeUpFragment setCallback(IMakeUpCallback callback) {
        mCallback = callback;
        return this;
    }

    public void setSelectItem(EffectButtonItem item, boolean updateLocalParam){

        if (item == null && mEffectItemGroup!=null && mEffectItemGroup.length > mSelectedIndex) {
            item = mEffectItemGroup[mSelectedIndex].getChildren()[0];
        }
        if (item == null)return;

        if (mCurrentItem != null) mCurrentItem.setSelected(false).setSelectedRelation(false);

        item.setSelected(true);
        // update ui select status
        ResourceIndex resourceIndex = mMaterialIndexMap.get(item.getRemoteMaterial());
        if (resourceIndex == null) {
            if (item.getId() == TYPE_CLOSE){
                updateUISelectStatus(0, 0);
            }
        } else {
            updateUISelectStatus(resourceIndex.tabIndex, resourceIndex.contentIndex);
        }
        // update progress bar
        mItemIntensity = item.getIntensityArray();
        updateProgressBar();
        item.setSelectedRelation(true);
        if (updateLocalParam) {
            // update local param of last select item
            if (mCurrentItem != null) {
                LocalParamDataManager.updateComposerNode(mCurrentItem);
            }
            // update select record
            mCurrentItem = item;
            // update local param of current select item
//            mCurrentItem.setSelectedRelation(true);
            if (item.getId() != TYPE_CLOSE) {
                item.setSelected(true);
                LocalParamDataManager.saveComposerNode(item);
            }
        } else {
            // update select record
            mCurrentItem = item;
        }
    }

    public void refresh() {
        EffectMaterialFragment fragment = (EffectMaterialFragment) getCurrentFragment();
        if(fragment != null){
            fragment.refresh();
        }
    }

    public void updateUISelectStatus(int tabIndex, int contentIndex) {
        LogUtils.e("index: selectItem tabindex = " + tabIndex + ", contentIndex = " + contentIndex);
        if(mEffectItemGroup.length > mSelectedTab && mEffectItemGroup[mSelectedTab].getChildren().length < mSelectedIndex){
            mEffectItemGroup[mSelectedTab].getChildren()[mSelectedIndex].setSelected(false);
        }
        if (mSelectedTab != tabIndex) {
            ((EffectMaterialFragment) getFragment(mSelectedTab)).setSelected(0);
            mEffectItemGroup[mSelectedTab].setSelectChild(null);
            mSelectedTab = tabIndex;
            mCurrentHoverTab = tabIndex;
        }
        mSelectedIndex = contentIndex;
        LogUtils.e("index: selectItem mSelectedTab = " + mSelectedTab + ", mSelectedIndex = " + mSelectedIndex);
        EffectMaterialFragment fragment = (EffectMaterialFragment) getFragment(tabIndex);
        if (fragment != null) {
            fragment.setSelected(contentIndex);
        }
    }

    public void refreshItem(int tabIndex, int contentIndex) {
        EffectMaterialFragment fragment = (EffectMaterialFragment) getFragment(tabIndex);
        if (fragment != null) {
            fragment.refreshItem(contentIndex);
        }
    }

    public void refreshItem(Material material) {
        ResourceIndex index = mMaterialIndexMap.get(material);
        if (index == null) {
            refreshItem(0, 0);
        }else {
            refreshItem(index.tabIndex, index.contentIndex);
        }
    }

    //*****************************************************************
    // style make-up methods
    //*****************************************************************

    @Override
    public void onClick(View view) {
        super.onClick(view);

        if (view.getId() == R.id.rt_filter) {
            mRtFilter.setState(true);
            mRtMakeUp.setState(false);
            updateProgressBar();
        } else if (view.getId() == R.id.rt_makeup) {
            mRtFilter.setState(false);
            mRtMakeUp.setState(true);
            updateProgressBar();
        }
    }

    private void updateProgressBar(){
        if (mItemIntensity == null || mItemIntensity.length == 0){
            mProgressbar.setProgress(0);
            return;
        }
        if (mRtMakeUp.isSelected()){
            mProgressbar.setProgress(mItemIntensity[1]);
        }else if (mRtFilter.isSelected()){
            mProgressbar.setProgress(mItemIntensity[0]);

        }
    }

    @Override
    public void onProgressChanged(ProgressBar progressBar, float progress, boolean isFromUser) {
        if (!isFromUser) {
            return;
        }
        if (mCurrentItem == null || mCurrentItem.getParent() != mEffectItemGroup[mCurrentHoverTab]) {
            return;
        }
        LogUtils.e("onProgressChanged "+progress);
        if (mCurrentItem == null || mCurrentItem.getId() < 0) return;
        if (progressBar != null && progressBar.getProgress() != progress) {
            progressBar.setProgress(progress);
        }

        int index = mRtFilter.isSelected() ? 0 : 1;
        if (mCurrentItem.getAvailableItem() == null ||
                (mCurrentItem.getAvailableItem().getNode().getKeyArray() == null || mCurrentItem.getAvailableItem().getNode().getKeyArray().length == 0) ||
                (mCurrentItem.getAvailableItem().getIntensityArray().length <= index)) {
            return;
        }
        mCurrentItem.getAvailableItem().getIntensityArray()[index] = progress;
        ComposerNode node = mCurrentItem.getAvailableItem().getNode();
        mCallback.updateComposerNodeIntensity(node.getPath(), node.getKeyArray()[index], progress);
    }

    //**********************************************************************************************
    // local param. storage
    @Override
    public void onProgressEnd(ProgressBar progressBar, float progress, boolean isFormUser) {
        LocalParamDataManager.saveComposerNode(mCurrentItem);
    }

    public void resetDefault(){
        mCurrentItem = null;
        mItemIntensity = null;
        mSelectedTab = 0;
        mSelectedIndex = 0;
        LogUtils.e("resetDefault mSelectedTab = " + mSelectedTab + ", mSelectedIndex = " + mSelectedIndex);
        if (LocalParamDataManager.useLocalParamStorage()) {
            LocalParamDataManager.reset();
            if (((StyleMakeUpActivity)getActivity()).isEnableDefaultBeauty()) {
                Set<EffectButtonItem> defaultSet = ((StyleMakeUpActivity)getActivity()).getEffectDataManager().getDefaultItems();
                for (EffectButtonItem defaultItem : defaultSet) {
                    LocalParamDataManager.saveComposerNode(defaultItem);
                }
            }
        }
        updateUI();
    }

    public void updateUI(){
        if (getFragmentList() != null) {
            int fragmentsSize = getFragmentList().size();
            for (int tabIndex = 0; tabIndex < fragmentsSize; tabIndex++) {
                if (mSelectedTab != tabIndex) {
                    updateUISelectStatus(tabIndex, 0);
                }
                else {
                    updateUISelectStatus(mSelectedTab, mSelectedIndex);
                }
            }
            updateProgressBar();
        }
    }

    public void updateLocalParam(EffectType effectType, boolean isFirstRun){
        if (mEffectItemGroup == null) {
            return;
        }
        LogUtils.e("LocalParamDataManager.useLocalParamStorage() = "+LocalParamDataManager.useLocalParamStorage());
        if (LocalParamDataManager.useLocalParamStorage() && !isFirstRun) {
            if (mEffectItemGroup != null) {
                for (int i = 0; i < mEffectItemGroup.length; i++) {
                    LocalParamDataManager.load(mEffectItemGroup[i], effectType);
                }
            }
        }
        updateUI();
    }

    public void refreshItemFocus() {
        switchTab(mSelectedTab);
        if (getCurrentFragment() != null) {
            ((EffectMaterialFragment)getCurrentFragment()).scrollToPosition(mSelectedIndex);
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
