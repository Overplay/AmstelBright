package io.ourglass.amstelbright.services.http.handlers;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import io.ourglass.amstelbright.core.OGCore;
import io.ourglass.amstelbright.core.exceptions.OGServerException;
import io.ourglass.amstelbright.services.http.NanoHTTPBase.NanoHTTPD;

/**
 * Created by mkahn on 5/9/16.
 */
public class JSONAppDataHandler extends JSONHandler {


    public String getText(Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {


        final HashMap<String, String> map = new HashMap<String, String>();
        try {
            session.parseBody(map);
        } catch (IOException e) {
            e.printStackTrace();
            responseStatus = NanoHTTPD.Response.Status.INTERNAL_ERROR;
            return makeErrorJson(e);
        } catch (NanoHTTPD.ResponseException e) {
            e.printStackTrace();
            responseStatus = NanoHTTPD.Response.Status.INTERNAL_ERROR;
            return makeErrorJson(e);
        }

        // This is dirty magic from inside of NanoHTTPD
        final String jsonString = map.get("postData");
        String appId = urlParams.get("appid");

        switch (session.getMethod()) {

            case GET:

                try {
                    JSONObject appData = OGCore.getInstance().getAppDataFor(appId);
                    responseStatus = NanoHTTPD.Response.Status.OK;
                    return appData.toString();
                } catch (OGServerException e) {
                    return processOGException(e);
                }

            // These are treated the same for now

            case POST:
            case PUT:

                // TODO This is busted with nested Realm transaction
                //return new JSONObject();

                JSONObject dataJson = null;

                if (jsonString==null){
                    responseStatus = NanoHTTPD.Response.Status.NOT_ACCEPTABLE;
                    return makeErrorJson("try adding some actual json data, my friend");
                }

                try {
                    dataJson = new JSONObject(jsonString);
                } catch (JSONException e) {
                    responseStatus = NanoHTTPD.Response.Status.NOT_ACCEPTABLE;
                    return makeErrorJson(e);
                } catch (Exception e){
                    responseStatus = NanoHTTPD.Response.Status.INTERNAL_ERROR;
                    return makeErrorJson(e);
                }

                try {
                    JSONObject jobj =  OGCore.getInstance().updateAppData(appId, dataJson);
                    return jobj.toString();
                } catch (OGServerException e) {
                    return processOGException(e);
                }


            default:
                // Only allowed verbs are GET/POST/PUT
                responseStatus = NanoHTTPD.Response.Status.NOT_ACCEPTABLE;
                return "";
        }


    }



}
