package io.ourglass.amstelbright2.tvui.wifi;

import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.List;


/**
 * Created by ethan on 9/25/16.
 */

public class WifiUtils {

    private static WifiConfiguration initWifiConfig(String networkSSID){
        WifiConfiguration conf = new WifiConfiguration();

        //wrap in quotes if not already done
        if(networkSSID.charAt(0) != '"'){
            networkSSID = "\"" + networkSSID;
        }
        if(networkSSID.charAt(networkSSID.length() - 1) != '"'){
            networkSSID = networkSSID + "\"";
        }
        conf.SSID = networkSSID;

        return conf;
    }

    public static void connectToWEPSecuredNetwork(String networkSSID, String networkPass, WifiManager manager){
        WifiConfiguration conf = initWifiConfig(networkSSID);

        conf.wepKeys[0] = "\"" + networkPass + "\"";
        conf.wepTxKeyIndex = 0;
        conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);

        manager.addNetwork(conf);

        List<WifiConfiguration> list = manager.getConfiguredNetworks();
        for( WifiConfiguration i : list ) {
            if(i.SSID != null && i.SSID.equals("\"" + networkSSID + "\"")) {
                manager.disconnect();
                manager.enableNetwork(i.networkId, true);
                manager.reconnect();

                break;
            }
        }
    }

    public static void connectToWPASecuredNetwork(String networkSSID, String networkPass, WifiManager manager){
        WifiConfiguration conf = initWifiConfig(networkSSID);

        conf.preSharedKey = "\""+ networkPass +"\"";

        manager.addNetwork(conf);

        List<WifiConfiguration> list = manager.getConfiguredNetworks();
        for( WifiConfiguration i : list ) {
            if(i.SSID != null && i.SSID.equals("\"" + networkSSID + "\"")) {
                manager.disconnect();
                manager.enableNetwork(i.networkId, true);
                manager.reconnect();

                break;
            }
        }
    }

    public static void connectToOpenNetwork(String networkSSID, WifiManager manager){
        WifiConfiguration conf = initWifiConfig(networkSSID);

        conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);

        manager.addNetwork(conf);

        List<WifiConfiguration> list = manager.getConfiguredNetworks();
        for( WifiConfiguration i : list ) {
            if(i.SSID != null && i.SSID.equals("\"" + networkSSID + "\"")) {
                manager.disconnect();
                manager.enableNetwork(i.networkId, true);
                manager.reconnect();

                break;
            }
        }
        Log.wtf("!!!!!!!!!!!!!!!!!", "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFf");
    }


}
