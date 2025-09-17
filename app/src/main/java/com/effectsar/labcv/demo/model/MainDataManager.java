package com.effectsar.labcv.demo.model;

import android.hardware.Camera;
import android.os.Build;
import android.text.TextUtils;

import com.effectsar.labcv.algorithm.config.AlgorithmConfig;
import com.effectsar.labcv.algorithm.activity.AlgorithmActivity;
import com.effectsar.labcv.common.base.EffectEBoxConfig;
import com.effectsar.labcv.common.config.ImageSourceConfig;
import com.effectsar.labcv.common.config.UIConfig;
import com.effectsar.labcv.common.ebox.EBoxEffectType;
import com.effectsar.labcv.common.model.EffectType;
import com.effectsar.labcv.common.utils.LocaleUtils;
import com.effectsar.labcv.core.Config;
import com.effectsar.labcv.core.algorithm.AvaBoostAlgorithmTask;
import com.effectsar.labcv.core.algorithm.BachSkeletonAlgorithmTask;
import com.effectsar.labcv.core.algorithm.C1AlgorithmTask;
import com.effectsar.labcv.core.algorithm.C2AlgorithmTask;
import com.effectsar.labcv.core.algorithm.CarAlgorithmTask;
import com.effectsar.labcv.core.algorithm.ChromaKeyingAlgorithmTask;
import com.effectsar.labcv.core.algorithm.ConcentrateAlgorithmTask;
import com.effectsar.labcv.core.algorithm.DynamicGestureAlgorithmTask;
import com.effectsar.labcv.core.algorithm.FaceAlgorithmTask;
import com.effectsar.labcv.core.algorithm.FaceClusterAlgorithmTask;
import com.effectsar.labcv.core.algorithm.FaceFittingAlgorithmTask;
import com.effectsar.labcv.core.algorithm.FaceVerifyAlgorithmTask;
import com.effectsar.labcv.core.algorithm.GazeEstimationAlgorithmTask;
import com.effectsar.labcv.core.algorithm.HairParserAlgorithmTask;
import com.effectsar.labcv.core.algorithm.HandAlgorithmTask;
import com.effectsar.labcv.core.algorithm.HeadSegAlgorithmTask;
import com.effectsar.labcv.core.algorithm.HumanDistanceAlgorithmTask;
import com.effectsar.labcv.core.algorithm.LicenseCakeAlgorithmTask;
import com.effectsar.labcv.core.algorithm.LightClsAlgorithmTask;
import com.effectsar.labcv.core.algorithm.ObjectTrackingAlgorithmTask;
import com.effectsar.labcv.core.algorithm.PetFaceAlgorithmTask;
import com.effectsar.labcv.core.algorithm.PortraitMattingAlgorithmTask;
import com.effectsar.labcv.core.algorithm.SaliencyMattingAlgorithmTask;
import com.effectsar.labcv.core.algorithm.Skeleton3DAlgorithmTask;
import com.effectsar.labcv.core.algorithm.SkeletonAlgorithmTask;
import com.effectsar.labcv.core.algorithm.SkinSegmentationAlgorithmTask;
import com.effectsar.labcv.core.algorithm.SkySegAlgorithmTask;
import com.effectsar.labcv.core.algorithm.SlamAlgorithmTask;
import com.effectsar.labcv.core.algorithm.VideoClsAlgorithmTask;
import com.effectsar.labcv.demo.R;
import com.effectsar.labcv.demo.utils.StreamUtils;
import com.effectsar.labcv.ebox.EBoxPageConfigGroup;
import com.effectsar.labcv.ebox.MainPageConfig;
import com.effectsar.labcv.ebox.Page;
import com.effectsar.labcv.ebox.PageDetail;
import com.effectsar.labcv.effect.activity.BackgroundBlurActivity;
import com.effectsar.labcv.effect.activity.LocalStyleMakeUpActivity;
import com.effectsar.labcv.effect.activity.StickerTestActivity;
import com.effectsar.labcv.effect.config.EffectConfig;
import com.effectsar.labcv.effect.config.StickerConfig;
import com.effectsar.labcv.effect.activity.BeautyActivity;
import com.effectsar.labcv.effect.activity.MattingStickerActivity;
import com.effectsar.labcv.effect.activity.QRScanActivity;
import com.effectsar.labcv.effect.activity.StickerActivity;
import com.effectsar.labcv.effect.activity.StyleMakeUpActivity;
import com.effectsar.labcv.effect.utils.SLAMBlackList;
import com.effectsar.labcv.lens.activity.LensUploadActivity;
import com.effectsar.labcv.lens.activity.PhotoImageQualityActivity;
import com.effectsar.labcv.lens.config.ImageQualityConfig;
import com.effectsar.labcv.lens.activity.ImageQualityActivity;
import com.effectsar.labcv.sports.SportsHomeActivity;
import com.volcengine.effectone.singleton.AppSingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.effectsar.labcv.common.config.ImageSourceConfig.ImageSourceType.TYPE_CAMERA;

import androidx.annotation.StringRes;

public class MainDataManager {
    /**
     * GROUP
     */
    public static final String GROUP_HOT = "group_hot";
    public static final String GROUP_ALGORITHM = "group_algorithm";
    public static final String GROUP_QR_SCAN = "group_qr_scan";
    public static final String GROUP_EFFECT = "group_effect";
    public static final String GROUP_SPORTS = "group_sports";
    public static final String GROUP_LENS = "group_lens";
    public static final String GROUP_AR = "group_ar";
    public static final String GROUP_AR_TRY_ON = "group_ar_try_on";
    public static final String GROUP_TEST = "group_test";
    /**
     * Feature Items
     */
    public static final String FEATURE_BEAUTY_LITE = "feature_beauty_lite";
    public static final String FEATURE_BEAUTY_STANDARD = "feature_beauty_standard";
    public static final String FEATURE_STICKER = "feature_sticker";
    public static final String FEATURE_CAMERA_MOVEMENT = "feature_camera_movement";
    public static final String FEATURE_STYLE_MAKEUP = "feature_style_makeup";
    public static final String FEATURE_STYLE_MAKEUP_LOCAL = "feature_style_makeup_local";
    public static final String FEATURE_AVATAR_DRIVE = "feature_animoji";
    public static final String FEATURE_MATTING_STIKCER = "feature_matting_sticker";
    public static final String FEATURE_CHROMA_MATTING_STIKCER = "feature_chroma_matting_sticker";
    public static final String FEATURE_BACKGROUND_BLUR = "feature_background_blur";
    public static final String FEATURE_AR_SCAN = "feature_ar_scan";
    public static final String FEATURE_QR_SCAN = "feature_qr_scan";
    public static final String FEATURE_AMAZING_STICKER = "feature_amazing_sticker";
    public static final String FEATURE_GAME = "feature_game";
    public static final String FEATURE_GIFT_STICKER = "feature_gift_sticker";

