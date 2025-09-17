package com.effectsar.labcv.algorithm.fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.effectsar.labcv.common.utils.BitmapUtils;
import com.effectsar.labcv.common.utils.CommonUtils;
import com.effectsar.labcv.common.utils.ToastUtils;
import com.effectsar.labcv.common.view.ButtonView;
import com.effectsar.labcv.core.util.LogUtils;
import com.effectsar.labcv.cv.R;
import com.volcengine.ck.LocalAlbumActivityContract;
import com.volcengine.ck.album.AlbumEntrance;
import com.volcengine.ck.album.base.AlbumConfig;
import com.volcengine.ck.album.utils.AlbumExtKt;
import com.volcengine.effectone.permission.Scene;
import com.volcengine.effectone.utils.EOUtils;

;

public class FaceVerifyFragment extends Fragment
        implements View.OnClickListener{

    private ButtonView bvUpload;
    private IFaceVerifyCallback mCallback;


    public interface IFaceVerifyCallback {
        void onPicChoose(Bitmap bitmap);

        void faceVerifyOn(boolean flag);
    }

    protected final LocalAlbumActivityContract albumActivityContract = new LocalAlbumActivityContract();
    private ActivityResultLauncher<AlbumConfig> albumLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        albumLauncher = registerForActivityResult(albumActivityContract, result -> {
            if (!result.isEmpty()) {
                String mediaPath = AlbumExtKt.getAbsolutePath(result.get(0));
                processSelectedMedia(mediaPath);
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return LayoutInflater.from(getActivity()).inflate(R.layout.fragment_face_verify, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bvUpload = view.findViewById(R.id.bv_upload_face_verify);

        bvUpload.setOnClickListener(this);
        bvUpload.on();
        mCallback.faceVerifyOn(true);

    }

    public FaceVerifyFragment setCallback(IFaceVerifyCallback callback) {
        mCallback = callback;
        return this;
    }

    @Override
    public void onClick(View v) {
        if (CommonUtils.isFastClick()) {
            LogUtils.e("too fast click");
            return;
        }
        if (mCallback == null) return;
        int id = v.getId();
        if (id == R.id.bv_upload_face_verify) {
                chooseImg();

        }
    }

    private void chooseImg() {
        EOUtils.INSTANCE.getPermission().checkPermissions(requireActivity(), Scene.ALBUM, () -> {
            AlbumConfig config = new AlbumConfig();
            config.setAllEnable(false);
            config.setVideoEnable(false);
            config.setImageEnable(true);
            config.setMaxSelectCount(1);
            config.setShowGif(false);
            albumLauncher.launch(config);
            return null;
        }, strings -> {
            AlbumEntrance.INSTANCE.showAlbumPermissionTips(requireActivity());
            return null;
        });
    }

    private void processSelectedMedia(String imagePath){
        displayImage(imagePath);
    }

    private void displayImage(String imagePath) {
        Bitmap bitmap = BitmapUtils.decodeBitmapFromFile(imagePath, 800, 800);
        if (bitmap != null && !bitmap.isRecycled()) {
            mCallback.onPicChoose(bitmap);
        } else {
            ToastUtils.show("failed to get image");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mCallback.faceVerifyOn(false);
    }
}
