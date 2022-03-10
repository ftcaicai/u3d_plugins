package com.huihe.gameapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NativeActivity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.huihe.gameapp.input.InputDialogManager;
import com.tencent.bugly.crashreport.CrashReport;
import com.huihe.gameapp.input.InputDialogSetting;
import com.huihe.gameapp.input.WellcomeDialog;
import com.unity3d.player.UnityPlayer;

import org.json.JSONException;
import org.json.JSONObject;

import static android.view.View.SYSTEM_UI_FLAG_FULLSCREEN;
import static android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
import static android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

public class GameMainActivity extends NativeActivity {

    protected UnityPlayer mUnityPlayer;

    private String apkPath = null;

    // 变度 0 - 255
    private int defaultLigt = 150;
    private int settingLigt = 30;

    // 自定义软键盘
    private InputDialogSetting setting;

    // 屏幕宽高
    private int width, height;

    private WellcomeDialog wcDialog = null;

    // Setup activity layout
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        getWindow().takeSurface(null);
        setTheme(android.R.style.Theme_NoTitleBar_Fullscreen);
        getWindow().setFormat(PixelFormat.RGB_565);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                , WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        CrashReport.initCrashReport(getBaseContext(), "eed50d4700", false);

        mUnityPlayer = new UnityPlayer(this);
        if (mUnityPlayer.getSettings().getBoolean("hide_status_bar", true))
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(mUnityPlayer);
        mUnityPlayer.requestFocus();


        //保存一下游戏屏幕的宽和高
        DisplayMetrics dm = getResources().getDisplayMetrics();
        width = dm.widthPixels;
        height = dm.heightPixels;