    public static final String FEATURE_FACE = "feature_face";
    public static final String FEATURE_FACE_FITTING = "feature_facefitting";
    public static final String FEATURE_HAND = "feature_hand";
    public static final String FEATURE_SKELETON = "feature_skeleton";
    public static final String FEATURE_PET_FACE = "feature_pet_face";
    public static final String FEATURE_HEAD_SEG = "feature_head_seg";
    public static final String FEATURE_HAIR_PARSE = "feature_hair_parse";
    public static final String FEATURE_PORTRAIT_MATTING = "feature_portrait";
    public static final String FEATURE_SALIENCY_MATTING = "feature_saliency_matting";
    public static final String FEATURE_SKY_SEG = "feature_sky_seg";
    public static final String FEATURE_LIGHT = "feature_light";
    public static final String FEATURE_HUMAN_DISTANCE = "feature_human_distance";
    public static final String FEATURE_CONCENTRATE = "feature_concentrate";
    public static final String FEATURE_GAZE = "feature_gaze_estimation";
    public static final String FEATURE_C1 = "feature_c1";
    public static final String FEATURE_C2 = "feature_c2";
    public static final String FEATURE_VIDEO_CLS = "feature_video_cls";
    public static final String FEATURE_CAR = "feature_car";
    public static final String FEATURE_FACE_VREIFY = "feature_face_verify";
    public static final String FEATURE_FACE_CLUSTER = "feature_face_cluster";
    public static final String FEATURE_DYNAMIC_GESTURE = "feature_dynamic_gesture";
    public static final String FEATURE_LICENSE_CAKE = "feature_license_cake";
    public static final String FEATURE_SKIN_SEGMENTATION = "feature_skin_segmentation";
    public static final String FEATURE_BACH_SKELETON = "feature_bach_skeleton";
    public static final String FEATURE_CHROMA_KEYING = "feature_chroma_keying";
    public static final String FEATURE_SLAM_ALGO = "feature_slam_algo";
    public static final String FEATURE_SKELETON_3D = "feature_skeleton_3d";
    public static final String FEATURE_SPORT_ASSISTANCE = "feature_sport_assistance";
    public static final String FEATURE_VIDEO_SR = "feature_video_sr";
    public static final String FEATURE_NIGHT_SCENE = "feature_night_scene";
    public static final String FEATURE_ADAPTIVE_SHARPEN = "feature_adaptive_sharpen";
    public static final String FEATURE_PHOTO_NIGHT_SCENE = "feature_photo_night_scene";
    public static final String FEATURE_VFI = "feature_video_frame_insert";
    public static final String FEATURE_ONEKEY_ENHANCE = "feature_smart_hd";
    public static final String FEATURE_VIDA = "feature_vida";
    public static final String FEATURE_TAINT_DETECT = "feature_taint_detect";
    public static final String FEATURE_CINE_MOVE = "feature_cine_move";
    public static final String FEATURE_VIDEO_LITE_HDR = "feature_video_lite_hdr";
    public static final String FEATURE_CREATION_KIT = "feature_creation_kit";
    public static final String FEATURE_AVABOOST = "feature_avaboost";
    public static final String FEATURE_OBJECT_TRACKING = "feature_object_tracking";
    public static final String FEATURE_VIDEO_STAB = "feature_video_stab";
    public static final String FEATURE_VIDEO_DEFLICKER = "feature_video_deflicker";

    public static final String FEATURE_AR_SLAM = "feature_ar_slam";
    public static final String FEATURE_AR_OBJECT = "feature_ar_object";
    public static final String FEATURE_AR_LANDMARK = "feature_ar_landmark";
    public static final String FEATURE_AR_SKY_LAND = "feature_sky_land";

    public static final String FEATURE_AR_PURSE = "feature_ar_purse";
    public static final String FEATURE_AR_NAIL = "feature_ar_nail";
    public static final String FEATURE_AR_SHOE = "feature_ar_shoe";
    public static final String FEATURE_AR_LIPSTICK = "feature_ar_lipstick";
    public static final String FEATURE_AR_HAT = "feature_ar_hat";
    public static final String FEATURE_AR_NECKLACE = "feature_ar_necklace";
    public static final String FEATURE_AR_GLASSES = "feature_ar_glasses";
    public static final String FEATURE_AR_BRACELET = "feature_ar_bracelet";
    public static final String FEATURE_AR_RING = "feature_ar_ring";
    public static final String FEATURE_AR_EARRINGS = "feature_ar_earrings";
    public static final String FEATURE_AR_WATCH = "feature_ar_watch";
    public static final String FEATURE_AR_BACK_MOUNTED = "feature_back_mount";

    public static final String FEATURE_TEST_STICKER = "feature_sticker_test";

    private static final HashMap<String, FeatureTabItem> featureItemMap = new HashMap<>();
    private static final HashMap<String, String> groupTitleMap = new HashMap<>();

    public MainDataManager() {
        initData();
    }

