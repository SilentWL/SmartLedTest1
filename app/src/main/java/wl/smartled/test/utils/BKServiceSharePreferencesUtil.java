package wl.smartled.test.utils;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Administrator on 2017/12/16 0016.
 */

public class BKServiceSharePreferencesUtil {
    private final static String BKSERVICE_SHAREPREFERENCES = "BKServiceSetting";
    private final static String STOP_BKSERVICE_SHAREPREFERENCES_KEY = "Stop";

    public static boolean getStopBKServicePreferences(Context c, boolean defaultValue) {
        SharedPreferences sharedPreferences = c.getSharedPreferences(BKSERVICE_SHAREPREFERENCES, MODE_PRIVATE);
        return sharedPreferences.getBoolean(STOP_BKSERVICE_SHAREPREFERENCES_KEY, defaultValue);
    }

    public static void writeStopBKServicePreferences(Context c, boolean stop) {
        SharedPreferences sharedPreferences = c.getSharedPreferences(BKSERVICE_SHAREPREFERENCES, MODE_PRIVATE);
        boolean stopPreferences = sharedPreferences.getBoolean(STOP_BKSERVICE_SHAREPREFERENCES_KEY, !stop);

        if (stopPreferences != stop) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(STOP_BKSERVICE_SHAREPREFERENCES_KEY, stop);
            editor.apply();
        }
    }
}
