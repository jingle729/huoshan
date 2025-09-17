package com.effectsar.labcv.effect.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.IdRes;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.bef.effectsdk.message.MessageCenter;
import com.effectsar.labcv.common.base.BaseBarGLActivity;
import com.effectsar.labcv.common.database.LocalParamHelper;
import com.effectsar.labcv.common.imgsrc.camera.CameraSourceImpl;
import com.effectsar.labcv.common.model.EffectType;
import com.effectsar.labcv.common.model.ProcessInput;
import com.effectsar.labcv.common.model.ProcessOutput;
import com.effectsar.labcv.common.utils.LocaleUtils;
import com.effectsar.labcv.common.view.bubble.BubbleWindowManager;
import com.effectsar.labcv.common.database.LocalParamManager;
import com.effectsar.labcv.core.license.EffectLicenseHelper;
import com.effectsar.labcv.core.effect.EffectManager;
import com.effectsar.labcv.core.util.LogUtils;
import com.effectsar.labcv.core.util.timer_record.LogTimerRecord;
import com.effectsar.labcv.effect.R;
import com.effectsar.labcv.effect.config.EffectConfig;
import com.effectsar.labcv.common.gesture.GestureManager;
import com.effectsar.labcv.effect.manager.FilterDataManager;
import com.effectsar.labcv.effect.manager.LocalParamDataManager;
import com.effectsar.labcv.effect.model.ComposerNode;
import com.effectsar.labcv.effect.model.EffectButtonItem;
import com.effectsar.labcv.effect.manager.EffectDataManager;
import com.effectsar.labcv.core.effect.EffectResourceHelper;
import com.effectsar.labcv.effectsdk.EffectsSDKEffectConstants;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/** {zh} 
 * 特效基类
 */
/** {en} 
 * Effect base class
 */

public abstract class BaseEffectActivity extends BaseBarGLActivity implements EffectManager.OnEffectListener, GestureManager.OnTouchListener, MessageCenter.Listener {

    /** {zh} 
     * 特效管理类
     */
    /** {en} 
     * Effect Management
     */

    protected EffectManager mEffectManager = null;
    protected EffectDataManager mEffectDataManager = null;
    protected FilterDataManager mFilterDatamanager = null;
    /** {zh}
     * 屏幕触摸手势管理
     */
    /** {en} 
     * Screen Touch Gesture Management
     */

    protected GestureManager mGestureManager;

    private ImageView imgCompare;

    protected volatile boolean isEffectOn = true;

    protected EffectConfig mEffectConfig = null;


    @IdRes
    protected int mBoardFragmentTargetId = R.id.fl_effect_board;

    protected boolean mFirstEnter = true;

    private boolean mFirstLaunch;

    protected BubbleWindowManager.BubbleCallback mBubbleCallback;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mEffectConfig = parseEffectConfig(getIntent());

        LocalParamManager.init(mContext.getApplicationContext());
        if ( !TextUtils.isEmpty(mEffectConfig.getFeature())
                && (mEffectConfig.getFeature().equals("feature_beauty_lite") ||
                mEffectConfig.getFeature().equals("feature_beauty_standard") ||
                mEffectConfig.getFeature().equals("feature_style_makeup") ||
                mEffectConfig.getFeature().equals("feature_style_makeup_local") )
        ) {
            LocalParamDataManager.setUseLocalParamStorage(true);
            LocalParamManager.getInstance().setTableName(mEffectConfig.getFeature());
        } else {
            LocalParamDataManager.setUseLocalParamStorage(false);
        }

        LayoutInflater.from(this).inflate(R.layout.activity_base_effect, findViewById(R.id.fl_base_gl), true);
        imgCompare = findViewById(R.id.img_compare);

