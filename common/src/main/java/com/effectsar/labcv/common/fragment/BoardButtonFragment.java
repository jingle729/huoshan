package com.effectsar.labcv.common.fragment;

import android.graphics.Rect;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;;

import com.effectsar.labcv.common.R;
import com.effectsar.labcv.common.adapter.ItemViewRVAdapter;
import com.effectsar.labcv.common.model.ButtonItem;
import com.effectsar.labcv.common.utils.DensityUtils;

public abstract class BoardButtonFragment<T extends ButtonItem, VH extends RecyclerView.ViewHolder> extends Fragment {
    protected TextView tvTitle;
    protected RecyclerView rv;
    protected ImageView ivClose;
    protected ImageView ivRecord;

    protected IBoardCallback<T> mCallback;
    protected T mItem;

    public interface IBoardCallback<T> extends View.OnClickListener {
        void onItem(T item, boolean flag);
    }

    protected abstract int layoutId();
    protected abstract ItemViewRVAdapter<T,VH> createAdapter();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return LayoutInflater.from(getContext())
                .inflate(layoutId(), container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        tvTitle = view.findViewById(R.id.tv_title_algorithm_board);
        rv = view.findViewById(R.id.rv_algorithm_board);
        ivClose = view.findViewById(R.id.iv_close_board);
        ivRecord = view.findViewById(R.id.iv_record_board);
        ivClose.setOnClickListener(mCallback);
        ivRecord.setOnClickListener(mCallback);
        view.findViewById(R.id.img_default).setVisibility(View.GONE);
    }

    protected void initRVView() {
        tvTitle.setText(mItem.getTitle());

        ItemViewRVAdapter<T, VH> adapter = createAdapter();
        rv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rv.setAdapter(adapter);
        rv.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                int position = parent.getChildAdapterPosition(view);
                int totalCount = parent.getAdapter().getItemCount();
                int space = getResources().getDimensionPixelSize(com.effectsar.labcv.common.R.dimen.item_distance);
                if (position == 0) {// {zh} 第一个 {en} The first
                    outRect.left = 0;
                    outRect.right = space / 2;
                } else if (position == totalCount - 1){
                    outRect.left = space / 2;
                    outRect.right = 0;
                } else {// {zh} 中间其它的 {en} Other in the middle
                    outRect.left = space / 2;
                    outRect.right = space / 2;
                }
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (rv != null) {
            int width = DensityUtils.getScreenWidth(getActivity());
            int itemWidth = getResources().getDimensionPixelSize(com.effectsar.labcv.common.R.dimen.logo_size);
            int itemDistance = getResources().getDimensionPixelSize(com.effectsar.labcv.common.R.dimen.item_distance);
            int rvMarginHorizontal = getResources().getDimensionPixelSize(com.effectsar.labcv.common.R.dimen.rv_margin_horizontal);
            int itemCount = rv.getAdapter().getItemCount();
            boolean rvContained = width - 2*rvMarginHorizontal >= itemCount * itemWidth + (itemCount - 1) * itemDistance;

            if(rvContained) {
                FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) rv.getLayoutParams();
                lp.rightMargin = rvMarginHorizontal;
                lp.gravity = Gravity.CENTER_HORIZONTAL;
                rv.setLayoutParams(lp);
            }
        }
    }

    public BoardButtonFragment<T, VH> setItem(T item) {
        mItem = item;
        return this;
    }

    public BoardButtonFragment<T, VH> setCallback(IBoardCallback<T> callback) {
        mCallback = callback;
        return this;
    }


    public void refreshUI() {
        if (rv == null || rv.getAdapter() == null) return;
        RecyclerView.Adapter<?> adapter = rv.getAdapter();
        adapter.notifyDataSetChanged();
    }
}
