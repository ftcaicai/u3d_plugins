package com.huihe.gameapp;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.OnPermissionPageCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;

import java.util.List;

public class FirstActivity extends AppCompatActivity   {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);


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
                            startGame();
                        }
                    }

                    @Override
                    public void onDenied(List<String> permissions, boolean never) {
                        if (never) {
                            Toast.makeText(FirstActivity.this, "被永久拒绝授权后无法进入游戏，请手动授予.", Toast.LENGTH_SHORT).show();
                            // 如果是被永久拒绝就跳转到应用权限系统设置页面
                            //XXPermissions.startPermissionActivity(getBaseContext(), permissions);

                            showPermissionDeniedDialog(permissions);
                        }

                    }
                });
    }


    private static final int REQUEST_CODE_SETTING = 0x0012345;

    @TargetApi(Build.VERSION_CODES.M)
    private void showPermissionDeniedDialog(List<String> permissions) {
        //启动当前App的系统设置界面
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("帮助")
                .setMessage("当前应用缺少必要权限")
                .setNegativeButton("取消", (dialog12, which) -> dialog12.dismiss())
                .setPositiveButton("设置", (dialog1, which) -> {
                    // 如果是被永久拒绝就跳转到应用权限系统设置页面
                    XXPermissions.startPermissionActivity(this, permissions, new OnPermissionPageCallback() {
                        @Override
                        public void onGranted() {
                            startGame();
                        }

                        @Override
                        public void onDenied() {
                            Toast.makeText(FirstActivity.this, "被永久拒绝授权后无法进入游戏，请重启应用后手动授予.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }).create();
        dialog.show();
    }

    private void startGame() {
        Intent intent = new Intent(this, GameMainActivity.class);
        startActivity(intent);
    }

}
