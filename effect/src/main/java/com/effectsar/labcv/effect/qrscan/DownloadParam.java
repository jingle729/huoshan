package com.effectsar.labcv.effect.qrscan;

public class DownloadParam {
    public String encryptUrl;

    public static class Builder {
        private String encryptUrl;

        public Builder setEncryptUrl(String encryptUrl) {
            this.encryptUrl = encryptUrl;
            return this;
        }

        public DownloadParam build() {
            DownloadParam param = new DownloadParam();
            param.encryptUrl = encryptUrl;
            return param;
        }
    }
}
