package io.ourglass.amstelbright2.realm;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

import io.ourglass.amstelbright2.services.amstelbright.AmstelBrightService;
import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Created by eadams on 5/27/16.
 */
public class OGDevice extends RealmObject{

    public String name;

    public String locationWithinVenue;

    @Required
    public String wifiMacAddress;

    public String settings;

    @Required
    public String apiToken;

    @PrimaryKey
    public String uuid;

    //TODO add field owners

    //TODO add field manager

    public static OGDevice getDevice(Realm realm){

        //I am under the impression that there is only one OGDevice in the database
        //so unconditionally finding OGDevice should suffice
        RealmResults<OGDevice> result = realm.where(OGDevice.class)
            .findAll();

        if(result.size() > 0){
            return result.first();
        }

        //if there is nothing in db, then create OGDevice
        OGDevice newDevice = new OGDevice();

        newDevice.name = "New Device";

        newDevice.locationWithinVenue = "undefined";

        //retrieve the MAC address
        WifiManager manager = (WifiManager) AmstelBrightService.context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = manager.getConnectionInfo();
        String MACaddress = info.getMacAddress();

        if(MACaddress == null){
            MACaddress = "undefined";
        }

        newDevice.wifiMacAddress = MACaddress;

        //set settings equal to empty javascript object
        newDevice.settings = "{}";

        //not sure whether this is an acceptable default value
        newDevice.apiToken = "undefined";

        newDevice.uuid = UUID.randomUUID().toString();

        //newDevice.owners = null;
        //newDevice.managers = null;

        //add the newDevice to the realm database
        realm.beginTransaction();
        realm.copyToRealm(newDevice);
        realm.commitTransaction();

        //return the created device
        return newDevice;
    }

    public JSONObject getSettings(){
        JSONObject rval;

        try{
            rval = new JSONObject(this.settings);
        } catch (JSONException e){
            Log.e("OGDevice.model", "Error parsing settings to JSON");
            rval = new JSONObject();
        }

        return rval;
    }

    public static JSONObject getDeviceAsJSON(Realm realm){
        OGDevice device = getDevice(realm);

        JSONObject deviceJSON = new JSONObject();
        try {
            deviceJSON.put("name", device.name);
            deviceJSON.put("locationWithinVenue", device.locationWithinVenue);
            deviceJSON.put("wifiMacAddress", device.wifiMacAddress);
            deviceJSON.put("settings", device.settings);
            deviceJSON.put("apiToken", device.apiToken);
            deviceJSON.put("uuid", device.uuid);
            deviceJSON.put("version", ">0.5.3");
            //deviceJSON.put("owners", );
            //deviceJSON.put("managers", );
        } catch (JSONException e){
            Log.e("OGDevice.model", e.toString());
            return null;
        }

        return deviceJSON;
    }

    public static void setName(Realm realm, String newName){
        OGDevice device = getDevice(realm);

        realm.beginTransaction();
        device.name = newName;
        realm.copyToRealmOrUpdate(device);
        realm.commitTransaction();
    }

    public static void setLocationWithinVenue(Realm realm, String newLoc){
        OGDevice device = getDevice(realm);

        realm.beginTransaction();
        device.locationWithinVenue = newLoc;
        realm.copyToRealmOrUpdate(device);
        realm.commitTransaction();
    }

}
