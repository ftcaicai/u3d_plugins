package com.huihe.gameapp;


import android.content.Context;
import java.lang.reflect.Method;

public class NotchUtil {
    public static final int NOTCH_IN_SCREEN_VOIO = 32;

    public static boolean hasNotchInScreen(Context context) {
        boolean ret = false;
        ret = hasNotchInScreenHuawei(context);
        if (ret)
            return ret;
        ret = hasNotchInScreenAtOppo(context);
        if (ret)
            return ret;
        ret = hasNotchInScreenAtVoio(context);
        if (ret)
            return ret;
        return false;
    }

    public static boolean hasNotchInScreenHuawei(Context context) {
        boolean ret = false;
        try {
            ClassLoader cl = context.getClassLoader();
            Class<?> HwNotchSizeUtil = cl.loadClass("com.huawei.android.util.HwNotchSizeUtil");
            Method get= HwNotchSizeUtil.getMethod("hasNotchInScreen");

            ret=(Boolean) get.invoke(HwNotchSizeUtil);
        } catch (ClassNotFoundException classNotFoundException) {
            return ret;
        } catch (NoSuchMethodException noSuchMethodException) {
            return ret;
        } catch (Exception exception) {
            return ret;
        } finally {
            Exception exception = null;
        }
        return ret;
    }

    public static boolean hasNotchInScreenAtOppo(Context context) {
        return context.getPackageManager().hasSystemFeature("com.oppo.feature.screen.heteromorphism");
    }

    public static boolean hasNotchInScreenAtVoio(Context context) {
        boolean ret = false;
        try {
            ClassLoader cl = context.getClassLoader();
            Class<?> FtFeature = cl.loadClass("android.util.FtFeature");

            Method get= FtFeature.getMethod("isFeatureSupport", int.class);

            ret=(Boolean) get.invoke(FtFeature, NOTCH_IN_SCREEN_VOIO);

            return ret;
        } catch (ClassNotFoundException classNotFoundException) {
            return ret;
        } catch (NoSuchMethodException noSuchMethodException) {
            return ret;
        } catch (Exception exception) {
            return ret;
        } finally {
            Exception exception = null;
        }
    }
}
