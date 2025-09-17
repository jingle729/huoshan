package com.effectsar.labcv.effect.fragment;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
;

import com.effectsar.labcv.common.adapter.ItemViewRVAdapter;
import com.effectsar.labcv.common.fragment.ItemViewPageFragment;
import com.effectsar.labcv.common.model.EffectType;
import com.effectsar.labcv.effect.adapter.EffectRVAdapter;
import com.effectsar.labcv.effect.manager.LocalParamDataManager;
import com.effectsar.labcv.effect.model.EffectButtonItem;
import com.effectsar.labcv.effect.manager.EffectDataManager;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.effectsar.labcv.effect.manager.EffectDataManager.TYPE_BEAUTY_SUIT;
import static com.effectsar.labcv.effect.manager.EffectDataManager.TYPE_CLOSE;

public class BeautyFaceFragment extends ItemViewPageFragment<EffectRVAdapter> implements ItemViewRVAdapter.OnItemClickListener<EffectButtonItem>  {
//        Fragment
//        implements ItemViewRVAdapter.OnItemClickListener<EffectButtonItem> {
//    private RecyclerView rv;
    private EffectButtonItem mItemGroup;
    private Set<EffectButtonItem> mSelectNodes;
    private IBeautyCallBack mCallback;
    private EffectDataManager mEffectDataManager;
//    private EffectRVAdapter mAdapter;

    public BeautyFaceFragment setBeautyCallBack(IBeautyCallBack mCallback) {
        this.mCallback = mCallback;
        return this;
    }

    public BeautyFaceFragment setEffectDataManager(EffectDataManager dataManager) {
        mEffectDataManager = dataManager;
        return this;
    }

    public interface IBeautyCallBack {
        void onEffectItemClick(EffectButtonItem item);

        void onEffectItemClose(EffectButtonItem item);

        EffectType getEffectType();
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {

        if (getAdapter() == null) {
            setAdapter(new EffectRVAdapter(items(), this));
        } else {
            getAdapter().setItemList(items());
        }

        if (mItemGroup != null && mItemGroup.getParent() != null) {
            getAdapter().setShowIndex(false);
        }
        refreshUI();

        super.onViewCreated(view, savedInstanceState);
    }


    public BeautyFaceFragment setData(EffectButtonItem item) {
        mItemGroup = item;
        // refresh ui if view loaded
        if (getRecyclerView() != null && getAdapter() != null){
            getRecyclerView().scrollToPosition(0);
            if (mItemGroup != null && mItemGroup.getParent() != null) {
                ((EffectRVAdapter)getAdapter()).setShowIndex(false);
            }
            ((EffectRVAdapter)getAdapter()).setItemList(items());
        }
        return this;
    }


    /** {zh} 
     * 绑定外层对象
     * @param selectNode
     * @return
     */
    /** {en} 
     * Binding outer object
     * @param selectNode
     * @return
     */
    public BeautyFaceFragment setSelectNodes(Set<EffectButtonItem> selectNode) {
        mSelectNodes = selectNode;
        return this;
    }