        imgCompare.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    setEffectOn(false);
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    setEffectOn(true);
                    break;
            }
            return true;
        });


        /** {zh} 
         * 手势相关特效需要输入手势信息
         */
        /** {en} 
         * Gesture-related effects require input of gesture information
         */

        findViewById(R.id.glview).setOnTouchListener((v, event) -> {
            if (!closeBoardFragment()) {
                if (mGestureManager == null) {
                    mGestureManager = new GestureManager(getApplicationContext(), BaseEffectActivity.this);
                }
                return mGestureManager.onTouchEvent(event);
            }
            return true;
        });

        mEffectDataManager = new EffectDataManager(mEffectConfig.getEffectType());
        mFilterDatamanager = new FilterDataManager(mContext);

        /** {zh} 
         * EffectManager是RenderManger的封装类，
         * 与当前GL上下文强关联，因此GL上下文发生变化之前，需要调用各自的销毁函数，但对象本身不销毁，再次使用时会根据内部变量的状态，重新关联当前上下文初始化
         */
        /** {en} 
         * EffectManager is the encapsulated class of RenderManger,
         *  is strongly associated with the current GL context, so before the GL context changes, you need to call the respective destruction function, but the object itself is not destroyed, and when you use it again, it will be based on the state of the internal variable, Reassociate the current context initialization
         */

        mEffectManager = new EffectManager(this, new EffectResourceHelper(this), EffectLicenseHelper.getInstance(this));

        mEffectManager.setOnEffectListener(this);
        mEffectManager.addMessageListener(this);

        createHalManager();

        mBubbleCallback = new BubbleWindowManager.BubbleCallback() {

            @Override
            public void onBeautyDefaultChanged(boolean on) {
                if (mSurfaceView == null) return;
                mSurfaceView.queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        appendBeautyDefault(on);
                    }
                });

            }

            @Override
            public void onResolutionChanged(int width, int height) {
                //    {zh} 只有超分需要该功能        {en} Only super points need this function
                if (mImageSourceProvider instanceof CameraSourceImpl){
                    // config mImageSourceConfig so that resolution info could be saved
                    // when app is running in the background.
                    mImageSourceConfig.setRequestWidth(width);
                    mImageSourceConfig.setRequestHeight(height);
                    ((CameraSourceImpl)mImageSourceProvider).setPreferSize(width,height);
                    ((CameraSourceImpl)mImageSourceProvider).changeCamera(((CameraSourceImpl)mImageSourceProvider).getCameraID(),null);
                }
            }

            @Override
            public void onPerformanceChanged(boolean on) {
                mRlPerformance.setVisibility(on?View.VISIBLE:View.INVISIBLE);
            }

            @Override
            public void onPictureModeChanged(boolean on) {
                if (mEffectManager != null) {
                    mEffectManager.enableAlgorithmPictureMode(on);
                }
            }
            @Override
            public void onVideoScaleChanged(int width, int height) {}
        };
        checkIsFirstLaunch();
    }

    void createHalManager() {
    }

    private EffectConfig parseEffectConfig(Intent intent){
        String sStr = intent.getStringExtra(EffectConfig.EffectConfigKey);
        LogUtils.d("effectConfig ="+sStr);
        if (sStr == null) {
            return new EffectConfig().setEffectType(LocaleUtils.isAsia(mContext)?EffectType.LITE_ASIA:EffectType.LITE_NOT_ASIA);
        }

        return new Gson().fromJson(sStr, EffectConfig.class);
    }

    // check if it is first launch every time before sdk init() & destroy()
    public void checkIsFirstLaunch(){
        SharedPreferences sharedPreferences = this.getSharedPreferences(mEffectConfig.getFeature(), 0);
        mFirstLaunch = sharedPreferences.getBoolean("isFirstLaunch", true);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (mFirstLaunch) {
            editor.putBoolean("isFirstLaunch", false);
            editor.commit();
        }
    }

    @Override
    protected void onDestroy() {
        mEffectManager.removeMessageListener(this);
        super.onDestroy();
    }

    @Override
    public void onPause() {
        checkIsFirstLaunch();
        mSurfaceView.queueEvent(() -> {
            mEffectManager.destroy();
            needRecover = true;
        });

        // {zh} super.onPause 会触发glsurfaceview释放context，需要放在gl资源释放后调用 {en} super.on Pause will trigger glsurfaceview to release the context, which needs to be called after the gl resource is released
        super.onPause();
    }

    /** {zh} 
     * 重置美颜效果,同时清除其他composer nodes节点，以及贴纸、滤镜,必须在GL线程执行
     */
    /** {en} 
     * Reset the beauty effect, and clear other composer nodes, stickers and filters at the same time, must be executed in the GL thread
     */

    protected void resetDefault( ){
        LogUtils.d("resetDefault");
        mSurfaceView.queueEvent(new Runnable() {
            @Override
            public void run() {
                if (null != mBubbleConfig && mBubbleConfig.isEnableBeauty()){
                    setBeautyDefault();
                }
                mEffectManager.setFilter("");
                mEffectManager.setSticker("");
            }
        });
    }
    protected int storedItemStatus = 0;
    protected void loadLocalStorage(boolean notExcludeDefaultBeauty){
        // check which item has been taken activated
        Set<EffectButtonItem> mLocalItemsOrigin = mEffectDataManager.getLocalStoredItems(storedItemStatus);

        Set<EffectButtonItem> mLocalItems = new HashSet<>(mLocalItemsOrigin);
        if (!notExcludeDefaultBeauty) {
            for (EffectButtonItem item : mLocalItemsOrigin) {
                if (mEffectDataManager.isDefaultEffect(item.getId())) {
                    mLocalItems.remove(item);
                }
            }
        }

        // setComposeNodes
        String[][] nodesAndTags = mEffectDataManager.generateComposerNodesAndTags(mLocalItems);
        mEffectManager.setComposeNodes(nodesAndTags[0], nodesAndTags[1]);

        // updateFilterIntensity / updateComposerNodeIntensity
        ArrayList<LocalParamHelper.LocalParam> items =  LocalParamDataManager.allItems();
        boolean filterRecorded = false;
        if (items != null) {
            for (LocalParamHelper.LocalParam param : items) {
                if (param.effect && param.category.equals(LocalParamHelper.CATEGORY_FILTER)) {
                    mEffectManager.setFilter(param.path);
                    mEffectManager.updateFilterIntensity(param.intensity);
                    filterRecorded = true;
                } else if (param.effect && param.category.equals(LocalParamHelper.CATEGORY_COMPOSER_NODE)) {
                    mEffectManager.updateComposerNodeIntensity(param.path, param.key, param.intensity);
                }
            }
            // To make sure that no filter is set, setFilter(null) should be called once.
            if (!filterRecorded) {
                mEffectManager.setFilter(null);
            }
        }

    }


    /** {zh} 
     * 追加或者移除默认美颜
     * @param flag true表示追加，false表示移除
     */
    /** {en} 
     * Append or remove default beauty
     * @param flag true means append, false means remove
     */

    protected void appendBeautyDefault(boolean flag){
        Set<EffectButtonItem> mDefaults = mEffectDataManager.getDefaultItems();
        String[][] nodesAndTags = mEffectDataManager.generateComposerNodesAndTags(mDefaults);

        if (flag){

            mEffectManager.appendComposeNodes(nodesAndTags[0]);
            for (EffectButtonItem it : mDefaults) {
                if (it.getNode() == null) {
                    continue;
                }
                for (int i = 0; i < it.getNode().getKeyArray().length; i++) {
                    mEffectManager.updateComposerNodeIntensity(it.getNode().getPath(),
                            it.getNode().getKeyArray()[i], it.getIntensityArray()[i]);
                }
            }
        }else {
            mEffectManager.removeComposeNodes(nodesAndTags[0]);
        }
    }

    /** {zh} 
     * 设置效果为默认美颜，会同时清除其他特效节点
     */
    /** {en} 
     * Set the effect to the default beauty, and other special effects nodes will be cleared at the same time
     */

    protected void setBeautyDefault(){
        LogUtils.d("setBeautyDefault invoked");
        Set<EffectButtonItem> mDefaults = mEffectDataManager.getDefaultItems();
        String[][] nodesAndTags = mEffectDataManager.generateComposerNodesAndTags(mDefaults);
        mEffectManager.setComposeNodes(nodesAndTags[0], nodesAndTags[1]);
        for (EffectButtonItem it : mDefaults) {
            if (it.getNode() == null) {
                continue;
            }
            for (int i = 0; i < it.getNode().getKeyArray().length; i++) {
                if ( !TextUtils.isEmpty(mEffectConfig.getFeature())
                        && (mEffectConfig.getFeature().equals("feature_style_makeup") ||
                        mEffectConfig.getFeature().equals("feature_style_makeup_local") )
                ) {
                    LocalParamDataManager.saveComposerNode(it);
                }
                mEffectManager.updateComposerNodeIntensity(it.getNode().getPath(),
                        it.getNode().getKeyArray()[i], it.getIntensityArray()[i]);
            }
        }
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        super.onSurfaceCreated(gl10,eglConfig);
        int ret =  mEffectManager.init();
        if (ret != EffectsSDKEffectConstants.EffectsSDKResultCode.BEF_RESULT_SUC){
            LogUtils.e("mEffectManager.init() fail!! error code ="+ret);
        }
    }

    @Override
    public ProcessOutput processImpl(ProcessInput input) {
        LogTimerRecord.RECORD("totalProcess");
        //    {zh} 准备帧缓冲区纹理对象        {en} Prepare frame buffer texture objects  
        int dstTexture = mImageUtil.prepareTexture(input.getWidth(), input.getHeight());
        //    {zh} 调试代码，用于显示输入图        {en} Debugging code for displaying input diagrams  
            /*Bitmap b = mImageUtil.transferTextureToBitmap(input.getTexture(), EffectsSDKEffectConstants.TextureFormat.Texure2D, input.getWidth(),input.getHeight());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((ImageView)findViewById(R.id.debug_window)).setImageBitmap(b);
                }
            });*/
        if (isEffectOn){
            //    {zh} AR扫一扫依赖该信息        {en} AR scan depends on this information  
            mEffectManager.setCameraPosition(mImageSourceProvider.isFront());
            long timestamp = mImageSourceProvider.getTimestamp();
            timestamp = System.nanoTime();
            boolean ret = mEffectManager.process(input.getTexture(),dstTexture, input.getWidth(),input.getHeight(),input.getSensorRotation(), timestamp);
            if (!ret){
                dstTexture = input.getTexture();
            }

        }else {
            dstTexture = input.getTexture();
//             {zh} 对比按钮按下和恢复时，画面闪动一帧，新增清除缓存             {en} When the compare button is pressed and restored, the screen flashes one frame, and the new clear cache is added.
            mEffectManager.cleanPipeline();
        }
        LogTimerRecord.STOP("totalProcess");

        output.setTexture(dstTexture);
        output.setWidth(input.getWidth());
        output.setHeight(input.getHeight());
        return output;
    }

    @Override
    public ProcessOutput processImageImpl(ProcessInput input) {
        return null;
    }

    @Override
    public void processImageYUV(byte[] data, int format, EffectsSDKEffectConstants.Rotation rotation, int width, int height, int runtimes) {
    }

    //  {zh} 是否需要恢复状态  {en} Need to restore state
    private boolean needRecover = false;
    @Override
    public void onEffectInitialized() {

        // load initial status
        if (LocalParamDataManager.useLocalParamStorage()) {
            if (isFirstLaunch()) {
                resetDefault();
            } else if ( !TextUtils.isEmpty(mEffectConfig.getFeature())
                    && ( mEffectConfig.getFeature().equals("feature_beauty_lite") ||
                    mEffectConfig.getFeature().equals("feature_beauty_standard") ||
                    mEffectConfig.getFeature().equals("feature_style_makeup") ||
                    mEffectConfig.getFeature().equals("feature_style_makeup_local") )
            ){
                loadLocalStorage(mBubbleConfig.isEnableBeauty());
            } else {
                resetDefault();
            }
        } else {
            if (needRecover){
                //    {zh} 在锁屏恢复或者后台回到前台过程中，用于恢复之前的设置        {en} Used to restore previous settings during screen lock recovery or background return to the foreground
                mEffectManager.recoverStatus();
            }
        }

        if (!mFirstEnter) {
            return;
        }
        mFirstEnter = false;

        if (isAutoTest()){
            LogUtils.d("hit AutoTest");
            if(mEffectConfig.getNodes() != null || mEffectConfig.getFilter() != null)
            {
                setEffectByConfig();
            }

            runOnUiThread(()->{
                startPlay();

            });

        } else {
            if (!LocalParamDataManager.useLocalParamStorage()) {
                resetDefault();
            }
        }
    }

    protected boolean isFirstLaunch(){
        return mFirstLaunch;
    }

    /** {zh} 
     * 是否使用自动化测试传入的配置
     * @return
     */
    /** {en} 
     * Whether to use automated test incoming configuration
     * @return
     */
    protected boolean isAutoTest(){
        return mEffectConfig !=null && mEffectConfig.isAutoTest();
    }

    /** {zh} 
     * 自动化测试使用
     */
    /** {en} 
     * Automated test usage
     */

    protected void setEffectByConfig(){
        if (mEffectManager == null || mEffectConfig == null)return;
        if (null != mEffectConfig.getNodeArray()) {
            mEffectManager.resourcePath = mEffectConfig.getResourcePath();
            mEffectManager.setComposeNodes(mEffectConfig.getNodeArray());
            for (ComposerNode node : mEffectConfig.getNodes()) {
                for (int i = 0; i < node.getKeyArray().length;i++){
                    mEffectManager.updateComposerNodeIntensity(node.getPath(), node.getKeyArray()[i],node.getIntensityArray()[i]);
                }
            }
        }

        if (null != mEffectConfig.getFilter() && !TextUtils.isEmpty(mEffectConfig.getFilter().getResource())) {
            mEffectManager.setFilter(mEffectConfig.getFilter().getResource());
            mEffectManager.updateFilterIntensity(mEffectConfig.getFilter().getIntensity());
        }
    }

    /** {zh} 
     * @param isOn 是否执行特效渲染
     *             whether open effect
     * @brief 设置是否执行特效渲染
     * set effect on
     */
    /** {en} 
     * @param isOn Whether to perform special effect rendering
     *             Whether to open effect
     * @brief Set whether to perform special effect rendering
     * Set effect on
     */
    protected void setEffectOn(boolean isOn){
        isEffectOn = isOn;
    }


    /** {zh}
     * @param y 原图对比按钮的高度位置的变化量，单位像素
     * @param duration 特效执行时长，单位毫秒
     * @brief 调整原图对比按钮的高度，可设置高度变化动画的过渡时长及高度变化量
     */
    /** {en}
     * @param y the position difference, unit px
     * @param duration duration time of the y position change, unit ms
     * @brief Adjust button_image_compare by position difference
     */
    protected void setImgCompareViewHeightBy(float y,int duration){
        imgCompare.animate().yBy(y).setDuration(duration).start();
    }

    protected void setImgCompareViewVisibility(int visibility){
        imgCompare.setVisibility(visibility);
    }

    @Override
    protected void showOrHideBoard(boolean show, boolean animate) {
        super.showOrHideBoard(show, animate);
    }

    @Override
    public void onTouchEvent(EffectsSDKEffectConstants.TouchEventCode eventCode, float x, float y, float force, float majorRadius, int pointerId, int pointerCount) {
        if (mSurfaceView == null) return;
        mSurfaceView.queueEvent(new Runnable() {
            @Override
            public void run() {
                mEffectManager.processTouch(eventCode, x, y, force, majorRadius, pointerId, pointerCount);
            }
        });
    }

    @Override
    public void onGestureEvent(EffectsSDKEffectConstants.GestureEventCode eventCode, float x, float y, float dx, float dy, float factor) {
        if (mSurfaceView == null) return;
        mSurfaceView.queueEvent(() -> mEffectManager.processGesture(eventCode, x, y, dx, dy, factor));
    }

    @Override
    public void onMessageReceived(int i, int i1, int i2, String s) {
        if(i == EffectManager.BEF_MSG_TYPE_LICENSE_CHECK && i1 != 0) {
            // 鉴权失败，arg1 对应离线鉴权or在线鉴权，arg2对应鉴权错误码，args返回license路径或buffer长度
            Log.e("cvtest", "Message Type: BEF_MSG_TYPE_LICENSE_CHECK, arg1: "+i1+",arg2: "+i2+",arg3: "+s);
        } else if (i == EffectManager.BEF_MSG_TYPE_MODEL_MISS) {
            // arg3为模型路径，素材加载过程中出现了模型找不到的问题
            Log.e("cvtest", "Message Type: BEF_MSG_TYPE_MODEL_MISS, arg3: "+s);
        } else if (i == EffectManager.RENDER_MSG_TYPE_RESOURCE) {
            // 存在feature加载失败，arg3中保存对应feature的名称
            Log.e("cvtest", "Message Type: RENDER_MSG_TYPE_RESOURCE, arg1: "+i1+",arg2: "+i2+",arg3: "+s);
        } else if (i == EffectManager.BEF_MSG_TYPE_EFFECT_INIT) {
            // EffectManager初始化失败，arg1对应埋点序号，arg3对应错误提示信息
            Log.e("cvtest", "Message Type: BEF_MSG_TYPE_EFFECT_INIT, arg1: "+i1+",arg3: "+s);
        }
    }
}
