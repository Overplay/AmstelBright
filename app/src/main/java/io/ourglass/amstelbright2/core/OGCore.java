package io.ourglass.amstelbright2.core;

import android.content.Intent;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.util.Log;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
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
import io.ourglass.amstelbright2.realm.OGLog;
import io.ourglass.amstelbright2.realm.OGScraper;
import io.ourglass.amstelbright2.services.amstelbright.AmstelBrightService;
import io.ourglass.amstelbright2.services.stbservice.TVShow;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

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

    private static final OkHttpClient client = ABApplication.okclient;  // share it

    // TODO why is this here and not in OGSystem? I don't remember, MAK.

    public static TVShow currentlyOnTV;

    private static int mAdIdx = 0;

    public static boolean setCurrentlyOnTV(TVShow show) {

        if (show == null) {
            Log.wtf(TAG, "Got a NULL show, what da fu?");
            return false;
        }

        // TODO this if-then could be simplified
        if (currentlyOnTV == null) {
            currentlyOnTV = show;
        } else if (OGCore.currentlyOnTV.equals(show)) {
            return false;
        } else {
            //log the changed information
            log_channelChange(currentlyOnTV, show);
            currentlyOnTV = show;
        }

        Intent intent = new Intent();
        intent.setAction("com.ourglass.amstelbrightserver");
        intent.putExtra("command", "NEW_CHANNEL");
        // TODO this was blowing null pointer chunks on colde boot. Not sure why.
        intent.putExtra("channel", (currentlyOnTV.networkName == null) ? "NULLTV" : currentlyOnTV.networkName);
        ABApplication.sharedContext.sendBroadcast(intent);

        //  Add scrape for channel

        Realm realm = Realm.getDefaultInstance();

        // TODO doesn't seem like this should be here. CloudScraper should listen for this
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {

                OGScraper channelScraper;

                RealmResults<OGScraper> ctwitter = realm.where(OGScraper.class)
                        .equalTo("appId", "io.ourglass.core.channeltweets")
                        .findAll();

                if (ctwitter.size() == 0) {
                    // There isn't a channel scrape, create
                    channelScraper = realm.createObject(OGScraper.class);
                } else {
                    channelScraper = ctwitter.first();
                }

                channelScraper.appId = "io.ourglass.core.channeltweets";
                channelScraper.setQuery(currentlyOnTV.title + "&lang=en&result_type=popular&include_entities=false");

            }
        });


        realm.close();

        return true;

    }

    public static JSONObject getCurrentChannel() {

        JSONObject jobj = new JSONObject();
        if (currentlyOnTV != null) {
            try {
                jobj.put("channel", currentlyOnTV.networkName);
                jobj.put("programId", currentlyOnTV.programId);
                jobj.put("programTitle", currentlyOnTV.title);

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        return jobj;
    }


    public static JSONObject getAppDataFor(String appId) throws OGServerException {

        Realm realm = Realm.getDefaultInstance();

        OGApp app = OGApp.getApp(realm, appId);

        if (app == null) {
            throw new OGServerException("no such app")
                    .ofType(OGServerException.ErrorType.NO_SUCH_APP);
        }

        JSONObject rval = app.getPublicData();
        realm.close();

        return rval;

    }

    public static JSONObject launchApp(String appId) throws OGServerException {

        Realm realm = Realm.getDefaultInstance();

        Log.d(TAG, "Launching app");
        final OGApp target = OGApp.getApp(realm, appId);

        if (target == null) {
            realm.close();
            throw new OGServerException("No such app")
                    .ofType(OGServerException.ErrorType.NO_SUCH_APP);
        }

        if (!target.running) {

            // One app at a time of any particular kind

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
        }

        JSONObject rval = target.toJson();
        return rval;
    }

    private static OGApp killApp(Realm realm, final OGApp markedForDeath) {

        sendCommandIntent("kill", markedForDeath);
        realm.beginTransaction();
        markedForDeath.running = false;
        realm.commitTransaction();
        return markedForDeath;

    }

    public static JSONObject killApp(String appId) throws OGServerException {

        Realm realm = Realm.getDefaultInstance();
        Log.d(TAG, "Killing app");
        OGApp target = OGApp.getApp(realm, appId);

        if (target == null) {
            realm.close();
            throw new OGServerException("No such app")
                    .ofType(OGServerException.ErrorType.NO_SUCH_APP);
        }

        if (!target.running){
            realm.close();
            throw new OGServerException("App not currently running")
                    .ofType(OGServerException.ErrorType.APP_NOT_RUNNING);
        }

        OGApp killed = killApp(realm, target);
        JSONObject rval = killed.toJson();
        realm.close();
        return rval;

    }

    public static JSONObject moveApp(String appId) throws OGServerException {

        Log.d(TAG, "Moving app");
        Realm realm = Realm.getDefaultInstance();

        final OGApp target = OGApp.getApp(realm, appId);

        if (target == null) {
            realm.close();
            throw new OGServerException("No such app")
                    .ofType(OGServerException.ErrorType.NO_SUCH_APP);
        }

        if (!target.running){
            realm.close();
            throw new OGServerException("App not currently running")
                    .ofType(OGServerException.ErrorType.APP_NOT_RUNNING);
        }

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
        JSONObject rval = target.toJson();
        realm.close();
        return rval;
    }

    //TODO scaling needs to be saved in Realm OGApp
    public static JSONObject adjustApp(String appId, float scale, int xAdjust, int yAdjust) throws OGServerException {
        Log.d(TAG, "scaling app");

        Realm realm = Realm.getDefaultInstance();
        final OGApp target = OGApp.getApp(realm, appId);

        if (target == null) {
            realm.close();
            throw new OGServerException("No such app")
                    .ofType(OGServerException.ErrorType.NO_SUCH_APP);
        }

        if (!target.running){
            realm.close();
            throw new OGServerException("App not currently running")
                    .ofType(OGServerException.ErrorType.APP_NOT_RUNNING);
        }

        //realm.beginTransaction();
        Intent intent = new Intent();
        intent.setAction("com.ourglass.amstelbrightserver");
        intent.putExtra("command", "adjust");
        intent.putExtra("appId", target.appId);
        intent.putExtra("app", target.toJson().toString());
        intent.putExtra("scale", scale);
        intent.putExtra("xAdjust", xAdjust);
        intent.putExtra("yAdjust", yAdjust);
        ABApplication.sharedContext.sendBroadcast(intent);

        //realm.commitTransaction();
        JSONObject rval = target.toJson();
        realm.close();
        return rval;

    }

    private static void sendCommandIntent(String cmd, OGApp target) {

        // Notify
        Intent intent = new Intent();
        intent.setAction("com.ourglass.amstelbrightserver");
        intent.putExtra("command", cmd);
        intent.putExtra("appId", target.appId);
        intent.putExtra("app", target.toJson().toString());
        ABApplication.sharedContext.sendBroadcast(intent);

    }

    public static void sendStatusIntent(String cmd, String msg, int code) {

        // Notify
        Intent intent = new Intent();
        intent.setAction("com.ourglass.amstelbrightserver.status");
        intent.putExtra("command", cmd);
        intent.putExtra("message", msg);
        intent.putExtra("code", code);
        ABApplication.sharedContext.sendBroadcast(intent);

    }

    public static JSONArray installStockApps() {
        Log.d(TAG, "Attempting alternative to install stock apps");
        final JSONArray jsonAppArr = new JSONArray(), scrapeArr = new JSONArray();

        //first need to get the names of all stock apps by reading the opp directory on the sdcard
        ArrayList<String> appArr = getNamesOfAppsFromSd();

        //now should iterate over the list of apps and do a few things in order to install
        for (String appName : appArr) {
            try {
                //1. read the info.json into a string (readInfo())
                String appInfoString = readInfo(appName);
                //2. convert that string into a json object
                JSONObject appInfo = new JSONObject(appInfoString);
                if (OGApp.jsonObjCorrectlyFormatted(appInfo)) {
                    //3. put the json object into a JSON array
                    jsonAppArr.put(appInfo);
                } else {
                    Log.e(TAG, "incorrectly formatted: " + appInfo);
                }
            } catch (JSONException e) {
                Log.e(TAG, "there was a problem installing " + appName);
            }
        }

        //copied over default scrape objects into array from other installstockapps
        try {
            JSONObject scrapeTwitter = new JSONObject()
                    .put("source", "twitter")
                    .put("query", "sports")
                    .put("appId", "io.ourglass.ogcrawler");

            scrapeArr.put(scrapeTwitter);
        } catch (JSONException e) {
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

    private static void removeUninstalledAppsFromRealm(JSONArray installedApps) {

        //populate an arraylist with the names of the apps which are installed in opp
        ArrayList<String> appIds = new ArrayList<String>();
        for (int i = 0; i < installedApps.length(); i++) {
            try {
                JSONObject obj = installedApps.getJSONObject(i);
                appIds.add(obj.getString("appId"));
            } catch (org.json.JSONException e) {
                Log.e(TAG, "there was a problem with JSON parsing, " +
                        "will probably affect the information in the realm database pertaining to apps" +
                        "\n" + e.getMessage());
            }
        }

        Realm realm = Realm.getDefaultInstance();

        // initial query, all apps
        RealmQuery<OGApp> query = realm.where(OGApp.class);

        // Build a query of all appids NOT in the installed list pulled from SDCARD
        for (String appid : appIds) {
            query = query.not().equalTo("appId", appid);
        }

        final RealmResults<OGApp> toDie = query.findAll();

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                toDie.deleteAllFromRealm();
            }
        });

        realm.close();


    }

    private static void setAppDataIfEmpty(JSONArray newApps) {
        for (int i = 0; i < newApps.length(); i++) {
            try {
                JSONObject obj = newApps.getJSONObject(i);
                String appId = obj.getString("appId");
                Realm tempRealm = Realm.getDefaultInstance();
                OGApp foundApp = OGApp.getApp(tempRealm, appId);
                if (foundApp == null || foundApp.getPublicData().toString().equals("{}")) {
                    Log.v(TAG, "there was no appdata for " + appId + " so setting it to initialValue");
                    JSONObject initValue = obj.getJSONObject("initialValue");
                    obj.put("publicData", initValue);
                }
                obj.remove("initialValue");
            } catch (org.json.JSONException e) {
                Log.e(TAG, "there was a problem with JSON parsing, " +
                        "will probably affect the information in the realm database pertaining to apps" +
                        "\n" + e.getMessage());
            }
        }
    }

    private static ArrayList<String> getNamesOfAppsFromSd() {

        String sdcard = Environment.getExternalStorageDirectory().toString() + OGConstants.PATH_TO_ABWL + "/opp";
        File sdcard_dir = new File(sdcard);

        ArrayList<String> fileNamesOnSd = new ArrayList<String>();

        if (sdcard_dir.isDirectory()) {
            File[] fileNames = sdcard_dir.listFiles();
            if (fileNames != null) {
                for (File f : fileNames) {
                    fileNamesOnSd.add(f.getAbsolutePath());
                }
            } else {
                Log.e(TAG, "Filesnames is null");
            }
        }

        return fileNamesOnSd;
    }

    private static String readInfo(String filename) {
        String toReturn = "";

        BufferedReader br = null;
        try {
            String fpath = filename + "/info/info.json";
            Log.v(TAG, "attempting to read: " + fpath);
            try {
                br = new BufferedReader(new FileReader(fpath));
            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
            }
            if (br != null) {
                String line = "";
                while ((line = br.readLine()) != null) {
                    toReturn += line + '\n';
                }
            } else {
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
        for (OGApp app : allApps) {
            toReturn[i++] = app.appId;
        }

        return toReturn;
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

    public static void log_adImpression(String adId) {
        try {
            JSONObject logData = new JSONObject();

            logData.put("adId", adId);

            systemLog("IMPRESSION", logData);
            Log.v(TAG, "Ad Impression logged");
        } catch (JSONException e) {
            Log.w(TAG, "Ad Impression log could not be created\n" + e.getMessage());
        }
    }

    /**
     * method to structure the passed in parameters into a HEARTBEAT type system log
     * <p>
     * NOTE:some required information for the heartbeat is added automatically
     *
     * @param abVersion      String of the Amstel Bright version installed at time of heartbeat
     * @param aquiVersion    String of the Aqui version installed at the time of heartbeat
     * @param androidVersion String of the current build of android at time of the heartbeat
     */
    public static void log_heartbeat(String abVersion, String aquiVersion, String androidVersion) {
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
     *
     * @param alertCause     Cause of the alert (should be loosely enumerated)
     * @param additionalInfo Additional info relating to the alert, if not applicable, pass in an empty string
     */
    public static void log_alert(String alertCause, String additionalInfo) {
        try {
            JSONObject logData = new JSONObject();

            logData.put("alertCause", alertCause);
            logData.put("additionalInfo", additionalInfo);

            systemLog("ALERT", logData);
            Log.v(TAG, "Alert logged");
        } catch (JSONException e) {
            Log.w(TAG, "Alert log could not be created\n" + e.getMessage());
        }
    }

    /**
     * method to structure the passed in parameters into a CHANNEL_CHANGE type system log
     */
    public static void log_channelChange(TVShow oldShow, TVShow newShow) {

        try {
            JSONObject logData = new JSONObject(),
                    changedFrom = new JSONObject(),
                    changedTo = new JSONObject();

            changedFrom.put("channelId", oldShow.networkName);
            changedFrom.put("programId", oldShow.programId);
            changedFrom.put("programTitle", oldShow.title);

            changedTo.put("channelId", newShow.networkName);
            changedTo.put("programId", newShow.programId);
            changedTo.put("programTitle", newShow.title);

            logData.put("changedFrom", changedFrom);
            logData.put("changedTo", changedTo);

            systemLog("CHANNEL", logData);
            Log.v(TAG, "channel change logged");
        } catch (Exception e) {
            Log.w(TAG, "Channel change log could not be created\n" + e.getMessage());
        }
    }

    /**
     * method to structure the passed in parameters into a OVERRIDE_PLACEMENT type system log
     *
     * @param channelId Channel programmed to when the placement override event occurred
     * @param programId Id of the program that was being watched when the placement override event occurred
     * @param appId     Id of the app which was moved
     * @param movedTo   Slot that the app was moved to
     */
    public static void log_placementOverride(String channelId, String programId, String appId, int movedTo) {
        try {
            JSONObject logData = new JSONObject();

            logData.put("channelId", channelId);
            logData.put("programId", programId);
            logData.put("appId", appId);
            logData.put("movedTo", movedTo);

            systemLog("PLACEMENT", logData);
            Log.v(TAG, "placement override logged");
        } catch (JSONException e) {
            Log.w(TAG, "Placement override log could not be created\n" + e.getMessage());
        }
    }

    /**
     * method to create an OGLog object given a type and messageBody
     * this is private to force people to use the more structured public methods which call this method
     * <p>
     * NOTE: that OGLog automatically sets the timeLogged and deviceId upon object creation
     *
     * @param type    Type of log message
     * @param logData Payload of the log
     */
    private static void systemLog(String type, JSONObject logData) {
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

    // TODO, this is a shitty implementation. Returning true/false is almost useless
    public static JSONHTTPResponse registerWithAsahi(String regCode) {


        RequestBody formBody = new FormBody.Builder()
                .add("regCode", regCode)
                .add("udid", OGSystem.uniqueDeviceId())
                .build();

        Request request = new Request.Builder()
                .url(OGConstants.ASAHI_ADDRESS + "/device/registerDevice")
                .post(formBody)
                .build();


        Response response = null;
        JSONHTTPResponse jsonResponse;

        try {
            response = client.newCall(request).execute();
            jsonResponse = new JSONHTTPResponse(response.body().string(), response.code());

            if (!response.isSuccessful()) {
                Log.d(TAG, "Bad server response registering code.");
                return jsonResponse;
            }

            Log.d(TAG, "Successfully registered with server with code!");

            if (!jsonResponse.isGoodJSON) {
                Log.d(TAG, "Got bad JSON on registration!");
                return jsonResponse;
            }

            OGSystem.setVenueId(jsonResponse.jsonResponseObject.optString("venue", ""));
            OGSystem.setDeviceId(jsonResponse.jsonResponseObject.optString("id", ""));
            OGSystem.setDeviceAPIToken(jsonResponse.jsonResponseObject.optString("apiToken", ""));

            OGSystem.setSystemLocation(jsonResponse.jsonResponseObject.optString("locationWithinVenue", "Not Set"));
            OGSystem.setSystemName(jsonResponse.jsonResponseObject.optString("name", "No Name"));

            return jsonResponse;

        } catch (IOException e) {
            e.printStackTrace();
            Log.w(TAG, "There was an error registering (" + e.getMessage() + ")");
            return null;

        }


    }

    // TODO, this is a shitty implementation. Returning true/false is almost useless
    public static JSONHTTPResponse updateNameLocOnAsahi() {


        RequestBody formBody = new FormBody.Builder()
                .add("name", OGSystem.getSystemName())
                .add("locationWithinVenue", OGSystem.getSystemLocation())
                .build();

        Request request = new Request.Builder()
                .url(OGConstants.ASAHI_ADDRESS + "/device/updateNameLocation/" + OGSystem.getDeviceId())
                .post(formBody)
                .build();


        Response response = null;
        JSONHTTPResponse jsonResponse;

        try {
            response = client.newCall(request).execute();
            jsonResponse = new JSONHTTPResponse(response.body().string(), response.code());

            if (!response.isSuccessful()) {
                Log.d(TAG, "Bad server response changing name or loc.");
                return jsonResponse;
            }

            Log.d(TAG, "Successfully updated name and location on Asahi!");

            if (!jsonResponse.isGoodJSON) {
                Log.d(TAG, "Got bad JSON on name change!");
                return jsonResponse;
            }

            return jsonResponse;

        } catch (IOException e) {
            e.printStackTrace();
            Log.w(TAG, "There was an error changing name (" + e.getMessage() + ")");
            return null;
        }


    }

    @Nullable
    public static JSONObject getAdvertisement() {

        Realm realm = Realm.getDefaultInstance();

        RealmResults<OGAdvertisement> ads = realm.where(OGAdvertisement.class).findAll();

        if (ads.size() == 0) {
            Log.w(TAG, "Attempted to get advertisement and there are none in the database");
            return null;
        }

        OGAdvertisement nextAd = ads.get(mAdIdx = (mAdIdx + 1) % ads.size());
        JSONObject rval = nextAd.adAsJSON();
        realm.close();

        //TODO insert code to log previous advertisement
        return rval;
    }

    public static DateTime dateTimeFromISOString(String isoString) {

        DateTime dateTimeObj = null;
        try {
            dateTimeObj = ISODateTimeFormat.dateTime().parseDateTime(isoString);
            return dateTimeObj;
        } catch (Exception e) {
            Log.e(TAG, "That was a bad ISO date time string!");
        }
        return dateTimeObj;
    }

}