    private void initData() {
        groupTitleMap.put(GROUP_HOT, getString(R.string.hot_feature));
        groupTitleMap.put(GROUP_EFFECT, getString(R.string.effect));
        groupTitleMap.put(GROUP_ALGORITHM, getString(R.string.feature_algorithm));
        groupTitleMap.put(GROUP_SPORTS, getString(R.string.feature_sport));
        groupTitleMap.put(GROUP_LENS, getString(R.string.feature_image_quality));
        groupTitleMap.put(GROUP_AR_TRY_ON, getString(R.string.feature_ar_try_on));
        groupTitleMap.put(GROUP_TEST, getString(R.string.feature_test));
        if (EffectEBoxConfig.useEbox()) {
            groupTitleMap.put(GROUP_QR_SCAN, getString(R.string.feature_qr_scan));
        }

        featureItemMap.put(FEATURE_BEAUTY_LITE, new FeatureTabItem(
                FEATURE_BEAUTY_LITE,
                getString(R.string.feature_beauty_lite),
                R.drawable.feature_beauty,
                new FeatureConfig().setActivityClassName(BeautyActivity.class.getName()).setEffectConfig(new EffectConfig().setEffectType(LocaleUtils.isAsia(AppSingleton.instance) ? EffectType.LITE_ASIA : EffectType.LITE_NOT_ASIA).setFeature(FEATURE_BEAUTY_LITE))));
        featureItemMap.put(FEATURE_BEAUTY_STANDARD, new FeatureTabItem(
                FEATURE_BEAUTY_STANDARD,
                getString(R.string.feature_beauty_standard),
                R.drawable.feature_beauty,
                new FeatureConfig().setActivityClassName(BeautyActivity.class.getName()).setEffectConfig(new EffectConfig().setEffectType(LocaleUtils.isAsia(AppSingleton.instance) ? EffectType.STANDARD_ASIA : EffectType.STANDARD_NOT_ASIA).setFeature(FEATURE_BEAUTY_STANDARD))));
        featureItemMap.put(FEATURE_STICKER, new FeatureTabItem(
                FEATURE_STICKER,
                getString(R.string.feature_sticker),
                R.drawable.feature_sticker,
                new FeatureConfig().setStickerConfig(new StickerConfig().setType(FEATURE_STICKER)).setActivityClassName(StickerActivity.class.getName())
        ));
        featureItemMap.put(FEATURE_CAMERA_MOVEMENT, new FeatureTabItem(
                FEATURE_CAMERA_MOVEMENT,
                getString(R.string.feature_cine_move),
                R.drawable.feature_cine_move,
                new FeatureConfig().setStickerConfig(new StickerConfig().setType(FEATURE_CAMERA_MOVEMENT)).setActivityClassName(StickerActivity.class.getName())
        ));

        featureItemMap.put(FEATURE_QR_SCAN, new FeatureTabItem(
                FEATURE_QR_SCAN,
                getString(R.string.feature_qr_scan),
                R.drawable.feature_qr_scan,
                new FeatureConfig().setImageSourceConfig(new ImageSourceConfig(TYPE_CAMERA, String.valueOf(Camera.CameraInfo.CAMERA_FACING_BACK)))
                        .setActivityClassName(QRScanActivity.class.getName())
                        .setFeatureCategory(FEATURE_QR_SCAN)
        ));

        featureItemMap.put(FEATURE_STYLE_MAKEUP, new FeatureTabItem(
                FEATURE_STYLE_MAKEUP,
                getString(R.string.feature_style_makeup),
                R.drawable.feature_style_makeup,
                new FeatureConfig().setActivityClassName(StyleMakeUpActivity.class.getName()).setEffectConfig(new EffectConfig().setFeature(FEATURE_STYLE_MAKEUP))
        ));
        featureItemMap.put(FEATURE_STYLE_MAKEUP_LOCAL, new FeatureTabItem(
                FEATURE_STYLE_MAKEUP_LOCAL,
                getString(R.string.feature_style_makeup_local),
                R.drawable.feature_style_makeup,
                new FeatureConfig().setActivityClassName(LocalStyleMakeUpActivity.class.getName()).setEffectConfig(new EffectConfig().setFeature(FEATURE_STYLE_MAKEUP_LOCAL))
        ));
        featureItemMap.put(FEATURE_AMAZING_STICKER, new FeatureTabItem(
                FEATURE_AMAZING_STICKER,
                getString(R.string.feature_amazing_sticker),
                R.drawable.feature_amazing_sticker,
                new FeatureConfig().setStickerConfig(new StickerConfig().setType(FEATURE_AMAZING_STICKER)).setActivityClassName(StickerActivity.class.getName())
        ));
        featureItemMap.put(FEATURE_GAME, new FeatureTabItem(
                FEATURE_GAME,
                getString(R.string.feature_game),
                R.drawable.feature_game,
                new FeatureConfig().setStickerConfig(new StickerConfig().setType(FEATURE_GAME)).setActivityClassName(StickerActivity.class.getName())
                        .setUiConfig(new UIConfig().setEnbaleAblum(false))
        ));
        featureItemMap.put(FEATURE_AVATAR_DRIVE, new FeatureTabItem(
                FEATURE_AVATAR_DRIVE,
                getString(R.string.feature_avatar_drive),
                R.drawable.feature_avatar_drive,
                new FeatureConfig().setStickerConfig(new StickerConfig().setType(FEATURE_AVATAR_DRIVE)).setActivityClassName(StickerActivity.class.getName())
        ));
        featureItemMap.put(FEATURE_MATTING_STIKCER, new FeatureTabItem(
                FEATURE_MATTING_STIKCER,
                getString(R.string.feature_matting_sticker),
                R.drawable.feature_matting_sticker,
                new FeatureConfig().setStickerConfig(new StickerConfig().setType(FEATURE_MATTING_STIKCER)).setActivityClassName(MattingStickerActivity.class.getName())
        ));
        featureItemMap.put(FEATURE_CHROMA_MATTING_STIKCER, new FeatureTabItem(
                FEATURE_CHROMA_MATTING_STIKCER,
                getString(R.string.feature_chroma_matting_sticker),
                R.drawable.feature_chroma_matting,
                new FeatureConfig().setStickerConfig(new StickerConfig().setType(FEATURE_CHROMA_MATTING_STIKCER)).setActivityClassName(MattingStickerActivity.class.getName())
        ));

        featureItemMap.put(FEATURE_BACKGROUND_BLUR, new FeatureTabItem(
                FEATURE_BACKGROUND_BLUR,
                getString(R.string.feature_background_blur),
                R.drawable.feature_background_blur,
                new FeatureConfig().setActivityClassName(BackgroundBlurActivity.class.getName())));

        featureItemMap.put(FEATURE_AR_SCAN, new FeatureTabItem(
                FEATURE_AR_SCAN,
                getString(R.string.feature_ar_scan),
                R.drawable.feature_ar_scan,
                new FeatureConfig().setStickerConfig(new StickerConfig().setType(FEATURE_AR_SCAN)).setActivityClassName(StickerActivity.class.getName()).
                        setImageSourceConfig(new ImageSourceConfig(TYPE_CAMERA, String.valueOf(Camera.CameraInfo.CAMERA_FACING_BACK)))

        ));
        featureItemMap.put(FEATURE_GIFT_STICKER, new FeatureTabItem(
                FEATURE_GIFT_STICKER,
                getString(R.string.feature_gift_sticker),
                R.drawable.feature_live_gift,
                new FeatureConfig().setStickerConfig(new StickerConfig().setType(FEATURE_GIFT_STICKER)).setActivityClassName(StickerActivity.class.getName())
        ));

        featureItemMap.put(FEATURE_AR_PURSE, new FeatureTabItem(
                FEATURE_AR_PURSE,
                getString(R.string.feature_ar_purse),
                R.drawable.feature_ar_purse,
                new FeatureConfig().setStickerConfig(new StickerConfig().setType(FEATURE_AR_PURSE)).setActivityClassName(StickerActivity.class.getName())
        ));

        featureItemMap.put(FEATURE_AR_SHOE, new FeatureTabItem(
                FEATURE_AR_SHOE,
                getString(R.string.feature_ar_shoe),
                R.drawable.feature_ar_shoe,
                new FeatureConfig().setStickerConfig(new StickerConfig().setType(FEATURE_AR_SHOE)).setActivityClassName(StickerActivity.class.getName()).
                        setImageSourceConfig(new ImageSourceConfig(TYPE_CAMERA, String.valueOf(Camera.CameraInfo.CAMERA_FACING_BACK)))
        ));

        featureItemMap.put(FEATURE_AR_HAT, new FeatureTabItem(
                FEATURE_AR_HAT,
                getString(R.string.feature_ar_hat),
                R.drawable.feature_ar_hat,
                new FeatureConfig().setStickerConfig(new StickerConfig().setType(FEATURE_AR_HAT)).setActivityClassName(StickerActivity.class.getName()).
                        setImageSourceConfig(new ImageSourceConfig(TYPE_CAMERA, String.valueOf(Camera.CameraInfo.CAMERA_FACING_FRONT)))
        ));

        featureItemMap.put(FEATURE_AR_NECKLACE, new FeatureTabItem(
                FEATURE_AR_NECKLACE,
                getString(R.string.feature_ar_necklace),
                R.drawable.feature_ar_necklace,
                new FeatureConfig().setStickerConfig(new StickerConfig().setType(FEATURE_AR_NECKLACE)).setActivityClassName(StickerActivity.class.getName()).
                        setImageSourceConfig(new ImageSourceConfig(TYPE_CAMERA, String.valueOf(Camera.CameraInfo.CAMERA_FACING_FRONT)))
        ));

        featureItemMap.put(FEATURE_AR_GLASSES, new FeatureTabItem(
                FEATURE_AR_GLASSES,
                getString(R.string.feature_ar_glasses),
                R.drawable.feature_ar_glasses,
                new FeatureConfig().setStickerConfig(new StickerConfig().setType(FEATURE_AR_GLASSES)).setActivityClassName(StickerActivity.class.getName()).
                        setImageSourceConfig(new ImageSourceConfig(TYPE_CAMERA, String.valueOf(Camera.CameraInfo.CAMERA_FACING_FRONT)))
        ));

        featureItemMap.put(FEATURE_AR_BRACELET, new FeatureTabItem(
                FEATURE_AR_BRACELET,
                getString(R.string.feature_ar_bracelet),
                R.drawable.feature_ar_bracelet,
                new FeatureConfig().setStickerConfig(new StickerConfig().setType(FEATURE_AR_BRACELET)).setActivityClassName(StickerActivity.class.getName()).
                        setImageSourceConfig(new ImageSourceConfig(TYPE_CAMERA, String.valueOf(Camera.CameraInfo.CAMERA_FACING_BACK)))
        ));

        featureItemMap.put(FEATURE_AR_EARRINGS, new FeatureTabItem(
                FEATURE_AR_EARRINGS,
                getString(R.string.feature_ar_earrings),
                R.drawable.feature_ar_earrings,
                new FeatureConfig().setStickerConfig(new StickerConfig().setType(FEATURE_AR_EARRINGS)).setActivityClassName(StickerActivity.class.getName()).
                        setImageSourceConfig(new ImageSourceConfig(TYPE_CAMERA, String.valueOf(Camera.CameraInfo.CAMERA_FACING_FRONT)))
        ));

        featureItemMap.put(FEATURE_AR_WATCH, new FeatureTabItem(
                FEATURE_AR_WATCH,
                getString(R.string.feature_ar_watch),
                R.drawable.feature_ar_watch,
                new FeatureConfig().setStickerConfig(new StickerConfig().setType(FEATURE_AR_WATCH)).setActivityClassName(StickerActivity.class.getName()).
                        setImageSourceConfig(new ImageSourceConfig(TYPE_CAMERA, String.valueOf(Camera.CameraInfo.CAMERA_FACING_BACK)))
        ));

        featureItemMap.put(FEATURE_AR_BACK_MOUNTED, new FeatureTabItem(
                FEATURE_AR_BACK_MOUNTED,
                getString(R.string.feature_back_mount),
                R.drawable.feature_ar_back_mount,
                new FeatureConfig().setStickerConfig(new StickerConfig().setType(FEATURE_AR_BACK_MOUNTED)).setActivityClassName(StickerActivity.class.getName())
        ));

        // Algorithm start

        featureItemMap.put(FEATURE_FACE, new FeatureTabItem(
                FEATURE_FACE,
                getString(R.string.feature_face),
                R.drawable.feature_face,
                new FeatureConfig().setAlgorithmConfig(new AlgorithmConfig(FaceAlgorithmTask.FACE.getKey(), mapOf(FaceAlgorithmTask.FACE.getKey(), true)))
                        .setActivityClassName(AlgorithmActivity.class.getName()).setFeatureCategory(FEATURE_FACE)
        ));
        featureItemMap.put(FEATURE_FACE_FITTING, new FeatureTabItem(
                FEATURE_FACE_FITTING,
                getString(R.string.feature_facefitting),
                R.drawable.feature_face_fitting,
                new FeatureConfig().setAlgorithmConfig(new AlgorithmConfig(FaceFittingAlgorithmTask.FACE_FITTING.getKey(), mapOf(FaceFittingAlgorithmTask.FACE_FITTING.getKey(), true)))
                        .setActivityClassName(AlgorithmActivity.class.getName()).setFeatureCategory(FEATURE_FACE_FITTING)
        ));
        featureItemMap.put(FEATURE_HAND, new FeatureTabItem(
                FEATURE_HAND,
                getString(R.string.feature_hand),
                R.drawable.feature_hand,
                new FeatureConfig().setAlgorithmConfig(new AlgorithmConfig(HandAlgorithmTask.HAND.getKey(), mapOf(HandAlgorithmTask.HAND.getKey(), true)))
                        .setActivityClassName(AlgorithmActivity.class.getName()).setFeatureCategory(FEATURE_HAND)
        ));
        featureItemMap.put(FEATURE_SKELETON, new FeatureTabItem(
                FEATURE_SKELETON,
                getString(R.string.feature_skeleton),
                R.drawable.feature_skeleton,
                new FeatureConfig().setAlgorithmConfig(new AlgorithmConfig(SkeletonAlgorithmTask.SKELETON.getKey(), mapOf(SkeletonAlgorithmTask.SKELETON.getKey(), true)))
                        .setActivityClassName(AlgorithmActivity.class.getName()).setFeatureCategory(FEATURE_SKELETON)
        ));
        featureItemMap.put(FEATURE_PET_FACE, new FeatureTabItem(
                FEATURE_PET_FACE,
                getString(R.string.feature_pet_face),
                R.drawable.feature_pet_face,
                new FeatureConfig().setAlgorithmConfig(new AlgorithmConfig(PetFaceAlgorithmTask.PET_FACE.getKey(), mapOf(PetFaceAlgorithmTask.PET_FACE.getKey(), true)))
                        .setActivityClassName(AlgorithmActivity.class.getName()).setImageSourceConfig(new ImageSourceConfig(TYPE_CAMERA, String.valueOf(Camera.CameraInfo.CAMERA_FACING_BACK)))
                        .setFeatureCategory(FEATURE_PET_FACE)
        ));
        featureItemMap.put(FEATURE_HEAD_SEG, new FeatureTabItem(
                FEATURE_HEAD_SEG,
                getString(R.string.feature_head_seg),
                R.drawable.feature_head_seg,
                new FeatureConfig().setAlgorithmConfig(new AlgorithmConfig(HeadSegAlgorithmTask.HEAD_SEGMENT.getKey(), mapOf(HeadSegAlgorithmTask.HEAD_SEGMENT.getKey(), true)))
                        .setActivityClassName(AlgorithmActivity.class.getName()).setFeatureCategory(FEATURE_HEAD_SEG)
        ));
        featureItemMap.put(FEATURE_PORTRAIT_MATTING, new FeatureTabItem(
                FEATURE_PORTRAIT_MATTING,
                getString(R.string.feature_portrait),
                R.drawable.feature_portrait,
                new FeatureConfig().setAlgorithmConfig(new AlgorithmConfig(PortraitMattingAlgorithmTask.PORTRAIT_MATTING.getKey(), mapOf(PortraitMattingAlgorithmTask.PORTRAIT_MATTING.getKey(), true)))
                        .setActivityClassName(AlgorithmActivity.class.getName()).setFeatureCategory(FEATURE_PORTRAIT_MATTING)
        ));
        featureItemMap.put(FEATURE_SALIENCY_MATTING, new FeatureTabItem(
                FEATURE_SALIENCY_MATTING,
                getString(R.string.feature_saliency_matting),
                R.drawable.feature_portrait,
                new FeatureConfig().setAlgorithmConfig(new AlgorithmConfig(SaliencyMattingAlgorithmTask.SALIENCY_MATTING.getKey(), mapOf(SaliencyMattingAlgorithmTask.SALIENCY_MATTING.getKey(), true)))
                        .setActivityClassName(AlgorithmActivity.class.getName()).setFeatureCategory(FEATURE_SALIENCY_MATTING)
        ));
        featureItemMap.put(FEATURE_HAIR_PARSE, new FeatureTabItem(
                FEATURE_HAIR_PARSE,
                getString(R.string.feature_hair_parse),
                R.drawable.feature_hair_parse,
                new FeatureConfig().setAlgorithmConfig(new AlgorithmConfig(HairParserAlgorithmTask.HAIR_PARSER.getKey(), mapOf(HairParserAlgorithmTask.HAIR_PARSER.getKey(), true)))
                        .setActivityClassName(AlgorithmActivity.class.getName()).setFeatureCategory(FEATURE_HAIR_PARSE)
        ));
        featureItemMap.put(FEATURE_SKY_SEG, new FeatureTabItem(
                FEATURE_SKY_SEG,
                getString(R.string.feature_sky_seg),
                R.drawable.feature_sky_seg,
                new FeatureConfig().setAlgorithmConfig(new AlgorithmConfig(SkySegAlgorithmTask.SKY_SEGMENT.getKey(), mapOf(SkySegAlgorithmTask.SKY_SEGMENT.getKey(), true)))
                        .setActivityClassName(AlgorithmActivity.class.getName()).setImageSourceConfig(new ImageSourceConfig(TYPE_CAMERA, String.valueOf(Camera.CameraInfo.CAMERA_FACING_BACK)))
                        .setFeatureCategory(FEATURE_SKY_SEG)
        ));
        featureItemMap.put(FEATURE_LIGHT,
                new FeatureTabItem(
                        FEATURE_LIGHT,
                        getString(R.string.feature_light),
                        R.drawable.feature_light,
                        new FeatureConfig().setAlgorithmConfig(new AlgorithmConfig(LightClsAlgorithmTask.LIGHT_CLS.getKey(), mapOf(LightClsAlgorithmTask.LIGHT_CLS.getKey(), true)))
                                .setActivityClassName(AlgorithmActivity.class.getName()).setFeatureCategory(FEATURE_LIGHT)
                ));
        featureItemMap.put(FEATURE_HUMAN_DISTANCE,
                new FeatureTabItem(
                        FEATURE_HUMAN_DISTANCE,
                        getString(R.string.feature_human_distance),
                        R.drawable.feature_human_distance,
                        new FeatureConfig().setAlgorithmConfig(new AlgorithmConfig(HumanDistanceAlgorithmTask.HUMAN_DISTANCE.getKey(), mapOf(HumanDistanceAlgorithmTask.HUMAN_DISTANCE.getKey(), true)))
                                .setActivityClassName(AlgorithmActivity.class.getName()).setFeatureCategory(FEATURE_HUMAN_DISTANCE)
                ));
        featureItemMap.put(FEATURE_CONCENTRATE,
                new FeatureTabItem(
                        FEATURE_CONCENTRATE,
                        getString(R.string.feature_concentrate),
                        R.drawable.feature_concentrate,
                        new FeatureConfig().setAlgorithmConfig(new AlgorithmConfig(ConcentrateAlgorithmTask.CONCENTRATION.getKey(), mapOf(ConcentrateAlgorithmTask.CONCENTRATION.getKey(), true)))
                                .setActivityClassName(AlgorithmActivity.class.getName())
                                .setFeatureCategory(FEATURE_CONCENTRATE)
                ));
        featureItemMap.put(FEATURE_GAZE,
                new FeatureTabItem(
                        FEATURE_GAZE,
                        getString(R.string.feature_gaze_estimation),
                        R.drawable.feature_gaze_estimation,
                        new FeatureConfig().setAlgorithmConfig(new AlgorithmConfig(GazeEstimationAlgorithmTask.GAZE_ESTIMATION.getKey(), mapOf(GazeEstimationAlgorithmTask.GAZE_ESTIMATION.getKey(), true)))
                                .setActivityClassName(AlgorithmActivity.class.getName())
                                .setFeatureCategory(FEATURE_GAZE)
                ));
        featureItemMap.put(FEATURE_C1,
                new FeatureTabItem(
                        FEATURE_C1,
                        getString(R.string.feature_c1),
                        R.drawable.feature_c1,
                        new FeatureConfig().setAlgorithmConfig(new AlgorithmConfig(C1AlgorithmTask.C1.getKey(), mapOf(C1AlgorithmTask.C1.getKey(), true)))
                                .setActivityClassName(AlgorithmActivity.class.getName()).setImageSourceConfig(new ImageSourceConfig(TYPE_CAMERA, String.valueOf(Camera.CameraInfo.CAMERA_FACING_BACK)))
                                .setFeatureCategory(FEATURE_C1)
                ));
        featureItemMap.put(FEATURE_C2,
                new FeatureTabItem(
                        FEATURE_C2,
                        getString(R.string.feature_c2),
                        R.drawable.feature_c2,
                        new FeatureConfig().setAlgorithmConfig(new AlgorithmConfig(C2AlgorithmTask.C2.getKey(), mapOf(C2AlgorithmTask.C2.getKey(), true)))
                                .setActivityClassName(AlgorithmActivity.class.getName()).setImageSourceConfig(new ImageSourceConfig(TYPE_CAMERA, String.valueOf(Camera.CameraInfo.CAMERA_FACING_BACK)))
                                .setFeatureCategory(FEATURE_C2)
                ));
        featureItemMap.put(FEATURE_CAR,
                new FeatureTabItem(
                        FEATURE_CAR,
                        getString(R.string.feature_car),
                        R.drawable.feature_car,
                        new FeatureConfig().setAlgorithmConfig(new AlgorithmConfig(CarAlgorithmTask.CAR_ALGO.getKey(), mapOf(CarAlgorithmTask.CAR_ALGO.getKey(), true, CarAlgorithmTask.CAR_RECOG.getKey(), true)))
                                .setActivityClassName(AlgorithmActivity.class.getName()).setImageSourceConfig(new ImageSourceConfig(TYPE_CAMERA, String.valueOf(Camera.CameraInfo.CAMERA_FACING_BACK)))
                                .setFeatureCategory(FEATURE_CAR)
                ));
        featureItemMap.put(FEATURE_VIDEO_CLS,
                new FeatureTabItem(
                        FEATURE_VIDEO_CLS,
                        getString(R.string.feature_video_cls),
                        R.drawable.feature_video_cls,
                        new FeatureConfig().setAlgorithmConfig(new AlgorithmConfig(VideoClsAlgorithmTask.VIDEO_CLS.getKey(), mapOf(VideoClsAlgorithmTask.VIDEO_CLS.getKey(), true)))
                                .setActivityClassName(AlgorithmActivity.class.getName()).setImageSourceConfig(new ImageSourceConfig(TYPE_CAMERA, String.valueOf(Camera.CameraInfo.CAMERA_FACING_BACK)))
                                .setFeatureCategory(FEATURE_VIDEO_CLS)
                ));
        featureItemMap.put(FEATURE_FACE_VREIFY,
                new FeatureTabItem(
                        FEATURE_FACE_VREIFY,
                        getString(R.string.feature_face_verify),
                        R.drawable.feature_face_verify,
                        new FeatureConfig().setAlgorithmConfig(new AlgorithmConfig(FaceVerifyAlgorithmTask.FACE_VERIFY.getKey(), mapOf(FaceVerifyAlgorithmTask.FACE_VERIFY.getKey(), true)))
                                .setActivityClassName(AlgorithmActivity.class.getName())
                                .setFeatureCategory(FEATURE_FACE_VREIFY)
                ));
        featureItemMap.put(FEATURE_FACE_CLUSTER,
                new FeatureTabItem(
                        FEATURE_FACE_CLUSTER,
                        getString(R.string.feature_face_cluster),
                        R.drawable.feature_face_cluster,
                        new FeatureConfig().setAlgorithmConfig(new AlgorithmConfig(FaceClusterAlgorithmTask.FACE_CLUSTER.getKey(), mapOf(FaceClusterAlgorithmTask.FACE_CLUSTER.getKey(), true)))
                                .setActivityClassName(AlgorithmActivity.class.getName())
                                .setUiConfig(new UIConfig().setEnbaleAblum(false))
                                .setFeatureCategory(FEATURE_FACE_CLUSTER)
                ));
        featureItemMap.put(FEATURE_DYNAMIC_GESTURE,
                new FeatureTabItem(
                        FEATURE_DYNAMIC_GESTURE,
                        getString(R.string.feature_dynamic_gesture),
                        R.drawable.feature_dynamic_gesture,
                        new FeatureConfig().setAlgorithmConfig(new AlgorithmConfig(DynamicGestureAlgorithmTask.DYNAMIC_GESTURE.getKey(), mapOf(DynamicGestureAlgorithmTask.DYNAMIC_GESTURE.getKey(), true)))
                                .setActivityClassName(AlgorithmActivity.class.getName())
                                .setFeatureCategory(FEATURE_DYNAMIC_GESTURE)
                ));
        featureItemMap.put(FEATURE_LICENSE_CAKE,
                new FeatureTabItem(
                        FEATURE_LICENSE_CAKE,
                        getString(R.string.license_cake),
                        R.drawable.feature_license_cake,
                        new FeatureConfig().setAlgorithmConfig(new AlgorithmConfig(LicenseCakeAlgorithmTask.LICENSE_CAKE.getKey(), mapOf(LicenseCakeAlgorithmTask.LICENSE_CAKE.getKey(), true)))
                                .setActivityClassName(AlgorithmActivity.class.getName())
                ));

        featureItemMap.put(FEATURE_SKIN_SEGMENTATION,
                new FeatureTabItem(
                        FEATURE_SKIN_SEGMENTATION,
                        getString(R.string.feature_skin_segmentation),
                        R.drawable.feature_skin_segmentation,
                        new FeatureConfig().setAlgorithmConfig(new AlgorithmConfig(SkinSegmentationAlgorithmTask.SKIN_SEGMENTATION.getKey(), mapOf(SkinSegmentationAlgorithmTask.SKIN_SEGMENTATION.getKey(), true)))
                                .setActivityClassName(AlgorithmActivity.class.getName())
                                .setFeatureCategory(FEATURE_SKIN_SEGMENTATION)
                ));
        featureItemMap.put(FEATURE_BACH_SKELETON,
                new FeatureTabItem(
                        FEATURE_BACH_SKELETON,
                        getString(R.string.feature_bach_skeleton),
                        R.drawable.feature_bach_skeleton,
                        new FeatureConfig().setAlgorithmConfig(new AlgorithmConfig(BachSkeletonAlgorithmTask.BACH_SKELETON.getKey(), mapOf(BachSkeletonAlgorithmTask.BACH_SKELETON.getKey(), true)))
                                .setActivityClassName(AlgorithmActivity.class.getName())
                                .setFeatureCategory(FEATURE_BACH_SKELETON)
                ));
        featureItemMap.put(FEATURE_CHROMA_KEYING,
                new FeatureTabItem(
                        FEATURE_CHROMA_KEYING,
                        getString(R.string.feature_chroma_keying),
                        R.drawable.feature_chroma_keying,
                        new FeatureConfig().setAlgorithmConfig(new AlgorithmConfig(ChromaKeyingAlgorithmTask.CHROMA_KEYING.getKey(), mapOf(ChromaKeyingAlgorithmTask.CHROMA_KEYING.getKey(), true)))
                                .setActivityClassName(AlgorithmActivity.class.getName())
                                .setFeatureCategory(FEATURE_CHROMA_KEYING)
                ));
        featureItemMap.put(FEATURE_SLAM_ALGO,
                new FeatureTabItem(
                        FEATURE_SLAM_ALGO,
                        getString(R.string.slam_tab),
                        R.drawable.feature_slam_algo,
                        new FeatureConfig().setAlgorithmConfig(new AlgorithmConfig(SlamAlgorithmTask.SLAM.getKey(), mapOf(SlamAlgorithmTask.SLAM.getKey(), true)))
                                .setActivityClassName(AlgorithmActivity.class.getName())
                                .setUiConfig(new UIConfig().setEnbaleAblum(false))
                                .setImageSourceConfig(new ImageSourceConfig(TYPE_CAMERA, String.valueOf(Camera.CameraInfo.CAMERA_FACING_BACK)))
                                .setFeatureCategory(FEATURE_SLAM_ALGO)
                ));
        featureItemMap.put(FEATURE_SKELETON_3D,
                new FeatureTabItem(
                        FEATURE_SKELETON_3D,
                        getString(R.string.feature_skeleton_3d),
                        R.drawable.feature_skeleton3d,
                        new FeatureConfig().setAlgorithmConfig(new AlgorithmConfig(Skeleton3DAlgorithmTask.SKELETON3D.getKey(), mapOf(Skeleton3DAlgorithmTask.SKELETON3D.getKey(), true)))
                                .setActivityClassName(AlgorithmActivity.class.getName())
                ));
        featureItemMap.put(FEATURE_AVABOOST,
                new FeatureTabItem(
                        FEATURE_AVABOOST,
                        getString(R.string.feature_emotion_driven),
                        R.drawable.ic_feature_emotion_driven,
                        new FeatureConfig().setAlgorithmConfig(new AlgorithmConfig(AvaBoostAlgorithmTask.AVABOOST.getKey(), mapOf(AvaBoostAlgorithmTask.AVABOOST.getKey(), true)))
                                .setActivityClassName(AlgorithmActivity.class.getName())
                ));
        featureItemMap.put(FEATURE_OBJECT_TRACKING,
                new FeatureTabItem(
                        FEATURE_OBJECT_TRACKING,
                        getString(R.string.object_tracking),
                        R.drawable.feature_slam_algo,
                        new FeatureConfig().setAlgorithmConfig(new AlgorithmConfig(ObjectTrackingAlgorithmTask.OBJECT_TRACKING.getKey(), mapOf(ObjectTrackingAlgorithmTask.OBJECT_TRACKING.getKey(), true)))
                                .setActivityClassName(AlgorithmActivity.class.getName())
                                .setImageSourceConfig(new ImageSourceConfig(TYPE_CAMERA, String.valueOf(Camera.CameraInfo.CAMERA_FACING_BACK)))
                ));
        featureItemMap.put(FEATURE_VIDEO_SR,
                new FeatureTabItem(
                        FEATURE_VIDEO_SR,
                        getString(R.string.feature_video_sr),
                        R.drawable.feature_video_sr,
                        new FeatureConfig().setImageQualityConfig(new ImageQualityConfig(ImageQualityConfig.KEY_VIDEO_SR))
                                .setActivityClassName(ImageQualityActivity.class.getName())
                                .setFeatureCategory(FEATURE_VIDEO_SR)
                ));
        featureItemMap.put(FEATURE_NIGHT_SCENE,
                new FeatureTabItem(
                        FEATURE_NIGHT_SCENE,
                        getString(R.string.feature_night_scene),
                        R.drawable.feature_night_scene,
                        new FeatureConfig().setImageQualityConfig(new ImageQualityConfig(ImageQualityConfig.KEY_NIGHT_SCENE))
                                .setActivityClassName(ImageQualityActivity.class.getName())
                ));
        featureItemMap.put(FEATURE_ONEKEY_ENHANCE, new FeatureTabItem(
                FEATURE_ONEKEY_ENHANCE,
                getString(R.string.feature_onekey_enhance),
                R.drawable.feature_onekey,
                new FeatureConfig()
                        .setImageQualityConfig(new ImageQualityConfig(ImageQualityConfig.KEY_ONEKEY_ENHANCE))
                        .setActivityClassName(ImageQualityActivity.class.getName())
                        .setImageSourceConfig(new ImageSourceConfig(TYPE_CAMERA, String.valueOf(Camera.CameraInfo.CAMERA_FACING_BACK)))
        ));

        featureItemMap.put(FEATURE_VIDA, new FeatureTabItem(
                FEATURE_VIDA,
                getString(R.string.feature_vida),
                R.drawable.feature_vida,
                new FeatureConfig()
                        .setImageQualityConfig(new ImageQualityConfig(ImageQualityConfig.KEY_VIDA))
                        .setActivityClassName(ImageQualityActivity.class.getName())
                        .setFeatureCategory(FEATURE_VIDA)
        ));
        featureItemMap.put(FEATURE_TAINT_DETECT, new FeatureTabItem(
                FEATURE_TAINT_DETECT,
                getString(R.string.feature_taint_detect),
                R.drawable.feature_taint_detect,
                new FeatureConfig()
                        .setImageQualityConfig(new ImageQualityConfig(ImageQualityConfig.KEY_TAINT_DETECT))
                        .setActivityClassName(ImageQualityActivity.class.getName())
                        .setImageSourceConfig(new ImageSourceConfig(TYPE_CAMERA, String.valueOf(Camera.CameraInfo.CAMERA_FACING_BACK)))
                        .setFeatureCategory(FEATURE_TAINT_DETECT)
        ));
        featureItemMap.put(FEATURE_CINE_MOVE, new FeatureTabItem(
                FEATURE_CINE_MOVE,
                getString(R.string.feature_cine_move),
                R.drawable.feature_cine_move,
                new FeatureConfig()
                        .setImageQualityConfig(new ImageQualityConfig(ImageQualityConfig.KEY_CINE_MOVE))
                        .setActivityClassName(ImageQualityActivity.class.getName())
                        .setImageSourceConfig(new ImageSourceConfig(TYPE_CAMERA, String.valueOf(Camera.CameraInfo.CAMERA_FACING_FRONT)))
        ));
        featureItemMap.put(FEATURE_VIDEO_LITE_HDR, new FeatureTabItem(
                FEATURE_VIDEO_LITE_HDR,
                getString(R.string.feature_video_lite_hdr),
                R.drawable.feature_lite_hdr,
                new FeatureConfig()
                        .setImageQualityConfig(new ImageQualityConfig(ImageQualityConfig.KEY_VIDEO_LITE_HDR))
                        .setActivityClassName(ImageQualityActivity.class.getName())
                        .setImageSourceConfig(new ImageSourceConfig(TYPE_CAMERA, String.valueOf(Camera.CameraInfo.CAMERA_FACING_FRONT)))
        ));
        featureItemMap.put(FEATURE_ADAPTIVE_SHARPEN,
                new FeatureTabItem(
                        FEATURE_ADAPTIVE_SHARPEN,
                        getString(R.string.feature_adaptive_sharpen),
                        R.drawable.feature_adaptive_sharpen,
                        new FeatureConfig().setImageQualityConfig(new ImageQualityConfig(ImageQualityConfig.KEY_ADAPTIVE_SHARPEN))
                                .setActivityClassName(ImageQualityActivity.class.getName())
                ));
        featureItemMap.put(FEATURE_VFI,
                new FeatureTabItem(
                        FEATURE_VFI,
                        getString(R.string.feature_video_vfi),
                        R.drawable.feature_vfi,
                        new FeatureConfig().setImageQualityConfig(new ImageQualityConfig(ImageQualityConfig.KEY_VFI))
                                .setActivityClassName(LensUploadActivity.class.getName())
                                .setUiConfig(new UIConfig().setEnableRotate(false))
                                .setFeatureCategory(FEATURE_VFI)
                ));
        featureItemMap.put(FEATURE_VIDEO_STAB,
                new FeatureTabItem(
                        FEATURE_VIDEO_STAB,
                        getString(R.string.feature_video_stab),
                        R.drawable.feature_video_stab,
                        new FeatureConfig().setImageQualityConfig(new ImageQualityConfig(ImageQualityConfig.KEY_VIDEO_STAB))
                                .setActivityClassName(LensUploadActivity.class.getName())
                                .setUiConfig(new UIConfig().setEnableRotate(false))
                                .setFeatureCategory(FEATURE_VIDEO_STAB)
                ));
        featureItemMap.put(FEATURE_VIDEO_DEFLICKER,
                new FeatureTabItem(
                        FEATURE_VIDEO_DEFLICKER,
                        getString(R.string.feature_video_deflicker),
                        R.drawable.feature_video_deflicker,
                        new FeatureConfig().setImageQualityConfig(new ImageQualityConfig(ImageQualityConfig.KEY_VIDEO_DEFLICKER))
                                .setActivityClassName(LensUploadActivity.class.getName())
                                .setUiConfig(new UIConfig().setEnableRotate(false))
                                .setFeatureCategory(FEATURE_VIDEO_DEFLICKER)
                ));

        featureItemMap.put(FEATURE_SPORT_ASSISTANCE,
                new FeatureTabItem(
                        FEATURE_SPORT_ASSISTANCE,
                        getString(R.string.feature_sport_assistance),
                        R.drawable.feature_sports,
                        new FeatureConfig().setActivityClassName(SportsHomeActivity.class.getName())
                                .setFeatureCategory(FEATURE_SPORT_ASSISTANCE)
                ));
        featureItemMap.put(FEATURE_PHOTO_NIGHT_SCENE,
                new FeatureTabItem(
                        FEATURE_PHOTO_NIGHT_SCENE,
                        getString(R.string.feature_photo_night_scene),
                        R.drawable.feature_night_scene,
                        new FeatureConfig().setImageQualityConfig(new ImageQualityConfig(ImageQualityConfig.KEY_PHOTO_NIGHT_SCENE))
                                .setActivityClassName(PhotoImageQualityActivity.class.getName())
                                .setUiConfig(new UIConfig().setEnbaleAblum(false).setEnableRotate(false))
                                .setImageSourceConfig(new ImageSourceConfig(TYPE_CAMERA, String.valueOf(Camera.CameraInfo.CAMERA_FACING_BACK))))
        );
        featureItemMap.put(FEATURE_AR_SLAM,
                new FeatureTabItem(
                        FEATURE_AR_SLAM,
                        getString(R.string.feature_ar_slam),
                        R.drawable.feature_ar_slam,
                        new FeatureConfig().setStickerConfig(new StickerConfig().setType(FEATURE_AR_SLAM)).setActivityClassName(StickerActivity.class.getName())
                                .setImageSourceConfig(new ImageSourceConfig(TYPE_CAMERA, String.valueOf(Camera.CameraInfo.CAMERA_FACING_BACK)))
                                .setUiConfig(new UIConfig().setEnbaleAblum(false).setEnableRotate(false))
                ));
        featureItemMap.put(FEATURE_AR_OBJECT,
                new FeatureTabItem(
                        FEATURE_AR_OBJECT,
                        getString(R.string.feature_ar_object),
                        R.drawable.feature_ar_obj,
                        new FeatureConfig().setStickerConfig(new StickerConfig().setType(FEATURE_AR_OBJECT)).setActivityClassName(StickerActivity.class.getName())
                                .setImageSourceConfig(new ImageSourceConfig(TYPE_CAMERA, String.valueOf(Camera.CameraInfo.CAMERA_FACING_BACK)))
                                .setUiConfig(new UIConfig().setEnbaleAblum(false).setEnableRotate(false))


                ));

        featureItemMap.put(FEATURE_AR_LANDMARK,
                new FeatureTabItem(
                        FEATURE_AR_LANDMARK,
                        getString(R.string.feature_ar_landmark),
                        R.drawable.feature_ar_landmark,
                        new FeatureConfig().setStickerConfig(new StickerConfig().setType(FEATURE_AR_LANDMARK)).setActivityClassName(StickerActivity.class.getName())
                                .setImageSourceConfig(new ImageSourceConfig(TYPE_CAMERA, String.valueOf(Camera.CameraInfo.CAMERA_FACING_BACK)))
                                .setUiConfig(new UIConfig().setEnbaleAblum(false).setEnableRotate(false))


                ));
        featureItemMap.put(FEATURE_AR_SKY_LAND,
                new FeatureTabItem(
                        FEATURE_AR_SKY_LAND,
                        getString(R.string.feature_ar_sky_land),
                        R.drawable.feature_ar_sky_land,
                        new FeatureConfig().setStickerConfig(new StickerConfig().setType(FEATURE_AR_SKY_LAND)).setActivityClassName(StickerActivity.class.getName())
                                .setImageSourceConfig(new ImageSourceConfig(TYPE_CAMERA, String.valueOf(Camera.CameraInfo.CAMERA_FACING_BACK)))
                                .setUiConfig(new UIConfig().setEnbaleAblum(false).setEnableRotate(false))


                ));
        featureItemMap.put(FEATURE_TEST_STICKER,
                new FeatureTabItem(
                        FEATURE_TEST_STICKER,
                        getString(R.string.feature_test_sticker),
                        R.drawable.feature_sticker,
                        new FeatureConfig()
                                .setActivityClassName(StickerTestActivity.class.getName())
                ));
    }

