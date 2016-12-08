package io.ourglass.amstelbright2.realm;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;

import io.ourglass.amstelbright2.core.OGCore;
import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.annotations.PrimaryKey;

/**
 * Created by mkahn on 12/5/16.
 */

public class OGProgram extends RealmObject {

    public static final String TAG = "OGProgram";

    @PrimaryKey
    public String id;

    // Program info
    public String programID;
    public String programName;
    public String episodeName;
    public String description;
    public boolean hd;

    public Date startTime;
    public int duration; //in minutes

    public int channel;
    public String carrier;
    public String network;
    public String logoFilename;

    public int bestPositionCrawler;
    public int bestPositionWidget;

    public JSONObject toJSON(){

        JSONObject robj = new JSONObject();

        try {
            robj.put("programID", this.programID);
            robj.put("programName", this.programName);
            robj.put("episodeName", this.episodeName);
            robj.put("channel", this.channel);
            robj.put("description", this.description);
            robj.put("startTime", this.startTime);
            robj.put("duration", this.duration);
            robj.put("carrier", this.carrier);
            robj.put("network", this.network);
            robj.put("logoFilename", this.logoFilename);
            robj.put("hd", this.hd);

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return robj;

    }

    public static JSONArray getAllAsJSON(Realm realm){

        RealmQuery<OGProgram> query = realm.where(OGProgram.class);
        RealmResults<OGProgram> next4 = query.findAll().sort("carrier");

        JSONArray rval = new JSONArray();

        for (OGProgram program : next4) {
            JSONObject robj = program.toJSON();
            if (robj!=null)
                rval.put(robj);
        }

        return rval;
    }

    public static JSONArray getNextHourAsJSON(Realm realm) {

        RealmQuery<OGProgram> query = realm.where(OGProgram.class);
        Date now = new Date();

        Calendar c = Calendar.getInstance();
        c.add(Calendar.HOUR, 1);
        Date plus4 = c.getTime();

        query.between("startTime", now, plus4);

        RealmResults<OGProgram> next4 = query.findAll().sort("carrier");

        JSONArray rval = new JSONArray();

        for (OGProgram program : next4) {
            JSONObject robj = program.toJSON();
            if (robj!=null)
                rval.put(robj);
        }

        return rval;

    }

    public static void addOrUpdate(Realm realm, final JSONObject jsonObject){

        final String programID = jsonObject.optString("programID", "");
        String dateS = jsonObject.optString("startTime");
        final Date date = OGCore.dateTimeFromISOString(dateS).toDate();
        final int channel = jsonObject.optInt("channel", 0);

        realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {

                    RealmQuery<OGProgram> query = realm.where(OGProgram.class)
                            .equalTo("programID", programID)
                            .equalTo("startTime", date)
                            .equalTo("channel", channel);

                    OGProgram existing = query.findFirst();

                    if (existing==null){
                        existing = realm.createObject(OGProgram.class);
                        Log.d(TAG, "Creating new program");

                    } else {
                        Log.d(TAG, "Found existing Program to update");
                    }

                    existing.programID = programID;
                    existing.programName = jsonObject.optString("programName", "");
                    existing.episodeName = jsonObject.optString("episodeName", "");
                    existing.channel = jsonObject.optInt("channel");
                    existing.duration = jsonObject.optInt("duration");
                    existing.startTime = date;
                    existing.carrier = jsonObject.optString("carrier", "");
                    existing.network = jsonObject.optString("network", "");
                    existing.logoFilename = jsonObject.optString("logoFilename", "");
                    existing.hd = jsonObject.optBoolean("hd");
                    existing.id = jsonObject.optString("id", "");



                }
            });



//        realm.commitTransaction();
//        realm.close();

       // return existing;

    }

}

/*

{
        "bestPosition": "583f4e433b0024ce2eb53454",
        "programID": 987167,
        "programName": "Jewelry TV",
        "episodeName": "",
        "channel": 313,
        "description": "",
        "duration": 720,
        "startTime": "2016-12-05T19:00:00.000Z",
        "carrier": "JeweleryTV",
        "lineupID": "2381D",
        "stationID": "3000",
        "name": "Jewelry Television",
        "callsign": "Jewelry Television",
        "network": "JeweleryTV",
        "logoFilename": "jtv.png",
        "showType": "Paid Program",
        "showTypeID": "V",
        "league": "",
        "team1ID": "0",
        "team1": "",
        "team2ID": "0",
        "team2": "",
        "hd": false,
        "event": "",
        "location": "",
        "showPicture": null,
        "artwork": [
            "Please upgrade to Pro Plan."
        ],
        "createdAt": "2016-12-05T21:47:49.378Z",
        "updatedAt": "2016-12-05T21:47:49.391Z",
        "id": "5845e0853539ad5a1a179b88"
    }

 */