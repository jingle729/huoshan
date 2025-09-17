package com.effectsar.labcv.algorithm.fragment;

import android.os.Bundle;
import androidx.annotation.StringRes;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;;


import com.effectsar.labcv.algorithm.adapter.AlgorithmRVAdapter;
import com.effectsar.labcv.algorithm.model.AlgorithmItem;
import com.effectsar.labcv.algorithm.model.AlgorithmItemGroup;
import com.effectsar.labcv.common.adapter.ItemViewRVAdapter;
import com.effectsar.labcv.common.fragment.ItemViewPageFragment;
import com.effectsar.labcv.common.fragment.TabBoardFragment;
import com.effectsar.labcv.common.utils.ToastUtils;
import com.effectsar.labcv.core.algorithm.FaceAlgorithmTask;
import com.effectsar.labcv.core.algorithm.base.AlgorithmTaskKey;
import com.effectsar.labcv.core.util.LogUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AlgorithmBoardFragment extends TabBoardFragment implements ItemViewRVAdapter.OnItemClickListener<AlgorithmItem>, AlgorithmRVAdapter.ICheckAvailable{
//        extends BoardButtonFragment<AlgorithmItem, ButtonViewHolder>
    private Fragment mInnerFragment;
    private int mTitleId;
    private AlgorithmItem mItem;

    private IAlgorithmCallback mCallback;

    private HashSet<AlgorithmTaskKey> mSelectSet;

    private final Set<AlgorithmItem> mCurrentKeys = new HashSet<>();

    public AlgorithmBoardFragment(IAlgorithmCallback callback) {
        this.mCallback = callback;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        removeButtonImgDefault();
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
                    AlgorithmRVAdapter adapter = createAdapter();
                    ItemViewPageFragment<AlgorithmRVAdapter> fragment = new ItemViewPageFragment();
                    fragment.setAdapter(adapter);
                    add(fragment);
                }
            });
        } else {
            setTitleList(new ArrayList<String>(){
                {
                    add(getContext().getString(mTitleId));
                }
            });
            setFragmentList(new ArrayList<Fragment>(){
                {
                    add(mInnerFragment);
                }
            });
        }
    }

    public AlgorithmBoardFragment setSelectSet(HashSet<AlgorithmTaskKey> set) {
        mSelectSet = set;
        return this;
    }

    public AlgorithmBoardFragment setInnerFragment(Fragment fragment) {
        mInnerFragment = fragment;
        return this;
    }

    public AlgorithmBoardFragment setTitleId(@StringRes int title) {
        mTitleId = title;
        return this;
    }

    @Override
    public void onItemClick(AlgorithmItem item, int position) {
        boolean selected = mSelectSet.contains(item.getKey());
        if (!selected) {
            closeItem(item);
        }
        ItemViewPageFragment currentFragment = (ItemViewPageFragment)getCurrentFragment();
        currentFragment.refreshUI();
    }

    @Override
    public boolean checkAvailable(AlgorithmItem item) {
        if (!mSelectSet.contains(item.getKey())) {
            return openItem(item);
        }
        return true;
    }

    private boolean openItem(AlgorithmItem item) {
        mCurrentKeys.add(item);
        if (item.getDependency() != null) {
            for (AlgorithmTaskKey key : item.getDependency()) {
                if (!mSelectSet.contains(key)) {
                    ToastUtils.show(getString(item.getDependencyToastId()));
                    return false;
                }
            }
        }

        if (mCallback != null) {
            mCallback.onItem(item, true);
        }
        return true;
    }

    private void closeItem(AlgorithmItem item) {
        for (AlgorithmItem t : mCurrentKeys) {
            if (t.getDependency() != null && t.getDependency().contains(item.getKey())) {
                closeItem(t);
            }
        }
        if (mCallback != null) {
            mCallback.onItem(item, false);
        }
        mSelectSet.remove(item.getKey());
    }

//    @Override
//    protected int layoutId() {
//        if (mItem != null) {
//            return R.layout.fragment_algorithm_board;
//        } else {
//            return R.layout.fragment_algorithm_board_with_inner_fragment;
//        }
//    }

    public AlgorithmBoardFragment setItem(AlgorithmItem item) {
        mItem = item;
        return this;
    }

    protected AlgorithmRVAdapter createAdapter() {

        List<AlgorithmItem> items = mItem instanceof AlgorithmItemGroup ?
                ((AlgorithmItemGroup) mItem).getItems() : Collections.singletonList(mItem);

        AlgorithmRVAdapter adapter = new AlgorithmRVAdapter(items, this, mSelectSet);
        adapter.setCheckAvailableCallback(this);
        //  {zh} 人脸多item英文版禁止滚动  {en} Face multi-item English version prohibits scrolling
        if (mItem!= null && TextUtils.equals(mItem.getKey().getKey(), FaceAlgorithmTask.FACE.getKey()) ){
            adapter.setMarqueue(false);
        }
        return adapter;
    }

    public interface IAlgorithmCallback{
        void onItem(AlgorithmItem item, boolean flag);
        void onClickEvent(View view);
    }

}
