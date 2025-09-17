package com.effectsar.labcv.lens.view;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.effectsar.labcv.core.util.LogUtils;
import com.effectsar.labcv.lens.R;

public class TaintDetectInfoFragment extends AppCompatDialogFragment {
    private TextView tvScore;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return View.inflate(getContext(), R.layout.fragment_taint_info, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvScore = view.findViewById(R.id.tv_taint_score);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void resetProperty(boolean isAttrOn){
        tvScore.setText("0.0");
    }

    public void updateProperty(float score) {
        if (this.isVisible() && Math.abs(score + 1.0) > 1e-6) {
            LogUtils.e("score:" + score);
            tvScore.setText(String.format("%.2f", score));
        }
    }

    public void onClose() {
        resetProperty(false);
    }
}
