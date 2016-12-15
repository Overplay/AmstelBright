package io.ourglass.amstelbright2.core;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by mkahn on 12/14/16.
 */

public class AppSettings {

    private static SharedPreferences mPrefs = ABApplication.sharedContext.getSharedPreferences(
            "ourglass", Context.MODE_PRIVATE);

    private static SharedPreferences.Editor mEditor = mPrefs.edit();

    /**
     * Save a string to app settings
     * @param key
     * @param string
     */
    public static void putString( String key, String string){

        mEditor.putString(key, string);
        mEditor.apply();
    }

    /**
     * Get a string, or a default if the key does not exist
     * @param key
     * @param defValue
     * @return
     */
    public static String getString( String key, String defValue ){

        return mPrefs.getString(key, defValue);

    }

    /**
     * Get a string or null if the key does not exist
     * @param key
     * @return
     */
    public static String getString( String key ){

        return mPrefs.getString(key, null);

    }


    /**
     * Save an int to app settings
     * @param key
     * @param integer
     */
    public static void putInt( String key, int integer){

        mEditor.putInt(key, integer);
        mEditor.apply();
    }

    /**
     * Get an int or zero if the key is not in settings
     * @param key
     * @return
     */
    public static int getInt( String key ){

        return mPrefs.getInt(key, 0);

    }

    /**
     * Get an int or a default if key not in settings
     * @param key
     * @return
     */
    public static int getInt( String key, int defValue ){

        return mPrefs.getInt(key, defValue);

    }

}
