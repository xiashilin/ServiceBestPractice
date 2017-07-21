package com.xsl.servicebestpractice;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.widget.Toast;

import java.io.File;

public class DownLoadService extends Service {
    private DownLoadTask downLoadTask;
    private String downLoadUrl;

    private DownLoadListener downLoadListener = new DownLoadListener() {
        @Override
        public void onProgress(int progress) {
            getNotificationManager().notify(1, getNotification("下载中...", progress));
        }

        @Override
        public void onSuccess() {
            downLoadTask = null;
            stopForeground(true);
            getNotificationManager().notify(1, getNotification("下载成功！", -1));
            Toast.makeText(DownLoadService.this, "下载成功", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onFailed() {
            downLoadTask = null;
            stopForeground(true);
            getNotificationManager().notify(1, getNotification("下载失败！", -1));
            Toast.makeText(DownLoadService.this, "下载失败", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onPaused() {
            downLoadTask = null;
            Toast.makeText(DownLoadService.this, "暂停", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onCanceled() {
            downLoadTask = null;
            stopForeground(true);
            Toast.makeText(DownLoadService.this, "取消", Toast.LENGTH_LONG).show();
        }
    };


    private DownLoadBinder mBinder = new DownLoadBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    class DownLoadBinder extends Binder {
        public void startDownLoad(String url) {
            if (downLoadTask == null) {
                downLoadUrl = url;
                downLoadTask = new DownLoadTask(downLoadListener);
                downLoadTask.execute(downLoadUrl);
                startForeground(1, getNotification("下载中...", 0));
                Toast.makeText(DownLoadService.this, "下载中...", Toast.LENGTH_LONG).show();
            }
        }

        public void pauseDownLoad() {
            if (downLoadTask != null) {
                downLoadTask.pauseDownLoad();
            }
        }

        public void cancelDownLoad() {
            if (downLoadTask != null) {
                downLoadTask.cancelDownLoad();
            } else if (downLoadUrl != null) {
                String fileName = downLoadUrl.substring(downLoadUrl.lastIndexOf("/"));
                String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
                File file = new File(directory + fileName);
                if (file.exists()) {
                    file.delete();
                }
                getNotificationManager().cancel(1);
                stopForeground(true);
                Toast.makeText(DownLoadService.this, "取消", Toast.LENGTH_LONG).show();
            }

        }

    }

    private NotificationManager getNotificationManager() {
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    private Notification getNotification(String title, int progress) {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        builder.setContentIntent(pendingIntent);
        builder.setContentTitle(title);
        if (progress > 0) {
            builder.setContentText(progress + "%");
            builder.setProgress(100, progress, false);
        }
        return builder.build();
    }
}

