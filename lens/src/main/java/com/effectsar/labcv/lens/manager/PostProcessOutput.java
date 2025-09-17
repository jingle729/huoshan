package com.effectsar.labcv.lens.manager;

public class PostProcessOutput {
    private int texture;
    private boolean processDone;
    private long timeCost;
    private boolean needRecord;

    public PostProcessOutput() {
    }

    public PostProcessOutput(int texture, boolean processDone, long timeCost, boolean needRecord) {
        this.texture = texture;
        this.processDone = processDone;
        this.timeCost = timeCost;
        this.needRecord = needRecord;
    }

    public int getTexture() {
        return texture;
    }

    public void setTexture(int texture) {
        this.texture = texture;
    }

    public boolean isProcessDone() {
        return processDone;
    }

    public void setProcessDone(boolean processDone) {
        this.processDone = processDone;
    }

    public long getTimeCost() {
        return timeCost;
    }

    public void setTimeCost(long timeCost) {
        this.timeCost = timeCost;
    }

    public boolean isNeedRecord() {
        return needRecord;
    }

    public void setNeedRecord(boolean needRecord) {
        this.needRecord = needRecord;
    }
}
