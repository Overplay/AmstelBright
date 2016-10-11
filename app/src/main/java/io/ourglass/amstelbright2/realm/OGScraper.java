package io.ourglass.amstelbright2.realm;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ListIterator;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.PrimaryKey;

/**
 * Created by mkahn on 6/21/16.
 */

public class OGScraper extends RealmObject {

    @PrimaryKey
    public String appId;

    public String source;
    private String query = "";

    public String data = "{}";

    public int lastUpdate = 0;
    public int refreshPeriod = 1;


    public void setQuery(String query){
        this.query = query;
        // Clear old data and lastUpdate since query changed
        data = "{}";
        lastUpdate = 0;

    }

    public String getQuery(){
        return query;
    }

    public static JSONArray getAllScrapes( Realm realm ){

        RealmResults<OGScraper> result = realm.where(OGScraper.class)
                .findAll();

        ListIterator<OGScraper> litr = result.listIterator();

        JSONArray rval = new JSONArray();

        while (litr.hasNext()){
            OGScraper scrape = litr.next();
            rval.put(scrape.getJson());
        }

        return rval;

    }

    public  JSONObject getJson(){

        JSONObject rval = new JSONObject();

        try {

            rval.put("appId", this.appId);
            rval.put("query", this.query);
            rval.put("lastUpdate", this.lastUpdate);
            rval.put("data", this.data);


        } catch (Exception e){

            Log.e("OGScrape.model", "Failure converting to JSON");

        }

        return rval;


    }

    public static String getScrape(Realm realm, String appId) {

        RealmResults<OGScraper> result = realm.where(OGScraper.class)
                .equalTo("appId", appId)
                .findAll();

        if (result.size() > 0) {
            return result.first().data;
        }

        return null;
    }

    public static String setQueryFor(Realm realm, String appId, String query) {

        RealmResults<OGScraper> result = realm.where(OGScraper.class)
                .equalTo("appId", appId)
                .findAll();

        if (result.size() > 0) {
            OGScraper ogs = result.first();
            realm.beginTransaction();
            ogs.setQuery(query);
            realm.commitTransaction();
        }

        return null;
    }

}
