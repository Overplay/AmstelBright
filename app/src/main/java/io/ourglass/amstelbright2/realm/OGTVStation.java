package io.ourglass.amstelbright2.realm;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.annotations.PrimaryKey;

/**
 * Created by mkahn on 12/7/16.
 */

public class OGTVStation extends RealmObject {

    public String number;
    public int channelNumber;
    public int subChannelNumber;

    @PrimaryKey
    public int stationID;

    public String name;
    public String callsign;
    public String network;
    public int NTSC_TSID;
    public int DTV_TSID;
    public String Twitter;
    public String webLink;
    public String logoFilename;
    public Boolean stationHD;
    public int defaultBestPositionCrawler;
    public int defaultBestPositionWidget;
    public boolean favorite;
    public int displayWeight;

    public String getLogoUrl(){
        return "http://cdn.tvpassport.com/image/station/100x100/"+this.logoFilename;
    }

    public JSONObject toJson(){

        JSONObject rval = new JSONObject();

        try {
            rval.put("channelNumber", channelNumber);
            rval.put("stationID", stationID);
            rval.put("name", name);
            rval.put("callsign", callsign);
            rval.put("network", network);
            rval.put("Twitter", Twitter);
            rval.put("stationHD", stationHD);
            rval.put("defaultBestPositionCrawler", defaultBestPositionCrawler);
            rval.put("defaultBestPositionWidget", defaultBestPositionWidget);
            rval.put("favorite", favorite);
            rval.put("displayWeight", displayWeight);

            rval.put("logoUrl", getLogoUrl());

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return rval;

    }

    public static JSONArray getAllAsJSON(Realm realm){

        RealmQuery<OGTVStation> query = realm.where(OGTVStation.class);
        RealmResults<OGTVStation> all = query.findAll();

        JSONArray rval = new JSONArray();

        for (OGTVStation channel : all) {
            JSONObject robj = channel.toJson();
            if (robj!=null)
                rval.put(robj);
        }

        return rval;
    }


}

