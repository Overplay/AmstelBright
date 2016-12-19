package io.ourglass.amstelbright2.realm;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by mkahn on 12/15/16.
 */

public class OGHTTPCachedResponse extends RealmObject {

    @PrimaryKey
    public String uri;

    public String stringResponse;
    public Date cachedAt;

    public JSONObject getAsJsonObject(){

        try {
            JSONObject rval = new JSONObject(this.stringResponse);
            return rval;
        } catch (JSONException e) {
            Log.e("OGHTTPCachedResponse", "Could not provide a JSON object from response" );
            return null;
        }

    }

    public JSONArray getAsJsonArray(){

        try {
            JSONArray rval = new JSONArray(this.stringResponse);
            return rval;
        } catch (JSONException e) {
            Log.e("OGHTTPCachedResponse", "Could not provide a JSONArray from response" );
            return null;
        }
    }

    public static OGHTTPCachedResponse getForUri(Realm realm, String uri){

        OGHTTPCachedResponse resp = realm.where(OGHTTPCachedResponse.class)
                .equalTo("uri", uri)
                .findFirst();

        return resp;

    }

    public static JSONObject getURIResponseAsJSONObject(Realm realm, String uri){

        OGHTTPCachedResponse resp = getForUri(realm, uri);

        if (resp!=null){
            return resp.getAsJsonObject();
        }

        return null;
    }

    public static JSONArray getURIResponseAsJSONArray(Realm realm, String uri){

        OGHTTPCachedResponse resp = getForUri(realm, uri);

        if (resp!=null){
            return resp.getAsJsonArray();
        }

        return null;
    }

    public static String getURIResponseAsString(Realm realm, String uri){

        OGHTTPCachedResponse resp = getForUri(realm, uri);
        if (resp!=null){
            return resp.stringResponse;
        }

        return null;
    }

    public static void createOrUpdate(Realm realm, final String uri, final String response){

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {

                OGHTTPCachedResponse resp = getForUri(realm, uri);
                if (resp==null){
                    resp = realm.createObject(OGHTTPCachedResponse.class, uri);
                }
                resp.stringResponse = response;
                resp.cachedAt = new Date();

            }
        });
    }
}
