package com.effectsar.labcv.demo.fragment;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static com.effectsar.labcv.demo.activity.MainActivity.LICENSE_INFO_TAG;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.effectsar.labcv.common.utils.ToastUtils;
import com.effectsar.labcv.core.Config;
import com.effectsar.labcv.core.license.EffectLicenseHelper;
import com.effectsar.labcv.demo.R;

import java.io.File;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ExtraMenuFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ExtraMenuFragment extends Fragment implements View.OnClickListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private TextView mTvDeleteLicense;
    private TextView mTvCheckDeviceId;
    private TextView mTvClose;

    private ExtraMenuFragment.IExtraMenuCallback mCallback;

    public ExtraMenuFragment() {
        // Required empty public constructor
    }

    public static ExtraMenuFragment newInstance() {
        ExtraMenuFragment fragment = new ExtraMenuFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTvDeleteLicense = view.findViewById(R.id.tv_delete_license);
        mTvDeleteLicense.setOnClickListener(this);
        mTvCheckDeviceId = view.findViewById(R.id.tv_check_deviceid_type);
        mTvCheckDeviceId.setOnClickListener(this);

        mTvClose = view.findViewById(R.id.tv_close);
        mTvClose.setOnClickListener(this);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_extra_menu, container, false);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_delete_license) {
            mCallback.deleteLicenseCache();
        }
        else if (v.getId() == R.id.tv_check_deviceid_type) {
            mCallback.checkDeviceIdType();
        }
        else if (v.getId() == R.id.tv_close) {
            mCallback.hideExtraMenuFragment();
        }
    }

    public ExtraMenuFragment setCallback(ExtraMenuFragment.IExtraMenuCallback mCallback) {
        this.mCallback = mCallback;
        return this;
    }

    public interface IExtraMenuCallback{
        void deleteLicenseCache();
        void checkDeviceIdType();
        void hideExtraMenuFragment();
    }

}