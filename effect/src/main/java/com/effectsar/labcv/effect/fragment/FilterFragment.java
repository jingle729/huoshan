package com.effectsar.labcv.effect.fragment;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.effectsar.labcv.common.adapter.ItemViewRVAdapter;
import com.effectsar.labcv.common.fragment.ItemViewPageFragment;
import com.effectsar.labcv.effect.R;
import com.effectsar.labcv.effect.adapter.FilterRVAdapter;
import com.effectsar.labcv.effect.model.FilterItem;
import com.effectsar.labcv.effect.manager.FilterDataManager;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** {zh}
 * 滤镜
 */
/** {en}
 * Filter
 */

public class FilterFragment extends ItemViewPageFragment<FilterRVAdapter>
        implements ItemViewRVAdapter.OnItemClickListener<FilterItem>{
    //    private RecyclerView rv;
//    private String mSavedFilterPath;
    private IFilterCallback mCallback = null;
    private FilterDataManager mFilterDataManager = null;
    private FilterItem mItemGroup;
//    private FilterItem mSelectFilter;

    public FilterFragment setData(FilterItem item) {
        mItemGroup = item;
        // refresh ui if view loaded
        if (getRecyclerView() != null && getAdapter() != null){
            getRecyclerView().scrollToPosition(0);
            getAdapter().setItemList(items());
//            getAdapter().setSelect(item.getSelectChildPosition());
        }
        return this;
    }

    public FilterFragment setFilterCallback(IFilterCallback mCallback) {
        this.mCallback = mCallback;
        return this;
    }

    public interface IFilterCallback {
        void onFilterSelected(FilterItem filterItem, int position);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
//        if (mItemGroup == null) {
//            mFilterDataManager = new FilterDataManager(getContext());
//            mFilterDataManager.resetAll();
//            mItemGroup = mFilterDataManager.getItems();
//        }

        setAdapter(new FilterRVAdapter(items(), this));

        setItemSelectedPadding(getResources().getDimensionPixelSize(R.dimen.select_padding));

        super.onViewCreated(view, savedInstanceState);
    }

//    public void setSavedFilterPath(String path) {
//        //  {zh} 注释掉这里是为了与iOS对齐，filter在退出再进入后保持原有的选择状态  {en} Comment out here to align with iOS, filter keeps the original selection state after exiting and re-entering
////        if (null != getAdapter()) {
////            getAdapter().setSelect(0);
////        }
//        mSavedFilterPath = path;
//        refreshUI();
//    }



    @Override
    public void onItemClick(FilterItem item, int position) {
        if (mCallback == null) {
            return;
        }
        mCallback.onFilterSelected(item, position);
//        mSavedFilterPath = item.getResource();
//        mSelectFilter = item;
        refreshUI();
    }

    public void setSelected(int pos) {
        if (mAdapter == null) return;
        if (pos <0 || pos > getAdapter().getItemList().size()){
            return;
        }
        mAdapter.setSelect(pos);
        onItemClick((FilterItem) mAdapter.getItemList().get(pos), pos);
    }

    private List<FilterItem> items() {
        if (mItemGroup.hasChildren()) {
            return Arrays.asList((FilterItem[]) mItemGroup.getChildren());
        }
        return Collections.singletonList(mItemGroup);
    }

    public FilterFragment setSelectFilter(FilterItem selectFilter) {
//        mSelectFilter = selectFilter;

        refreshUI();
        return this;
    }
}