    @Override
    public void onItemClick(EffectButtonItem item, int position) {
        if (item.getId() == TYPE_CLOSE) {
            // for close node
            if (item.getParent() != null && item.getParent().getId() == TYPE_BEAUTY_SUIT) {
                EffectButtonItem lastItem = item.getParent().getSelectChild();
                if (lastItem != null){
                    EffectButtonItem[] childItems = mEffectDataManager.getSuitItemsAndDefaultValue(lastItem.getId()).keySet().toArray(new EffectButtonItem[0]);
                    for (int i = 0; i < childItems.length; i++) {
                        removeOrAddItem(mSelectNodes, childItems[i], false);
                    }
                }
            }
            removeOrAddItem(mSelectNodes, item.getParent(), false);
        } else {
            // if select a leaf node which has never been selected before
            if (!mSelectNodes.contains(item) && !item.hasChildren()) {
                float[] itemIntensity = null;
                // remove last select item from mSelectNodes
                if (!mItemGroup.isEnableMultiSelect()) {
                    // if disable multi-select, reuse intensity among different child
                    if (mItemGroup.getSelectChild() != null ) {
                        EffectButtonItem itemToRemove = mItemGroup.getSelectChild();
                        // if reuse children's intensity, record the selected intensity to be reused later.
                        if (mItemGroup.isReuseChildrenIntensity() && itemToRemove.getId() != TYPE_CLOSE) {
                            itemIntensity = itemToRemove.getIntensityArray().clone();
                        }

                        removeOrAddItem(mSelectNodes, itemToRemove, false);
                    }
                }
                // add newly selected one
                if (item.getNode() != null) {
                    // there 3 possible cases for inter-child relations of mItemGroup's children:
                    // case 1: isEnableMultiSelect()==true  && isReuseChildrenIntensity()==false
                    // case 2: isEnableMultiSelect()==false && isReuseChildrenIntensity()==true
                    // case 3: isEnableMultiSelect()==false && isReuseChildrenIntensity()==false

                    // case 2, do nothing to itemIntensity
                    if (itemIntensity != null && mItemGroup.isReuseChildrenIntensity() && !mItemGroup.isEnableMultiSelect()) {

                    } else {
                        // case 1 & 3
                        // set intensity with set param, or use default param if not set
                        if (item.getNode().getIntensityArray() != null) {
                            itemIntensity =  item.getNode().getIntensityArray();
                        } else {
                            itemIntensity = EffectDataManager.getDefaultIntensity(item.getId(), mCallback.getEffectType(), item.isEnableNegative());
                        }
                    }

                    if (item.getColorItems() != null) {
                        itemIntensity[1] = item.getColorItems().get(item.getSelectColorIndex()).getR();
                        itemIntensity[2] = item.getColorItems().get(item.getSelectColorIndex()).getG();
                        itemIntensity[3] = item.getColorItems().get(item.getSelectColorIndex()).getB();
                    }

                    if (itemIntensity != null && item.getIntensityArray() != null) {
                        for (int i = 0; i < itemIntensity.length && i < item.getIntensityArray().length; i++) {
                            item.getIntensityArray()[i] = itemIntensity[i];
                        }
                    }
                    mItemGroup.setSelectChild(item);
                    removeOrAddItem(mSelectNodes, item, true);
                }
            }

        }
        mItemGroup.setSelectChild(item);
        mCallback.onEffectItemClick(item);
        refreshUI();

        // to update selected status of all unselected nodes, especially those who has been unselected just now.
        if (LocalParamDataManager.useLocalParamStorage()) {
            for (EffectButtonItem effectButtonItem : mItemGroup.getChildren()) {
                LocalParamDataManager.updateComposerNode(effectButtonItem);
            }
        }
    }

//    public void refreshUI() {
//        if (rv == null) return;
//        RecyclerView.Adapter adapter = rv.getAdapter();
//        if (adapter != null) {
//            adapter.notifyDataSetChanged();
//        }
//    }


    private List<EffectButtonItem> items() {
        if (mItemGroup != null && mItemGroup.hasChildren()) {
            return Arrays.asList(mItemGroup.getChildren());
        }
        return Collections.singletonList(mItemGroup);
    }

    /** {zh} 
     * 删除或者添加选中的小项，注意删除时，需要同时将小项的强度置为0
     * @param set
     * @param item
     * @param add
     */
    /** {en} 
     * Delete or add the selected item. Note that when deleting, you need to set the intensity of the item to 0 at the same time
     * @param set
     * @param item
     * @param add
     */
    private void removeOrAddItem(Set<EffectButtonItem> set, EffectButtonItem item, boolean add) {
        if (null == item) {
            return;
        }
        if (add) {
            if (item.getAvailableItem() != null) {
                set.add(item);
                item.setSelected(true).setSelectedRelation(true);
                if (LocalParamDataManager.useLocalParamStorage()) {
                    LocalParamDataManager.saveComposerNode(item);
                }
            }
        } else {
            item.setSelectChild(null);
            set.remove(item);
            item.setSelected(false).setSelectedRelation(false);
            if (!item.hasChildren() && LocalParamDataManager.useLocalParamStorage()) {
                LocalParamDataManager.updateComposerNode(item);
            }
            mCallback.onEffectItemClose(item);
            if (item.hasChildren()) {
                for (EffectButtonItem child : item.getChildren()) {
                    removeOrAddItem(set, child, false);
                }
            }
            if (item != null && item.getParent() != null && item.getParent().getId() == TYPE_BEAUTY_SUIT) {
                LocalParamDataManager.removeComposerNode(item);
            }
        }
    }

    public void refreshSelectedItem(){
        if (!mItemGroup.isSelected()) {
//            mItemGroup.setSelectChild(mItemGroup.getChildren()[0]);
            mItemGroup.setSelectChild(null);
        }
    }

    public void updateLocalParam(EffectType effectType){
        LocalParamDataManager.load(mItemGroup, effectType);
        if (mItemGroup.getId() == TYPE_BEAUTY_SUIT && mItemGroup.getSelectChild() == null) {
            mItemGroup.setSelectChild(mItemGroup.getChildren()[0]);
        }
    }
}
