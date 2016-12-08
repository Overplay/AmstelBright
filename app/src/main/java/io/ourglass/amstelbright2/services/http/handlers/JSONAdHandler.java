package io.ourglass.amstelbright2.services.http.handlers;

import org.json.JSONObject;

import java.util.Map;

import io.ourglass.amstelbright2.core.OGCore;
import io.ourglass.amstelbright2.services.http.NanoHTTPBase.NanoHTTPD;

/**
 * Created by ethan on 8/24/16.
 */

public class JSONAdHandler extends JSONHandler {
    public final String TAG = "JSONAdHandler";

    @Override
    public String getText(Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {

//        String adId = urlParams.get("id");
//        String extension = urlParams.get("extension");

        switch (session.getMethod()) {
            case GET:

                JSONObject responseObj = OGCore.getAdvertisement();
                responseStatus = NanoHTTPD.Response.Status.OK;
                return responseObj.toString();

            default:
                // Only allowed verbs is GET
                responseStatus = NanoHTTPD.Response.Status.NOT_ACCEPTABLE;
                return "";
        }
    }
}
