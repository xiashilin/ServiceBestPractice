package com.xsl.servicebestpractice;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button startDownLoad, pauseDownLoad, cancelDownLoad;
    private DownLoadService.DownLoadBinder downLoadBinder;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            downLoadBinder = (DownLoadService.DownLoadBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        startDownLoad = findViewById(R.id.btn_start);
        pauseDownLoad = findViewById(R.id.btn_pause);
        cancelDownLoad = findViewById(R.id.btn_cancel);
        startDownLoad.setOnClickListener(this);
        pauseDownLoad.setOnClickListener(this);
        cancelDownLoad.setOnClickListener(this);
        Intent intent = new Intent(this, DownLoadService.class);
        startService(intent);
        bindService(intent, connection, BIND_AUTO_CREATE);
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    @Override
    public void onClick(View view) {
        if (downLoadBinder == null) {
            return;
        }
        switch (view.getId()) {
            case R.id.btn_start:
                String url = "http://mirror.rise.ph/eclipse/oomph/epp/oxygen/R/eclipse-inst-win64.exe";
                downLoadBinder.startDownLoad(url);
                break;
            case R.id.btn_pause:
                downLoadBinder.pauseDownLoad();
                break;
            case R.id.btn_cancel:
                downLoadBinder.cancelDownLoad();
                break;
            default:
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "拒绝权限将无法使用程序", Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
    }
}
