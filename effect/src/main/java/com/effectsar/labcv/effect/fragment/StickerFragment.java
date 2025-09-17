package com.effectsar.labcv.effect.fragment;

import android.os.Bundle;
import androidx.annotation.Nullable;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.effectsar.labcv.common.view.item.ItemViewPageFragment;
import com.effectsar.labcv.common.view.item.ItemViewRVAdapter;
import com.effectsar.labcv.common.view.item.SelectOnlineViewHolder;
import com.effectsar.labcv.common.R;
import com.effectsar.labcv.common.view.item.DownloadView;
import com.effectsar.labcv.resource.MaterialResource;
import com.effectsar.platform.utils.ExtensionKt;

import java.util.List;

public class StickerFragment extends ItemViewPageFragment<MaterialResource, SelectOnlineViewHolder> {

    protected int mType;
    protected int mSelect = 0;
    private StickerFragmentCallback mCallback;

    interface StickerFragmentCallback {
        void onItemClick(MaterialResource item, int position);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        setItemSelectedPadding(getResources().getDimensionPixelSize(R.dimen.select_padding));
        super.onViewCreated(view, savedInstanceState);
    }

    public StickerFragment setCallback(StickerFragmentCallback callback) {
        mCallback = callback;
        return this;
    }

    public StickerFragment setData(List<MaterialResource> materials) {
        setAdapter(new ItemViewRVAdapter<>(materials, new ItemViewRVAdapter.OnItemClickListener<MaterialResource, SelectOnlineViewHolder>() {
            @Override
            public void onItemClick(SelectOnlineViewHolder holder,MaterialResource item, int position) {
                if (mCallback != null) {
                    mCallback.onItemClick(item, position);
                }
            }

            @Override
            public SelectOnlineViewHolder onCreateViewHolderInternal(ViewGroup parent, int viewType) {
                return new SelectOnlineViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.view_holder_online_item, parent, false));
            }

            @Override
            public void onBindViewHolderInternal(SelectOnlineViewHolder holder, int position, MaterialResource material) {
                if (mSelect == position) {
                    holder.setFocused(true,position,material);
                } else {
                    holder.setFocused(false, position, material);
                }

                if (material != null) {
                    if (material.isRemote()) {
                        holder.setIcon(material.getIcon());
                        boolean exits = ExtensionKt.exists(material.getRemoteMaterial());
                        if (exits || material.getProgress() >= 100) {
                            holder.setState(DownloadView.DownloadState.CACHED);
                        } else {
                            if (!material.isDownloading()) {
                                holder.setState(DownloadView.DownloadState.REMOTE);
                            } else {
                                holder.setState(DownloadView.DownloadState.DOWNLOADING);
                                holder.setProgress(material.getProgress() / 100F);
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

            }
        }));
        return this;
    }

    public StickerFragment setType(int type) {
        mType = type;
        return this;
    }

    public void refresh() {
        mAdapter.notifyDataSetChanged();
    }

    public void refreshItem(int index) {
        mAdapter.notifyItemChanged(index);
    }

    public void setSelected(int select) {
        if (mAdapter == null) return;
        if (mSelect != select) {
            int oldSelect = mSelect;
            mSelect = select;
            mAdapter.notifyItemChanged(oldSelect);
            mAdapter.notifyItemChanged(select);
        }
    }

}

//
//public class StickerFragment extends ItemViewPageFragment<MaterialRVAdapter>
//        implements ItemViewRVAdapter.OnItemClickListener<MaterialResource> {
//
//    protected int mType;
//
//    private StickerFragmentCallback mCallback;
//
//    @Override
//    public void onItemClick(MaterialResource item, int position) {
//        if (mCallback != null) {
//            mCallback.onItemClick(item, position);
//        }
//    }
//
//    interface StickerFragmentCallback {
//        void onItemClick(MaterialResource item, int position);
//    }
//
//    @Override
//    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
//        setItemSelectedPadding(getResources().getDimensionPixelSize(R.dimen.select_padding));
//        super.onViewCreated(view, savedInstanceState);
//    }
//
//    public StickerFragment setCallback(StickerFragmentCallback callback) {
//        mCallback = callback;
//        return this;
//    }
//
//    public StickerFragment setData(List<MaterialResource> materials) {
//        setAdapter(new MaterialRVAdapter(materials, this));
//        return this;
//    }
//
//    public StickerFragment setType(int type) {
//        mType = type;
//        return this;
//    }
//
//    public void refresh() {
//        mAdapter.notifyDataSetChanged();
//    }
//
//    public void refreshItem(int index) {
//        mAdapter.notifyItemChanged(index);
//    }
//
//    public void setSelected(int pos) {
//        if (mAdapter == null) return;
//        mAdapter.setSelect(pos);
//    }
//
//}
