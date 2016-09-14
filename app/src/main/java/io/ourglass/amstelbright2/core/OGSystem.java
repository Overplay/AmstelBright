package io.ourglass.amstelbright2.core;


import android.os.Build;
import android.util.Log;

import com.mstar.android.tv.TvCommonManager;
import com.mstar.android.tvapi.common.TvManager;
import com.mstar.android.tvapi.common.exception.TvCommonException;
import com.mstar.android.tvapi.common.vo.TvOsType;

import io.ourglass.amstelbright2.tvui.WidthHeight;

/**
 * Created by mkahn on 7/25/16.
 *
 * Hardware related methods
 *
 */
public class OGSystem {

    public static WidthHeight screenResolution = new WidthHeight(0,0);

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
        return ""+screenResolution.width+"x"+screenResolution.height;
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
