package com.effectsar.labcv.effect.task;

import static com.effectsar.labcv.effectsdk.EffectsSDKEffectConstants.EffectsSDKResultCode.BEF_RESULT_INVALID_LICENSE;
import static com.effectsar.labcv.effectsdk.EffectsSDKEffectConstants.EffectsSDKResultCode.BEF_RESULT_SUC;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import androidx.annotation.NonNull;

import com.effectsar.labcv.common.model.CaptureResult;
import com.effectsar.labcv.common.utils.BitmapUtils;
import com.effectsar.labcv.common.utils.ToastUtils;
import com.effectsar.labcv.core.algorithm.AlgorithmResourceHelper;
import com.effectsar.labcv.core.algorithm.FaceVerifyAlgorithmTask;
import com.effectsar.labcv.core.license.EffectLicenseHelper;
import com.effectsar.labcv.common.R;
import com.effectsar.labcv.effectsdk.BefFaceFeature;
import com.effectsar.labcv.effectsdk.EffectsSDKEffectConstants;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;

public class FaceVerifyThreadHandler extends Handler {
    public static final int VERIFY = 2001;
    public static final int SET_ORIGINAL = 2002;
    public static final int SUCCESS = 2003;
    public static final int FACE_DETECT = 2005;

    private static final String THREAD_NAME = "FAVE VERIFY THREAD";

    private HandlerThread mHandlerThread;
    private Context mContext;
    private FaceVerifyAlgorithmTask mFaceVerify;
    private BefFaceFeature mOriginalFeature;
    private boolean isRunning = false;
    private final Object mLock = new Object();

    private FaceVerifyThreadHandler(HandlerThread thread, WeakReference<Context> context) {
        super(thread.getLooper());
        mHandlerThread = thread;
        mContext = context.get();
        initVerify(mContext);
    }

    private void initVerify(@NonNull final  Context context) {
        mFaceVerify = new FaceVerifyAlgorithmTask(context, new AlgorithmResourceHelper(context), EffectLicenseHelper.getInstance(context));
        int ret = mFaceVerify.initTask();
        if (ret == BEF_RESULT_INVALID_LICENSE) {
            ToastUtils.show(mContext.getResources().getString(R.string.tab_face_verify) + mContext.getResources().getString(R.string.invalid_license_file));
        } else if (ret != BEF_RESULT_SUC) {
            ToastUtils.show("FaceVerify Initialization failed");
        }
    }

    public void resume() {
        isRunning = true;
    }

    void pause() {
        isRunning = false;
        synchronized (mLock) {
            mOriginalFeature = null;
        }
        removeCallbacksAndMessages(null);
    }

    public void quit() {
        isRunning = false;
        synchronized (mLock) {
            mOriginalFeature = null;
        }
        mHandlerThread.quit();
        removeCallbacksAndMessages(null);

        if (mFaceVerify != null) {
            mFaceVerify.destroyTask();
            mFaceVerify = null;
        }
    }

    @Override
    public void handleMessage(Message msg) {
        if (!isRunning || null == mFaceVerify) {
            return;
        }
        Messenger messenger = msg.replyTo;
        Message resultMsg = obtainMessage();
        switch (msg.what) {
            case SET_ORIGINAL:
                synchronized (mLock) {
                    ByteBuffer buffer = null;
                    BefFaceFeature mOriginalFeature = null;

                    Bitmap bitmap = (Bitmap) msg.obj;
                    if (bitmap != null) {
                        buffer = BitmapUtils.bitmap2ByteBuffer(bitmap);
                        mOriginalFeature =  mFaceVerify.extractFeature(buffer, EffectsSDKEffectConstants.PixlFormat.RGBA8888, bitmap.getWidth(), bitmap.getHeight(), 4 * bitmap.getWidth(), EffectsSDKEffectConstants.Rotation.CLOCKWISE_ROTATE_0);
                    }

                    resultMsg.what = FACE_DETECT;
                    resultMsg.arg1 = (mOriginalFeature == null ? 0 : mOriginalFeature.getValidFaceNum());
                    resultMsg.obj = new CaptureResult(buffer, bitmap.getWidth(),bitmap.getHeight());
                    try {
                        messenger.send(resultMsg);
                    } catch (RemoteException ex) {
                        ex.printStackTrace();
                    }
                }
                break;
        }
    }

    private BefFaceFeature getOriginalFaceFeature(Bitmap mOriginalBitmap) {
        if (mOriginalBitmap != null) {
            ByteBuffer buffer = BitmapUtils.bitmap2ByteBuffer(mOriginalBitmap);
            return mFaceVerify.extractFeature(buffer, EffectsSDKEffectConstants.PixlFormat.RGBA8888, mOriginalBitmap.getWidth(), mOriginalBitmap.getHeight(), 4 * mOriginalBitmap.getWidth(), EffectsSDKEffectConstants.Rotation.CLOCKWISE_ROTATE_0);
        }
        return null;

    }

    public static FaceVerifyThreadHandler createFaceVerifyHandlerThread(final Context context) {
        HandlerThread thread = new HandlerThread(THREAD_NAME);
        thread.start();
        return new FaceVerifyThreadHandler(thread, new WeakReference<>(context));
    }
}
