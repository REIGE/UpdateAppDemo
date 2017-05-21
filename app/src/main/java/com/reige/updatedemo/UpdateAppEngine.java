package com.reige.updatedemo;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by REIGE
 * Date :2017/5/21.
 */

public class UpdateAppEngine {

    private static Context mContext;
    private static class InnerHolder {
        private static UpdateAppEngine INSTACE = new UpdateAppEngine();
    }

    public static void init(Context context) {
        mContext = context.getApplicationContext();  //防止内存泄露
    }

    public static UpdateAppEngine getInstance() {
        return InnerHolder.INSTACE;
    }



    public void downLoad(Context ctx,String subAppName,String url){

        if (canDownloadState(ctx)) {
            Log.d("UpdateVersion", "DownloadManager 可用");
            Intent downloadApkIntent = new Intent(ctx, DownApkService.class);
            Bundle bundle = new Bundle();
            bundle.putString("downloadUrl", url);
            bundle.putString("title", subAppName);
            downloadApkIntent.putExtra("download", bundle);
            ctx.startService(downloadApkIntent);
        } else {
            Log.d("UpdateVersion", "DownloadManager 不可用");
            Intent intent = new Intent();
            intent.setAction("android.intent.action.VIEW");
            Uri content_url = Uri.parse(url);
            intent.setData(content_url);
            ctx.startActivity(intent);
        }
    }


    private boolean canDownloadState(Context ctx) {
        try {
            int state = ctx.getPackageManager().getApplicationEnabledSetting("com.android.providers.downloads");
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
}