    public List<FeatureTab> getFeatureTabs(MainPageConfig mainPageConfig) {
        ArrayList<FeatureTab> totalFeatureTab = new ArrayList<>();
        if (mainPageConfig != null) {
            totalFeatureTab.addAll(getPageConfigFeatureTabs(mainPageConfig));
        }
        totalFeatureTab.addAll(getLocalFeatureTabs());
        return totalFeatureTab;
    }

    private List<FeatureTab> getPageConfigFeatureTabs(MainPageConfig mainPageConfig) {
        ArrayList<FeatureTab> featureTabs = new ArrayList<>();
        List<EBoxPageConfigGroup> EBoxPageConfigGroupList = mainPageConfig.getEboxPageConfigGroups();
        for (int i = 0; i < EBoxPageConfigGroupList.size(); i++) {
            EBoxPageConfigGroup group = EBoxPageConfigGroupList.get(i);
            List<Page> pageList = group.getPages();
            ArrayList<FeatureTabItem> featureTabItemList = new ArrayList<>();
            for (int pageIndex = 0; pageIndex < pageList.size(); pageIndex++) {
                Page page = pageList.get(pageIndex);
                if (page != null) {
                    featureTabItemList.add(convertPageToFeatureTabItem(page, mainPageConfig.getPageDetails()));
                }
            }
            String title = "";
            if (group.getTitleDict() != null) {
                title = group.getTitleDict().getTitle();
            }
            FeatureTab featureTab = new FeatureTab(group.getId(), title, featureTabItemList);
            featureTabs.add(featureTab);
        }
        return featureTabs;
    }

