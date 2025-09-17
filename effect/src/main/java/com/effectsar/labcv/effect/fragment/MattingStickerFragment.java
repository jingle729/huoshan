package com.effectsar.labcv.effect.fragment;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;


import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;;
import android.view.ViewGroup;
import android.widget.FrameLayout;;


import com.effectsar.labcv.common.fragment.TabBoardFragment;
import com.effectsar.labcv.core.util.LogUtils;
import com.effectsar.labcv.effect.R;
import com.effectsar.labcv.effect.view.ProgressBar;

import java.util.ArrayList;


public class MattingStickerFragment extends TabBoardFragment implements ProgressBar.OnProgressChangedListener{
    private MattingStickerCallback mattingStickerCallback;
    private ProgressBar pb;
    private boolean useProgressBar = false;
    private FrameLayout.LayoutParams lp;
    private int pbHeightDefault;

//    private boolean isEnabled = true;
//    private ButtonView mBvNone;
//    private ButtonView mBvUpload;
//    private int mLayoutRes = R.layout.fragment_matting;

    public interface MattingStickerCallback{
        void onClickEvent(View view);
        void onProgressChanged(ProgressBar progressBar, float progress, boolean isFormUser) ;
        void onProgressEnd(ProgressBar progressBar, float progress, boolean isFormUser);
    }

    public MattingStickerFragment() {

    }

    public MattingStickerFragment(ArrayList<Fragment> fragments, ArrayList<String> titles){
        setFragmentList(fragments);
        setTitleList(titles);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        int layoutID = this.getResources().getIdentifier("fl_effect_board", "id", getActivity().getPackageName());
        View view = getActivity().getWindow().findViewById(layoutID);
        lp = (FrameLayout.LayoutParams)view.getLayoutParams();
        int pbHeight = getResources().getDimensionPixelSize(R.dimen.height_progress_bar);
        lp.height = lp.height + pbHeight;

        pbHeightDefault = lp.height;
        // {zh} 初始化界面显不显示colorListView {en} Initial UI display colorListView
//        switch (colorListPosition) {
//            case BOARD_FRAGMENT_HEAD_ABOVE:
//                int colorListID = this.getResources().getIdentifier("color_list_above", "id", getActivity().getPackageName());
//                View colorListView = getActivity().getWindow().findViewById(colorListID);
//                lp.height = lp.height + colorListView.getHeight() + getResources().getDimensionPixelSize(R.dimen.colorlistview_margin_bottom);
//                break;
//            default:
//        }
        view.setLayoutParams(lp);
    }


//    public MattingStickerFragment(int layoutRes) {
//       mLayoutRes = layoutRes;
//    }

    public void setMattingStickerCallback(MattingStickerCallback mattingStickerCallback) {
        this.mattingStickerCallback = mattingStickerCallback;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_effect, container, false);
    }

    @Override
    public void onViewCreated(@NonNull  View view, @Nullable  Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        pb = view.findViewById(R.id.pb1);
        pb.setOnProgressChangedListener(this);
        if (!useProgressBar) {
            pb.setVisibility(View.GONE);
        }
//        removeButtonImgDefault();
//        mBvNone = view.findViewById(R.id.bv_none_matting);
//        mBvNone.setOnClickListener(this);
//        mBvUpload = view.findViewById(R.id.bv_upload_matting);
//        mBvUpload.setOnClickListener(this);
//        view.findViewById(R.id.iv_close_board).setOnClickListener(this);
//        view.findViewById(R.id.iv_record_board).setOnClickListener(this);
//        view.findViewById(R.id.img_default).setVisibility(View.GONE);
    }

    @Override
    public void onProgressChanged(ProgressBar progressBar, float progress, boolean isFormUser) {
        LogUtils.e("=======11111");
        if (null != mattingStickerCallback){
            mattingStickerCallback.onProgressChanged(progressBar, progress, isFormUser);
        }
    }

    @Override
    public void onProgressEnd(ProgressBar progressBar, float progress, boolean isFormUser) {
        LogUtils.e("=======22222");
        if (null != mattingStickerCallback){
            mattingStickerCallback.onProgressEnd(progressBar, progress, isFormUser);
        }
    }

    //    @Override
//    public void onClick(View view) {
//
//         {zh} // UI 变化         {en} //UI change
//       if (view.getId() == R.id.bv_none_matting){
//           isEnabled = !isEnabled;
//       }
//       if (view.getId() == R.id.bv_upload_matting){
//           if (!isEnabled){
//               ToastUtils.show(getString(R.string.matting_open_first));
//               return;
//           }
//       }
//
//        if (null != mattingStickerCallback){
//            mattingStickerCallback.onClickEvent(view);
//        }
//    }

//    public void changeButtonIcon(int icon) {
//       mBvUpload.setIcon(icon);
//    }

    public MattingStickerFragment useProgressBar(Boolean bool){
        if (!isAdded()) {
            LogUtils.e("fragment not added");
            return this;
        }
        useProgressBar = bool;
        if (bool){
            pb.setVisibility(View.VISIBLE);
        }
        else {
            pb.setVisibility(View.GONE);
        }
        pb.setProgress(0.8f);
        return this;
    }


    @Override
    public void onViewPagerSelected(int position) {

    }

    @Override
    public void onClickEvent(View view) {
        if (null != mattingStickerCallback){
            mattingStickerCallback.onClickEvent(view);
        }
    }

    @Override
    public void setData() {

    }

}
