package com.reige.updatedemo;

import android.app.DownloadManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.util.LongSparseArray;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;

public class DownApkService extends Service {
    static final String TAG = "DownApkService";
    Context context = this;
    SharedPreferences mSp;
    private DownloadManager mDownloadManager;


    private DownloadBinder mBinder = new DownloadBinder();
    private LongSparseArray<String> mApkPaths;
    private boolean mIsRoot = false;
    private DownApkReceiver mReceiver;

    public DownApkService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle downloadBundle = intent.getBundleExtra("download");
        if (downloadBundle != null) {
            String downloadUrl = downloadBundle.getString("downloadUrl");
            String title = downloadBundle.getString("title");
            if (!TextUtils.isEmpty(downloadUrl)) {
                mSp = context.getSharedPreferences("downloadApk", MODE_PRIVATE);
                long downloadId = downloadApk(downloadUrl, title);
                mSp.edit().putLong("downloadId", downloadId).commit();
            }
        }
        stopSelf();
        return super.onStartCommand(intent, flags, startId);
    }


    private long downloadApk(String url, String title) {
        Uri downloadUri = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(downloadUri);
        String apkName = title + ".apk";
        File file = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS + "/" + apkName);
        if (file != null && file.exists()) {
//            file.delete();
            file.deleteOnExit();
        }
        request.setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS,
                apkName);
        mSp.edit().putString("apkName", apkName).commit();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setVisibleInDownloadsUi(true);
        request.setTitle(title);
        mDownloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        return mDownloadManager.enqueue(request);
    }

    public class DownloadBinder extends Binder {

        public long startDownload(String apkUrl) {
            //点击下载
            //删除原有的APK
//            IOUtils.clearApk(DownApkService.this, "test.apk");
            //使用DownLoadManager来下载
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(apkUrl));
            //将文件下载到自己的Download文件夹下,必须是External的
            //这是DownloadManager的限制
            File file = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "test.apk");
            request.setDestinationUri(Uri.fromFile(file));

            //添加请求 开始下载
            long downloadId = mDownloadManager.enqueue(request);
            Log.d("DownloadBinder", file.getAbsolutePath());
            mApkPaths.put(downloadId, file.getAbsolutePath());
            return downloadId;
        }

        public void setInstallMode(boolean isRoot) {
            mIsRoot = isRoot;
        }

        public int getProgress(long downloadId) {
            //查询进度
            DownloadManager.Query query = new DownloadManager.Query().setFilterById(downloadId);
            Cursor cursor = null;
            int progress = 0;
            try {
                cursor = mDownloadManager.query(query);//获得游标
                if (cursor != null && cursor.moveToFirst()) {
                    //当前的下载量
                    int downloadSoFar = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                    //文件总大小
                    int totalBytes = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));

                    progress = (int) (downloadSoFar * 1.0f / totalBytes * 100);
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            return progress;
        }
    }
}