    private FeatureTabItem convertPageToFeatureTabItem(Page page, List<PageDetail> pageDetailList) {
        PageDetail matchedPageDetail = null;
        for (int i = 0; i < pageDetailList.size(); i++) {
            PageDetail pageDetail = pageDetailList.get(i);
            if (page.getId().equals(pageDetail.getId())) {
                matchedPageDetail = pageDetail;
                break;
            }
        }
        String pageTitle = "";
        if (page.getTitleDict() != null) {
            pageTitle = page.getTitleDict().getTitle();
        }
        if (matchedPageDetail != null) {
            matchedPageDetail.setTitle(pageTitle);
        }
        int icon = getPageItemIcon(page.getId());
        return new FeatureTabItem(
                page.getId(),
                pageTitle,
                icon,
                new FeatureConfig().
                        setActivityClassName("com.effectsar.labcv.ebox.activity.EBoxRecordActivity")
                        .setPageDetail(matchedPageDetail)
        );
    }

    private String getString(@StringRes int redId) {
        return AppSingleton.instance.getString(redId);
    }

    private List<FeatureTab> getLocalFeatureTabs() {
        ArrayList<FeatureTab> sFeatureTabs = new ArrayList<>();
        String data;
        if (EffectEBoxConfig.useEbox()) {
            data = StreamUtils.readString(AppSingleton.instance, R.raw.ebox_custom_config);
        } else {
            data = StreamUtils.readString(AppSingleton.instance, R.raw.custom_config);
        }
        if (!TextUtils.isEmpty(data)) {
            try {
                JSONArray jsonArray = new JSONArray(data);

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String group = jsonObject.optString("group");
                    if (!groupTitleMap.containsKey(group)) {
                        continue;
                    }
                    FeatureTab tab = new FeatureTab(group, groupTitleMap.get(group), new ArrayList<>());

                    JSONArray features = jsonObject.getJSONArray("features");
                    for (int j = 0; j < features.length(); j++) {
                        String feature = features.optString(j);
                        if (!featureItemMap.containsKey(feature)) {
                            continue;
                        }
                        if (LocaleUtils.isFaceLimit(AppSingleton.instance)) {
                            if (TextUtils.equals(feature, FEATURE_FACE_VREIFY) || TextUtils.equals(feature, FEATURE_FACE_CLUSTER)) {
                                continue;
                            }
                        }
                        //  {zh} SLAM AR黑名单检查  {en} SLAM AR Blacklist Check
                        if (SLAMBlackList.SLAM_BLACK_LIST.contains(Build.MODEL.toLowerCase())) {
                            if (TextUtils.equals(feature, FEATURE_AR_SLAM)
                                    || TextUtils.equals(feature, FEATURE_AR_OBJECT)
                                    || TextUtils.equals(feature, FEATURE_AR_LANDMARK)
                                    || TextUtils.equals(feature, FEATURE_AR_SKY_LAND)) {
                                continue;
                            }
                        }
                        if (!LocaleUtils.isAsia(AppSingleton.instance)) {
                            if (!feature.equals("feature_license_cake")) {
                                tab.addChild(featureItemMap.get(feature));
                            }
                        } else {
                            tab.addChild(featureItemMap.get(feature));
                        }
                    }
                    sFeatureTabs.add(tab);
                }
            } catch (JSONException e) {
                //do nothing
            }
        }

