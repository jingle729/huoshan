package com.effectsar.labcv.effect.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.effectsar.labcv.common.adapter.ItemViewRVAdapter;
import com.effectsar.labcv.common.view.SelectViewHolder;
import com.effectsar.labcv.effect.model.EffectButtonItem;

import java.util.List;

public class SelectStatusRVAdapter extends ItemViewRVAdapter<EffectButtonItem, SelectViewHolder>{

    protected boolean mShowIndex = false;
    protected int mSelect = 0;

    public SelectStatusRVAdapter(List<EffectButtonItem> itemList, OnItemClickListener<EffectButtonItem> listener) {
        super(itemList, listener);
    }

    public SelectStatusRVAdapter(List<EffectButtonItem> itemList, OnItemClickListener<EffectButtonItem> listener, int selectItem) {
        super(itemList, listener);
        mSelect = selectItem;
    }


    @Override
    public SelectViewHolder onCreateViewHolderInternal(ViewGroup parent, int viewType) {
        return new SelectViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(com.effectsar.labcv.common.R.layout.view_holder_select, parent, false));
    }

    @Override
    public void onBindViewHolderInternal(SelectViewHolder holder, int position, EffectButtonItem item) {
        Context context = holder.itemView.getContext();

        holder.change(item.shouldHighLight());

        holder.setIcon(item.getIconId());

        if (mShowIndex && position > 0){
            holder.setTitle(getIndex(position));
        }else {
            holder.setTitle(context.getString(item.getTitleId()));
//            holder.setTitle(item.getTitleId());
        }

    }

    @Override
    public void changeItemSelectRecord(EffectButtonItem item, int position) {
//        setSelect(position);
    }

    private String getIndex(int pos){
        if (pos < 10){
            return "0"+pos;
        }else {
            return Integer.toString(pos);
        }
    }

//    public void setSelect(int select) {
//        if (mSelect != select) {
//            int oldSelect = mSelect;
//            mSelect = select;
//            notifyItemChanged(oldSelect);
//            notifyItemChanged(select);
//        }
//    }

    public EffectButtonItem getSelectItem(){
        if (mItemList == null) {
            return null;
        }
        return mItemList.get(mSelect);
    }
}
