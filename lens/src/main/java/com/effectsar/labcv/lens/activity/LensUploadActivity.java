package com.effectsar.labcv.lens.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import com.effectsar.labcv.common.utils.ToastUtils;
import com.effectsar.labcv.core.util.LogUtils;
import com.effectsar.labcv.effectsdk.EffectsSDKEffectConstants;
import com.effectsar.labcv.lens.R;
import com.effectsar.labcv.lens.config.ImageQualityConfig;
import com.google.gson.Gson;
import com.volcengine.ck.LocalAlbumActivityContract;
import com.volcengine.ck.album.AlbumEntrance;
import com.volcengine.ck.album.base.AlbumConfig;
import com.volcengine.ck.album.utils.AlbumExtKt;
import com.volcengine.effectone.permission.Scene;
import com.volcengine.effectone.singleton.AppSingleton;
import com.volcengine.effectone.utils.EOUtils;

public class LensUploadActivity extends AppCompatActivity implements View.OnClickListener {
    private RelativeLayout uploadRl;
    private ImageView backView;
    private ImageQualityConfig mConfig;

    private final LocalAlbumActivityContract albumActivityContract = new LocalAlbumActivityContract();
    private ActivityResultLauncher<AlbumConfig> albumLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(Color.WHITE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(decor.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        setContentView(R.layout.activity_lens_upload);
        initView();

        albumLauncher = registerForActivityResult(albumActivityContract, result -> {
            if (!result.isEmpty()) {
                String mediaPath = AlbumExtKt.getAbsolutePath(result.get(0));
                processSelectedMedia(mediaPath);
            }
        });
    }

    private ImageQualityConfig parseConfig(Intent intent) {
        String sConfig = intent.getStringExtra(ImageQualityConfig.IMAGE_QUALITY_KEY);
        if (sConfig == null) {
            return null;
        }
        LogUtils.d("imagequlity config ="+sConfig);
        return new Gson().fromJson(sConfig, ImageQualityConfig.class);
    }

    private void initView() {
        backView = findViewById(R.id.iv_back_main_view);
        backView.setOnClickListener(this);

        uploadRl = findViewById(R.id.ll_len_upload_video);
        uploadRl.setOnClickListener(this);

        mConfig = parseConfig(getIntent());
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_back_main_view) {
            finish();
        } else if (v.getId() == R.id.ll_len_upload_video) {
            startChoosePic();
        }
    }

    protected void startChoosePic() {
        EOUtils.INSTANCE.getPermission().checkPermissions(this, Scene.ALBUM, () -> {
            AlbumConfig config = new AlbumConfig();
            config.setAllEnable(false);
            config.setVideoEnable(true);
            config.setImageEnable(false);
            config.setMaxSelectCount(1);
            config.setShowGif(false);
            albumLauncher.launch(config);
            return null;
        }, strings -> {
            AlbumEntrance.INSTANCE.showAlbumPermissionTips(LensUploadActivity.this);
            return null;
        });
    }

    private void processSelectedMedia(String videoPath){
        LogUtils.e("Video Path:" + videoPath);
        if (!videoPath.endsWith(".mp4") && !videoPath.endsWith(".mov"))  {
            ToastUtils.show(getString(R.string.video_format_not_support));
            return ;
        }
        if (mConfig.getKey().equals(ImageQualityConfig.KEY_VFI)) {
            ImageQualityPostProcessActivity.startActivity(this,
                    EffectsSDKEffectConstants.ImageQualityPostProcessType.IMAGE_QUALITY_POST_PROCESS_TYPE_VFI,
                    videoPath);
        } else if (mConfig.getKey().equals(ImageQualityConfig.KEY_VIDEO_STAB)) {
            ImageQualityPostProcessActivity.startActivity(this,
                    EffectsSDKEffectConstants.ImageQualityPostProcessType.IMAGE_QUALITY_POST_PROCESS_TYPE_VIDEO_STAB,
                    videoPath);
        } else if(mConfig.getKey().equals(ImageQualityConfig.KEY_VIDEO_DEFLICKER)) {
            ImageQualityPostProcessActivity.startActivity(this,
                    EffectsSDKEffectConstants.ImageQualityPostProcessType.IMAGE_QUALITY_POST_PROCESS_TYPE_VIDEO_DEFLICKER,
                    videoPath);
        }
    }
}