        return sFeatureTabs;
    }

    private static Map<String, Object> mapOf(Object... varargs) {
        if (varargs.length % 2 != 0) {
            throw new IllegalArgumentException("args count must be multiple of 2");
        }

        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < varargs.length; i += 2) {
            map.put((String) varargs[i], varargs[i + 1]);
        }
        return map;
    }

    private int getPageItemIcon(String id) {
        if (id.startsWith("pkg_")) {
            return R.drawable.feature_page_config;
        }
        switch (id) {
            case "beauty":
            case "beauty_pro":
                return R.drawable.feature_beauty;
            case "image_quality":
                return R.drawable.feature_image_quality;
            case "style_makeup":
                return R.drawable.feature_style_makeup;
            case "sticker":
                return R.drawable.feature_sticker;
            case "sticker_new":
                return R.drawable.feature_amazing_sticker;
            case "lite_game":
                return R.drawable.feature_game;
            case "live_gift":
                return R.drawable.feature_live_gift;
            case "animoji":
                return R.drawable.feature_avatar_drive;
            case "matting_bg":
                return R.drawable.feature_matting_sticker;
            case "background_blur":
                return R.drawable.feature_background_blur;
            case "matting_chroma_bg":
                return R.drawable.feature_chroma_matting;
            case "camera_movement":
                return R.drawable.feature_cine_move;
        }
        return R.drawable.feature_page_config;
    }
}
