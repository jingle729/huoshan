package com.effectsar.labcv.lens.manager;

import com.effectsar.labcv.common.model.ButtonItem;
import com.effectsar.labcv.effectsdk.EffectsSDKEffectConstants;
import com.effectsar.labcv.lens.config.ImageQualityConfig;
import com.effectsar.labcv.lens.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImageQualityDataManager {
    private static Map<String, ImageQualityItem> sMap;

    public ImageQualityItem getItem(String key) {
        if (sMap != null) {
            return sMap.get(key);
        }

        sMap = new HashMap<>();
        sMap.put(ImageQualityConfig.KEY_VIDEO_SR, new ImageQualityItem(R.string.tab_video_sr,
                R.drawable.ic_video_sr, EffectsSDKEffectConstants.ImageQualityType.IMAGE_QUALITY_TYPE_VIDEO_SR));
        sMap.put(ImageQualityConfig.KEY_NIGHT_SCENE, new ImageQualityItem(R.string.tab_night_scene,
                R.drawable.ic_video_sr, EffectsSDKEffectConstants.ImageQualityType.IMAGE_QUALITY_TYPE_NIGHT_SCENE));
        sMap.put(ImageQualityConfig.KEY_ADAPTIVE_SHARPEN, new ImageQualityItem(R.string.tab_adaptive_sharpen,
                R.drawable.ic_adaptive_sharpen, EffectsSDKEffectConstants.ImageQualityType.IMAGE_QUALITY_TYPE_ADAPTIVE_SHARPEN));
        sMap.put(ImageQualityConfig.KEY_PHOTO_NIGHT_SCENE, new ImageQualityItem(R.string.feature_photo_night_scene, R.drawable.ic_night_secene_, EffectsSDKEffectConstants.ImageQualityType.IMAGE_QUALITY_TYPE_NIGHT_SCENE));
        sMap.put(ImageQualityConfig.KEY_VFI, new ImageQualityItem(R.string.feature_video_vfi, R.drawable.ic_video_fi, EffectsSDKEffectConstants.ImageQualityType.IMAGE_QUALITY_TYPE_NONE));
        sMap.put(ImageQualityConfig.KEY_ONEKEY_ENHANCE, new ImageQualityItem(R.string.feature_onekey_enhance, R.drawable.ic_onekey, EffectsSDKEffectConstants.ImageQualityType.IMAGE_QUALITY_TYPE_ONEKEY_ENHANCE));
        sMap.put(ImageQualityConfig.KEY_VIDA, new ImageQualityItem(R.string.feature_vida, R.drawable.ic_vida, EffectsSDKEffectConstants.ImageQualityType.IMAGE_QUALITY_TYPE_VIDAS));
        sMap.put(ImageQualityConfig.KEY_TAINT_DETECT, new ImageQualityItem(R.string.feature_taint_detect, R.drawable.ic_taint_detect, EffectsSDKEffectConstants.ImageQualityType.IMAGE_QUALITY_TYPE_TAINT_DETECT));
        sMap.put(ImageQualityConfig.KEY_VIDEO_LITE_HDR, new ImageQualityItem(R.string.feature_video_lite_hdr, R.drawable.ic_lite_hdr, EffectsSDKEffectConstants.ImageQualityType.IMAGE_QUALITY_TYPE_VIDEO_LITE_HDR ));
        sMap.put(ImageQualityConfig.KEY_VIDEO_STAB, new ImageQualityItem(R.string.feature_video_stab, R.drawable.ic_video_stab, EffectsSDKEffectConstants.ImageQualityType.IMAGE_QUALITY_TYPE_NONE));

        List<ImageQualityItem> items = new ArrayList<>();
        items.add(new ImageQualityItem(R.string.feature_cine_move_snake, R.drawable.ic_cine_move_snake, EffectsSDKEffectConstants.ImageQualityType.IMAGE_QUALITY_TYPE_CINE_MOVE_ALG_SNAKE_V8));
        items.add(new ImageQualityItem(R.string.feature_cine_move_heart_beat, R.drawable.ic_cine_move_beat, EffectsSDKEffectConstants.ImageQualityType.IMAGE_QUALITY_TYPE_CINE_MOVE_ALG_HEART_BEAT_V9));
        items.add(new ImageQualityItem(R.string.feature_cine_move_breath, R.drawable.ic_cine_move_berath, EffectsSDKEffectConstants.ImageQualityType.IMAGE_QUALITY_TYPE_CINE_MOVE_ALG_BREATH_V10));
        items.add(new ImageQualityItem(R.string.feature_cine_move_rot360, R.drawable.ic_cine_move_rov, EffectsSDKEffectConstants.ImageQualityType.IMAGE_QUALITY_TYPE_CINE_MOVE_ALG_ROT360_V11));
        ImageQualityItemGroup itemGroup = new ImageQualityItemGroup(items);
        itemGroup.setSelect(items.get(0));
        itemGroup.setTitle(R.string.feature_cine_move);
        sMap.put(ImageQualityConfig.KEY_CINE_MOVE, itemGroup);
        return sMap.get(key);
    }

    public class ImageQualityItem  extends ButtonItem {
        private EffectsSDKEffectConstants.ImageQualityType type;

        public ImageQualityItem() {}

        public ImageQualityItem(int title, int icon, EffectsSDKEffectConstants.ImageQualityType type) {
            super(title, icon, 0);
            this.type = type;
        }

        public EffectsSDKEffectConstants.ImageQualityType getType() {
            return type;
        }

        public void setType(EffectsSDKEffectConstants.ImageQualityType type) {
            this.type = type;
        }
    }

    public class ImageQualityItemGroup extends ImageQualityItem {
        private List<ImageQualityItem> items;
        private ImageQualityItem selectItem = null;
        private boolean scrollable;

        public ImageQualityItemGroup(List<ImageQualityItem> items) {
            this.items = items;
        }

        public ImageQualityItemGroup(List<ImageQualityItem> items, boolean scrollable) {
            this.items = items;
            this.scrollable = scrollable;
        }

        public List<ImageQualityItem> getItems() {
            return items;
        }

        public void setItems(List<ImageQualityItem> items) {
            this.items = items;
        }

        public void setSelect(ImageQualityItem item) {
            this.selectItem = item;
        }

        public ImageQualityItem getSelect(){ return selectItem; }

    }
}
