package io.ourglass.amstelbright2.core;

import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import io.ourglass.amstelbright2.core.exceptions.OGServerException;
import io.ourglass.amstelbright2.realm.OGApp;
import io.ourglass.amstelbright2.realm.OGDevice;
import io.ourglass.amstelbright2.realm.OGScraper;
import io.ourglass.amstelbright2.services.amstelbright.AmstelBrightService;
import io.realm.OGAppRealmProxy;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

/**
 * Created by mkahn on 5/9/16.
 * <p/>
 * Switched this to a singleton and forcing all Realm operations to be here so if we have
 * to synch lock, we can.
 */
public class OGCore {

    private static final String TAG = "OGCore";

    private static final int NUM_WIDGET_SLOTS = 4;
    private static final int NUM_CRAWLER_SLOTS = 2;

    public static String channel = "";
    public static String programId = "";
    public static String programTitle = "";

    public static boolean setChannelInfo(String channel, String programId, String programTitle){

        if ( OGCore.channel.equalsIgnoreCase(channel) &&
                OGCore.programId.equalsIgnoreCase(programId) &&
                    OGCore.programTitle.equalsIgnoreCase(programTitle) )
            return false;

        OGCore.channel = channel;
        OGCore.programId = programId;
        OGCore.programTitle = programTitle;

        Intent intent = new Intent();
        intent.setAction("com.ourglass.amstelbrightserver");
        intent.putExtra("command", "NEW_CHANNEL");
        intent.putExtra("channel", channel);
        AmstelBrightService.context.sendBroadcast(intent);

        //  Add scrape for channel

        Realm realm = Realm.getDefaultInstance();

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {

                OGScraper channelScraper;

                RealmResults<OGScraper> ctwitter = realm.where(OGScraper.class)
                        .equalTo("appId", "io.ourglass.core.channeltweets")
                        .findAll();

                if (ctwitter.size()==0){
                    // There isn't a channel scrape, create
                    channelScraper = realm.createObject(OGScraper.class);
                } else {
                    channelScraper = ctwitter.first();
                }

                channelScraper.appId = "io.ourglass.core.channeltweets";
                channelScraper.setQuery(OGCore.programTitle+"&lang=en&result_type=popular&include_entities=false");

            }
        });


        return true;

    }

    public static JSONObject getCurrentChannel(){

            JSONObject jobj = new JSONObject();
        try {
            jobj.put("channel", OGCore.channel);
            jobj.put("programId", OGCore.programId);
            jobj.put("programTitle", OGCore.programTitle);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jobj;
    }

    public static OGDevice getDeviceAsObject(Realm realm) {
        return OGDevice.getDevice(realm);
    }

    public static JSONObject getDeviceAsJSON(Realm realm) {

        return OGDevice.getDeviceAsJSON(realm);
    }


    public static JSONObject getAppDataFor(Realm realm, String appId) throws OGServerException {

        OGApp app = OGApp.getApp(realm, appId);

        if (app == null) {
            throw new OGServerException("no such app")
                    .ofType(OGServerException.ErrorType.NO_SUCH_APP);
        }

        return app.getPublicData();

    }

    public static OGApp launchApp(Realm realm, String appId) throws OGServerException {

        Log.d(TAG, "Launching app");
        final OGApp target = OGApp.getApp(realm, appId);

        if (target == null) {
            throw new OGServerException("No such app")
                    .ofType(OGServerException.ErrorType.NO_SUCH_APP);
        }

        if (target.running)
            return target;


        // Per Treb, one app at a time of any particular kind

        OGApp alreadyRunning = OGApp.getRunningByType(realm, target.appType);

        int slotNumber = -1;

        if (alreadyRunning != null) {
            // grab its slot
            slotNumber = alreadyRunning.slotNumber;
            // We need to kill the already running app
            killApp(realm, alreadyRunning);
        }

        final int newSlot = slotNumber;

        realm.beginTransaction();
        target.running = true;
        if (newSlot > -1)
            target.slotNumber = newSlot;
        sendCommandIntent("launch", target);
        realm.commitTransaction();

        return target;
    }

    public static OGApp killApp(Realm realm, final OGApp markedForDeath) {

        sendCommandIntent("kill", markedForDeath);

        realm.beginTransaction();
        markedForDeath.running = false;
        realm.commitTransaction();

        return markedForDeath;

    }

    public static OGApp killApp(Realm realm, String appId) throws OGServerException {

        Log.d(TAG, "Killing app");
        OGApp target = OGApp.getApp(realm, appId);

        if (target == null) {
            throw new OGServerException("No such app")
                    .ofType(OGServerException.ErrorType.NO_SUCH_APP);
        }

        if (!target.running)
            throw new OGServerException("App not currently running")
                    .ofType(OGServerException.ErrorType.APP_NOT_RUNNING);

        return killApp(realm, target);

    }

    public static OGApp moveApp(Realm realm, String appId) throws OGServerException {

        Log.d(TAG, "Moving app");

        final OGApp target = OGApp.getApp(realm, appId);

        if (target == null) {
            throw new OGServerException("No such app")
                    .ofType(OGServerException.ErrorType.NO_SUCH_APP);
        }

        if (!target.running)
            throw new OGServerException("App not currently running")
                    .ofType(OGServerException.ErrorType.APP_NOT_RUNNING);

        realm.beginTransaction();

        target.slotNumber++;

        switch (target.appType) {

            case "crawler":
                if (target.slotNumber == NUM_CRAWLER_SLOTS)
                    target.slotNumber = 0;
                break;

            case "widget":
                if (target.slotNumber == NUM_WIDGET_SLOTS)
                    target.slotNumber = 0;
                break;

        }

        sendCommandIntent("move", target);

        realm.commitTransaction();

        return target;

    }

    public void scaleApp(float scale) {

        // TODO implement scaling

    }

    private static void sendCommandIntent(String cmd, OGApp target) {

        // Notify
        Intent intent = new Intent();
        intent.setAction("com.ourglass.amstelbrightserver");
        intent.putExtra("command", cmd);
        intent.putExtra("appId", target.appId);
        intent.putExtra("app", target.getAppAsJson().toString());
        AmstelBrightService.context.sendBroadcast(intent);

    }

    public static void sendStatusIntent(String cmd, String msg, int code) {

        // Notify
        Intent intent = new Intent();
        intent.setAction("com.ourglass.amstelbrightserver.status");
        intent.putExtra("command", cmd);
        intent.putExtra("message", msg);
        intent.putExtra("code", code);
        AmstelBrightService.context.sendBroadcast(intent);

    }

    public static JSONArray installStockApps(){
        Log.d(TAG, "Attempting alternative to install stock apps");
        final JSONArray jsonAppArr = new JSONArray(), scrapeArr = new JSONArray();

        //first need to get the names of all stock apps by reading the opp directory on the sdcard
        ArrayList<String> appArr = getNamesOfAppsFromSd();

        //now should iterate over the list of apps and do a few things in order to install
        for(String appName : appArr) {
            try {
                //1. read the info.json into a string (readInfo())
                String appInfoString = readInfo(appName);
                //2. convert that string into a json object
                JSONObject appInfo = new JSONObject(appInfoString);
                if(OGApp.jsonObjCorrectlyFormatted(appInfo)) {
                    //3. put the json object into a JSON array
                    jsonAppArr.put(appInfo);
                }
                else {
                    Log.e(TAG, "incorrectly formatted: " + appInfo);
                }
            } catch(JSONException e){
                Log.e(TAG, "there was a problem installing " + appName);
            }
        }

        //copied over default scrape objects into array from other installstockapps
        try{
            JSONObject scrapeTwitter = new JSONObject()
                    .put("source", "twitter")
                    .put("query", "\"Steph Curry\"")
                    .put("appId", "io.ourglass.pubcrawler");

            scrapeArr.put(scrapeTwitter);
        } catch (JSONException e){
            Log.e(TAG, "There was a problem inserting the scraper object into the JSON array");
        }

        //check the existing realm Apps and delete whatever is not in the jsonAppArray
        removeUninstalledAppsFromRealm(jsonAppArr);

        //set public data to initialValue for existingApps if they don't have any appdata
        setAppDataIfEmpty(jsonAppArr);

        //commit both of these arrays into realm
        Realm realm = Realm.getDefaultInstance();

        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm bgRealm) {
                bgRealm.createOrUpdateAllFromJson(OGApp.class, jsonAppArr);
                bgRealm.createOrUpdateAllFromJson(OGScraper.class, scrapeArr);
            }
        }, null, null);

        realm.close();

        return jsonAppArr;
    }

    private static void removeUninstalledAppsFromRealm(JSONArray installedApps){
        //populate an arraylist with the names of the apps which are installed in opp
        ArrayList<String> appIds = new ArrayList<String>();
        for (int i = 0 ; i < installedApps.length(); i++) {
            try {
                JSONObject obj = installedApps.getJSONObject(i);
                appIds.add(obj.getString("appId"));
            } catch(org.json.JSONException e){
                Log.e(TAG, "there was a problem with JSON parsing, " +
                        "will probably affect the information in the realm database pertaining to apps" +
                        "\n" + e.getMessage());
            }
        }

        Realm realm = Realm.getDefaultInstance();

        //now iterate over the apps currently in the realm database and if they are not present in appIds, then remove them
        RealmResults<OGApp> appsInDatabase = OGApp.getAllApps(realm);
        for(int j = 0; j < appsInDatabase.size(); j++){
            final OGApp appInDatabase = appsInDatabase.get(j);
            boolean goingToDie = true;
            for(int i = 0; i < appIds.size(); i++){
                String appId = ((OGAppRealmProxy) appInDatabase).realmGet$appId();
                if(appId.equals(appIds.get(i))){
                    goingToDie = false;
                    break;
                }
            }
            //if app in database is not present in installed apps, then delete it from realm
            if(goingToDie){
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm bgRealm) {
                        appInDatabase.deleteFromRealm();
                    }
                });
            }
        }
        realm.close();
    }

    private static void setAppDataIfEmpty(JSONArray newApps){
        for (int i = 0 ; i < newApps.length(); i++) {
            try {
                JSONObject obj = newApps.getJSONObject(i);
                String appId = obj.getString("appId");
                Realm tempRealm = Realm.getDefaultInstance();
                OGApp foundApp = OGApp.getApp(tempRealm, appId);
                if(foundApp == null || foundApp.getPublicData().toString().equals("{}")){
                    Log.v(TAG, "there was no appdata for " + appId + " so setting it to initialValue");
                    JSONObject initValue = obj.getJSONObject("initialValue");
                    obj.put("publicData", initValue);
                }
                obj.remove("initialValue");
            } catch(org.json.JSONException e){
                Log.e(TAG, "there was a problem with JSON parsing, " +
                        "will probably affect the information in the realm database pertaining to apps" +
                        "\n" + e.getMessage());
            }
        }
    }

    private static ArrayList<String> getNamesOfAppsFromSd(){
        String sdcard = Environment.getExternalStorageDirectory().toString() + OGConstants.PATH_TO_ABWL+"/opp";
        File sdcard_dir = new File(sdcard);

        ArrayList<String> fileNamesOnSd = new ArrayList<String>();

        if(sdcard_dir.isDirectory()){
            File[] fileNames = sdcard_dir.listFiles();
            if(fileNames != null) {
                for (File f : fileNames) {
                    fileNamesOnSd.add(f.getAbsolutePath());
                }
            }
            else {
                Log.e(TAG, "Filesnames is null");
            }
        }

        return fileNamesOnSd;
    }

    private static String readInfo(String filename) {
        String toReturn = "";

        BufferedReader br = null;
        try {
            String fpath  = filename  + "/info/info.json";
            Log.v(TAG, "attempting to read: " + fpath);
            try {
                br = new BufferedReader(new FileReader(fpath));
            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
            }
            if(br != null) {
                String line = "";
                while ((line = br.readLine()) != null) {
                    toReturn += line + '\n';
                }
            }
            else {
                Log.e(TAG, "file (" + fpath + ") did not exist");
            }
        } catch (IOException e) {
            Log.e("InfoActivity", e.toString());
        }
        Log.v(TAG, toReturn);
        return toReturn;
    }

    public static void getApps() {


    }

    public void setChannel(String channel) {
        if (this.channel.equalsIgnoreCase(channel))
            return;

        Log.d(TAG, "New channel is: " + channel);
        this.channel = channel;
        //TODO add auto-placement code here

    }

    public void setProgramTitle(String title) {
        this.programTitle = title;
    }

    public void setProgramId(String id) {
        this.programId = id;
    }


    private JSONObject validOrEmptyJson(String jsonString) {

        JSONObject rval;

        try {
            rval = new JSONObject(jsonString);
        } catch (Exception e) {
            Log.e("OGApp.model", "Error converting string to JSON");
            rval = new JSONObject();
        }

        return rval;

    }

}