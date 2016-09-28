package io.ourglass.amstelbright2.core;


import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;

import com.mstar.android.tv.TvCommonManager;
import com.mstar.android.tvapi.common.TvManager;
import com.mstar.android.tvapi.common.exception.TvCommonException;
import com.mstar.android.tvapi.common.vo.TvOsType;

import org.json.JSONException;
import org.json.JSONObject;

import io.ourglass.amstelbright2.services.amstelbright.AmstelBrightService;
import io.ourglass.amstelbright2.tvui.WidthHeight;

/**
 * Created by mkahn on 7/25/16.
 *
 * Hardware related methods
 *
 */
public class OGSystem {

    public static WidthHeight screenResolution = new WidthHeight(0,0);

    private static SharedPreferences mPrefs = ABApplication.sharedContext.getSharedPreferences(
            "ourglass", Context.MODE_PRIVATE);

    private static SharedPreferences.Editor mEditor = mPrefs.edit();

    public static final String TAG = "OGSystem";

    /**
     *
     * @return os string like "4.4.2"
     */
    public static String osVersion(){
        return Build.VERSION.RELEASE;
    }

    /**
     *
     * @return in value like 19
     */
    public static int osLevel(){
        return Build.VERSION.SDK_INT;
    }

    public static boolean isEmulator(){
        return Build.FINGERPRINT.contains("generic");
    }

    /**
     * Tells whether running on Tronsmart hardware
     * @return
     */
    public static boolean isTronsmart(){
        return ( osLevel() == 19 ) && !isEmulator();
    }

    /**
     * Tells whether running on real Ourglass hardware
     * @return
     */
    public static boolean isRealOG(){
        return ( osLevel() > 19 ) && !isEmulator();
    }


    public static boolean enableHDMI(){

        if ( isRealOG() ){

            return false;

        } else if ( isTronsmart() ){

            enableTronsmartHDMI();
            return true;

        } else {

            return false;

        }

    }

    public static void setCurrentResolution(WidthHeight rez){
        screenResolution = rez;
    }

    public static String getCurrentResolution(){
        return ""+ (int)screenResolution.width + "x" + (int)screenResolution.height;
    }

    public static void putStringToPrefs( String key, String string){

        mEditor.putString(key, string);
        mEditor.apply();
    }

    public static String getStringFromPrefs( String key, String defValue ){

        return mPrefs.getString(key, defValue);

    }

    public static void putIntToPrefs( String key, int integer){

        mEditor.putInt(key, integer);
        mEditor.apply();
    }

    public static int getIntFromPrefs( String key ){

        return mPrefs.getInt(key, 0);

    }



    public static void setSystemName(String name){

       putStringToPrefs("systemName", name);

    }

    public static String getSystemName(){


        return getStringFromPrefs("systemName", "No Name");

    }

    public static void setSystemLocation(String location){

        putStringToPrefs("systemLocation", location);

    }

    public static String getSystemLocation(){

        return getStringFromPrefs("systemLocation", "No Location");

    }

    public static void setPairedSTBIP(String location){

        putStringToPrefs("pairedSTBIP", location);

    }


    public static String getPairedSTBIP(){
        return getStringFromPrefs("pairedSTBIP", null);

    }

    public static void setABVersionName(String vName){
        putStringToPrefs("abVersionName", vName);
    }

    public static String getABVersionName(){
        return getStringFromPrefs("abVersionName", null);

    }

    public static void setABVersionCode(int vCode){
        putIntToPrefs("abVersionCode", vCode);
    }


    public static int getABVersionCode(){
        return getIntFromPrefs("abVersionCode");
    }

    public static JSONObject getSystemInfo(){

        JSONObject deviceJSON = new JSONObject();

        try {

            deviceJSON.put("name", getSystemName());
            deviceJSON.put("locationWithinVenue", getSystemLocation());
            deviceJSON.put("randomFactoid", "Bunnies are cute");


            WifiManager manager = (WifiManager) AmstelBrightService.context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo info = manager.getConnectionInfo();
            String macAddress = info.getMacAddress();

            if (macAddress == null){
                macAddress = "undefined";
            }


            deviceJSON.put("wifiMacAddress", macAddress);

            //deviceJSON.put("settings", device.settings);
            //deviceJSON.put("apiToken", device.apiToken);

            //deviceJSON.put("uuid", device.uuid);
            String pairIp = getPairedSTBIP();
            boolean isPaired = false;

            if (pairIp!=null && !pairIp.isEmpty()){
                isPaired = true;
            } else {
                pairIp = "";
            }

            deviceJSON.put("isPairedToSTB", isPaired );
            deviceJSON.put("pairedSTBIP", pairIp );
            deviceJSON.put("outputRes", getCurrentResolution());

            deviceJSON.put("abVersionName", getABVersionName());
            deviceJSON.put("abVersionCode", getABVersionCode());

            deviceJSON.put("osVersion", osVersion());
            deviceJSON.put("osApiLevel", osLevel());

        } catch (JSONException e){
            Log.e("OGDevice.model", e.toString());
            return null;
        }

        return deviceJSON;
    }

    /*******************************************************************************
     *
     * TRONSMART SPECIFIC CODE
     *
     *******************************************************************************/

    /***************************************
     * TRONSMART CODE
     ***************************************/

    private static boolean enableOGHDMI() {

        Log.wtf(TAG, "Yeah, that's not implemented yet. But nice try.");
        return false;

    }


    /*******************************************************************************
     *
     * TRONSMART SPECIFIC CODE
     *
     *******************************************************************************/

    /***************************************
     * TRONSMART CODE
     ***************************************/

    private static boolean enableTronsmartHDMI() {
        boolean bRet = false;
        try {
            changeTronsmartInputSource(TvOsType.EnumInputSource.E_INPUT_SOURCE_STORAGE);
            changeTronsmartInputSource(TvOsType.EnumInputSource.E_INPUT_SOURCE_HDMI);
            bRet = TvManager.getInstance().getPlayerManager().isSignalStable();
        } catch (TvCommonException e) {
            e.printStackTrace();
        }
        return bRet;
    }

    public static void changeTronsmartInputSource(TvOsType.EnumInputSource eis) {

        TvCommonManager commonService = TvCommonManager.getInstance();

        if (commonService != null) {
            TvOsType.EnumInputSource currentSource = commonService.getCurrentInputSource();
            if (currentSource != null) {
                if (currentSource.equals(eis)) {
                    return;
                }

                commonService.setInputSource(eis);
            }
        }
    }



}
