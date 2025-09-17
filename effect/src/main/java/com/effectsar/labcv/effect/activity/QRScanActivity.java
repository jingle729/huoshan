package com.effectsar.labcv.effect.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.effectsar.labcv.common.base.BaseGLActivity;
import com.effectsar.labcv.common.imgsrc.bitmap.BitmapSourceImpl;
import com.effectsar.labcv.common.imgsrc.camera.CameraSourceImpl;
import com.effectsar.labcv.common.model.BubbleConfig;
import com.effectsar.labcv.common.model.CaptureResult;
import com.effectsar.labcv.common.model.ProcessInput;
import com.effectsar.labcv.common.model.ProcessOutput;
import com.effectsar.labcv.common.utils.BitmapUtils;
import com.effectsar.labcv.common.utils.DensityUtils;
import com.effectsar.labcv.common.utils.ToastUtils;
import com.effectsar.labcv.common.view.bubble.BubbleWindowManager;
import com.effectsar.labcv.core.effect.EffectManager;
import com.effectsar.labcv.core.effect.EffectResourceHelper;
import com.effectsar.labcv.core.license.EffectLicenseHelper;
import com.effectsar.labcv.core.util.ImageUtil;
import com.effectsar.labcv.core.util.LogUtils;
import com.effectsar.labcv.effect.R;
import com.effectsar.labcv.effect.fragment.SelectUploadFragment;
import com.effectsar.labcv.common.gesture.GestureManager;
import com.effectsar.labcv.effect.model.SelectUploadItem;
import com.effectsar.labcv.effect.qrscan.EncryptResult;
import com.effectsar.labcv.effect.qrscan.QRResourceInfo;
import com.effectsar.labcv.effect.qrscan.QRScanResourceFinder;
import com.effectsar.labcv.effect.task.DownloadResourceTask;
import com.effectsar.labcv.effect.task.FaceVerifyThreadHandler;
import com.effectsar.labcv.effect.view.ProgressBar;
import com.effectsar.labcv.effect.view.ViewfinderView;
import com.effectsar.labcv.effectsdk.EffectsSDKEffectConstants;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.volcengine.ck.album.AlbumEntrance;
import com.volcengine.ck.album.base.AlbumConfig;
import com.volcengine.ck.album.utils.AlbumExtKt;
import com.volcengine.ebox.loader.EBoxSDKManager;
import com.volcengine.effectone.permission.Scene;
import com.volcengine.effectone.utils.EOUtils;

