package com.effectsar.labcv.demo.fragment;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static com.effectsar.labcv.demo.activity.MainActivity.LICENSE_INFO_TAG;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.effectsar.labcv.common.utils.ToastUtils;
import com.effectsar.labcv.demo.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LicenseInfoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LicenseInfoFragment extends Fragment implements View.OnClickListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private EditText mEtKey;
    private EditText mEtSecret;
    private TextView mTvConfirm;

    // TODO: Rename and change types of parameters
    private String mKey;
    private String mSecret;

    private ILicenseInfoCallback mCallback;

    public LicenseInfoFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LicenseInfoFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LicenseInfoFragment newInstance(String param1, String param2) {
        LicenseInfoFragment fragment = new LicenseInfoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mKey = getArguments().getString(ARG_PARAM1);
            mSecret = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mEtKey = view.findViewById(R.id.et_key);
        mEtSecret = view.findViewById(R.id.et_secret);
        mTvConfirm = view.findViewById(R.id.tv_confirm);
        mTvConfirm.setOnClickListener(this);

        if (mKey != null) {
            mEtKey.setText(mKey);
        }
        if (mSecret != null) {
            mEtSecret.setText(mSecret);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_license_info, container, false);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_confirm) {
            String key= mEtKey.getText().toString();
            String secret = mEtSecret.getText().toString();
            if (!TextUtils.isEmpty(key) && !TextUtils.isEmpty(secret)) {
                // TODO: set class

                SharedPreferences sharedPreferences = getActivity().getSharedPreferences(LICENSE_INFO_TAG, 0);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("key", key);
                editor.putString("secret", secret);
                editor.commit();

                if (mCallback != null) {
                    InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(mEtKey.getWindowToken(), 0);
                    imm.hideSoftInputFromWindow(mEtSecret.getWindowToken(), 0);
                    mCallback.hideLicenseInfoFragment();
                    mCallback.onLicenseInfoSaved(key, secret);
                }
            } else {
                ToastUtils.show("Please fulfill both KEY and SECRET!");
            }
        }
    }

    public LicenseInfoFragment setCallback(ILicenseInfoCallback mCallback) {
        this.mCallback = mCallback;
        return this;
    }

    public interface ILicenseInfoCallback{
        void hideLicenseInfoFragment();
        void onLicenseInfoSaved(String key, String secret);
    }

}