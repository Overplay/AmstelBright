package io.ourglass.amstelbright2.realm;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ListIterator;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Created by mkahn on 5/9/16.
 */
public class OGApp extends RealmObject {

    @Required
    @PrimaryKey
    public String appId;

    @Required
    public String appType;

    @Required
    public String appName;

    public String version;

    public long installDate;

    int primaryColor;
    int secondaryColor;
    String icon;

    public boolean running = false;

    public int slotNumber = 0;
    public float scale = 1;

    public int xPos = 0;
    public int yPos = 0;

    //height and width are now percentages which can range
    // from 1-100 which represents their percentage of the screen
    public int height;
    public int width;

    private String publicData = "{}";
    private String privateData = "{}";

    private static JSONObject validOrEmptyJson(String jsonString){

        JSONObject rval;

        try {
            rval = new JSONObject(jsonString);
        } catch (Exception e) {
            Log.e("OGApp.model", "Error converting string to JSON");
            rval = new JSONObject();
        }

        return rval;

    }

    public JSONObject getPublicData(){

       return OGApp.validOrEmptyJson(this.publicData);

    }

    public void setPublicData(JSONObject jobj){

        this.publicData = jobj.toString();

    }

    public JSONObject getPrivateData(){

        return OGApp.validOrEmptyJson(this.publicData);

    }

    public JSONObject getAppAsJson(){

        JSONObject rval = new JSONObject();

        try {

            rval.put("appId", this.appId);
            rval.put("appType", this.appType);
            rval.put("appName", this.appName);
            rval.put("running", this.running);
            rval.put("slotNumber", this.slotNumber);
            rval.put("height", this.height);
            rval.put("width", this.width);

            rval.put("primaryColorHex", String.format("#%06X", (0xFFFFFF & this.primaryColor)));
            rval.put("secondaryColorHex", String.format("#%06X", (0xFFFFFF & this.secondaryColor)));

            rval.put("icon", this.icon);
            rval.put("iconPath", "www/opp/"+this.appId+"/assets/icons/"+this.icon);

            rval.put("version", this.version);
            rval.put("installDate", this.installDate);

        } catch (Exception e){

            Log.e("OGApp.model", "Failure converting to JSON");

        }

        return rval;

    }

    public static RealmResults<OGApp> getAllApps(Realm realm){

        return realm.where(OGApp.class).findAll();

    }

    public static JSONArray getAllAppsAsJSON(Realm  realm){

        RealmResults<OGApp> result = getAllApps(realm);

        ListIterator<OGApp> litr = result.listIterator();

        JSONArray rval = new JSONArray();

        while (litr.hasNext()){
            OGApp app = litr.next();
            rval.put(app.getAppAsJson());
        }

        return rval;
    }

    public static Boolean appExists(Realm realm,  String appId){

        RealmResults<OGApp> result = realm.where(OGApp.class)
                .equalTo("appId", appId)
                .findAll();

        return result.size()>0;
    }

    public static OGApp getApp(Realm realm, String appId){

        RealmResults<OGApp> result = realm.where(OGApp.class)
                .equalTo("appId", appId)
                .findAll();

        if (result.size()>0){
            return result.first();
        }

        return null;

    }

    public static OGApp getRunningByType(Realm realm, String appType){

        RealmResults<OGApp> result = realm.where(OGApp.class)
                .equalTo("appType", appType)
                .equalTo("running", true)
                .findAll();

        if (result.size()>0){
            return result.first();
        }

        return null;

    }

    /**
     * this method checks whether or not the jsonObject object passed in contains all required values
     * this was added to with the intent of filtering a json array which possibly contains incorrectly formatted objects
     * because if one object in the array is incorrect, realm seems to not allow any of the objects to be pushed
     * @return true if contains all required fields, else false
     */
    public static boolean jsonObjCorrectlyFormatted(JSONObject candidate){
        try {
            return candidate.has("appId") && !candidate.getString("appId").isEmpty()
                    && candidate.has("appType") && !candidate.getString("appType").isEmpty()
                    && candidate.has("appName") && !candidate.getString("appName").isEmpty()
                    && candidate.has("height") && heightOrWidthIsValidPercentage(candidate.getInt("height"))
//                    && candidate.has("width") && heightOrWidthIsValidPercentage(candidate.getInt("width"))
            ;
                    && candidate.has("appName") && !candidate.getString("appName").isEmpty()
                    && candidate.has("version") && !candidate.getString("version").isEmpty()
                    && candidate.has("installDate") && candidate.getLong("installDate") != 0;
        } catch(JSONException e){
            return false;
        }
    }

    private static boolean heightOrWidthIsValidPercentage(int heightOrWidth){
        return heightOrWidth > 0 && heightOrWidth <= 100;
    }

}