import java.lang.ref.WeakReference;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class QRScanActivity extends BaseGLActivity
    implements DownloadResourceTask.DownloadResourceTaskCallback,
               ProgressBar.OnProgressChangedListener,
               SelectUploadFragment.ISelectUploadCallback,
               GestureManager.OnTouchListener {
  private final boolean mIsZh = Locale.getDefault().getLanguage() == "zh";

  private ViewfinderView vfvQRScan;
  private ProgressDialog pdDownload;
  private TextView tvScanTip;

  protected GestureManager mGestureManager;
  private EffectManager mManager;
  private QRScanResourceFinder mResourceFinder = null;
  private CameraSourceImpl mCameraSource;
  private boolean mShowingQRScan = true;
  private DownloadResourceTask mDownloadTask;
  private DownloadResourceTask.ResourceType mCurrentResourceType;

  private String mLastDownloadParam = null;
  private AlertDialog mVersionCheckDialog = null;

  private ProgressBar pb;

  private ImageView mImgRotate;
  private ImageView mImgPhoto;
  private ImageView mImgSetting;
  private ImageView mImgRecord;
  private TextView mTitleText;
  private String mHint = "";
  private long mCameras = 0;

  //   {zh} 顶部弹出的设置气泡窗口管理类       {en} Top pop-up settings bubble window management class
  protected BubbleWindowManager mBubbleWindowManager;
  //   {zh} 顶部弹出的气泡窗口设置值封装       {en} Top pop-up bubble window setting value encapsulation
  protected BubbleConfig mBubbleConfig;

  //   {zh} 性能数据展示相关       {en} Performance data display related
  private TextView mTvFps;
  private TextView mTimeCost;
  private TextView mInputSize;
  private RelativeLayout mRlPerformance;
  //   {zh} 自定义Handler       {en} Custom Handler
  private InnerHandler mHandler = null;
  private static final int UPDATE_INFO = 1;
  private static final int UPDATE_INFO_INTERVAL = 1000;

  //  {zh} 上传贴纸所用成员  {en} Members used to upload stickers
  private SelectUploadFragment mSelectUploadFragment;
  public static final int SELECT_UPLOAD = R.drawable.ic_select_upload;
  private Messenger mMessenger;
  private FaceVerifyThreadHandler mThreadHandler;
  private Handler mFaceHandler;
  private String mSelectedItemKey = "";
  public static final int ANIMATION_DURATION = 400;
  public static final String FRAGMENT_SELECT_UPLOAD = "fragment_select_upload";

  private ActivityResultLauncher<AlbumConfig> uploadSelectedLauncher;

  private Boolean mIsVideoLandscape = false;
  protected ImageUtil mQRScanImageUtil;
  private final Timer mCheckReadyTimer = new Timer();

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    LayoutInflater.from(this).inflate(R.layout.activity_qrscan,
                                      findViewById(R.id.fl_container), true);
    LayoutInflater.from(this).inflate(R.layout.layout_select_upload,
                                      findViewById(R.id.fl_container), true);
    mBubbleWindowManager = new BubbleWindowManager(this);
    mBubbleConfig = mBubbleWindowManager.getConfig();
    mHandler = new InnerHandler(this);
    mFaceHandler = new Handler(Looper.getMainLooper()) {
      @Override
      public void handleMessage(Message msg) {
        super.handleMessage(msg);
        int what = msg.what;
        switch (what) {
        case FaceVerifyThreadHandler.FACE_DETECT:
          //   {zh} 上传的图片中有且仅有一张才开始设置渲染缓存       {en} Only
          //   one of the uploaded images was detected
          // start detect when only one of the uploaded images is detected
          if (msg.arg1 == 1) {
            //                             {zh}
            //                              {zh} ToastUtils.show("人脸检测完成！开始setRenderCache");                              {en} ToastUtils.show ("face detection complete! Start setRenderCache");
            //                             {en} ToastU tils.show ("face
            //                             detection complete! start
            //                             setRenderCache");
            setRenderCacheTexture((CaptureResult)msg.obj);

          } else {
            runOnUiThread(new Runnable() {
              @Override
              public void run() {
                ToastUtils.show(
                    getResources().getString(R.string.no_face_detected));
              }
            });
            LogUtils.e(
                "the  bitmap uploaded contains no face or more than one face!!");
          }
          break;
        }
      }
    };
    initView();
    initManager();
    mQRScanImageUtil = new ImageUtil();
    if (mImageSourceProvider instanceof CameraSourceImpl) {
      mCameraSource = (CameraSourceImpl)mImageSourceProvider;
      mCameraSource.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
    } else if (mImageSourceProvider instanceof BitmapSourceImpl) {
      Bitmap bitmap = prepareBitmap(mImageSourceConfig.getMedia(),
                                    mImageSourceConfig.getRequestWidth(),
                                    mImageSourceConfig.getRequestHeight());
      if (bitmap != null && !bitmap.isRecycled()) {
        mImageSourceProvider.open(bitmap, null);
        //  {zh} 开始循环取帧  {en} Start loop fetch frame
        mSurfaceView.requestRender();
        onQRScanStopAndSetSticker();
        mFilePath = getIntent().getStringExtra("mFilePath_key");
        if (!mFilePath.equals("")) {
          mManager.setStickerAbs(mFilePath);
        }
        mImgKey = getIntent().getStringExtra("mImgKey");
        if (!mImgKey.equals("")) {
          mSelectedItemKey = mImgKey;
          showSelectUploadFragment(true);
        }
      }

    } else {
      throw new IllegalStateException(
          "QR scan activity must run in camera mode");
    }
    uploadSelectedLauncher = registerForActivityResult(albumActivityContract, result -> {
      if (!result.isEmpty()) {
        String mediaPath = AlbumExtKt.getAbsolutePath(result.get(0));
        checkFaceContained(mediaPath);
      }
    });
  }

  private void initView() {
    vfvQRScan = findViewById(R.id.vfv_qr_scan);
    mTitleText = findViewById(R.id.title_text);
    tvScanTip = findViewById(R.id.tv_qr_scan_tip);
    findViewById(R.id.img_back).setOnClickListener((view) -> { finish(); });
    mImgRotate = findViewById(R.id.img_rotate);
    mImgRotate.setOnClickListener((view) -> {
      if (mImageSourceProvider instanceof CameraSourceImpl) {
        int cameraId =
            1 - ((CameraSourceImpl)mImageSourceProvider).getCameraID();
        mImageSourceConfig.setMedia(String.valueOf(cameraId));
        ((CameraSourceImpl)mImageSourceProvider).changeCamera(cameraId, null);
      }
    });
    mImgRotate.setVisibility(View.INVISIBLE);

    mImgPhoto = findViewById(R.id.img_photo);
    mImgPhoto.setVisibility(View.GONE);
    mImgPhoto.setOnClickListener((view) -> { startChoosePic(); });

    mImgSetting = findViewById(R.id.img_setting);
    mImgSetting.setVisibility(View.INVISIBLE);
    mImgSetting.setOnClickListener((view) -> {
      mBubbleWindowManager.hideResolutionOption(view, 480);
      ArrayList<BubbleWindowManager.ITEM_TYPE> item_types = new ArrayList<>();
      item_types.add(BubbleWindowManager.ITEM_TYPE.RESOLUTION);
      item_types.add(BubbleWindowManager.ITEM_TYPE.VIDEO_SCALE);
      mBubbleWindowManager.show(mImgSetting, new BubbleWindowManager.BubbleCallback() {
        @Override
        public void onBeautyDefaultChanged(boolean on) {}
        @Override
        public void onResolutionChanged(int width, int height) {
          if (mImageSourceProvider instanceof CameraSourceImpl){
            // config mImageSourceConfig so that resolution info could be saved
            // when app is running in the background.
            int scaleMaxSide = Math.max(mBubbleConfig.getVideoScale().x, mBubbleConfig.getVideoScale().y);
            int scaleMinSide = Math.min(mBubbleConfig.getVideoScale().x, mBubbleConfig.getVideoScale().y);
            width = height / scaleMinSide * scaleMaxSide;
            mImageSourceConfig.setRequestWidth(width);
            mImageSourceConfig.setRequestHeight(height);
            ((CameraSourceImpl)mImageSourceProvider).setPreferSize(width,height);
            ((CameraSourceImpl)mImageSourceProvider).changeCamera(((CameraSourceImpl)mImageSourceProvider).getCameraID(),null);
          }
        }
        @Override
        public void onVideoScaleChanged(int scaleX, int scaleY) {
          //切换分辨率比例
          int height = mBubbleConfig.getResolution().y;
          int scaleMaxSide = Math.max(scaleX, scaleY);
          int scaleMinSide = Math.min(scaleX, scaleY);
          int width = height / scaleMinSide * scaleMaxSide;
          mImageSourceConfig.setRequestWidth(width);
          mImageSourceConfig.setRequestHeight(height);
          ((CameraSourceImpl)mImageSourceProvider).setPreferSize(width,height);
          ((CameraSourceImpl)mImageSourceProvider).changeCamera(((CameraSourceImpl)mImageSourceProvider).getCameraID(),null);
          //切换横竖屏
          if (scaleX == 4 || scaleX == 16){
            if (mIsVideoLandscape) {
              mIsVideoLandscape = false;
              ToastUtils.show(getString(R.string.feature_rotate_90), R.drawable.ic_phone_rotote_90);
            }
          } else if (scaleX == 9) {
            if (!mIsVideoLandscape) {
              mIsVideoLandscape = true;
              ToastUtils.show(getString(R.string.feature_rotate_90), R.drawable.ic_phone_rotote_270);
            }
          }
        }
        @Override
        public void onPerformanceChanged(boolean on) {}

        @Override
        public void onPictureModeChanged(boolean on) {}

      }, item_types.toArray(new BubbleWindowManager.ITEM_TYPE[item_types.size()]));
    });

    mImgRecord = findViewById(R.id.iv_record);
    mImgRecord.setVisibility(View.INVISIBLE);
    mImgRecord.setOnClickListener((view) -> {
      mManager.resetSticker();
    });

    pb = findViewById(R.id.pb_qr_scan);
    pb.setOnProgressChangedListener(this);

    findViewById(R.id.glview).setOnTouchListener(new View.OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        if (mGestureManager == null) {
          mGestureManager =
              new GestureManager(getApplicationContext(), QRScanActivity.this);
        }
        return mGestureManager.onTouchEvent(event);
      }
    });

    //    {zh} 设置性能的展示区        {en} Set performance display area
    mInputSize = findViewById(R.id.tv_resolution);
    mTvFps = findViewById(R.id.tv_fps);
    mTimeCost = findViewById(R.id.tv_time);
    mRlPerformance = findViewById(R.id.rl_performance);
    mRlPerformance.setVisibility(View.INVISIBLE);
    /*onQRScanStop();
    hideProgress();
    mImgSetting.setVisibility(View.VISIBLE);
    mImgRotate.setVisibility(View.VISIBLE);*/
  }

  double frameCount = 0;
  @Override
  public void onDrawFrame(GL10 gl10) {
    long start = System.currentTimeMillis();
    super.onDrawFrame(gl10);
    long cost = System.currentTimeMillis() - start;
    if (frameCount++ % 10 == 0) {
      runOnUiThread(() -> {
        mTimeCost.setText(cost + "ms");
        mInputSize.setText(mTextureHeight + "*" + mTextureWidth);
      });
    }
  }

  @Override
  public void onUploadSelected(SelectUploadItem buttonItem, int position) {
    if (buttonItem.getIcon() == SELECT_UPLOAD) {
      EOUtils.INSTANCE.getPermission().checkPermissions(this, Scene.ALBUM, () -> {
        AlbumConfig config = new AlbumConfig();
        config.setAllEnable(false);
        config.setVideoEnable(false);
        config.setImageEnable(true);
        config.setMaxSelectCount(1);
        config.setShowGif(false);
        uploadSelectedLauncher.launch(config);
        return null;
      }, strings -> {
        AlbumEntrance.INSTANCE.showAlbumPermissionTips(QRScanActivity.this);
        return null;
      });
    } else {
      setRenderCacheTexture(buttonItem.getIcon());
    }
  }
  public boolean setRenderCacheTexture(@DrawableRes int id) {
    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), id);
    if (bitmap != null) {
      ByteBuffer buffer = BitmapUtils.bitmap2ByteBuffer(bitmap);
      if (!mManager.setRenderCacheTexture(
              mSelectedItemKey, buffer, bitmap.getWidth(), bitmap.getHeight(),
              4 * bitmap.getWidth(), EffectsSDKEffectConstants.PixlFormat.RGBA8888,
              EffectsSDKEffectConstants.Rotation.CLOCKWISE_ROTATE_0)) {
        LogUtils.e("setRenderCacheTexture fail!!");
        return false;
      }
      return true;
    }
    return false;
  }

  public boolean setRenderCacheTexture(CaptureResult captureResult) {
    if (captureResult == null) {
      LogUtils.e("decodeByteBuffer return null!!");
      return false;
    }
    if (!mManager.setRenderCacheTexture(
            mSelectedItemKey, captureResult.getByteBuffer(),
            captureResult.getWidth(), captureResult.getHeight(),
            4 * captureResult.getWidth(),
            EffectsSDKEffectConstants.PixlFormat.RGBA8888,
            EffectsSDKEffectConstants.Rotation.CLOCKWISE_ROTATE_0)) {
      LogUtils.e("setRenderCacheTexture fail!!");
      return false;
    }
    return true;
  }

  public void setSelectUploadFragmentHeight(float y, int duration) {
    int boardLayoutID = getResources().getIdentifier("fl_select_upload", "id",
                                                     getPackageName());
    View selectUploadView = getWindow().findViewById(boardLayoutID);
    selectUploadView.setY(y);
  }

  private void showSelectUploadFragment(boolean bool) {
    // create SelectUploadFragment, ThreadHandler, Messenger if empty
    if (!bool){
      return;
    }
    List<SelectUploadItem> list = new ArrayList<SelectUploadItem>() {
      {
        add(new SelectUploadItem(SELECT_UPLOAD));
        add(new SelectUploadItem(R.drawable.ic_qinglv_0));
        add(new SelectUploadItem(R.drawable.ic_qinglv_1));
        add(new SelectUploadItem(R.drawable.ic_qinglv_2));
      }
    };
    if (mSelectUploadFragment == null) {
      mSelectUploadFragment =
          SelectUploadFragment.newInstance(list).setUploadSelectedCallback(
              this);
    } else {
      mSelectUploadFragment.updateItem(list);
    }

    // set SelectUpload position height
    float height =
        getResources().getDisplayMetrics().heightPixels -
        getResources().getDimensionPixelSize(R.dimen.height_board_bottom) +
        getResources().getDimensionPixelSize(R.dimen.board_bottom_padding) -
        DensityUtils.dp2px(mContext, 64 + 6);
    if (bool) {
      height = height / 3.8f;
    }
    setSelectUploadFragmentHeight(height, ANIMATION_DURATION);
    showFragment(mSelectUploadFragment, R.id.fl_select_upload,
                 FRAGMENT_SELECT_UPLOAD);

    if (mThreadHandler == null) {
      mThreadHandler =
          FaceVerifyThreadHandler.createFaceVerifyHandlerThread(mContext);
    }
    if (mMessenger == null) {
      mMessenger = new Messenger(mFaceHandler);
    }
    mThreadHandler.resume();
  }

  protected void showFragment(Fragment fragment, int layoutId, String tag) {
    FragmentManager fm = getSupportFragmentManager();
    FragmentTransaction ft = fm.beginTransaction();
    Fragment existFragment = fm.findFragmentByTag(tag);

    if (existFragment == null) {
      ft.add(layoutId, fragment, tag).show(fragment).commitNow();
    } else {
      ft.show(fragment).commitNow();
    }
  }

  public void checkFaceContained(String imagePath) {
    Bitmap bitmap = BitmapUtils.decodeBitmapFromFile(imagePath, 800, 800);
    if (bitmap != null && !bitmap.isRecycled()) {
      Message msg = mThreadHandler.obtainMessage(
          FaceVerifyThreadHandler.SET_ORIGINAL, bitmap);
      msg.replyTo = mMessenger;
      msg.sendToTarget();
    } else {
      ToastUtils.show("failed to get image");
    }
  }

  protected static class InnerHandler extends Handler {
    private final WeakReference<QRScanActivity> mActivity;

    public InnerHandler(QRScanActivity activity) {
      mActivity = new WeakReference<>(activity);
    }

    @Override
    public void handleMessage(Message msg) {
      QRScanActivity activity = mActivity.get();
      if (activity != null) {
        switch (msg.what) {
        case UPDATE_INFO:
          activity.mTvFps.setText("" + activity.mFrameRator.getFrameRate());
          sendEmptyMessageDelayed(UPDATE_INFO, UPDATE_INFO_INTERVAL);
          break;
        }
      }
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    mHandler.sendEmptyMessageDelayed(UPDATE_INFO, UPDATE_INFO_INTERVAL);
    checkAndSetTextViewMargin();
  }

  @Override
  protected void onStop() {
    super.onStop();

    if (mDownloadTask == null ||
        mDownloadTask.getStatus() != AsyncTask.Status.FINISHED) {
      return;
    }

    mDownloadTask.cancel(true);

    hideProgress();
  }

  private void checkAndSetTextViewMargin() {
    new Handler().postDelayed(new Runnable() {
      @Override
      public void run() {
        Rect maskRect = vfvQRScan.getScreenRect();
        if (maskRect == null) {
          checkAndSetTextViewMargin();
          return;
        }
        ViewGroup.MarginLayoutParams lp =
            (ViewGroup.MarginLayoutParams)tvScanTip.getLayoutParams();
        lp.topMargin = (int)(vfvQRScan.getTop() + maskRect.bottom +
                             DensityUtils.dp2px(getApplicationContext(), 16));
        tvScanTip.setLayoutParams(lp);
      }
    }, 0);
  }

  private void showEffectRender() {
    if (mCameras == 1) {
      mCameraSource.changeCamera(1, null);
    } else if (mCameras == 2) {
      mCameraSource.changeCamera(0, null);
    }
    new Handler().postDelayed(new Runnable() {
      public void run() {
        if (!mHint.isEmpty() && mHint != null) {
          ToastUtils.show(mHint);
        }
        if (mImgKey != "" && mImgKey != null) {
          showSelectUploadFragment(false);
        }
        hideProgress();
      }
    }, 400);

    int cameraId = 1 - mCameraSource.getCameraID();
    mImageSourceConfig.setMedia(String.valueOf(cameraId));
    mCameraSource.setPreferSize(1280, 720);
    mCameraSource.changeCamera(cameraId, null);

    mImgRotate.setVisibility(View.VISIBLE);
    mImgSetting.setVisibility(View.VISIBLE);
    mImgRecord.setVisibility(View.VISIBLE);
    mRlPerformance.setVisibility(View.VISIBLE);
    mTitleText.setVisibility(View.INVISIBLE);
  }
  private void initManager() {
    EffectResourceHelper resourceHelper = new EffectResourceHelper(this);
    mManager = new EffectManager(this, resourceHelper,
                                 EffectLicenseHelper.getInstance(this));
    if (mIsZh) {
      mResourceFinder = new QRScanResourceFinder(EBoxSDKManager.resourceConfig.getModelDir());
    }
  }

  @Override
  public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
    super.onSurfaceCreated(gl10, eglConfig);
    if (mResourceFinder != null) {
      mManager.init(mResourceFinder);
    } else {
      mManager.init();
    }
    mManager.recoverStatus();
  }

  @Override
  public void onPause() {
    super.onPause();
    mHandler.removeCallbacksAndMessages(null);
    mSurfaceView.queueEvent(new Runnable() {
      @Override
      public void run() {
        if (mResourceFinder != null)
          mResourceFinder.shutdown();

        mCheckReadyTimer.cancel();
        mManager.destroy();
        mQRScanImageUtil.release();
      }
    });
  }

  @Override
  public ProcessOutput processImpl(ProcessInput input) {
    if (mShowingQRScan) {
      doQRScan(input);
    }
    if (mIsVideoLandscape) {
      //旋转90度
      int width = input.getHeight();
      int height = input.getWidth();
      ImageUtil.Transition transition = new ImageUtil.Transition().rotate(90).flip(false, false);
      int inputTexture = mQRScanImageUtil.transferTextureToTexture(input.getTexture(), EffectsSDKEffectConstants.TextureFormat.Texure2D, EffectsSDKEffectConstants.TextureFormat.Texure2D,
              input.getWidth(), input.getHeight(), transition);

      input.setWidth(width);
      input.setHeight(height);
      input.setTexture(inputTexture);
    }

    {
      int dstTexture =
              mImageUtil.prepareTexture(input.getWidth(), input.getHeight());
      long timestamp = System.nanoTime();
      mManager.setCameraPosition(mImageSourceProvider.isFront());
      if (mManager.process(input.getTexture(), dstTexture, input.getWidth(),
              input.getHeight(), EffectsSDKEffectConstants.Rotation.CLOCKWISE_ROTATE_0, timestamp)) {
        output.texture = dstTexture;
      } else {
        output.texture = input.getTexture();
      }
      output.width = input.getWidth();
      output.height = input.getHeight();
    }

    if (mIsVideoLandscape) {
      //恢复角度
      ImageUtil.Transition transition = new ImageUtil.Transition().rotate(270).flip(false, false);
      output.texture = mQRScanImageUtil.transferTextureToTexture(output.getTexture(), EffectsSDKEffectConstants.TextureFormat.Texure2D, EffectsSDKEffectConstants.TextureFormat.Texure2D,
              output.getWidth(), output.getHeight(), transition);
      output.width = input.getHeight();
      output.height = input.getWidth();

    }
    return output;
  }

  private void doQRScan(ProcessInput input) {
    ByteBuffer buffer = mImageUtil.transferTextureToBuffer(
        input.getTexture(), EffectsSDKEffectConstants.TextureFormat.Texure2D,
        EffectsSDKEffectConstants.PixlFormat.RGBA8888, input.getWidth(),
        input.getHeight(), 1);

    int width = input.getWidth();
    int height = input.getHeight();
    int top = height / 3;
    int left = (width - top) / 2;
    IntBuffer intBuffer;
    try {
      intBuffer = IntBuffer.allocate(width * height).put(buffer.asIntBuffer());
    } catch (BufferOverflowException e) {
      e.printStackTrace();
      return;
    }
    LuminanceSource source =
        new RGBLuminanceSource(width, height, intBuffer.array())
            .crop(left, top, height / 3, height / 3);
    BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
    QRCodeReader reader = new QRCodeReader();
    try {
      Result result = reader.decode(bitmap);
      mLastDownloadParam = result.getText();
      mDownloadTask = new DownloadResourceTask(QRScanActivity.this);
      mDownloadTask.execute(mLastDownloadParam);
      // hide qr scan view once decoding succeed
      onQRScanStop();
    } catch (NotFoundException | ChecksumException | FormatException e) {
      e.printStackTrace();
    }
  }

  private void onQRScanStopAndSetSticker() {
    mShowingQRScan = false;
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        mImgPhoto.setVisibility(View.GONE);
        vfvQRScan.setVisibility(View.GONE);
        tvScanTip.setVisibility(View.GONE);
      }
    });
  }

  private void onQRScanStop() {
    mShowingQRScan = false;
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        vfvQRScan.setVisibility(View.GONE);
        tvScanTip.setVisibility(View.GONE);
        showProgress();
      }
    });
  }

  @Override
  public void onResult(EncryptResult Result) {
    mHint = Result.data.hint;
    mCameras = Result.data.cameras;
  }

  @Override
  public void onQRScanData(QRResourceInfo resourceInfo) {
    mImgKey = resourceInfo.imgK;
    mSelectedItemKey = mImgKey;
  }

  @Override
  public void onSuccess(String path,
                        DownloadResourceTask.ResourceType resourceType) {

    mCurrentResourceType = resourceType;
    mFilePath = path;
    mTitleText.setOnLongClickListener((view) -> {
      mImgPhoto.setVisibility(View.VISIBLE);
      mTitleText.setOnLongClickListener(null);
      return true;
    });

    if (resourceType == DownloadResourceTask.ResourceType.STICKER) {
      mManager.setStickerAbs(path);
    } else if (resourceType == DownloadResourceTask.ResourceType.FILTER) {
      mManager.setFilterAbs(path);
      mManager.updateFilterIntensity(0.8f);
      pb.setVisibility(View.VISIBLE);
      pb.setProgress(0.8f);
    }

    if (mResourceFinder != null && !mResourceFinder.isSyncDownload) {
      //wait model ready
      mCheckReadyTimer.schedule(new TimerTask() {
        @Override
        public void run() {
          //wait check model ready
          QRScanResourceFinder.DownloadStatus status = mResourceFinder.checkModelReady();
          if (QRScanResourceFinder.DownloadStatus.BEGIN != status) {
            runOnUiThread(() -> showEffectRender());
            mCheckReadyTimer.cancel();
            if (QRScanResourceFinder.DownloadStatus.FAILED == status) {
              List<String> failedModelNames = mResourceFinder.getFailedModelNames();
              String modelNames = "";
              for (String modelName : failedModelNames) {
                modelNames += modelName + ",";
              }
              ToastUtils.show("This sticker has unsupported models: " + modelNames);
            }
          }
        }
      }, 200, 200);
    } else {
      if (mResourceFinder != null) {
        mResourceFinder.setDownloadModelCallback(new QRScanResourceFinder.IDownModelCallback() {
          @Override
          public void onFailed(String modelName) {
            ToastUtils.show("This sticker has unsupported models: " + modelName);
          }
        });
        new Handler().postDelayed(() -> showEffectRender(), 2000);
      } else {
        showEffectRender();
      }
    }
  }

  @Override
  public void onFail(int errorCode, String message) {
    hideProgress();
    if (errorCode == ERROR_CODE_VERSION_NOT_MATCH) {
      if (mVersionCheckDialog == null) {
        mVersionCheckDialog =
            new AlertDialog.Builder(this, R.style.VersionCheckDialogTheme)
                .setTitle(R.string.question_ignore_version_not_match)
                .setPositiveButton(
                    R.string.button_yes,
                    new DialogInterface.OnClickListener() {
                      @Override
                      public void onClick(DialogInterface dialog, int which) {
                        showProgress();
                        mDownloadTask =
                            new DownloadResourceTask(QRScanActivity.this, true);
                        mDownloadTask.execute(mLastDownloadParam);
                      }
                    })
                .setNegativeButton(R.string.button_no,
                                   new DialogInterface.OnClickListener() {
                                     @Override
                                     public void onClick(DialogInterface dialog,
                                                         int which) {
                                       finish();
                                     }
                                   })
                .setCancelable(false)
                .create();
      }
      mVersionCheckDialog.show();
    } else {
      ToastUtils.show(message);
    }
  }

  @Override
  public void onProgressUpdate(float progress) {
    if (pdDownload == null)
      return;
    pdDownload.setProgress((int)(progress * 100));
  }

  @Override
  public String getAppVersionName() {
    try {
      return getPackageManager()
          .getPackageInfo(getPackageName(), 0)
          .versionName;
    } catch (PackageManager.NameNotFoundException e) {
      return null;
    }
  }

  private void showProgress() {
    if (pdDownload == null) {
      pdDownload = new ProgressDialog(this, R.style.ProgressTheme);
      pdDownload.setTitle(R.string.resource_download_progress);
      pdDownload.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
      pdDownload.setCancelable(false);
    }
    pdDownload.show();
    pdDownload.setProgress(0);
  }

  private void hideProgress() {
    if (pdDownload == null)
      return;
    pdDownload.dismiss();
  }

  @Override
  public void onProgressChanged(ProgressBar progressBar, float progress,
                                boolean isFormUser) {
    if (mCurrentResourceType == DownloadResourceTask.ResourceType.FILTER) {
      mManager.updateFilterIntensity(progress);
    }
  }

  @Override
  public void onProgressEnd(ProgressBar progressBar, float progress,
                            boolean isFormUser) {}

  @Override
  public void onTouchEvent(EffectsSDKEffectConstants.TouchEventCode eventCode,
                           float x, float y, float force, float majorRadius,
                           int pointerId, int pointerCount) {
    mSurfaceView.queueEvent(new Runnable() {
      @Override
      public void run() {
        mManager.processTouch(eventCode, x, y, force, majorRadius, pointerId,
                              pointerCount);
      }
    });
  }

  @Override
  public void onGestureEvent(EffectsSDKEffectConstants.GestureEventCode eventCode,
                             float x, float y, float dx, float dy,
                             float factor) {
    mSurfaceView.queueEvent(new Runnable() {
      @Override
      public void run() {
        mManager.processGesture(eventCode, x, y, dx, dy, factor);
      }
    });
  }
}
