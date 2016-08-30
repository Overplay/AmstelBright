package io.ourglass.amstelbright2.services.http.handlers;

import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.jar.Pack200;

import io.ourglass.amstelbright2.core.OGCore;
import io.ourglass.amstelbright2.realm.OGAdvertisement;
import io.ourglass.amstelbright2.services.http.NanoHTTPBase.NanoHTTPD;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by ethan on 8/24/16.
 */

public class JSONAdHandler extends JSONHandler {
    public final String TAG = "JSONAdHandler";

    @Override
    public String getText(Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {

//        String adId = urlParams.get("id");
//        String extension = urlParams.get("extension");

        switch (session.getMethod()){
            case GET:
//                if(adId != null){
//                    Realm realm = Realm.getDefaultInstance();
//                    final RealmResults<OGAdvertisement> ad = realm.where(OGAdvertisement.class).equalTo("id", adId).findAll();
//                    if(ad.size() == 0){
//                        responseStatus = NanoHTTPD.Response.Status.NOT_FOUND;
//                        return "";
//                    }
//                    OGAdvertisement found = ad.get(0);
//                    try {
//                        responseStatus = NanoHTTPD.Response.Status.OK;
//                        return found.adAsJSON().toString();
//                    } catch (JSONException e){
//                        responseStatus = NanoHTTPD.Response.Status.INTERNAL_ERROR;
//                        return "{ \"error\": " + e.getMessage() + "}";
//                    }
//                    }
//
//                }
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
