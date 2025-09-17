package com.effectsar.labcv.effect.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.effectsar.labcv.common.adapter.ItemViewRVAdapter;
import com.effectsar.labcv.common.view.ButtonViewHolder;
import com.effectsar.labcv.effect.model.EffectButtonItem;

import java.util.List;

public class MattingStickerRVAdapter extends ItemViewRVAdapter<EffectButtonItem, ButtonViewHolder> {

    public MattingStickerRVAdapter(List<EffectButtonItem> itemList, OnItemClickListener<EffectButtonItem> listener) {
        super(itemList, listener);
    }

    //  {zh} 此回调中使用定制ViewHolder的构造函数返回一个实例，inflate时可以使用不同的layout  {en} This callback uses the constructor of the custom ViewHolder to return an instance, a different layout can be used when inflating
    @Override
    public ButtonViewHolder onCreateViewHolderInternal(ViewGroup parent, int viewType) {
        return new ButtonViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(com.effectsar.labcv.common.R.layout.view_holder_button, parent, false));
    }

    @Override
    public void onBindViewHolderInternal(ButtonViewHolder holder, int position, EffectButtonItem item) {

        Context context = holder.itemView.getContext();

        holder.setIcon(item.getIconId());
        holder.setTitle(context.getString(item.getTitleId()));

        holder.setMarqueue(false);

        boolean se = item.isSelected();
        holder.change(se);

        holder.pointChange(false);
    }

    @Override
    public void changeItemSelectRecord(EffectButtonItem item, int position) {

    }
}
