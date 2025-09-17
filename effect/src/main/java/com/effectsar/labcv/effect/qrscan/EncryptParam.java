package com.effectsar.labcv.effect.qrscan;


import com.effectsar.labcv.effect.task.DownloadResourceTask;

import static com.effectsar.labcv.effect.task.DownloadResourceTask.SDK_VERSION;

public class EncryptParam {
    public String secId;
    public String sdkVersion;
    public String authFile;

    public static class Builder {
        private String secId;
        private String sdkVersion = SDK_VERSION;
        private String authFile = DownloadResourceTask.AUTH_FILE;

        public Builder setSecId(String secId) {
            this.secId = secId;
            return this;
        }

        public Builder setSdkVersion(String sdkVersion) {
            this.sdkVersion = sdkVersion;
            return this;
        }

        public Builder setAuthFile(String authFile) {
            this.authFile = authFile;
            return this;
        }

        public EncryptParam build() {
            EncryptParam param = new EncryptParam();
            param.sdkVersion = sdkVersion;
            param.secId = secId;
            param.authFile = authFile;
            return param;
        }
    }
}
