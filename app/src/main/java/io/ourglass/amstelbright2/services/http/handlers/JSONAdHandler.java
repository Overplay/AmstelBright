package io.ourglass.amstelbright2.services.http.handlers;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import io.ourglass.amstelbright2.core.OGCore;
import io.ourglass.amstelbright2.realm.OGAdvertisement;
import io.ourglass.amstelbright2.services.http.NanoHTTPBase.NanoHTTPD;

/**
 * Created by ethan on 8/24/16.
 */

public class JSONAdHandler extends JSONHandler {
    public final String TAG = "JSONAdHandler";

    @Override
    public String getText(Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {

        switch (session.getMethod()){
            case GET:
                OGAdvertisement retrievedAd = OGCore.getAdvertisement();
                try{
                    JSONObject responseObj = retrievedAd.adAsJSON();
                    responseStatus = NanoHTTPD.Response.Status.OK;
                    return responseObj.toString();
                } catch (JSONException e){
                    Log.e(TAG, "There was a JSON error converting OGAdvertisement to JSON: " + e.getMessage());
                    responseStatus = NanoHTTPD.Response.Status.INTERNAL_ERROR;
                    return "";
                } catch (NullPointerException e){
                    Log.w(TAG, "attempted to get advertisement and there were none stored");
                    responseStatus = NanoHTTPD.Response.Status.NO_CONTENT;
                    return "";
                }

            default:
                // Only allowed verbs is GET
                responseStatus = NanoHTTPD.Response.Status.NOT_ACCEPTABLE;
                return "";
        }
    }
}
