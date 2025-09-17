package com.effectsar.labcv.sports.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.effectsar.labcv.core.algorithm.ActionRecognitionAlgorithmTask;
import com.effectsar.labcv.effectsdk.BefActionRecognitionInfo;

public class SportItem implements Parcelable {
    public static final String SPORT_ITEM_KEY = "sport_item_key";

    private ActionRecognitionAlgorithmTask.ActionType type;
    private String imgRes;
    private String retImgRes;
    private int textRes;
    private String previewVideoRes;
    private String maskRes;
    private boolean needLandscape;
    private float defaultMaxValue;

    private int sportTime;

    public SportItem(ActionRecognitionAlgorithmTask.ActionType type, String imgRes, String retImgRes, int textRes, String previewVideoRes, String maskRes, boolean needLandscape, float defaultMaxValue) {
        this.type = type;
        this.imgRes = imgRes;
        this.retImgRes = retImgRes;
        this.textRes = textRes;
        this.previewVideoRes = previewVideoRes;
        this.maskRes = maskRes;
        this.needLandscape = needLandscape;
        this.defaultMaxValue = defaultMaxValue;
    }

    public SportItem(ActionRecognitionAlgorithmTask.ActionType type, String imgRes, int textRes, String previewVideoRes, String maskRes, boolean needLandscape) {
        this.type = type;
        this.imgRes = imgRes;
        this.textRes = textRes;
        this.previewVideoRes = previewVideoRes;
        this.maskRes = maskRes;
        this.needLandscape = needLandscape;
    }

    public BefActionRecognitionInfo.ActionRecognitionPoseType readyPoseType() {
        switch (type) {
            case OPEN_CLOSE_JUMP:
            case DEEP_SQUAT:
            case HIGH_RUN:
                return BefActionRecognitionInfo.ActionRecognitionPoseType.STAND;
            case SIT_UP:
            case HIP_BRIDGE:
                return BefActionRecognitionInfo.ActionRecognitionPoseType.SITTING;
            case PUSH_UP:
            case PLANK:
            case KNEELING_PUSH_UP:
                return BefActionRecognitionInfo.ActionRecognitionPoseType.LYING;
            case LUNGE:
            case LUNGE_SQUAT:
                return BefActionRecognitionInfo.ActionRecognitionPoseType.SIDERIGHT;

        }
        return BefActionRecognitionInfo.ActionRecognitionPoseType.STAND;
    }

    public ActionRecognitionAlgorithmTask.ActionType getType() {
        return type;
    }

    public void setType(ActionRecognitionAlgorithmTask.ActionType type) {
        this.type = type;
    }

    public String getImgRes() {
        return imgRes;
    }

    public void setImgRes(String imgRes) {
        this.imgRes = imgRes;
    }

    public int getTextRes() {
        return textRes;
    }

    public void setTextRes(int textRes) {
        this.textRes = textRes;
    }

    public void setPreviewVideoRes(String previewVideoRes) {
        this.previewVideoRes = previewVideoRes;
    }

    public String getPreviewVideoRes() {
        return previewVideoRes;
    }

    public int getSportTime() {
        return sportTime;
    }

    public void setSportTime(int sportTime) {
        this.sportTime = sportTime;
    }

    public boolean getNeedLandscape() {
        return needLandscape;
    }

    public void setNeedLandscape(boolean needLandscape) {
        this.needLandscape = needLandscape;
    }

    public void setDefaultMaxValue(float defaultMaxValue) { this.defaultMaxValue = defaultMaxValue; }

    public float getDefaultMaxValue() {
        return defaultMaxValue;
    }

    public String getMaskRes() {
        return maskRes;
    }

    public void setMaskRes(String maskRes) {
        this.maskRes = maskRes;
    }

    public String getRetImgRes() {
        return retImgRes;
    }

    public void setRetImgRes(String retImgRes) {
        this.retImgRes = retImgRes;
    }

    public boolean isNeedLandscape() {
        return needLandscape;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.type == null ? -1 : this.type.ordinal());
        dest.writeString(this.imgRes);
        dest.writeString(this.retImgRes);
        dest.writeInt(this.textRes);
        dest.writeString(this.previewVideoRes);
        dest.writeString(this.maskRes);
        dest.writeByte(this.needLandscape ? (byte) 1 : (byte) 0);
        dest.writeInt(this.sportTime);
        dest.writeFloat(this.defaultMaxValue);
    }

    protected SportItem(Parcel in) {
        int tmpType = in.readInt();
        this.type = tmpType == -1 ? null : ActionRecognitionAlgorithmTask.ActionType.values()[tmpType];
        this.imgRes = in.readString();
        this.retImgRes = in.readString();
        this.textRes = in.readInt();
        this.previewVideoRes = in.readString();
        this.maskRes = in.readString();
        this.needLandscape = in.readByte() != 0;
        this.sportTime = in.readInt();
        this.defaultMaxValue = in.readFloat();
    }

    public static final Creator<SportItem> CREATOR = new Creator<SportItem>() {
        @Override
        public SportItem createFromParcel(Parcel source) {
            return new SportItem(source);
        }

        @Override
        public SportItem[] newArray(int size) {
            return new SportItem[size];
        }
    };
}
