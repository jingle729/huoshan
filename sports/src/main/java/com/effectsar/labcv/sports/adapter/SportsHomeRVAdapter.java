package com.effectsar.labcv.sports.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.effectsar.labcv.common.utils.CommonUtils;
import com.effectsar.labcv.common.view.MarqueeTextView;
import com.effectsar.labcv.sports.R;
import com.effectsar.labcv.sports.model.SportItem;

import java.io.File;
import java.util.List;

public class SportsHomeRVAdapter extends RecyclerView.Adapter<SportsHomeRVAdapter.ViewHolder> {
    private List<SportItem> mDataList;
    private OnItemClickListener mOnClickListener;

    public SportsHomeRVAdapter(List<SportItem> mDataList, OnItemClickListener mOnClickListener) {
        this.mDataList = mDataList;
        this.mOnClickListener = mOnClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(
                LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_sports_home, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SportItem item = mDataList.get(position);
        File image = new File(item.getImgRes());
        if (image.exists()) {
            holder.iv.setImageBitmap(BitmapFactory.decodeFile(image.getAbsolutePath()));
        }
        holder.tv.setText(item.getTextRes());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CommonUtils.isFastClick()) {
                    return;
                }

                if (mOnClickListener == null) {
                    return;
                }

                mOnClickListener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView iv;
        MarqueeTextView tv;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            iv = itemView.findViewById(R.id.iv_item_sports_home);
            tv = itemView.findViewById(R.id.tv_item_sports_home);
            tv.setMarqueue(true);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(SportItem item);
    }
}
