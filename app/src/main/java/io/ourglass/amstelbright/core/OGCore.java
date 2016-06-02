package io.ourglass.amstelbright.core;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import io.ourglass.amstelbright.core.exceptions.OGServerException;
import io.ourglass.amstelbright.realm.OGApp;
import io.ourglass.amstelbright.core.OGDevice;
import io.ourglass.amstelbright.services.amstelbright.AmstelBrightService;
import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by mkahn on 5/9/16.
 *
 * Switched this to a singleton and forcing all Realm operations to be here so if we have
 * to synch lock, we can.
 *
 */
public class OGCore {

    private static final String TAG = "OGCore";

    private final RealmConfiguration mRealmConfig = new RealmConfiguration.Builder(AmstelBrightService.context).deleteRealmIfMigrationNeeded().build();

    private static final int NUM_WIDGET_SLOTS = 4;
    private static final int NUM_CRAWLER_SLOTS = 2;

    private Boolean mUseExclusionZones;

    private static OGCore instance = null;

    protected OGCore() {

    }

    public static OGCore getInstance() {
        if(instance == null) {
            instance = new OGCore();
        }
        return instance;
    }

    public Realm newThreadRealm(){

        return Realm.getInstance(mRealmConfig);

    }

    public String getAllApps(){

        return OGApp.getAllApps(newThreadRealm()).toString();

    }

    public OGDevice getDeviceAsObject(){
        return OGDevice.getDevice(newThreadRealm());
    }

    public String getDevice(){

        return OGDevice.getDeviceAsJSON(newThreadRealm()).toString();
    }

    public void updateDevice(String attrName, String newValue){
        switch(attrName){
            case "name":
                OGDevice.setName(newThreadRealm(), newValue);
                break;
            case "locationWithinVenue":
                OGDevice.setLocationWithinVenue(newThreadRealm(), newValue);
                break;
            default:
                Log.e(getClass().toString(),
                        "attempted to update device with invalid attribute <" + attrName + "> - device unaffected");
        }
    }

    public JSONObject updateAppData(String appId, JSONObject dataJson) throws OGServerException {

        Realm tempRealm = newThreadRealm();

        OGApp app = OGApp.getApp(tempRealm, appId);
        if (app==null){
            throw new OGServerException("no such app")
                    .ofType(OGServerException.ErrorType.NO_SUCH_APP);
        }

        tempRealm.beginTransaction();
        app.setPublicData(dataJson);
        tempRealm.commitTransaction();

        return dataJson;  // TODO is this needed?

    }

    public JSONObject getAppDataFor(String appId) throws OGServerException{

        OGApp app = OGApp.getApp(newThreadRealm(), appId);
        if (app==null){
            throw new OGServerException("no such app")
                    .ofType(OGServerException.ErrorType.NO_SUCH_APP);
        }

        return app.getPublicData();

    }

    public OGApp launchApp(String appId) throws OGServerException {

        Log.d(TAG, "Launching app");
        OGApp target = OGApp.getApp(newThreadRealm(), appId);

        if (target==null){
            throw new OGServerException("No such app")
                    .ofType(OGServerException.ErrorType.NO_SUCH_APP);
        }

        if (target.running)
            return target;

        // Per Treb, one app at a time of any particular kind

        Realm tempRealm = newThreadRealm();

        OGApp alreadyRunning = OGApp.getRunningByType(tempRealm, target.appType);

        int slotNumber = -1;

        if (alreadyRunning!=null){
            // grab it's slot
            slotNumber = alreadyRunning.slotNumber;
            // We need to kill the already running app
            killApp(alreadyRunning);
        }


        tempRealm.beginTransaction();
        target.running = true;
        // Replace already positioned slot, otherwise keep last slot
        if (slotNumber>-1)
            target.slotNumber = slotNumber;
        tempRealm.commitTransaction();

        sendCommandIntent("launch", target);

        return target;
    }

