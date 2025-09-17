package com.effectsar.labcv.effect.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.effectsar.labcv.common.adapter.ItemViewRVAdapter;
import com.effectsar.labcv.common.utils.DensityUtils;
import com.effectsar.labcv.effect.R;
import com.effectsar.labcv.effect.resource.StickerItem;
import com.effectsar.labcv.effect.view.DownloadView;
import com.effectsar.labcv.effect.view.StickerViewHolder;
import com.effectsar.labcv.resource.MaterialResource;
import com.effectsar.labcv.resource.RemoteResource;
import com.effectsar.platform.struct.Material;
import com.effectsar.platform.utils.ExtensionKt;

import java.util.List;

public class MaterialRVAdapter extends ItemViewRVAdapter<MaterialResource, StickerViewHolder> {

    protected int mSelect = 0;

    public MaterialRVAdapter(List<MaterialResource> itemList, OnItemClickListener<MaterialResource> listener) {
        super(itemList, listener);
    }

    @Override
    public StickerViewHolder onCreateViewHolderInternal(ViewGroup parent, int viewType) {
        return new StickerViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_holder_online_item, parent, false));
    }

    @Override
    public void onBindViewHolderInternal(StickerViewHolder holder, int position, MaterialResource material) {
        if (mSelect == position) {
            holder.change(true);
        } else {
            holder.change(false);
        }

        if (material != null) {
            if (material.isRemote()) {
                holder.setIcon(material.getIcon());
                boolean exits = ExtensionKt.exists(material.getRemoteMaterial());
                if (exits || material.getProgress() >= 100) {
                    holder.setState(DownloadView.DownloadState.CACHED);
                } else {
                    if (material.getProgress() > 0) {
                        holder.setState(DownloadView.DownloadState.DOWNLOADING);
                        holder.setProgress(material.getProgress() / 100F);
                    } else {
                        holder.setState(DownloadView.DownloadState.REMOTE);
                    }
                }
            } else {
                holder.setIcon(material.getIconId());
                holder.setState(DownloadView.DownloadState.CACHED);
            }
            holder.setTitle(material.getTitle());
        }
    }

    @Override
    public void changeItemSelectRecord(MaterialResource item, int position) {
        // change select status immediately
        // setSelect(position);
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
