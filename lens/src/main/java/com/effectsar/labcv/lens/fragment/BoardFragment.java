package com.effectsar.labcv.lens.fragment;

import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.effectsar.labcv.common.adapter.ItemViewRVAdapter;
import com.effectsar.labcv.common.fragment.ItemViewPageFragment;
import com.effectsar.labcv.common.fragment.TabBoardFragment;
import com.effectsar.labcv.core.util.LogUtils;
import com.effectsar.labcv.lens.adapter.ImageQualityRVAdapter;
import com.effectsar.labcv.lens.manager.ImageQualityDataManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BoardFragment extends TabBoardFragment implements ItemViewRVAdapter.OnItemClickListener<ImageQualityDataManager.ImageQualityItem> {
//        extends BoardButtonFragment<ImageQualityDataManager.ImageQualityItem, ButtonViewHolder>  {
    private Set<ImageQualityDataManager.ImageQualityItem> mSelectSet = new HashSet<>();
    private ImageQualityDataManager.ImageQualityItem mItem;
    private IImageQualityCallback mCallback;
    private int mRecordButtonResId = -1;
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        removeButtonImgDefault();
        if (mRecordButtonResId != -1) {
            setRecordButton(mRecordButtonResId);
        }
    }

    @Override
    public void onViewPagerSelected(int position) {

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
        if (mItem != null) {
            setTitleList(new ArrayList<String>(){
                {
                    add(getContext().getString(mItem.getTitle()));
                }
            });
            setFragmentList(new ArrayList<Fragment>(){
                {
                    ImageQualityRVAdapter adapter = createAdapter();
                    ItemViewPageFragment<ImageQualityRVAdapter> fragment = new ItemViewPageFragment();
                    fragment.setAdapter(adapter);
                    add(fragment);
                }
            });
        }
    }

//    @Override
//    protected int layoutId() {
//        return R.layout.fragment_board_button;
//    }

    protected ImageQualityRVAdapter createAdapter() {
        List<ImageQualityDataManager.ImageQualityItem> items = mItem instanceof ImageQualityDataManager.ImageQualityItemGroup ?
                ((ImageQualityDataManager.ImageQualityItemGroup) mItem).getItems() : Collections.singletonList(mItem);

        ImageQualityRVAdapter adapter = new ImageQualityRVAdapter(items, this, mSelectSet);
        return adapter;
    }

    @Override
    public void onItemClick(ImageQualityDataManager.ImageQualityItem item, int position) {
        boolean selected = mSelectSet.contains(item);
        mCallback.onItem(item, selected);

        ItemViewPageFragment currentFragment = (ItemViewPageFragment)getCurrentFragment();
        currentFragment.refreshUI();
    }

    public void setRecordButton(int resId) {
        mRecordButtonResId = resId;
        if (ivRecord != null) {
            ivRecord.setImageResource(resId);
        }
    }

    /** {zh} 
     * 设置选中按钮
     * @param set
     * @return
     */
    /** {en} 
     * Set the selected button
     * @param set
     * @return
     */

    public BoardFragment setSelectSet(Set<ImageQualityDataManager.ImageQualityItem> set) {
        mSelectSet = set;
        return this;
    }

    public BoardFragment setItem(ImageQualityDataManager.ImageQualityItem item) {
        mItem = item;
        return this;
    }

    public BoardFragment setCallback(IImageQualityCallback callback){
        mCallback = callback;
        return this;
    }

    public interface IImageQualityCallback{
        void onItem(ImageQualityDataManager.ImageQualityItem item, boolean flag);
        void onClickEvent(View view);
    }



}
