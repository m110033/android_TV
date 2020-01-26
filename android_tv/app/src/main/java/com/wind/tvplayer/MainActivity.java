package com.wind.tvplayer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

// Ori: SiteActivity -> MainActivity -> SeasonActivity -> DetailsActivity -> PlaybackOverlayActivity
// => MainActivity -> VideoActivity -> VideoCardActivity -> DetailsActivity ->

public class MainActivity extends Activity {
    public static final String APK_UPDATE_CONTENT = "updateMessage";
    public static final String APK_VERSION_CODE = "versionCode";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*
         * 由於從 google driver 下載下來的 APK 一直損毀，可能是程式寫得有問題，但原因尚未釐清，所以改成直接從 NFS 路徑下來比對版本，
         * 有較新版本的 APK 存在的話則安裝，不再透過 google driver，又由於開發模式下抓取不到 NFS 路徑，所以直接讓他 EXCEPTION 而不去更新版本。
         */
        final String filePath = "/mnt/nfsShare/nfs_share0/TV_APK";
        try {
            File jsonFile = new File(filePath + "/update.json");
            FileInputStream stream = new FileInputStream(jsonFile);
            String jsonStr = null;
            FileChannel fc = stream.getChannel();
            MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            jsonStr = Charset.defaultCharset().decode(bb).toString();
            JSONObject obj = new JSONObject(jsonStr);
            String updateMessage = obj.getString(APK_UPDATE_CONTENT);
            int apkCode = obj.getInt(APK_VERSION_CODE);
            int versionCode = getVersionCode(this);
            if (apkCode > versionCode) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.android_auto_update_dialog_title);
                builder.setMessage(Html.fromHtml(updateMessage))
                        .setPositiveButton(R.string.android_auto_update_dialog_btn_download, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent installIntent = new Intent(Intent.ACTION_VIEW);
                                File apkFile = new File(filePath, "app-release.apk");
                                installIntent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
                                installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(installIntent);
                            }
                        })
                        .setNegativeButton(R.string.android_auto_update_dialog_btn_cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static int getVersionCode(Context mContext) {
        if (mContext != null) {
            try {
                return mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionCode;
            } catch (PackageManager.NameNotFoundException ignored) {
            }
        }
        return 0;
    }
}