        // 省电相关
        defaultLigt = BrightnessUtil.getScreenBrightness(this);


    }

    // This ensures the layout will be correct.
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mUnityPlayer.configurationChanged(newConfig);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_MULTIPLE) {
            return mUnityPlayer.injectEvent(event);
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return mUnityPlayer.injectEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return mUnityPlayer.injectEvent(event);
//		return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mUnityPlayer.injectEvent(event);
    }

    /*API12*/
    public boolean onGenericMotionEvent(MotionEvent event) {
        return mUnityPlayer.injectEvent(event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);

    }


    @Override
    protected void onNewIntent(Intent intent) {
        // TODO Auto-generated method stub
        super.onNewIntent(intent);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        // TODO Auto-generated method stub
        super.onWindowFocusChanged(hasFocus);
        mUnityPlayer.windowFocusChanged(hasFocus);
//		ResetLight();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        mUnityPlayer.resume();
//		ResetLight();
        UnityPlayer.UnitySendMessage("SdkGameObject", "OnResume", "");

    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        mUnityPlayer.pause();
//		ResetLight();
        UnityPlayer.UnitySendMessage("SdkGameObject", "OnPause", "");
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
//		ResetLight();
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        mUnityPlayer.quit();
        super.onDestroy();

        ResetLight();
        System.exit(0);
    }

    /**
     * 设置亮度
     * @param rate 比率
     */
    public void SetLight(int rate) {
        settingLigt = rate;
        this.runOnUiThread(new Runnable() {
            public void run() {
                BrightnessUtil.setBrightness(GameMainActivity.this, settingLigt);
            }
        });
    }

    /**
     * 恢复亮度
     */
    public void ResetLight() {
        this.runOnUiThread(new Runnable() {
            public void run() {
                int light = defaultLigt;
                if (light < 50) {
                    light = 50;
                }
                if (light > 200) {
                    light = 200;
                }
                BrightnessUtil.setBrightness(GameMainActivity.this, light);
            }
        });
    }

    // 登陆
    public void showLoginView() {
        this.runOnUiThread(new Runnable() {
            public void run() {

            }
        });
    }


    // 登录成功
    public void OnloginOnFinish(String uid) {
        UnityPlayer.UnitySendMessage("SdkGameObject", "OnloginOnFinish", uid);
    }

    public void copyTextToClipboard(String txt) {
        try {
            ClipboardTools.copyTextToClipboard((Context)this, txt);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 返回键
    public void OnBackButtonClick() {
        this.runOnUiThread(new Runnable() {
            public void run() {
                OnShowExitConfirmPanel();
            }
        });
    }

    // 登录成功
    public void OnShowExitConfirmPanel() {
        UnityPlayer.UnitySendMessage("SdkGameObject", "OnShowExitConfirmPanel", "");
    }

    // 重新登录
    public void OnRelogin(String param) {
        UnityPlayer.UnitySendMessage("SdkGameObject", "OnRelogin", param);
    }

    public void OnShowReLoginView() {
        runOnUiThread(new Runnable() {
            public void run() {

            }
        });
    }

    /**
     * 显示登录页
     */
    public void OnShowLoginView() {
        this.runOnUiThread(new Runnable() {
            public void run() {

            }
        });
    }

    /**
     * 运行APK
     * @param path    APK路径
     */
    public void RunDownloadApk(String path) {
        this.apkPath = path;
        this.runOnUiThread(new Runnable() {
            public void run() {
                Intent intent = new Intent(Intent.ACTION_VIEW);
//				intent.setDataAndType(Uri.fromFile(new File(path)), "application/vnd.android.package-archive");
                intent.setDataAndType(Uri.parse("file:///" + GameMainActivity.this.apkPath), "application/vnd.android.package-archive");
                startActivity(intent);
            }
        });
    }

    public String GetNetworkType() {
        String strNetworkType = "";
        NetworkInfo networkInfo = ((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                strNetworkType = "WIFI";
            } else if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                String _strSubTypeName = networkInfo.getSubtypeName();

//                Log.e("cocos2d-x", "Network getSubtypeName : " + _strSubTypeName);

                // TD-SCDMA   networkType is 17
                int networkType = networkInfo.getSubtype();
                switch (networkType) {
                    case TelephonyManager.NETWORK_TYPE_GPRS:
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                    case TelephonyManager.NETWORK_TYPE_CDMA:
                    case TelephonyManager.NETWORK_TYPE_1xRTT:
                    case TelephonyManager.NETWORK_TYPE_IDEN: //api<8 : replace by 11
                        strNetworkType = "2G";
                        break;
                    case TelephonyManager.NETWORK_TYPE_UMTS:
                    case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    case TelephonyManager.NETWORK_TYPE_HSDPA:
                    case TelephonyManager.NETWORK_TYPE_HSUPA:
                    case TelephonyManager.NETWORK_TYPE_HSPA:
                    case TelephonyManager.NETWORK_TYPE_EVDO_B: //api<9 : replace by 14
                    case TelephonyManager.NETWORK_TYPE_EHRPD:  //api<11 : replace by 12
                    case TelephonyManager.NETWORK_TYPE_HSPAP:  //api<13 : replace by 15
                        strNetworkType = "3G";
                        break;
                    case TelephonyManager.NETWORK_TYPE_LTE:    //api<11 : replace by 13
                        strNetworkType = "4G";
                        break;
                    default:
                        // http://baike.baidu.com/item/TD-SCDMA 中国移动 联通 电信 三种3G制式
                        if (_strSubTypeName.equalsIgnoreCase("TD-SCDMA") || _strSubTypeName.equalsIgnoreCase("WCDMA") || _strSubTypeName.equalsIgnoreCase("CDMA2000")) {
                            strNetworkType = "3G";
                        } else {
                            strNetworkType = _strSubTypeName;
                        }

                        break;
                }
//                Log.e("cocos2d-x", "Network getSubtype : " + Integer.valueOf(networkType).toString());
            }
        }
//        Log.e("cocos2d-x", "Network Type : " + strNetworkType);
        return strNetworkType;
    }


    /**
     * 自定义软键盘
     * @param fromId
     * @param keyboardType
     * @param x
     * @param y
     * @param fontSize
     * @param gravity
     * @param width
     * @param text
     */
    public void ShowInputDialog(int fromId, int keyboardType, int x, int y, int fontSize, int gravity, int width, String text) {
        this.setting = new InputDialogSetting();
        setting.setFromId(fromId);
        setting.setKeyboardType(keyboardType);
        setting.setX(x);
        setting.setY(y);
        setting.setFontSize(fontSize);
        setting.setGravity(gravity);
        setting.setWidth(width);
        setting.setText(text);
        setting.setBgcolor(0);

//		InputDialogManager.GetInstance().Show(this, setting);
        this.runOnUiThread(new Runnable() {
            public void run() {
                InputDialogManager.GetInstance().Show(GameMainActivity.this, setting);
            }
        });
    }

    public void ShowInputDialogWhite(int fromId, int keyboardType, int x, int y, int fontSize, int gravity, int width, String text) {
        this.setting = new InputDialogSetting();
        setting.setFromId(fromId);
        setting.setKeyboardType(keyboardType);
        setting.setX(x);
        setting.setY(y);
        setting.setFontSize(fontSize);
        setting.setGravity(gravity);
        setting.setWidth(width);
        setting.setText(text);
        setting.setBgcolor(1);

//		InputDialogManager.GetInstance().Show(this, setting);
        this.runOnUiThread(new Runnable() {
            public void run() {
                InputDialogManager.GetInstance().Show(GameMainActivity.this, setting);
            }
        });
    }

    /**
     * 隐藏输入面板
     */
    public void HideInputDialog() {
        this.runOnUiThread(new Runnable() {
            public void run() {
                InputDialogManager.GetInstance().Hide();
            }
        });
    }

    /**
     * 唯一的设备ID：
     * GSM手机的 IMEI 和 CDMA手机的 MEID.
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public String GetDeviceIdIMEI() {
        try {

            TelephonyManager telephonyManager = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
            //return tm.getDeviceId();
            if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                askForPermission(this, Manifest.permission.READ_PHONE_STATE, WANT_TO_READ_PHONE_STATE);
            }

            String imei="";
            if (android.os.Build.VERSION.SDK_INT >= 26) {
                imei=telephonyManager.getImei();
            }
            else
            {
                imei=telephonyManager.getDeviceId();
            }
            return  imei;


        } catch (Exception e) {
            return "10000";
        }
    }

    public void ShowWellcomeDialog() {
        this.runOnUiThread(new Runnable() {
            public void run() {
                if (wcDialog == null) {
                    wcDialog = new WellcomeDialog(GameMainActivity.this);
                }
                wcDialog.show();
            }
        });
    }

    public void HideWellcomeDialog() {
        this.runOnUiThread(new Runnable() {
            public void run() {
                if (wcDialog != null) {
                    wcDialog.hide();
                    wcDialog.dismiss();
                    wcDialog = null;
                }
            }
        });
    }

    private static final int WANT_TO_READ_PHONE_STATE =1;

    /* ask for permissions */
    public static void askForPermission(Activity sender, String permission, Integer requestCode) {
        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(sender, permission) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(sender, permission)) {

                //This is called if user has denied the permission before
                //In this case I am just asking the permission again
                ActivityCompat.requestPermissions(sender, new String[]{permission}, requestCode);

            } else {
                /* if permission was requested in AndroidManifest.xml then it is automatically granted for SDK <=23 */
                ActivityCompat.requestPermissions(sender, new String[]{permission}, requestCode);
            }
        }
    }


    /* Callback when user agreed/disagreed */
    @Override
    public void onRequestPermissionsResult (int requestCode, String[] permissions, int[] grantResults) {

    }

    public boolean hasNotchInScreen() {
        return NotchUtil.hasNotchInScreen(getBaseContext());
    }

    public String getDeviceType() {
        return Build.MODEL;
    }


    public void setUiVisibility() {
        runOnUiThread(new Runnable() {
            public void run() {
                GameMainActivity.this.getWindow().getDecorView().setSystemUiVisibility(
                        SYSTEM_UI_FLAG_HIDE_NAVIGATION | SYSTEM_UI_FLAG_HIDE_NAVIGATION | SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                );
            }
        });
    }

    public void startWebcamService(String type, String filepath, String filename) {
        runOnUiThread(new UiThreadStartWebcam(type, filepath, filename) {
            public void run() {
                WebcamActivity.isCreate = false;
                Intent intent = new Intent((Context)GameMainActivity.this, WebcamActivity.class);
                intent.putExtra("type", this.type);
                intent.putExtra("filepath", this.filepath);
                intent.putExtra("filename", this.filename);
                startActivity(intent);
            }
        });
    }


}
