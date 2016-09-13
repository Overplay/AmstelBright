package io.ourglass.amstelbright2.core;

import android.content.Intent;
import android.os.Environment;
import android.support.annotation.Nullable;
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
import io.ourglass.amstelbright2.realm.OGAdvertisement;
import io.ourglass.amstelbright2.realm.OGApp;
import io.ourglass.amstelbright2.realm.OGDevice;
import io.ourglass.amstelbright2.realm.OGLog;
import io.ourglass.amstelbright2.realm.OGScraper;
import io.ourglass.amstelbright2.services.amstelbright.AmstelBrightService;
import io.realm.OGAppRealmProxy;
import io.realm.Realm;
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

    private static int adIndex = 0;

    public static boolean setChannelInfo(String channel, String programId, String programTitle){

        if ( OGCore.channel.equalsIgnoreCase(channel) &&
                OGCore.programId.equalsIgnoreCase(programId) &&
                    OGCore.programTitle.equalsIgnoreCase(programTitle) )
            return false;

        //log the changed information
        log_channelChange(OGCore.channel, OGCore.programId, OGCore.programTitle, channel, programId, programTitle);

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

    public static void adjustApp(Realm realm, String appId, float scale, int xAdjust, int yAdjust) throws OGServerException{
        Log.d(TAG, "scaling app");

        final OGApp target = OGApp.getApp(realm, appId);

        if (target == null) {
            throw new OGServerException("No such app")
                    .ofType(OGServerException.ErrorType.NO_SUCH_APP);
        }

        if (!target.running)
            throw new OGServerException("App not currently running")
                    .ofType(OGServerException.ErrorType.APP_NOT_RUNNING);

        realm.beginTransaction();

        Intent intent = new Intent();
        intent.setAction("com.ourglass.amstelbrightserver");
        intent.putExtra("command", "adjust");
        intent.putExtra("appId", target.appId);
        intent.putExtra("app", target.getAppAsJson().toString());
        intent.putExtra("scale", scale);
        intent.putExtra("xAdjust", xAdjust);
        intent.putExtra("yAdjust", yAdjust);
        AmstelBrightService.context.sendBroadcast(intent);

        realm.commitTransaction();


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

    public static String[] getApps() {
        String[] toReturn;

        Realm realm = Realm.getDefaultInstance();

        RealmResults<OGApp> allApps = realm.where(OGApp.class).findAll();
        toReturn = new String[allApps.size()];

        int i = 0;
        for(OGApp app : allApps){
            toReturn[i++] = app.appId;
        }

        return toReturn;
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

    public static void log_adImpression(String adId){
        try {
            JSONObject logData = new JSONObject();

            logData.put("adId", adId);

            systemLog("IMPRESSION", logData);
            Log.v(TAG, "Ad Impression logged");
        } catch (JSONException e){
            Log.w(TAG, "Ad Impression log could not be created\n" + e.getMessage());
        }
    }

    /**
     * method to structure the passed in parameters into a HEARTBEAT type system log
     *
     * NOTE:some required information for the heartbeat is added automatically
     * @param abVersion String of the Amstel Bright version installed at time of heartbeat
     * @param aquiVersion String of the Aqui version installed at the time of heartbeat
     * @param androidVersion String of the current build of android at time of the heartbeat
     */
    public static void log_heartbeat(String abVersion, String aquiVersion, String androidVersion){
        //calculate the uptime
        long uptime = System.currentTimeMillis() - AmstelBrightService.bootTime;

        //determine the apps currently installed
        String[] installedApps = OGCore.getApps();

        try {
            JSONObject logData = new JSONObject(),
               softwareVersions = new JSONObject();

            logData.put("uptime", uptime);

            softwareVersions.put("amstelBright", abVersion);
            softwareVersions.put("aqui", aquiVersion);
            softwareVersions.put("androidVersion", androidVersion);

            logData.put("softwareVersions", softwareVersions);

            JSONArray installedAppsJSONArray = new JSONArray(installedApps);
            logData.put("installedApps", installedAppsJSONArray);

            systemLog("HEARTBEAT", logData);
            Log.v(TAG, "Heartbeat logged");
       } catch (JSONException e) {
            Log.w(TAG, "Heartbeat log could not be created\n" + e.getMessage());
        }
    }

    /**
     * method to structure the passed in parameters into an ALERT type system log
     * @param alertCause Cause of the alert (should be loosely enumerated)
     * @param additionalInfo Additional info relating to the alert, if not applicable, pass in an empty string
     */
    public static void log_alert(String alertCause, String additionalInfo){
        try {
            JSONObject logData = new JSONObject();

            logData.put("alertCause", alertCause);
            logData.put("additionalInfo", additionalInfo);

            systemLog("ALERT", logData);
            Log.v(TAG, "Alert logged");
        } catch (JSONException e){
            Log.w(TAG, "Alert log could not be created\n" + e.getMessage());
        }
    }

    /**
     * method to structure the passed in parameters into a CHANNEL_CHANGE type system log
     * @param fromChannelId Id of the channel that was previously tuned
     * @param fromProgramId Id of the program that was previously tuned
     * @param fromProgramTitle Title of the program that was previously tuned to
     * @param toChannelId Id of the channel that has been changed to
     * @param toProgramId Id of the program that has been changed to
     * @param toProgramTitle Title of the program that has been changed to
     */
    public static void log_channelChange(
            String fromChannelId,
            String fromProgramId,
            String fromProgramTitle,
            String toChannelId,
            String toProgramId,
            String toProgramTitle
    ) {
        try {
            JSONObject logData = new JSONObject(),
                    changedFrom = new JSONObject(),
                    changedTo = new JSONObject();

            changedFrom.put("channelId", fromChannelId);
            changedFrom.put("programId", fromProgramId);
            changedFrom.put("programTitle", fromProgramTitle);

            changedTo.put("channelId", toChannelId);
            changedTo.put("programId", toProgramId);
            changedTo.put("programTitle", toProgramTitle);

            logData.put("changedFrom", changedFrom);
            logData.put("changedTo", changedTo);

            systemLog("CHANNEL", logData);
            Log.v(TAG, "channel change logged");
        } catch (JSONException e){
            Log.w(TAG, "Channel change could not be created\n" + e.getMessage());
        }
    }

    /**
     * method to structure the passed in parameters into a OVERRIDE_PLACEMENT type system log
     * @param channelId Channel programmed to when the placement override event occurred
     * @param programId Id of the program that was being watched when the placement override event occurred
     * @param appId Id of the app which was moved
     * @param movedTo Slot that the app was moved to
     */
    public static void log_placementOverride(String channelId, String programId, String appId, int movedTo){
        try {
            JSONObject logData = new JSONObject();

            logData.put("channelId", channelId);
            logData.put("programId", programId);
            logData.put("appId", appId);
            logData.put("movedTo", movedTo);

            systemLog("PLACEMENT", logData);
            Log.v(TAG, "placement override logged");
        } catch (JSONException e){
            Log.w(TAG, "Placement override log could not be created\n" + e.getMessage());
        }
    }

    /**
     * method to create an OGLog object given a type and messageBody
     * this is private to force people to use the more structured public methods which call this method
     *
     * NOTE: that OGLog automatically sets the timeLogged and deviceId upon object creation
     * @param type Type of log message
     * @param logData Payload of the log
     */
    private static void systemLog(String type, JSONObject logData){
        OGLog log = new OGLog();
        log.setType(type);
        log.setData(logData);

        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.copyToRealm(log);
//        realm.copyToRealmOrUpdate(log);
        realm.commitTransaction();

        // This should have realm.close(), no?

    }

    @Nullable
    public static OGAdvertisement getAdvertisement(){
        Realm realm = Realm.getDefaultInstance();

        RealmResults<OGAdvertisement> ads = realm.where(OGAdvertisement.class).findAll();
        //OGAdvertisement[] adArr = (OGAdvertisement[]) ads.toArray();
        Object[] adArr = ads.toArray();

        if(adArr.length == 0){
            Log.w(TAG, "Attempted to get advertisement and there are none in the database");
            return null;
        }

        //insert code to log previous advertisement

        return (OGAdvertisement) adArr[(adIndex = (adIndex + 1) % adArr.length)];
    }

}
