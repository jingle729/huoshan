package com.effectsar.labcv.demo.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.effectsar.labcv.common.utils.CommonUtils;
import com.effectsar.labcv.common.utils.LocaleUtils;
import com.effectsar.labcv.core.Config;
import com.effectsar.labcv.core.license.EffectLicenseHelper;
import com.effectsar.labcv.core.util.LogUtils;
import com.effectsar.labcv.demo.R;
import com.effectsar.labcv.demo.model.FeatureTab;
import com.effectsar.labcv.demo.model.FeatureTabItem;
import com.effectsar.labcv.demo.model.MainDataManager;
import com.effectsar.labcv.demo.model.UserData;
import com.effectsar.labcv.effect.resource.StickerFetch;
import com.effectsar.labcv.effect.task.DownloadResourceTask;
import com.effectsar.labcv.effectsdk.RenderManager;
import com.volcengine.effectone.singleton.AppSingleton;

import java.util.ArrayList;
import java.util.List;

public class MainTabRVAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int TYPE_TITLE = 1;
    public static final int TYPE_ITEM = 2;
    public static final int TYPE_FOOTER = 3;

    private OnItemClickListener mListener;
    private final List<FeatureTabItem> mData;

    public MainTabRVAdapter(List<FeatureTab> groups) {
        mData = new ArrayList<>();
        for (FeatureTab group : groups) {
            mData.addAll(group.toList());
        }
        if (UserData.getInstance(AppSingleton.instance).isBoe()) {
            EffectLicenseHelper.LICENSE_URL = EffectLicenseHelper.LICENSE_URL_BOE;
            DownloadResourceTask.setServerType(DownloadResourceTask.SERVER_TYPE_BOE);
            StickerFetch.BASE_URL = StickerFetch.BASE_URL_BOE;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_TITLE:
                return new TitleViewHolder(
                        LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.item_main_tab_title, parent, false)
                );
            case TYPE_ITEM:
                return new ItemViewHolder(
                        LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.item_main_tab_item, parent, false)
                );

            case TYPE_FOOTER:
                return new FooterViewHolder(
                        LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.item_main_version, parent, false)
                );
        }
        throw new IllegalStateException("viewType must be TYPE_TITLE or TYPE_ITEM");
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        if (position < mData.size()){
            FeatureTabItem item = mData.get(position);
            if (viewType == TYPE_TITLE) {
                TitleViewHolder vh = (TitleViewHolder) holder;
                vh.tv.setText(item.getTitle());
                if (item.getId().equals(MainDataManager.GROUP_ALGORITHM)){
                    holder.itemView.setOnLongClickListener(v->{
                        Config.ALGORITHM_MEMORY_SWITCH = true;
                        Toast.makeText(holder.itemView.getContext(), "open algorithm memory detect", Toast.LENGTH_SHORT).show();
                        return false;
                    });
                }
            } else if (viewType == TYPE_ITEM) {
                ItemViewHolder vh = (ItemViewHolder) holder;
                vh.iv.setImageResource(item.getIconId());
                vh.tv.setText(item.getTitle());
                vh.itemView.setOnClickListener(v -> {
                    if (CommonUtils.isFastClick()){
                        LogUtils.e("too fast click");
                        return;
                    }
                    if (mListener == null) {
                        return;
                    }
                    mListener.onItemClick(item);
                });
            }
        }

        if (viewType == TYPE_FOOTER) {
            FooterViewHolder vh = (FooterViewHolder) holder;
//            vh.tv.setText(getVersionName());
            if (DownloadResourceTask.getServerType().equals(DownloadResourceTask.SERVER_TYPE_BOE)) {
                vh.tv.setText(getVersionName() + "_boe");
            } else {
                vh.tv.setText(getVersionName());
            }
            boolean isZH = LocaleUtils.getCurrentLocale(AppSingleton.instance).getLanguage().equals("zh");
            if (isZH) {
                vh.icpLL.setVisibility(View.VISIBLE);
                vh.icpTV.setText("备案号: 京ICP备20018813号-194A");
                vh.icpTV.setOnClickListener(v -> mListener.onICPClick());
            } else {
                vh.icpLL.setVisibility(View.GONE);
            }

        }
    }

    private String getVersionName() {
        return "V" + RenderManager.getSDKVersion();
    }

    @Override
    public int getItemCount() {
        return mData.size()+1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position < mData.size()){
            FeatureTabItem item = mData.get(position);
            if (item instanceof FeatureTab) {
                return TYPE_TITLE;
            } else {
                return TYPE_ITEM;
            }
        }else {
            return TYPE_FOOTER;
        }

    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    static class TitleViewHolder extends RecyclerView.ViewHolder {
        TextView tv;
        public TitleViewHolder(@NonNull View itemView) {
            super(itemView);
            tv = itemView.findViewById(R.id.tv_main_tab_title);
        }
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView tv;
        ImageView iv;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tv = itemView.findViewById(R.id.tv_main_tab_item);
            iv = itemView.findViewById(R.id.iv_main_tab_item);
        }

    }

    static class FooterViewHolder extends RecyclerView.ViewHolder {
        TextView tv;
        TextView icpTV;
        LinearLayout icpLL;
        Button tb;
        public FooterViewHolder(@NonNull View itemView) {
            super(itemView);
            tv = itemView.findViewById(R.id.tv_version);
            icpTV = itemView.findViewById(R.id.tv_icp_registration_number);
            icpLL = itemView.findViewById(R.id.ll_icp_registration_number_layout);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(FeatureTabItem item);

        void onICPClick();
    }
}
