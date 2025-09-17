package com.effectsar.labcv.effect.adapter;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.effectsar.labcv.common.adapter.ItemViewRVAdapter;
import com.effectsar.labcv.effect.R;
import com.effectsar.labcv.effect.resource.StickerItem;
import com.effectsar.labcv.effect.view.DownloadView;
import com.effectsar.labcv.effect.view.StickerViewHolder;
import com.effectsar.labcv.resource.RemoteResource;

import java.util.List;

public class StickerRVAdapter extends ItemViewRVAdapter<StickerItem, StickerViewHolder> {

    protected int mSelect = 0;

    @LayoutRes
    protected int mLayoutId = R.layout.view_holder_online_item;

    public StickerRVAdapter(@LayoutRes int layoutId, List<StickerItem> itemList, OnItemClickListener<StickerItem> listener) {
        super(itemList, listener);
        mLayoutId = layoutId;
    }

    @Override
    public StickerViewHolder onCreateViewHolderInternal(ViewGroup parent, int viewType) {
        return new StickerViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(mLayoutId, parent, false));
    }

    @Override
    public void onBindViewHolderInternal(StickerViewHolder holder, int position, StickerItem item) {
        if (mSelect == position) {
            holder.change(true);
        } else {
            holder.change(false);
        }

        if (item.getResource() != null && item.getResource() instanceof RemoteResource) {
            holder.setIcon(item.getIconUrl());

            RemoteResource resource = (RemoteResource) item.getResource();
            switch (resource.getState()) {
                case REMOTE:
                    holder.setState(DownloadView.DownloadState.REMOTE);
                    break;
                case CACHED:
                    holder.setState(DownloadView.DownloadState.CACHED);
                    break;
                case DOWNLOADING:
                    holder.setState(DownloadView.DownloadState.DOWNLOADING);
                    holder.setProgress(resource.getDownloadProgress());
                    break;
                case UNKNOWN:
                    throw new IllegalStateException();
            }
        } else {
            holder.setIcon(item.getIconId());
            holder.setState(DownloadView.DownloadState.CACHED);
        }
        holder.setTitle(item.getTitle(holder.itemView.getContext()));
    }

    @Override
    public void changeItemSelectRecord(StickerItem item, int position) {
        setSelect(position);
    }

    public void setSelect(int select) {
        if (mSelect != select) {
            int oldSelect = mSelect;
            mSelect = select;
            notifyItemChanged(oldSelect);
            notifyItemChanged(select);
        }
    }

}
