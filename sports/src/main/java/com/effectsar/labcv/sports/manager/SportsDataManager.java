package com.effectsar.labcv.sports.manager;

import android.content.Context;

import com.effectsar.labcv.core.ResourceHelper;
import com.effectsar.labcv.core.algorithm.ActionRecognitionAlgorithmTask;
import com.effectsar.labcv.sports.R;
import com.effectsar.labcv.sports.model.NumberPickerItem;
import com.effectsar.labcv.sports.model.SportItem;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class SportsDataManager {

    public String getMediaPath(Context context){
        return new ResourceHelper(context).getMaterialPath("sportAssistantImageRecorder");
    }

    public String getMediaPath(Context context, String media){
        return new File(getMediaPath(context), media).getAbsolutePath();
    }

    public List<SportItem> getHomeItems(Context context) {
        return Arrays.asList(
                new SportItem(ActionRecognitionAlgorithmTask.ActionType.OPEN_CLOSE_JUMP, getMediaPath(context, "img_openclose.png"),
                        getMediaPath(context, "img_openclose_square.png"), R.string.open_close_jump,
                        getMediaPath(context, "sa_openclose.mp4"), getMediaPath(context, "mask_open_close_jump.svg"), false, 0.5f),
                new SportItem(ActionRecognitionAlgorithmTask.ActionType.DEEP_SQUAT, getMediaPath(context, "img_squat.png"),
                        getMediaPath(context, "img_squat_square.png"), R.string.deep_squat,
                        getMediaPath(context, "sa_squat.mp4"), getMediaPath(context, "mask_open_close_jump.svg"), false, 0.5f),
                new SportItem(ActionRecognitionAlgorithmTask.ActionType.PLANK, getMediaPath(context, "img_plank.png"),
                        getMediaPath(context, "img_plank_square.png"), R.string.plank,
                        getMediaPath(context, "sa_plank.mp4"), getMediaPath(context, "mask_push_up.svg"), true, 0.55f),
                new SportItem(ActionRecognitionAlgorithmTask.ActionType.PUSH_UP, getMediaPath(context, "img_pushup.png"),
                        getMediaPath(context, "img_pushup_square.png"), R.string.push_up,
                        getMediaPath(context, "sa_pushup.mp4"), getMediaPath(context, "mask_push_up.svg"), true, 0.52f),
                new SportItem(ActionRecognitionAlgorithmTask.ActionType.SIT_UP, getMediaPath(context, "img_situp.png"),
                        getMediaPath(context, "img_situp_square.png"), R.string.sit_up,
                        getMediaPath(context, "sa_situp.mp4"), getMediaPath(context, "mask_sit_up.svg"), true, 0.46f),
                new SportItem(ActionRecognitionAlgorithmTask.ActionType.HIGH_RUN, getMediaPath(context, "img_high_run.png"),
                        getMediaPath(context, "img_high_run_square.png"), R.string.sport_assistance_high_run,
                        getMediaPath(context, "sa_high_run.mp4"), getMediaPath(context, "mask_open_close_jump.svg"), false, 0.5f),
                new SportItem(ActionRecognitionAlgorithmTask.ActionType.LUNGE, getMediaPath(context, "img_lunge.png"),
                        getMediaPath(context, "img_lunge_square.png"), R.string.sport_assistance_lunge,
                        getMediaPath(context, "sa_lunge.mp4"), getMediaPath(context, "mask_deep_squat.svg"), false, 0.5f),
                new SportItem(ActionRecognitionAlgorithmTask.ActionType.HIP_BRIDGE, getMediaPath(context, "img_hip_bridge.png"),
                        getMediaPath(context, "img_hip_bridge_square.png"), R.string.sport_assistance_hip_bridge,
                        getMediaPath(context, "sa_hip_bridge.mp4"), getMediaPath(context, "mask_sit_up.svg"), true, 0.49f),
                new SportItem(ActionRecognitionAlgorithmTask.ActionType.LUNGE_SQUAT, getMediaPath(context, "img_lunge_squat.png"),
                        getMediaPath(context, "img_lunge_squat_square.png"), R.string.sport_assistance_lunge_squat,
                        getMediaPath(context, "sa_lunge_squat.mp4"), getMediaPath(context, "mask_deep_squat.svg"), false, 0.53f),
                new SportItem(ActionRecognitionAlgorithmTask.ActionType.KNEELING_PUSH_UP, getMediaPath(context, "img_kneeling_push_up.png"),
                        getMediaPath(context, "img_kneeling_push_up_square.png"), R.string.sport_assistance_kneeling_pushup,
                        getMediaPath(context, "sa_kneeling_pushup.mp4"), getMediaPath(context, "mask_push_up.svg"), true, 0.52f)
        );
    }

    public static NumberPickerItem getMinutePickerItem(String suffix) {
        return new NumberPickerItem(
                0, 59, 2, suffix
        );
    }

    public static NumberPickerItem getSecondPickerItem(String suffix) {
        return new NumberPickerItem(
                0, 59, 0, suffix
        );
    }
}
