package com.effectsar.labcv.lens.view;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.effectsar.labcv.lens.R;

public class VidaInfoFragment extends AppCompatDialogFragment {
    private TextView tvFace;
    private TextView tvAes;
    private TextView tvClarity;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return View.inflate(getContext(), R.layout.fragment_vida_info, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvFace = view.findViewById(R.id.tv_vida_face);
        tvAes = view.findViewById(R.id.tv_vida_face_aes);
        tvClarity = view.findViewById(R.id.tv_vida_face_clarity);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void resetProperty(boolean isAttrOn){
        tvFace.setText("0.0");
        tvAes.setText("0.0");
        tvClarity.setText("0.0");
    }

    public void updateProperty(float face, float aes, float clarity) {
        if (this.isVisible()) {
            tvFace.setText(String.format("%.2f", face * 100.f));
            tvAes.setText(String.format("%.2f", aes * 100.f));
            tvClarity.setText(String.format("%.2f", clarity));
        }
    }

    public void onClose() {
        resetProperty(false);
    }
}

