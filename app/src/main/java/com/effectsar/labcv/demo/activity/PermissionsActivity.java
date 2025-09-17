package com.effectsar.labcv.demo.activity;

import static android.Manifest.permission.READ_PHONE_STATE;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.RequiresApi;


public class PermissionsActivity extends Activity {
    public static final String PERMISSION_SUC_ACTIVITY = "permission_suc_activity";

    public static final String PERMISSION_REQUEST_LIST = "permission_request_list";
    // permissions
    public static final String PERMISSION_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
//    public static final String PERMISSION_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE;
    public static final String PERMISSION_MEDIA_VIDEO = Manifest.permission.READ_MEDIA_VIDEO;
    public static final String PERMISSION_MEDIA_IMAGES = Manifest.permission.READ_MEDIA_IMAGES;
    public static final String PERMISSION_AUDIO = Manifest.permission.RECORD_AUDIO;
    public static final String PERMISSION_CAMERA = Manifest.permission.CAMERA;

    public static final String PERMISSION_READ_PHONE_STATE = READ_PHONE_STATE;

    // permission code
    public static final int PERMISSION_CODE_STORAGE = 1;
    public static final int PERMISSION_CODE_CAMERA = 2;
    public static final int PERMISSION_CODE_AUDIO = 3;

    public static final int PERMISSION_CODE_LIST = 4;

    public static final int PERMISSION_READ_VIDEO = 5;

    public static final int PERMISSION_READ_IMAGES = 6;

    private String[] requestList = null;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        String requestListStr = getIntent().getStringExtra(PERMISSION_REQUEST_LIST);
        if (requestListStr != null && !requestListStr.isEmpty()) {
            requestList = requestListStr.split(";");
            checkPermission();
        } else {
            checkCameraPermission();
        }
    }

    private void checkPermission(){
        if (Build.VERSION.SDK_INT >= 23) {
            for (String requestPermission:requestList) {
                if (checkSelfPermission(requestPermission) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(requestList, PERMISSION_CODE_LIST);
                    return;
                }
            }
        }
        finish();
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (null == grantResults || grantResults.length < 1) return;
        if (requestCode == PERMISSION_CODE_CAMERA) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkStoragePermission();
            } else {
                Toast.makeText(this, "Camera权限被拒绝", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else if (requestCode == PERMISSION_CODE_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkMicrophonePermission();
            } else {
                Toast.makeText(this, "存储卡读写权限被拒绝", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else if (requestCode == PERMISSION_CODE_AUDIO) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startMainActivity();
            } else {
                Toast.makeText(this, "麦克风权限被拒绝", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == PERMISSION_READ_IMAGES) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    checkReadVideoPermission();
                }
            } else {
                Toast.makeText(this, "图片读取限被拒绝", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else if (requestCode == PERMISSION_READ_VIDEO) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkMicrophonePermission();
            } else {
                Toast.makeText(this, "视频读取限被拒绝", Toast.LENGTH_SHORT).show();
                finish();
            }

        } else if (requestCode == PERMISSION_CODE_LIST) {
            for (int index = 0; index < grantResults.length; index++) {
                if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, String.format("%s权限被拒绝", permissions[index]), Toast.LENGTH_SHORT).show();
                    break;
                }
            }
            finish();
        }
    }

    private void startMainActivity() {
        Class<?> sucActivity = (Class<?>) getIntent().getSerializableExtra(PERMISSION_SUC_ACTIVITY);
        Intent intent = new Intent(this, sucActivity);
        intent.putExtras(getIntent());
        startActivity(intent);
        finish();
        overridePendingTransition(0, 0);
    }

    private void checkCameraPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(PERMISSION_CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, PERMISSION_CODE_CAMERA);
            } else {
                checkStoragePermission();
            }
        } else {
            startMainActivity();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private void checkReadImagePermission() {
        if (checkSelfPermission(PERMISSION_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{PERMISSION_MEDIA_IMAGES},PERMISSION_READ_IMAGES);
        } else {
            checkReadVideoPermission();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private void checkReadVideoPermission() {
        if (checkSelfPermission(PERMISSION_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{PERMISSION_MEDIA_VIDEO},PERMISSION_READ_VIDEO);
        } else {
            checkMicrophonePermission();
        }
    }

    private void checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkReadImagePermission();
        } else if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(PERMISSION_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{PERMISSION_STORAGE},PERMISSION_CODE_STORAGE);
            } else {
                checkMicrophonePermission();
            }
        }

    }

    private void checkMicrophonePermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSION_CODE_AUDIO);
            } else {
                startMainActivity();
            }
        }
    }
}
