package com.huihe.gameapp;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.huihe.gameapp.util.PermissionsCallback;
import com.huihe.gameapp.util.PermissionsUtil;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import java.util.List;

public class FirstActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback    {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);

        /*
        PermissionsUtil.with(FirstActivity.this)
                .request(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.READ_PHONE_STATE)
                .execute(new PermissionsCallback() {
                    @Override
                    public void onResult(boolean granted) {
                        if (granted) {
                            startGame();
                        } else {
                            Toast.makeText(FirstActivity.this, "拒绝后无法进入游戏", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

         */

        checkPermissions();
    }

    private  void checkPermissions () {
        XXPermissions.with(this)
                // 申请单个权限
                .permission(Permission.RECORD_AUDIO)
                .permission(Permission.READ_PHONE_STATE)
                // 申请多个权限
                .permission(Permission.Group.STORAGE)
                // 设置权限请求拦截器（局部设置）
                //.interceptor(new PermissionInterceptor())
                // 设置不触发错误检测机制（局部设置）
                //.unchecked()
                .request(new OnPermissionCallback() {

                    @Override
                    public void onGranted(List<String> permissions, boolean all) {
                        if (all) {
                            Toast.makeText(FirstActivity.this, "OK.", Toast.LENGTH_SHORT).show();
                        } else {
                            // 如果是被永久拒绝就跳转到应用权限系统设置页面
                            XXPermissions.startPermissionActivity(getBaseContext(), permissions);
                        }
                    }

                    @Override
                    public void onDenied(List<String> permissions, boolean never) {
                        if (never) {
                            Toast.makeText(FirstActivity.this, "被永久拒绝授权后无法进入游戏，请手动授予.", Toast.LENGTH_SHORT).show();
                            // 如果是被永久拒绝就跳转到应用权限系统设置页面
                            XXPermissions.startPermissionActivity(getBaseContext(), permissions);
                        }

                    }
                });
    }



    private void startGame() {
        Intent intent = new Intent(this, GameMainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
