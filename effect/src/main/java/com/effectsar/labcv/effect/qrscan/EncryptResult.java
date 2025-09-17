package com.effectsar.labcv.effect.qrscan;

public class EncryptResult {
    public BaseResponse base_response;
    public EncryptResultData data;

    public static class EncryptResultData {
        public String encryptUrl;
        public String hint;
        public int cameras;
    }
}
