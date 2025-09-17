package com.effectsar.labcv.effect.qrscan;

public class QRResourceInfo {
    public String secId;
    public String sdkVersion;
    public String app_name;
    public String imgK;
    public String encrypted;
    public Object content;
    public QRResourceType goodsType;
    public QRResourceType goodsSubType;
    public int abroad;



    public static class QRResourceType {
        public String key;
        public String name;
        boolean enableQrcode;
    }
}
