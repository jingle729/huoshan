package com.effectsar.labcv.effect.qrscan;

import com.effectsar.labcv.effectsdk.ReflectResourceFinder;
import com.volcengine.ebox.loader.EBoxSDKManager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.effectsar.labcv.core.util.LogUtils;

public class QRScanResourceFinder extends ReflectResourceFinder{
    public enum DownloadStatus{
        BEGIN, SUCCESS, FAILED
    }

    static final String SCHEME_FILE = "file://";
    String mModelsDir;
    Map<String, String> mModelNameMap = new HashMap<>();
    ConcurrentMap<String, DownloadStatus> downloadTaskStatus = new ConcurrentHashMap<>();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private volatile boolean isDownloading = false;

    public boolean isSyncDownload = true;

    public interface IDownModelCallback{
        void onFailed(String modelName);
    }

    private IDownModelCallback mDownloadModelCallback;

    public QRScanResourceFinder(String modelsDir) {
        super(modelsDir);
        mModelsDir = modelsDir;
    }

    public void setDownloadModelCallback(IDownModelCallback callback) {
        mDownloadModelCallback = callback;
    }

    public List<String> getFailedModelNames() {
        List<String> failedModelNames = new ArrayList<>();
        for (String key : downloadTaskStatus.keySet()) {
            if (DownloadStatus.FAILED.equals(downloadTaskStatus.get(key))) {
                failedModelNames.add(key);
            }
        }
        return failedModelNames;
    }

    public DownloadStatus checkModelReady() {
        boolean isAllSuccess = true;
        for (String key : downloadTaskStatus.keySet()) {
            if (DownloadStatus.BEGIN.equals(downloadTaskStatus.get(key))) {
                if (!isDownloading) {
                    downloadModelTask();
                }
                return DownloadStatus.BEGIN;
            }
            if (DownloadStatus.FAILED.equals(downloadTaskStatus.get(key))) {
                isAllSuccess = false;
            }
        }

        return isAllSuccess ? DownloadStatus.SUCCESS : DownloadStatus.FAILED;
    }

    @Override
    public String findResource(String s) {
        LogUtils.i("QRScanResourceFinder findResource: " + s);
        String modelName = extractFileName(s);
        mModelNameMap = extractModelNames();
        if (mModelNameMap.containsKey(modelName)) {
            String retString = mModelNameMap.get(modelName);
            return SCHEME_FILE + retString;
        } else {
            if (isSyncDownload) {
                ArrayList<String> modelNames = new ArrayList<>();
                modelNames.add(modelName);
                if (EBoxSDKManager.INSTANCE.loadModel(modelNames, "16.8.0")) {
                    mModelNameMap = extractModelNames();
                    return SCHEME_FILE + mModelNameMap.get(modelName);
                } else {
                    //同步方式使用callback监听
                    if (mDownloadModelCallback != null) {
                        mDownloadModelCallback.onFailed(modelName);
                    }
                    return "";
                }

            } else {
                if (!downloadTaskStatus.containsKey(modelName)) {
                    downloadTaskStatus.put(modelName, DownloadStatus.BEGIN);
                    //异步下载
                    checkModelReady();
                } else {
                    if (DownloadStatus.SUCCESS.equals(downloadTaskStatus.get(modelName))) {
                        mModelNameMap = extractModelNames();
                        return SCHEME_FILE + mModelNameMap.get(modelName);
                    }
                }
            }
        }
        return "";
    }

    private synchronized ArrayList<String> getNoDownloadModelNames() {
        ArrayList<String> modelNames = new ArrayList<>();
        for (String key : downloadTaskStatus.keySet()) {
            if (DownloadStatus.BEGIN.equals(downloadTaskStatus.get(key))) {
                modelNames.add(key);
            }
        }
        return modelNames;
    }

    private void downloadModelTask() {
        isDownloading = true;
        executorService.execute(() -> {
            ArrayList<String> modelNames = getNoDownloadModelNames();
            while (!modelNames.isEmpty()) {
                if (EBoxSDKManager.INSTANCE.loadModel(modelNames, "16.8.0")) {
                    for (String key : modelNames) {
                        downloadTaskStatus.put(key, DownloadStatus.SUCCESS);
                    }
                } else {
                    for (String key : modelNames) {
                        downloadTaskStatus.put(key, DownloadStatus.FAILED);
                    }
                    isDownloading = false;
                    return;
                }
                modelNames = getNoDownloadModelNames();
            }
            isDownloading = false;
        });
    }
    private String extractFileName(String path) {
        int lastIndex = path.lastIndexOf('/');
        return lastIndex != -1 ? path.substring(lastIndex + 1) : path;
    }

    private Map<String, String> extractModelNames() {
        Map<String, String> modelNameMap = new HashMap<>();
        File dir = new File(mModelsDir);
        // 检查目录是否存在且为有效目录
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                // 定义正则表达式模式
                Pattern pattern = Pattern.compile("^[0-9a-f]{32}_(.*)$");
                for (File f : files) {
                    if (f.isFile()) {
                        String fileName = f.getName();
                        Matcher matcher = pattern.matcher(fileName);
                        if (matcher.matches()) {
                            // 提取匹配的部分
                            String extractedName = matcher.group(1);
                            // 将提取的名字和文件绝对路径存入 Map
                            modelNameMap.put(extractedName, f.getAbsolutePath());
                        }
                    }
                }
            }
        }
        return modelNameMap;
    }

    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, java.util.concurrent.TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
