package io.ourglass.amstelbright2.services.ajpgsservice;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import io.ourglass.amstelbright2.core.ABApplication;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by mkahn on 11/11/16.
 */

public class TVMediaAPI {

    public static final String TAG = "TVMedia";
    public static final String TVMEDIA_BASE_URL = "http://api.tvmedia.ca/tv/v4/";
    public static final String TVMEDIA_API_KEY = "api_key=761cbd1955e14ff1b1af8d677a488904";

    public static OkHttpClient mClient = ABApplication.okclient;

    /**
     *
     * @param latitude
     * @param longitude
     * @return [{
    "lineupID": "60033D",
    "lineupName": "AT&T U-Verse-San Jose, CA - Digital",
    "lineupType": "CAB",
    "providerID": "237",
    "providerName": "AT&T U-Verse",
    "serviceArea": "Verse-San Jose, CA ",
    "country": "USA"
    },...]
     */
    public static JSONObject lineupsForGeo(double latitude, double longitude) {

        String TASK = " lineup info on for geo region.";
        Log.d(TAG, "GETting " + TASK);

        JSONObject rval = null;

        try {

            Request req = new Request.Builder()
                    .url(TVMEDIA_BASE_URL + "lineups/geo?latitude=" + latitude
                            + "&longitude=" + longitude + "&detail=brief&" + TVMEDIA_API_KEY)
                    .build();
            Response response = mClient.newCall(req).execute();

            String respString = response.body().string();

            if (response.isSuccessful()) {
                rval = new JSONObject(respString);
            }

        } catch (IOException e) {
            Log.e(TAG, "IO Exception getting" + TASK);
        } catch (JSONException e) {
            Log.e(TAG, "JSON Exception getting" + TASK);
        } catch (Exception e) {
            Log.e(TAG, "Exception getting" + TASK);
        }

        return rval;
    }

    /**
     *
     * @param lineupID
     * @return {
    "lineupID": "60033D",
    "lineupName": "AT&T U-Verse-San Jose, CA - Digital",
    "lineupType": "CAB",
    "providerID": "237",
    "providerName": "AT&T U-Verse",
    "serviceArea": "Verse-San Jose, CA ",
    "country": "USA",
    "stations": [
        {
            "number": "2",
            "channelNumber": 2,
            "subChannelNumber": 0,
            "stationID": 2145,
            "name": "FOX (KTVU) San Francisco, CA",
            "callsign": "KTVU",
            "network": "FOX",
            "stationType": "Broadcast",
            "NTSC_TSID": 320,
            "DTV_TSID": 321,
            "Twitter": "KTVU",
            "webLink": "http://www.ktvu.com/",
            "logoFilename": "foxcable.png",
            "stationHD": false
            },...]}
     */
    public static JSONObject lineupWithStations(String lineupID) {

        String TASK = " chanel info on for lineup ID.";
        Log.d(TAG, "GETting " + TASK);

        JSONObject rval = null;

        try {

            Request req = new Request.Builder()
                    .url(TVMEDIA_BASE_URL + "lineups/" + lineupID + "?" + TVMEDIA_API_KEY)
                    .build();
            Response response = mClient.newCall(req).execute();

            String respString = response.body().string();

            if (response.isSuccessful()) {
                rval = new JSONObject(respString);
            }

        } catch (IOException e) {
            Log.e(TAG, "IO Exception getting" + TASK);
        } catch (JSONException e) {
            Log.e(TAG, "JSON Exception getting" + TASK);
        } catch (Exception e) {
            Log.e(TAG, "Exception getting" + TASK);
        }

        return rval;
    }

    /**
     *
     * [
     {
     "listingID": 374366771,
     "listDateTime": "2016-12-07 22:00:00",
     "duration": 60,
     "showID": 1992450,
     "seriesID": 320193,
     "showName": "Harry: The Harry Connick Jr. Show",
     "episodeTitle": "",
     "episodeNumber": "8069",
     "parts": 0,
     "partNum": 0,
     "seriesPremiere": false,
     "seasonPremiere": false,
     "seriesFinale": false,
     "seasonFinale": false,
     "repeat": false,
     "new": true,
     "rating": "TVPG",
     "captioned": true,
     "educational": false,
     "blackWhite": false,
     "subtitled": false,
     "live": false,
     "hd": true,
     "descriptiveVideo": false,
     "inProgress": false,
     "showTypeID": "T",
     "breakoutLevel": 3,
     "showType": "Talk Shows",
     "year": "",
     "guest": "Shemar Moore, Matt Czuchry, Jon Batiste",
     "cast": "",
     "director": "",
     "starRating": 0,
     "description": "Guests include Shemar Moore and Matt Czuchry with a performance from Jon Batiste.",
     "league": "",
     "team1ID": 0,
     "team2ID": 0,
     "team1": "",
     "team2": "",
     "event": "",
     "location": "",
     "showHost": "Harry Connick Jr.",
     "artwork": "Please upgrade to Pro Plan."
     },...]
     *
     * http://api.tvmedia.ca/tv/v4/stations/2145/listings?api_key=761cbd1955e14ff1b1af8d677a488904
     *
     * @param stationID
     * @return
     */
    public static JSONArray listingsForNext4HoursForStationID(int stationID) {

        String TASK = " listings array for next 4 hours on for stationID ID.";
        Log.d(TAG, "GETting " + TASK);

        JSONArray rval = null;

        try {

            Request req = new Request.Builder()
                    .url(TVMEDIA_BASE_URL + "stations/" + stationID + "/listings?" + TVMEDIA_API_KEY)
                    .build();
            Response response = mClient.newCall(req).execute();

            String respString = response.body().string();

            if (response.isSuccessful()) {
                rval = new JSONArray(respString);
            }

        } catch (IOException e) {
            Log.e(TAG, "IO Exception getting" + TASK);
        } catch (JSONException e) {
            Log.e(TAG, "JSON Exception getting" + TASK);
        } catch (Exception e) {
            Log.e(TAG, "Exception getting" + TASK);
        }

        return rval;
    }

}