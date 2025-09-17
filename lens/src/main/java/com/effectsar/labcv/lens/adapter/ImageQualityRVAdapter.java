package com.effectsar.labcv.lens.adapter;

import android.view.LayoutInflater;

import android.view.ViewGroup;

import com.effectsar.labcv.common.adapter.ItemViewRVAdapter;
import com.effectsar.labcv.common.view.ButtonViewHolder;
import com.effectsar.labcv.lens.manager.ImageQualityDataManager;

import java.util.List;
import java.util.Set;

public class ImageQualityRVAdapter extends ItemViewRVAdapter<ImageQualityDataManager.ImageQualityItem, ButtonViewHolder> {
    private Set<ImageQualityDataManager.ImageQualityItem> mSelectSet;

    public ImageQualityRVAdapter(List<ImageQualityDataManager.ImageQualityItem> itemList,
                                 OnItemClickListener<ImageQualityDataManager.ImageQualityItem> listener,
                                 Set<ImageQualityDataManager.ImageQualityItem> set) {
        super(itemList, listener);
        mSelectSet = set;
    }

    @Override
    public ButtonViewHolder onCreateViewHolderInternal(ViewGroup parent, int viewType) {
        return new ButtonViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(com.effectsar.labcv.common.R.layout.view_holder_button, parent, false));
    }

    @Override
    public void onBindViewHolderInternal(ButtonViewHolder holder, int position, ImageQualityDataManager.ImageQualityItem item) {
        holder.setIcon(item.getIcon());
        holder.setTitle(holder.itemView.getContext().getString(item.getTitle()));
        holder.change(mSelectSet.contains(item));
    }

    @Override
    public void changeItemSelectRecord(ImageQualityDataManager.ImageQualityItem item, int position) {
        if (mSelectSet.contains(item)) {
            mSelectSet.remove(item);
        } else {
            mSelectSet.clear();
            mSelectSet.add(item);
        }
    }

//    @Override
//    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
//        super.onBindViewHolder(holder, position);
//
//        ImageQualityDataManager.ImageQualityItem item = mItemList.get(position);
//        if (item == null) {
//            LogUtils.e("item must not be null");
//            return;
//        };
//        holder.bv.change(mSelectSet.contains(item));
//        holder.bv.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (mSelectSet.contains(item)) {
//                    mSelectSet.remove(item);
//                } else {
//                    mSelectSet.add(item);
//                }
//                mListener.onItemClick(item, holder.getAdapterPosition());
//            }
//        });
//    }
}
