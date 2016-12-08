package io.ourglass.amstelbright2.services.http.handlers;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import io.ourglass.amstelbright2.core.OGConstants;
import io.ourglass.amstelbright2.core.OGCore;
import io.ourglass.amstelbright2.core.exceptions.OGServerException;
import io.ourglass.amstelbright2.realm.OGApp;
import io.ourglass.amstelbright2.services.http.NanoHTTPBase.NanoHTTPD;
import io.ourglass.amstelbright2.services.http.ogutil.JWTHelper;
import io.realm.Realm;


// TODO Maybe this should handle appData too?

/**
 * Created by mkahn on 5/9/16.
 * <p>
 * Handles the following:
 * <p>
 * /api/app/:appid/:command
 * <p>
 * /api/app/:appid/launch
 * /api/app/:appid/kill
 * /api/app/:appid/move
 * <p>
 * Idea?
 * <p>
 * /api/app/:appid/data
 */

public class JSONAppCommandsHandler extends JSONHandler {


    public String getText(Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {

        //these operations require owner level permissions
        String tok = session.getHeaders().get("Authorization");
        if (!OGConstants.USE_JWT && (tok == null || JWTHelper.getInstance().checkJWT(tok, OGConstants.AUTH_LEVEL.OWNER))) {
            responseStatus = NanoHTTPD.Response.Status.UNAUTHORIZED;
            return "";
        }

        String appId = urlParams.get("appid");
        String cmd = urlParams.get("command");

        switch (session.getMethod()) {

            case POST:

                switch (cmd) {

                    case "move":

                        try {
                            JSONObject movedJson = OGCore.moveApp(appId);
                            responseStatus = NanoHTTPD.Response.Status.OK;
                            return movedJson.toString();
                        } catch (OGServerException e) {
                            return processOGException(e);
                        }

                    case "launch":

                        try {
                            JSONObject launchedApp = OGCore.launchApp(appId);
                            responseStatus = NanoHTTPD.Response.Status.OK;
                            return launchedApp.toString();
                        } catch (OGServerException e) {
                            return processOGException(e);
                        }

                    case "kill":

                        try {
                            JSONObject killedApp = OGCore.killApp(appId);
                            responseStatus = NanoHTTPD.Response.Status.OK;
                            return killedApp.toString();

                        } catch (OGServerException e) {
                            return processOGException(e);
                        }

                    case "adjust":

                        float scale;
                        int xAdjust, yAdjust;

                        try {
                            JSONObject body = getBodyAsJSONObject(session);
                            scale = Float.valueOf(body.get("scale").toString());
                            try {
                                xAdjust = body.getInt("xAdjust");
                            } catch (JSONException e) {
                                Log.v("JSONAppCommandsHandler", "xAdjust not supplied, setting to 0");
                                xAdjust = 0;
                            }
                            try {
                                yAdjust = body.getInt("yAdjust");
                            } catch (JSONException e) {
                                Log.v("JSONAppCommandsHandler", "yAdjust not supplied, setting to 0");
                                yAdjust = 0;
                            }
                        } catch (Exception e) {
                            responseStatus = NanoHTTPD.Response.Status.BAD_REQUEST;
                            return e.toString();
                        }

                        try {
                            JSONObject adjustedApp = OGCore.adjustApp(appId, scale, xAdjust, yAdjust);
                            responseStatus = NanoHTTPD.Response.Status.OK;
                            return adjustedApp.toString();
                        } catch (OGServerException e) {
                            return processOGException(e);
                        }

                    default:

                        responseStatus = NanoHTTPD.Response.Status.NOT_FOUND;
                        return "no such command, genius";

                }


            case GET:
            case PUT:
            default:
                // Only allowed verbs are GET/POST/PUT
                responseStatus = NanoHTTPD.Response.Status.NOT_ACCEPTABLE;
                return "";
        }


    }


}
