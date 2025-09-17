package com.effectsar.labcv.effect.fragment;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import android.widget.FrameLayout;

import com.effectsar.labcv.common.fragment.ItemViewPageFragment;
import com.effectsar.labcv.common.fragment.TabBoardFragment;
import com.effectsar.labcv.core.util.LogUtils;
import com.effectsar.labcv.effect.R;
import com.effectsar.labcv.effect.activity.LocalStyleMakeUpActivity;
import com.effectsar.labcv.effect.adapter.SelectStatusRVAdapter;
import com.effectsar.labcv.effect.manager.EffectDataManager;
import com.effectsar.labcv.effect.manager.LocalParamDataManager;
import com.effectsar.labcv.effect.model.ComposerNode;
import com.effectsar.labcv.effect.model.EffectButtonItem;
import com.effectsar.labcv.effect.view.ProgressBar;
import com.effectsar.labcv.effect.view.RadioTextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;


/** {zh} 
 * 风格妆Fragment
 * Created  on 2021/5/25 2:22 下午
 */
/** {en} 
 * Style Makeup Fragment
 * Created on 2021/5/25 2:22 pm
 */

public class StyleMakeUpFragment extends TabBoardFragment implements View.OnClickListener, SelectStatusRVAdapter.OnItemClickListener<EffectButtonItem>, ProgressBar.OnProgressChangedListener {

    private RadioTextView mRtFilter;
    private RadioTextView mRtMakeUp;
    private ProgressBar mProgressbar;
    private EffectButtonItem[] mEffectItemGroup;
    private EffectButtonItem mCurrentItem;
    private IMakeUpCallback mCallback;
    private float[] mItemIntensity;

    private Context mContext;

    // Fix: java.lang.NoSuchMethodException: StyleMakeUpFragment.<init> []
    public StyleMakeUpFragment() {

    }

    public StyleMakeUpFragment(Context mContext) {
        this.mContext = mContext;
    }

    public StyleMakeUpFragment setData(EffectButtonItem[] effectItemGroup, IMakeUpCallback callback) {
        mEffectItemGroup = effectItemGroup;
        mCallback = callback;

        return this;

    }

    public StyleMakeUpFragment setSelectItem(EffectButtonItem selectItem) {
        mCurrentItem = selectItem;
        if (mCurrentItem != null) {
            mItemIntensity = mCurrentItem.getIntensityArray();
        }
        return this;
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_style_makeup, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);
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

    @Override
    public void onViewPagerSelected(int position) {
        EffectButtonItem itemGroup = mEffectItemGroup[position];
        mCurrentItem = itemGroup.getSelectChild();
    }

    @Override
    public void onClickEvent(View view) {
        if (mCallback == null) {
            LogUtils.e("mCallback == null!!");
            return;
        }
        mCallback.onClickEvent(view);
    }

    @Override
    public void setData() {
        ArrayList<Fragment> fragments = new ArrayList<>();
        ArrayList<String> titles = new ArrayList<>();
        if (mEffectItemGroup == null) {
            return;
        }
        for (EffectButtonItem item:mEffectItemGroup) {
            ItemViewPageFragment<SelectStatusRVAdapter> fragment = new ItemViewPageFragment();

            fragment.setAdapter(new SelectStatusRVAdapter(Arrays.asList( item.getChildren()),StyleMakeUpFragment.this));
            fragment.setItemSelectedPadding(getResources().getDimensionPixelSize(R.dimen.select_padding));
            fragments.add(fragment);
            titles.add(getContext().getString(item.getTitleId()));
        }
        setFragmentList(fragments);
        setTitleList(titles);
    }

    public void updateUI(){
        List<Fragment> fragments = getFragmentList();
        if (fragments != null) {
            for (Fragment fragment : getFragmentList()) {
                if (null == fragment) {
                    continue;
                }
                List<EffectButtonItem> itemList = ((ItemViewPageFragment<SelectStatusRVAdapter>)fragment).getAdapter().getItemList();
                if (!itemList.contains(mCurrentItem)) {
                    itemList.get(0).setSelectedRelation(true);
                }
                ((ItemViewPageFragment<SelectStatusRVAdapter>)fragment).refreshUI();
            }
        }
        updateProgressBar();
    }

    public void updateLocalParam(boolean isFirstRun){
        if (mEffectItemGroup == null) {
            return;
        }
        LogUtils.e("LocalParamDataManager.useLocalParamStorage() = "+LocalParamDataManager.useLocalParamStorage());
        if (LocalParamDataManager.useLocalParamStorage() && !isFirstRun) {
            if (mEffectItemGroup != null) {
                for (int i = 0; i < mEffectItemGroup.length; i++) {
                    LocalParamDataManager.load(mEffectItemGroup[i], ((LocalStyleMakeUpActivity) getActivity()).getEffectType());
                }
            }
        }
        updateUI();
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
    public void onItemClick(EffectButtonItem item, int position) {
        onBeautySelect(item,position);
        updateUI();
    }

    @Override
    public void onProgressChanged(ProgressBar progressBar, float progress, boolean isFromUser) {
        if (!isFromUser) {
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

    @Override
    public void onProgressEnd(ProgressBar progressBar, float progress, boolean isFormUser) {
        LocalParamDataManager.saveComposerNode(mCurrentItem);
    }

    private void onBeautySelect(EffectButtonItem item, int position) {
        if (item == null) {
           return;
        }

        mItemIntensity = item.getIntensityArray();
        updateProgressBar();
        mCallback.onMakeUpItemSelect(item);

        if (mCurrentItem != null) {
            mCurrentItem.setSelected(false).setSelectedRelation(false);
            if (LocalParamDataManager.useLocalParamStorage()) {
                LocalParamDataManager.updateComposerNode(mCurrentItem);
            }
        }

        mCurrentItem = item;
        mCurrentItem.setSelectedRelation(true);
        if (item.getId() != EffectDataManager.TYPE_CLOSE) {
            item.setSelected(true);
            if (LocalParamDataManager.useLocalParamStorage()) {
                LocalParamDataManager.saveComposerNode(item);
            }
        }
    }

    /** {zh} 
     * 恢复默认状态
     */
    /** {en} 
     * Restore the default state
     */

    public void resetDefault(){
        mCurrentItem = null;
        mItemIntensity = null;
        if (LocalParamDataManager.useLocalParamStorage()) {
            LocalParamDataManager.reset();
            if (((LocalStyleMakeUpActivity)getActivity()).isEnableDefaultBeauty()) {
                Set<EffectButtonItem> defaultSet = ((LocalStyleMakeUpActivity)getActivity()).getEffectDataManager().getDefaultItems();
                for (EffectButtonItem defaultItem : defaultSet) {
                    LocalParamDataManager.saveComposerNode(defaultItem);
                }
            }
        }
        updateUI();
    }

    public interface IMakeUpCallback {
        void onMakeUpItemSelect(EffectButtonItem item);

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
}
