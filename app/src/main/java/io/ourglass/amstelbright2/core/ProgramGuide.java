package io.ourglass.amstelbright2.core;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import io.ourglass.amstelbright2.realm.OGHTTPCachedResponse;
import io.ourglass.amstelbright2.realm.OGTVListing;
import io.ourglass.amstelbright2.realm.OGTVStation;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by mkahn on 12/13/16.
 */

public class ProgramGuide {

    public static final String TAG = "ProgramGuide";
    public static final String GRID_CACHE_URI = "$$cachedGrid";

    /**
     * Return an object array nearly identical to TVMedia grid call, but sourced from
     * internal DB.
     * [{ channel: {}, listings: [{}]}
     * @param  realm
     * @param channelNumber
     * @return
     */
    public static JSONObject currentGridForStation(Realm realm, int channelNumber){

        JSONObject rval = new JSONObject();

        Date now = new Date();

        OGTVStation station = realm.where(OGTVStation.class)
                .equalTo("channelNumber", channelNumber)
                .findFirst();

        //FIXME is station=null this shouldn't blowup
        int stationID = station.getStationID();

        RealmResults<OGTVListing> allListings = realm.where(OGTVListing.class)
                .greaterThanOrEqualTo("endDateTime", now)
                .equalTo("stationID", stationID)
                .findAll();

        allListings.sort("listDateTime");

        JSONArray listingsArray = new JSONArray();
        for (OGTVListing l: allListings){
            listingsArray.put(l.toJSON());
        }

        try {
            rval.put("channel", station.toJson());
            rval.put("listings", listingsArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return rval;

    }

    public static String currentGridForAllStations(Realm realm){

        RealmResults<OGTVStation> stations = realm.where(OGTVStation.class)
                .findAll();

        JSONArray grid = new JSONArray();
        for (OGTVStation station: stations){
            grid.put(currentGridForStation(realm, station.channelNumber));
        }

        String rval = grid.toString();
        //Cache it
        OGHTTPCachedResponse.createOrUpdate(realm, GRID_CACHE_URI, rval);

        return rval;
    }

    public static String currentGridForAllStationsCached(Realm realm){

        //First, check the cache! This is an expensive operation!!
        String cachedGrid = OGHTTPCachedResponse.getURIResponseAsString(realm, GRID_CACHE_URI);

        if (cachedGrid!=null){
            return cachedGrid;
        }

        return currentGridForAllStations(realm);

    }


}
