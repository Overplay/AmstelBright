package io.ourglass.amstelbright2.realm;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.annotations.PrimaryKey;

/**
 * Created by mkahn on 12/5/16.
 * <p>
 * BASED ON TVMedia object format
 * <p>
 * [
 * {
 * "listingID": 374366771,
 * "listDateTime": "2016-12-07 22:00:00",
 * "duration": 60,
 * "showID": 1992450,
 * "seriesID": 320193,
 * "showName": "Harry: The Harry Connick Jr. Show",
 * "episodeTitle": "",
 * "episodeNumber": "8069",
 * "parts": 0,
 * "partNum": 0,
 * "seriesPremiere": false,
 * "seasonPremiere": false,
 * "seriesFinale": false,
 * "seasonFinale": false,
 * "repeat": false,
 * "new": true,
 * "rating": "TVPG",
 * "captioned": true,
 * "educational": false,
 * "blackWhite": false,
 * "subtitled": false,
 * "live": false,
 * "hd": true,
 * "descriptiveVideo": false,
 * "inProgress": false,
 * "showTypeID": "T",
 * "breakoutLevel": 3,
 * "showType": "Talk Shows",
 * "year": "",
 * "guest": "Shemar Moore, Matt Czuchry, Jon Batiste",
 * "cast": "",
 * "director": "",
 * "starRating": 0,
 * "description": "Guests include Shemar Moore and Matt Czuchry with a performance from Jon Batiste.",
 * "league": "",
 * "team1ID": 0,
 * "team2ID": 0,
 * "team1": "",
 * "team2": "",
 * "event": "",
 * "location": "",
 * "showHost": "Harry Connick Jr.",
 * "artwork": "Please upgrade to Pro Plan."
 * },
 */

public class OGTVListing extends RealmObject {

    public static final String TAG = "OGTVListing";

    @PrimaryKey
    public int listingID;

    // This has to be supplied after the GET since they don't send it down with the query results
    // as the stationID is a part of the query itself.
    public int stationID;
    public Date listDateTime;
    public int duration;
    public int showID;
    public int seriesID;
    public String showName;
    public String episodeTitle;
    public String episodeNumber;

    public Boolean live;
    public Boolean hd;

    public String showType;

    public String description;
    public String league;

    public String team1;
    public String team2;
    public String event;

    public int bestPositionCrawler;
    public int bestPositionWidget;

    public JSONObject toJSON() {

        JSONObject robj = new JSONObject();

        try {
            robj.put("listingID", this.listingID);
            robj.put("stationID", this.stationID);
            robj.put("listDateTime", this.listDateTime);
            robj.put("showID", this.showID);
            robj.put("description", this.description);
            robj.put("showName", this.showName);
            robj.put("duration", this.duration);
            robj.put("live", this.live);
            robj.put("description", this.description);
            robj.put("showType", this.showType);
            robj.put("hd", this.hd);
            robj.put("league", this.league);
            robj.put("team1", this.team1);
            robj.put("team2", this.team2);
            robj.put("bestPositionCrawler", this.bestPositionCrawler);
            robj.put("bestPositionWidget", this.bestPositionWidget);



        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return robj;

    }

    public static JSONArray getAllAsJSON(Realm realm) {

        RealmQuery<OGTVListing> query = realm.where(OGTVListing.class);
        RealmResults<OGTVListing> next4 = query.findAll().sort("carrier");

        JSONArray rval = new JSONArray();

        for (OGTVListing program : next4) {
            JSONObject robj = program.toJSON();
            if (robj != null)
                rval.put(robj);
        }

        return rval;
    }

    public static JSONArray getNextHourAsJSON(Realm realm) {

        RealmQuery<OGTVListing> query = realm.where(OGTVListing.class);
        Date now = new Date();

        Calendar c = Calendar.getInstance();
        c.add(Calendar.HOUR, 1);
        Date plus4 = c.getTime();

        query.between("listDateTime", now, plus4);

        RealmResults<OGTVListing> next4 = query.findAll().sort("listDateTime");

        JSONArray rval = new JSONArray();

        for (OGTVListing program : next4) {
            JSONObject robj = program.toJSON();
            if (robj != null)
                rval.put(robj);
        }

        return rval;

    }

}
