package com.huihe.gameapp;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import com.unity3d.player.UnityPlayer;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.RequiresApi;

public class WebcamActivity extends Activity {
    private static final int REQ_CODE_TAKE_PHOTO = 1;    // 请求类型: 拍照
    private static final int REQ_CODE_CROP = 2;          // 请求类型: 执行照片剪裁
    private static final int REQ_CODE_CROP_RESULT = 3;   // 请求类型: 处理剪裁结果

    // 处理后的照片临时保存路径(此值由unity传入)
    private String filepath = null;
    private String filename = null;

    public static boolean isCreate = false;

    // 本地相册所在路径 TODO: 不知道是不是所有的android机型都是将本地相册保存在此处，待查正
    private static final String GALLERY_DIR = "image/*";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 处理后的照片临时保存路径(此值由unity传入)
        filepath = this.getIntent().getStringExtra("filepath");
        filename = this.getIntent().getStringExtra("filename");

        if (isCreate)
            return;
        isCreate = true;

        // 判断调用的功能
        String type = this.getIntent().getStringExtra("type");
        if(type.equals("TakePhoto")) {
            takePhoto();
        }else if(type.equals("OpenPhotoGallery")){
            openPhotoGallery();
        }else{
            sendMessage("使用摄像机服务时，调用了未知功能: " + type);
            this.finish();
        }
    }

    public String getStackTraceInfo(Exception e) {
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(bao);
        e.printStackTrace(ps);
        ps.close();
        return bao.toString();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(Activity.RESULT_OK != resultCode) {
            sendMessage("WebcamActivity收到未知的resultCode: " + resultCode);
            this.finish(); // 用户点击了取消或是发生其它状况，关闭当前activity
            return;
        }

        switch(requestCode){
            default:
                sendMessage("发生内部错误，收到未知的requestCode: " + requestCode);
                return;
            case REQ_CODE_TAKE_PHOTO:
                // Log.v("Unity", "=> REQ_CODE_TAKE_PHOTO");
                File picture = new File(Environment.getExternalStorageDirectory() + "/temp.jpg");
                try{
                    crop(Uri.fromFile(picture));
                }catch(Exception e){
                    sendMessage("当前机器似乎没有安装照片剪裁组件，无法处理照片");
                }
                break;
            case REQ_CODE_CROP:
                // Log.v("Unity", "=> REQ_CODE_CROP");
                if(data == null) {
                    sendMessage("剪裁照片时收到空路径，可能是拍照失败或是选取了不存在的照片");
                    return;
                }
                try{
                    crop(data.getData());
                }catch(Exception e){
                    sendMessage("当前机器似乎没有安装照片剪裁组件，无法处理照片");
                }
                break;
            case REQ_CODE_CROP_RESULT:
                // Log.v("Unity", "=> REQ_CODE_CROP_RESULT");
                Bundle extras = data.getExtras();
                if (extras == null) {
                    sendMessage("剪裁照片时收到空路径，可能是拍照失败或是选取了不存在的照片");
                    return;
                }
                try{
                    Bitmap photo = extras.getParcelable("data");
                    savePhoto(photo);
                    Log.v("Unity", "WebcamActivity: 照片已经准备好");
                    sendMessage("success");
                }catch(Exception e) {
                    sendMessage("保存照片失败，原因: 可能是设置了错误的保存路径或是没有写入权限");
                }
                this.finish(); // 所有操作已经完成，关闭当前activity
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 供unity调用的接口
     * @param activity 调用者所处的activity
     * @param type 服务类型
     * @param filepath 照片保存路径
     * @param filename 照片名称
     */
    public static void startService(Activity activity, String type, String filepath, String filename){
        Intent intent = new Intent(activity, WebcamActivity.class);
        intent.putExtra("type", type);
        intent.putExtra("filepath", filepath);
        intent.putExtra("filename", filename);
        activity.startActivity(intent);
    }

    /**
     * 拍照
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void takePhoto() {

        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            GameMainActivity.askForPermission(this, Manifest.permission.CAMERA, 0);
        }

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "temp.jpg")));
        startActivityForResult(intent, REQ_CODE_TAKE_PHOTO);
    }

    /* Callback when user agreed/disagreed */
    @Override
    public void onRequestPermissionsResult (int requestCode, String[] permissions, int[] grantResults) {

    }

    /**
     * 打开相册
     */
    public void openPhotoGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, GALLERY_DIR);
        startActivityForResult(intent, REQ_CODE_CROP);
    }

    // 发送消息到Unity
    private void sendMessage(String errorMessage){
        UnityPlayer.UnitySendMessage("WebcamProxy", "MessageArrive", errorMessage);
    }

    // 调用剪裁照片功能
    private void crop(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, GALLERY_DIR);
        intent.putExtra("crop", "true");
        // 宽高比例，保持1:1，不要变形
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        //裁剪图片的尺寸
        intent.putExtra("outputX", 300);
        intent.putExtra("outputY", 300);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, REQ_CODE_CROP_RESULT);
    }

    // 保存照片
    private void savePhoto(Bitmap bitmap) throws IOException {
        FileOutputStream out = null;
        try {
            // 创建路径，如果不存在的话
            File dir = new File(filepath);
            if(!dir.exists()){
                dir.mkdirs();
            }

            out = new FileOutputStream(filepath + filename);
            // 保存成png格式
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
        }finally{
            if(out != null){
                out.close();
            }
        }
    }
}
