package org.hcilab.circog_watch;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.util.Log;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;

public class Util {
    private static final String TAG = Util.class.getSimpleName();

    public static void putBool(Context context, String key, boolean value) {
        SharedPreferences prefs = context.getSharedPreferences(CircogPrefs.PREFERENCES_NAME, Context.MODE_MULTI_PROCESS); //PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putBoolean(key, value);
        edit.commit();
    }

    public static boolean getBool(Context context, String key, boolean defValue) {
        SharedPreferences prefs = context.getSharedPreferences(CircogPrefs.PREFERENCES_NAME, Context.MODE_MULTI_PROCESS); //PreferenceManager.getDefaultSharedPreferences(context);
        boolean result = prefs.getBoolean(key, defValue);
        if (CircogPrefs.DEBUG_MODE) {
            Log.i(TAG, "getBool for key: " + key + " (result: " + result + ", default: " + defValue + ")");
        }
        return result;
    }

    public static long getLong(Context context, String key, long defValue) {
        SharedPreferences prefs = context.getSharedPreferences(CircogPrefs.PREFERENCES_NAME, Context.MODE_MULTI_PROCESS); //PreferenceManager.getDefaultSharedPreferences(context);
        long result = prefs.getLong(key, defValue);
        if (CircogPrefs.DEBUG_MODE) {
            Log.i(TAG, "getLong for key: " + key + " (result: " + result + ", default: " + defValue + ")");
        }
        return result;
    }

    public static void putLong(Context context, String key, long value) {
        SharedPreferences prefs = context.getSharedPreferences(CircogPrefs.PREFERENCES_NAME, Context.MODE_MULTI_PROCESS); //PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putLong(key, value);
        edit.commit();
    }

    public static void putInt(Context context, String key, int value) {
        SharedPreferences prefs = context.getSharedPreferences(CircogPrefs.PREFERENCES_NAME, Context.MODE_MULTI_PROCESS); //PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putInt(key, value);
        edit.commit();
    }

    public static int getInt(Context context, String key, int defValue) {
        SharedPreferences prefs = context.getSharedPreferences(CircogPrefs.PREFERENCES_NAME, Context.MODE_MULTI_PROCESS); //PreferenceManager.getDefaultSharedPreferences(context);
        int result = prefs.getInt(key, defValue);
        if (CircogPrefs.DEBUG_MODE) {
            Log.i(TAG, "getInt for key: " + key + " (result: " + result + ", default: " + defValue + ")");
        }
        return result;
    }

    /**
     * Returns a pseudo-random number between min and max.
     */
    public static int randInt(int min, int max) {
        Random rand = new Random();
        return rand.nextInt((max - min) + 1) + min;
    }

    public static String format(double d) {
        return format(d, 100.0);
    }

    public static String format(double d, double formatConstant) {
        int i = (int) (d * formatConstant);
        return "" + i / formatConstant;
    }

    public static String format(Location l) {
        try {
            return format(l.getLatitude(), 10000) + "/" + format(l.getLongitude(), 10000) + " " + l.getProvider();
        } catch (Exception e) {
            return "" + l;
        }
    }

    /**
     * parses a timestamp into a Date object, if not parseable, yesterday's Date is returned
     */
    public static Date getDateFromTimestamp(long ts) {
        Date date;
        try {
            date = new Date(ts);
        } catch (Exception pe) {
            pe.printStackTrace();
            //return date one day ago
            long DAY_IN_MS = 1000 * 60 * 60 * 24;
            date = new Date(System.currentTimeMillis() - (1 * DAY_IN_MS));
        }
        return date;
    }

    public static boolean isSameDay(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);
        boolean sameDay = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);

        if (CircogPrefs.DEBUG_MODE) {
            Log.i(TAG, "isSameDay: " + cal1.get(Calendar.DAY_OF_YEAR) + ", " + cal2.get(Calendar.DAY_OF_YEAR) + ": " + sameDay);
        }

        return sameDay;
    }
}