    public OGApp killApp(OGApp markedForDeath){

        Realm tempRealm = newThreadRealm();

        tempRealm.beginTransaction();
        markedForDeath.running = false;
        tempRealm.commitTransaction();
        sendCommandIntent("kill", markedForDeath);
        return markedForDeath;

    }

    public OGApp killApp(String appId) throws OGServerException {

        Log.d(TAG, "Killing app");
        OGApp target = OGApp.getApp(newThreadRealm(), appId);

        if (target==null){
            throw new OGServerException("No such app")
                    .ofType(OGServerException.ErrorType.NO_SUCH_APP);
        }

        if (!target.running)
            throw new OGServerException("App not currently running")
                    .ofType(OGServerException.ErrorType.APP_NOT_RUNNING);

        return killApp(target);

    }

    public OGApp moveApp(String appId) throws OGServerException {

        Log.d(TAG, "Moving app");

        Realm tempRealm = newThreadRealm();

        OGApp target = OGApp.getApp(tempRealm, appId);

        if (target==null){
            throw new OGServerException("No such app")
                    .ofType(OGServerException.ErrorType.NO_SUCH_APP);
        }

        if (!target.running)
            throw new OGServerException("App not currently running")
                    .ofType(OGServerException.ErrorType.APP_NOT_RUNNING);

        // TODO crashing here due to nested transaction...
        tempRealm.beginTransaction();

        target.slotNumber++;

        switch (target.appType){

            case "crawler":
                if (target.slotNumber==NUM_CRAWLER_SLOTS)
                    target.slotNumber = 0;
                break;

            case "widget":
                if (target.slotNumber==NUM_WIDGET_SLOTS)
                    target.slotNumber = 0;
                break;

        }


        tempRealm.commitTransaction();

        sendCommandIntent("move", target);

        return target;

    }

    private void sendCommandIntent(String cmd, OGApp target) {

        // Notify
        Intent intent = new Intent();
        intent.setAction("com.ourglass.amstelbrightserver");
        intent.putExtra("command", cmd );
        intent.putExtra("appId",target.appId );
        intent.putExtra("app", target.getAppAsJson().toString() );
        AmstelBrightService.context.sendBroadcast(intent);

    }

    public void sendStatusIntent(String cmd, String msg, int code) {

        // Notify
        Intent intent = new Intent();
        intent.setAction("com.ourglass.amstelbrightserver.status");
        intent.putExtra("command", cmd );
        intent.putExtra("message",msg );
        intent.putExtra("code", code);
        AmstelBrightService.context.sendBroadcast(intent);

    }


    public void installStockApps(Context context){
        Log.d(TAG, "Installing stock apps");

        JSONArray appArr = new JSONArray();

        try {

            JSONObject pubCrawlerJson = new JSONObject()
                    .put("appId", "io.ourglass.pubcrawler")
                    .put("appType", "crawler")
                    .put("screenName", "Pub Crawler")
                    .put("onLauncher", true);

            appArr.put(pubCrawlerJson);

            JSONObject pubCrawler2Json = new JSONObject()
                    .put("appId", "io.ourglass.pubcrawler2")
                    .put("appType", "crawler")
                    .put("screenName", "Combo Crawler")
                    .put("onLauncher", true);

            appArr.put(pubCrawler2Json);

            JSONObject budboard = new JSONObject()
                    .put("appId", "io.ourglass.budboard2")
                    .put("appType", "crawler")
                    .put("screenName", "Bud Board")
                    .put("onLauncher", true);

            appArr.put(budboard);

            JSONObject shuffle = new JSONObject()
                    .put("appId", "io.ourglass.shuffleboard")
                    .put("appType", "widget")
                    .put("screenName", "Shuffleboard")
                    .put("onLauncher", true);

            appArr.put(shuffle);



        } catch (Exception e){
            Log.e(TAG, "You screwed up your stock app Json, fix it!");
        }

        Realm tempRealm = newThreadRealm();

        tempRealm.beginTransaction();
        tempRealm.createOrUpdateAllFromJson(OGApp.class, appArr);
        tempRealm.commitTransaction();

    }

    public static void getApps(){


    }

}
