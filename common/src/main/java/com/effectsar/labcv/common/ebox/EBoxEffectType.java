package com.effectsar.labcv.common.ebox;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import com.effectsar.labcv.common.R;

/**
 * Author: gaojin.ivy
 * Time: 2025/6/20 15:21
 */

public enum EBoxEffectType {
    AMAZING_STICKER("sticker_new", R.string.feature_amazing_sticker, R.drawable.icon_ebox_amazing_sticker),
    GIFT_STICKER("sticker_live_gift", R.string.feature_gift_sticker, R.drawable.icon_ebox_gift_sticker),
    GAME_STICKER("sticker_lite_game", R.string.feature_game, R.drawable.icon_ebox_small_game),
    AVATAR_DRIVE_STICKER("sticker_animoji", R.string.feature_avatar_drive, R.drawable.icon_ebox_avatar_drive),
    CINE_MOVE_STICKER("sticker_camera_movement", R.string.feature_cine_move, R.drawable.icon_ebox_cine_move),


    BEAUTY("beauty", R.string.feature_beauty_lite, R.drawable.icon_ebox_beauty),
    FILTER("filter", R.string.filter, R.drawable.icon_ebox_filter),
    IMAGE_QUALITY("image_quality", R.string.feature_image_quality, R.drawable.icon_ebox_image_quality),
    STYLE_MAKEUP("style_makeup", R.string.feature_style_makeup, R.drawable.icon_ebox_style_makeup),
    MATTING("matting_bg", R.string.tab_matting, R.drawable.icon_ebox_matting_bg),
    MATTING_CHROMA("matting_chroma_bg", R.string.feature_chroma_matting_sticker, R.drawable.icon_ebox_matting_chroma),
    BACKGROUND_BLUR("background_blur", R.string.feature_background_blur, R.drawable.icon_ebox_background_blur),
    STICKER("sticker", R.string.sticker, R.drawable.icon_ebox_sticker),
    UNKNOWN("unknown", R.string.unknown, 0);

    private final String key;
    @StringRes
    private final int nameResId;
    @DrawableRes
    private final int icon;

    EBoxEffectType(String key, int nameResId, int icon) {
        this.key = key;
        this.nameResId = nameResId;
        this.icon = icon;
    }

    public String getKey() {
        return key;
    }

    public int getNameResId() {
        return nameResId;
    }

    public int getIcon() {
        return icon;
    }

    public static EBoxEffectType parsePanelKeyToEffectType(String panelKey) {
        for (EBoxEffectType type : EBoxEffectType.values()) {
            if (panelKey.startsWith(type.getKey())) {
                return type;
            }
        }
        return EBoxEffectType.UNKNOWN;
    }
}
