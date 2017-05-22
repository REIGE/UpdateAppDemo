package com.reige.updatedemo;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;

import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;


import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by REIGE
 * Date :2017/5/21.
 */

public class UpdateAppEngine {

    private static Context mContext;
    private Disposable mDisposable;
    private final Intent downloadApkIntent;
    private boolean isDownLoading = false;
    private OnProgressChangedListener onProgressChangedListener;

    public UpdateAppEngine(Context context) {
        mContext = context.getApplicationContext();  //防止内存泄露
        downloadApkIntent = new Intent(mContext, DownApkService.class);
        mContext.startService(downloadApkIntent);
        mContext.bindService(downloadApkIntent, mConnection, Context.BIND_AUTO_CREATE);
    }

    public void startDownLoad(String apkUrl, String apkName) {
        if (canDownloadState(mContext)) {
            isDownLoading = true;
            long downLoadId = mDownloadBinder.startDownload(apkUrl, apkName);
            startCheckProgress(downLoadId);
        }else {
            //调用浏览器进行更新
            Log.d("UpdateVersion", "DownloadManager 不可用");
            Intent intent = new Intent();
            intent.setAction("android.intent.action.VIEW");
            Uri content_url = Uri.parse(apkUrl);
            intent.setData(content_url);
            mContext.startActivity(intent);
        }
    }

    public boolean isDownLoading() {
        return isDownLoading;
    }

    private DownApkService.DownloadBinder mDownloadBinder;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mDownloadBinder = (DownApkService.DownloadBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isDownLoading = false;
            mDownloadBinder = null;
        }
    };

    private boolean canDownloadState(Context ctx) {
        try {
            int state = ctx.getPackageManager().getApplicationEnabledSetting("com.android" +
                    ".providers.downloads");
            if (state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                    || state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER
                    || state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED) {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    //开始监听进度
    private void startCheckProgress(final long downloadId) {

        Observable
                .interval(100, 200, TimeUnit.MILLISECONDS, Schedulers.io())//无限轮询,准备查询进度,在io线程执行
                .filter(new Predicate<Long>() {
                    @Override
                    public boolean test(Long aLong) throws Exception {
                        return mDownloadBinder != null;
                    }
                })
                .map(new Function<Long, Integer>() {
                    @Override
                    public Integer apply(Long aLong) throws Exception {
                        return mDownloadBinder.getProgress(downloadId);
                    }
                })//获得下载进度
                .takeUntil(new Predicate<Integer>() {
                    @Override
                    public boolean test(Integer progress) throws Exception {
                        return progress >= 100;
                    }
                })//返回true就停止了,当进度>=100就是下载完成了
                .distinct()//去重复
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new ProgressObserver());
    }


    //观察者
    private class ProgressObserver implements Observer<Integer> {

        @Override
        public void onSubscribe(Disposable d) {
            mDisposable = d;
        }

        @Override
        public void onNext(Integer progress) {
            if (onProgressChangedListener != null)
                onProgressChangedListener.progressChanged(progress);
            Log.e("进度", progress + "");
        }

        @Override
        public void onError(Throwable throwable) {
            throwable.printStackTrace();
            Toast.makeText(mContext.getApplicationContext(), "下载出错", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onComplete() {
            if (onProgressChangedListener != null)
                onProgressChangedListener.progressChanged(100);
            isDownLoading = false;//下载状态
            release();//下载完成 释放服务
            Toast.makeText(mContext.getApplicationContext(), "下载完成", Toast.LENGTH_SHORT).show();
        }
    }

    private void release() {
        mContext.unbindService(mConnection);//先解绑服务
        mContext.stopService(downloadApkIntent);//再停止服务
    }

    public void setOnProgressChangedListener(OnProgressChangedListener onProgressChangedListener) {
        this.onProgressChangedListener = onProgressChangedListener;
    }

    interface OnProgressChangedListener {
        int progressChanged(int progress);
    }
}


