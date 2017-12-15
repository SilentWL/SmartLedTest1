package wl.smartled.test.utils;

import android.util.Log;

import wl.smartled.test.BuildConfig;

public class LogUtil {
    public static int ERROR = 1;
    public static int WARN = 2;
    public static int INFO = 3;
    public static int DEBUG = 4;
    public static int VERBOS = 5;
    private static int LOG_LEVEL = (BuildConfig.DEBUG ? VERBOS : INFO);


    public static void e(String tag, String msg) {
        if (ERROR <= LOG_LEVEL)
            Log.e(tag, msg);
    }

    public static void w(String tag, String msg) {
        if (WARN <= LOG_LEVEL)
            Log.w(tag, msg);
    }

    public static void i(String tag, String msg) {
        if (INFO <= LOG_LEVEL)
            Log.i(tag, msg);
    }

    public static void d(String tag, String msg) {
        if (DEBUG <= LOG_LEVEL)
            Log.d(tag, msg);
    }

    public static void v(String tag, String msg) {
        if (VERBOS <= LOG_LEVEL)
            Log.v(tag, msg);
    }
}