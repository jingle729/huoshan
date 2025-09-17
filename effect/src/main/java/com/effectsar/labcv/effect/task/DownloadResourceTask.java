package com.effectsar.labcv.effect.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Base64;

import com.effectsar.labcv.common.utils.FileUtils;
import com.effectsar.labcv.core.util.LogUtils;
import com.effectsar.labcv.effect.R;
import com.effectsar.labcv.effect.qrscan.DownloadParam;
import com.effectsar.labcv.effect.qrscan.EncryptParam;
import com.effectsar.labcv.effect.qrscan.EncryptResult;
import com.effectsar.labcv.effect.qrscan.QRResourceInfo;
import com.effectsar.labcv.effect.utils.NetworkUtils;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class DownloadResourceTask
        extends AsyncTask<String, Float, DownloadResourceTask.DownloadResourceResult>
        implements NetworkUtils.DownloadProgressListener {
    public static final String SDK_VERSION = "18.8.8";
    public static String SERVER_TYPE_PROD = "PROD";
    public static String SERVER_TYPE_BOE = "BOE";
    public static String SERVER_TYPE = SERVER_TYPE_PROD;
    public static final String AUTH_FILE_CN_PROD = "https://lf6-cookiecut-tos.pstatp.com/obj/labcv-tob/7f90a488b4a9f8de1cb47b4d40ff3313";
    public static final String AUTH_FILE_CN_BOE = "http://tosv.boe.byted.org/obj/labcv-tob/7f90a488b4a9f8de1cb47b4d40ff3313";
    public static final String AUTH_FILE_I18N_BOE = "http://tosv.boe.byted.org/obj/labcv-tob/muse/7f90a488b4a9f8de1cb47b4d40ff3313";
    public static final String AUTH_FILE_I18N_PROD = "https://lf6-cookiecut-tos.pstatp.com/obj/labcv-tob/7f90a488b4a9f8de1cb47b4d40ff3313";
    public static String AUTH_FILE = AUTH_FILE_CN_PROD;

    public static final String BASE_URL_CN_BOE = "http://imuse-boe.bytedance.net";
    public static final String BASE_URL_CN_PROD = "https://cv.iccvlog.com";
    public static final String BASE_URL_I18N_BOE = "https://iccv-tob-i18n.byted.org";
    public static final String BASE_URL_I18N_PROD = "https://cv.iccvlog.com";
    public static String BASE_URL = BASE_URL_CN_PROD;

    private final WeakReference<DownloadResourceTaskCallback> mCallback;
    private NetworkUtils mNetwork;
    private final Gson mGson = new Gson();
    private boolean isIgnoreVersionRequire = false;

    public DownloadResourceTask(DownloadResourceTaskCallback callback) {
        mCallback = new WeakReference<>(callback);
    }

    public DownloadResourceTask(DownloadResourceTaskCallback callback, boolean isIgnoreVersionRequire) {
        mCallback = new WeakReference<>(callback);
        this.isIgnoreVersionRequire = isIgnoreVersionRequire;
    }

    @Override
    protected void onPreExecute() {
        mNetwork = new NetworkUtils();
        mNetwork.setDownloadProgressCallback(this);
    }

    @Override
    protected DownloadResourceResult doInBackground(String... strings) {
        DownloadResourceResult result = new DownloadResourceResult();
        if (mCallback.get() == null || !(mCallback.get() instanceof Context)) {
            result.code = -1;
            result.msg = "invalid context";
            return result;
        }

        if (mCallback.get() != null && mCallback.get() instanceof Context) {
            boolean networkAvailable = NetworkUtils.isNetworkConnected((Context) mCallback.get());
            if (!networkAvailable) {
                result.code = -1;
                result.msg = mCallback.get().getString(R.string.network_error);
                return result;
            }
        }

        if (strings.length == 0 || strings[0] == null) {
            result.code = -1;
            result.msg = "qr text not found";
            return result;
        }

        LogUtils.i("sticker scan result: " + strings[0]);

        QRResourceInfo resourceInfo = null;
        try {
            resourceInfo = mGson.fromJson(strings[0], QRResourceInfo.class);
        } catch (Exception e){
            e.printStackTrace();
            result.code = -1;
            result.msg = "invalid json info string";
            return result;
        }
        DownloadResourceTaskCallback callback = mCallback.get();
        callback.onQRScanData(resourceInfo);
        ResourceType resourceType = resourceTypeOfInfo(resourceInfo);
        result.resourceType = resourceType;
        String url = "";
        if (resourceInfo.app_name != null && resourceInfo.app_name.equals("AmazingEditor")) {
            if (resourceInfo.content == null) {
                result.code = -1;
                result.msg = "secId not found";
                return result;
            }
            ArrayList content = (ArrayList) resourceInfo.content;
            url = (String) content.get(0);
        }
        else if (resourceInfo.app_name != null && (resourceInfo.app_name.equals("EffectCreator") || resourceInfo.app_name.equals("PXARStudioPro"))) {
            if (resourceInfo.content == null) {
                result.code = -1;
                result.msg = "secId not found";
                return result;
            }
            String content = (String) resourceInfo.content;
//             {zh} 解密出来的URL链接             {en} Decrypted URL link
            url = decryptContent(content);
            LogUtils.i("sticker scan result: " + content);
        }
        else {
            if (resourceInfo == null || resourceInfo.secId == null) {
                result.code = -1;
                result.msg = "secId not found";
                return result;
            }


            if (resourceType == ResourceType.UNKNOWN) {
                result.code = -1;
                result.msg = "resource type not available";
                return result;
            }


            if (isGreaterThanAppVersion(resourceInfo.sdkVersion) && !isIgnoreVersionRequire) {
                result.code = DownloadResourceTaskCallback.ERROR_CODE_VERSION_NOT_MATCH;
                result.msg = "version not match";
                return result;
            }

            LogUtils.i("sticker secId: " + resourceInfo.secId);

            if (SERVER_TYPE.equals(SERVER_TYPE_BOE)) {
                if (resourceInfo.abroad == 1) {
                    AUTH_FILE = AUTH_FILE_I18N_BOE;
                    BASE_URL = BASE_URL_I18N_BOE;
                } else {
                    AUTH_FILE = AUTH_FILE_CN_BOE;
                    BASE_URL = BASE_URL_CN_BOE;
                }
            } else {
                if (resourceInfo.abroad == 1) {
                    AUTH_FILE = AUTH_FILE_I18N_PROD;
                    BASE_URL = BASE_URL_I18N_PROD;
                } else {
                    AUTH_FILE = AUTH_FILE_CN_PROD;
                    BASE_URL = BASE_URL_CN_PROD;
                }
            }

            EncryptParam param = new EncryptParam.Builder()
                    .setSecId(resourceInfo.secId)
                    .build();

            EncryptResult encryptResult = null;
            try {
                String ENCRYPT_URL = BASE_URL + "/sticker_mall_tob/v1/encrypt_sdk";
                Map<String, String> headerMap = new HashMap<String, String>(){{
                    put("Cv-region","sg");
                }} ;
                if (resourceInfo.abroad == 1){
                    encryptResult = mGson.fromJson(mNetwork.postWithJson(ENCRYPT_URL, mGson.toJson(param), headerMap), EncryptResult.class);
                }
                else {
                    encryptResult = mGson.fromJson(mNetwork.postWithJson(ENCRYPT_URL, mGson.toJson(param), null), EncryptResult.class);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (encryptResult == null || encryptResult.base_response == null) {
                result.code = -1;
                result.msg = "error when get encrypted url";
                return result;
            }

            if (encryptResult.base_response.code != 0) {
                result.code = encryptResult.base_response.code;
                result.msg = encryptResult.base_response.message;
                return result;
            }

            if (encryptResult.data == null || encryptResult.data.encryptUrl == null) {
                result.code = -1;
                result.msg = "invalid data or encryptUrl";
                return result;
            }
            url = encryptResult.data.encryptUrl;
            callback.onResult(encryptResult);
            LogUtils.i("encryptUrl: " + encryptResult.data.encryptUrl);
        }
        if (url == "") {
            result.code = -1;
            result.msg = "error when get encrypted url";
            return result;
        }
        String filePath = generateFilePath(url);
        if (SERVER_TYPE.equals(SERVER_TYPE_BOE)) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        DownloadParam downloadParam = new DownloadParam.Builder()
                .setEncryptUrl(url)
                .build();
        try {
            String errorMsg = "";
            if (resourceInfo.app_name != null && (resourceInfo.app_name.equals("AmazingEditor") || resourceInfo.app_name.equals("EffectCreator") || resourceInfo.app_name.equals("PXARStudioPro"))) {
                errorMsg = mNetwork.getdownloadFileWithJson(url, filePath);
            }
            else {
                String DOWNLOAD_URL = BASE_URL + "/sticker_mall_tob/v1/download_effect";
                errorMsg = mNetwork.downloadFileWithJson(DOWNLOAD_URL, mGson.toJson(downloadParam), filePath);
            }
//            String errorMsg = mNetwork.downloadFile(url, filePath);
            if (errorMsg != null) {
                result.code = -1;
                result.msg = errorMsg;
                return result;
            }
        } catch (IOException e) {
            result.code = -1;
            result.msg = e.getMessage();
            return result;
        }

        String dstDir = generateStickerDir(filePath);
        LogUtils.i("save sticker dir: " + dstDir);
        boolean unzipResult = FileUtils.unzipFile(filePath, new File(dstDir));
        if (!unzipResult) {
            result.code = -1;
            result.msg = "unzip sticker error";
            return result;
        }

        result.msg = dstDir;
        return result;
    }

    @Override
    protected void onPostExecute(DownloadResourceResult s) {
        DownloadResourceTaskCallback callback = mCallback.get();
        if (callback == null) return;
        if (s == null) {
            callback.onFail(-1, "fail");
            return;
        }
        if (s.code != 0) {
            callback.onFail(s.code, s.msg);
            return;
        }
        callback.onSuccess(s.msg, s.resourceType);
    }

    @Override
    protected void onProgressUpdate(Float... values) {
        DownloadResourceTaskCallback callback = mCallback.get();
        if (callback == null) return;
        callback.onProgressUpdate(values[0]);
    }

    @Override
    public void onProgressUpdate(float progress) {
        publishProgress(progress);
    }

    private String generateFilePath(String url) {
        String[] splits = url.split("/");
        String fileName = splits[splits.length - 1];
        if (mCallback.get() != null) {
            return mCallback.get().getExternalCacheDir() + File.separator + fileName;
        }
        return FileUtils.generateCacheFile(fileName);
    }

    private String generateStickerDir(String filePath) {
        return filePath.substring(0, filePath.length() - 4);
    }

    private ResourceType resourceTypeOfInfo(QRResourceInfo info) {
        if ((info.goodsType == null || info.goodsSubType == null) && info.app_name == null) {
            return ResourceType.UNKNOWN;
        }
        else if (info.app_name != null) {
            return ResourceType.STICKER;
        }

        if ("cv".equals(info.goodsType.key) && "filter".equals(info.goodsSubType.key)) {
            return ResourceType.FILTER;
        } else if ("cv".equals(info.goodsType.key) && "effect".equals(info.goodsSubType.key)) {
            return ResourceType.STICKER;
        }

        return ResourceType.UNKNOWN;
    }

    private boolean isGreaterThanAppVersion(String sdkVersion) {
        if (mCallback.get() == null) {
            return false;
        }
        String appVersion = mCallback.get().getAppVersionName();
        if (appVersion == null) {
            return false;
        }

        return compareString(appVersion, sdkVersion) < 0;
    }

    private String decryptContent(String encryptedContent) {

        byte [] content = Base64.decode(encryptedContent.getBytes(), Base64.DEFAULT);
        int offset = ((content[19] & 0xFF) << 24) |
                ((content[18] & 0xFF) << 16) |
                ((content[17] & 0xFF) << 8 ) |
                ((content[16] & 0xFF) << 0 );
        if (offset > 20) {
            offset = ((content[16] & 0xFF) << 24) |
                    ((content[17] & 0xFF) << 16) |
                    ((content[18] & 0xFF) << 8 ) |
                    ((content[19] & 0xFF) << 0 );
            if (offset > 20) {
                return null;
            }
        }

        byte[] decrypt = new byte[content.length - offset];
        byte keyA = 0x28;
        byte keyB = 0x5A;

        for(int i = 0; i< decrypt.length ; i++){
            decrypt[i] = (byte) ((content[i + offset] ^ keyB) - keyA);
        }

        String result = new String(decrypt);
        String url = null;

        try{
            JSONArray jsonArray = new JSONArray(result) ;
            url = (String)jsonArray.get(0);
        }  catch (JSONException e) {
            e.printStackTrace();
        }

        return url;
    }

    private int compareString(String a, String b) {
        String[] versionA = a.split("_");
        if (versionA == null) {
            return -1;
        }
        String[] arrA = versionA[0].split("\\.");
        String[] arrB = b.split("\\.");

        int maxLength = Math.min(arrA.length, arrB.length);
        for (int i = 0; i < maxLength; i++) {
            int iA = Integer.parseInt(arrA[i]);
            int iB = Integer.parseInt(arrB[i]);

            if (iA > iB) {
                return 1;
            }
            if (iA < iB) {
                return -1;
            }
        }

        if (arrA.length == arrB.length) return 0;
        return arrA.length > arrB.length ? 1 : -1;
    }

    public interface DownloadResourceTaskCallback {
        int ERROR_CODE_DEFAULT = -1;
        int ERROR_CODE_VERSION_NOT_MATCH = -2;

        void onSuccess(String path, ResourceType type);
        void onResult(EncryptResult Result);
        void onQRScanData(QRResourceInfo resourceInfo);
        void onFail(int errorCode, String message);
        void onProgressUpdate(float progress);
        String getString(int id);
        File getExternalCacheDir();
        String getAppVersionName();
    }

    public enum ResourceType {
        UNKNOWN,
        STICKER,
        FILTER
    }

    public static class DownloadResourceResult {
        int code = 0;
        String msg;
        ResourceType resourceType;
    }

    public static void setServerType(String type){
        if (type.equals(SERVER_TYPE_BOE)) {
            SERVER_TYPE = SERVER_TYPE_BOE;
        } else {
            SERVER_TYPE = SERVER_TYPE_PROD;
        }
    }

    public static String getServerType(){
        return SERVER_TYPE;
    }

}

