package com.effectsar.labcv.effect.fragment;

import static com.effectsar.labcv.effect.manager.EffectDataManager.TYPE_CLOSE;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.effectsar.labcv.common.R;
import com.effectsar.labcv.common.utils.PlatformUtils;
import com.effectsar.labcv.common.utils.ToastUtils;
import com.effectsar.labcv.common.view.item.DownloadView;
import com.effectsar.labcv.common.view.item.ItemViewPageFragment;
import com.effectsar.labcv.common.view.item.ItemViewRVAdapter;
import com.effectsar.labcv.common.view.item.SelectOnlineViewHolder;
import com.effectsar.labcv.core.util.LogUtils;
import com.effectsar.labcv.effect.model.EffectButtonItem;
import com.effectsar.platform.struct.Material;
import com.effectsar.platform.struct.PlatformError;
import com.effectsar.platform.utils.ExtensionKt;

import java.util.List;

public class EffectMaterialFragment extends ItemViewPageFragment<EffectButtonItem, SelectOnlineViewHolder> {

    protected int mType;
    protected static int mSelect = 0;
    private MaterialFragmentCallback mCallback;
    private ItemViewRVAdapter<EffectButtonItem, SelectOnlineViewHolder> mAdapter;

    public interface MaterialFragmentCallback {
        void onItemLoadSuccess(EffectButtonItem item, int position);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        setItemSelectedPadding(getResources().getDimensionPixelSize(R.dimen.select_padding));
        super.onViewCreated(view, savedInstanceState);
    }

    public EffectMaterialFragment setCallback(MaterialFragmentCallback callback) {
        mCallback = callback;
        return this;
    }

    public EffectMaterialFragment setData(List<EffectButtonItem> materials) {
        mAdapter = new ItemViewRVAdapter<>(materials, new ItemViewRVAdapter.OnItemClickListener<EffectButtonItem, SelectOnlineViewHolder>() {
            @Override
            public void onItemClick(SelectOnlineViewHolder holder, EffectButtonItem item, int position) {
                onMakeUpItemSelect(materials,item, position);
            }

            @Override
            public SelectOnlineViewHolder onCreateViewHolderInternal(ViewGroup parent, int viewType) {
                return new SelectOnlineViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.view_holder_online_item, parent, false));
            }

            @Override
            public void onBindViewHolderInternal(SelectOnlineViewHolder holder, int position, EffectButtonItem material) {
                LogUtils.e("onBindViewHolderInternal: mSelect = " + mSelect + "  position=" + position);
                if (material.isSelected()) {
//                if (material.shouldHighLight()) {
                    holder.setFocused(true, position, material);
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
                    if (TextUtils.isEmpty(material.getTitle())) {
                        holder.setTitleId(material.getTitleId());
                    } else {
                        holder.setTitle(material.getTitle());
                    }
                }
            }

            @Override
            public void changeItemSelectRecord(EffectButtonItem item, int position) {

            }
        });
        setAdapter(mAdapter);
        return this;
    }

    public EffectMaterialFragment setType(int type) {
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
        if (mAdapter == null) {
            return;
        }
        List<EffectButtonItem> items = mAdapter.getItemList();
        if (items == null) {
            return;
        }
        for (int i = 0; i < items.size(); i++) {
            items.get(i).setSelected(i == select);
        }
        mAdapter.notifyDataSetChanged();
    }

    EffectButtonItem mLastEffectButtonItem;
    int lastSelPosition = 0;

    //============
    private void onMakeUpItemSelect(List<EffectButtonItem> materials, EffectButtonItem item, int position){
        for (int i = 0; i < materials.size(); i++) {
            EffectButtonItem effectButtonItem = materials.get(i);
            if(effectButtonItem.isSelected()){
                lastSelPosition = i;
                mLastEffectButtonItem = effectButtonItem;
                mLastEffectButtonItem.setSelected(false);
                mAdapter.notifyItemChanged(i);
                break;
            }
        }
        if (mCallback != null && item.getId() == TYPE_CLOSE) {
            mCallback.onItemLoadSuccess(item, position);
            return;
        }
        if (item != null && item.isRemote()) {
            PlatformUtils.fetchMaterial(item.getRemoteMaterial(), new PlatformUtils.MaterialFetchListener() {
                @Override
                public void onStart(@NonNull Material material) {
                    if (mAdapter != null) mAdapter.notifyItemChanged(position);
                }

                @Override
                public void onSuccess(@NonNull Material material, @NonNull String path) {
                    if(position < materials.size() && mAdapter != null){
                        materials.get(position).setSelected(true);
                        mAdapter.notifyItemChanged(position);
                    }
                    if (mCallback != null) { mCallback.onItemLoadSuccess(item, position); }
                }

                @Override
                public void onProgress(@NonNull Material material, int i) {
                    item.setMaterial(material);
                    if (mAdapter != null) mAdapter.notifyItemChanged(position);
                }

                @Override
                public void onFailed(@NonNull Material material, @NonNull Exception e, @NonNull PlatformError platformError) {
                    if (mLastEffectButtonItem != null) {
                        mLastEffectButtonItem.setSelected(true);
                        mAdapter.notifyItemChanged(lastSelPosition);
                    }
                    ToastUtils.show(platformError.name());
                }
            });
        }
    }

}
