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

    public static final String TAG  = "OGScraper";

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

            Log.e(TAG, "Failure converting to JSON");

        }

        return rval;


    }

    public static String getScrape(Realm realm, String appId) {

        OGScraper result = realm.where(OGScraper.class)
                .equalTo("appId", appId)
                .findFirst();

        if (result !=null ) {
            return result.data;
        }

        return null;
    }

    public static String setQueryFor(Realm realm, final String appId, final String query) {

        final OGScraper scraper = realm.where(OGScraper.class)
                .equalTo("appId", appId)
                .findFirst();

        if (scraper==null){

            Log.d(TAG, "Adding new scraper for "+appId);
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    OGScraper newScraper = realm.createObject(OGScraper.class);
                    newScraper.appId = appId;
                    newScraper.setQuery(query);
                }
            });

        } else {

            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    scraper.setQuery(query);
                }
            });
        }

        return query;
    }


}
