package com.reige.updatedemo;

import android.app.DownloadManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.util.LongSparseArray;
import android.util.Log;

import java.io.File;

public class DownApkService extends Service {
    static final String TAG = "DownApkService";
    Context context = this;
    SharedPreferences mSp;
    private DownloadManager mDownloadManager;
    private DownloadBinder mBinder = new DownloadBinder();

    private LongSparseArray<String> mApkPaths = new LongSparseArray<>();
    private boolean mIsRoot = false;
    private DownApkReceiver mReceiver;

    public DownApkService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mSp = context.getSharedPreferences("downloadApk", MODE_PRIVATE);
        return super.onStartCommand(intent, flags, startId);
    }

    public class DownloadBinder extends Binder {

        public long startDownload(String apkUrl,String apkName) {
            //将文件下载到自己的Download文件夹下,必须是External的
            //这是DownloadManager的限制
            File file = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS+File.separator+apkName+".apk");
            if (file.exists()) file.delete();
            //使用DownLoadManager来下载
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(apkUrl));
            request.setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, apkName);
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setVisibleInDownloadsUi(true);
            request.setTitle(apkName);
            mDownloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);

            request.setDestinationUri(Uri.fromFile(file));

            //添加请求 开始下载
            long downloadId = mDownloadManager.enqueue(request);

            mSp.edit().putString("apkName", apkName+".apk").apply();
            mSp.edit().putLong("downloadId", downloadId).apply();

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