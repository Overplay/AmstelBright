package io.ourglass.amstelbright2.services.http.handlers;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import io.ourglass.amstelbright2.core.JSONHTTPResponse;
import io.ourglass.amstelbright2.core.OGConstants;
import io.ourglass.amstelbright2.core.OGCore;
import io.ourglass.amstelbright2.core.OGSystem;
import io.ourglass.amstelbright2.realm.OGApp;
import io.ourglass.amstelbright2.realm.OGLog;
import io.ourglass.amstelbright2.services.http.NanoHTTPBase.NanoHTTPD;
import io.ourglass.amstelbright2.services.http.ogutil.JWTHelper;
import io.realm.Realm;


/**
 * Created by mkahn on 5/9/16.
 */
public class JSONSystemHandler extends JSONHandler {


    public String getText(Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {

        //these operations require owner level permissions
        String tok = session.getHeaders().get("authorization");
        if (!OGConstants.USE_JWT && (tok == null || !JWTHelper.getInstance().checkJWT(tok, OGConstants.AUTH_LEVEL.OWNER))) {
            responseStatus = NanoHTTPD.Response.Status.UNAUTHORIZED;
            return "";
        }

        String cmd = urlParams.get("command");

        switch (session.getMethod()) {

            case GET:

                switch (cmd) {

                    case "apps": {
                        Realm realm = Realm.getDefaultInstance();
                        JSONArray arr = OGApp.getAllAppsAsJSON(realm);
                        realm.close();
                        responseStatus = NanoHTTPD.Response.Status.OK;
                        return arr.toString();
                    }
                    case "device":
                        JSONObject sysInfo = OGSystem.getSystemInfo();
                        if(sysInfo == null){
                            responseStatus = NanoHTTPD.Response.Status.INTERNAL_ERROR;
                            return makeErrorJson("JSONException trying to create System Info");
                        }
                        responseStatus = NanoHTTPD.Response.Status.OK;
                        return OGSystem.getSystemInfo().toString();

                    case "channel":
                        responseStatus = NanoHTTPD.Response.Status.OK;
                        return OGCore.getCurrentChannel().toString();

                    case "logs":{
                        Realm realm = Realm.getDefaultInstance();
                        JSONArray logs = OGLog.getAllAsJson(realm);
                        realm.close();
                        responseStatus = NanoHTTPD.Response.Status.OK;
                        return logs.toString();
                    }



                    default:
                        responseStatus = NanoHTTPD.Response.Status.NOT_ACCEPTABLE;
                        return makeErrorJson("no such command: " + cmd);
                }


            case POST:
            case PUT:

                switch (cmd) {
                    case "device":

                        //check for JWT
                        //todo add more checks to determine if the JWT contains the correct information
                        if (!OGConstants.TEST_MODE && !JWTPresent(session)) {
                            responseStatus = NanoHTTPD.Response.Status.UNAUTHORIZED;
                            return "Unauthorized";
                        }

                        //if JWT not present then set responseStatus accordingly

                        //parse the body of the request
                        Map<String, String> files = new HashMap<String, String>();

                        try {
                            session.parseBody(files);
                        } catch (IOException ioe) {
                            responseStatus = NanoHTTPD.Response.Status.INTERNAL_ERROR;
                            return ioe.toString();
                        } catch (NanoHTTPD.ResponseException re) {
                            responseStatus = NanoHTTPD.Response.Status.INTERNAL_ERROR;
                            return re.toString();
                        }

                        //check for name

                        // TODO Treb was talking while I made these changes so they are probably f-d

                        final String json = files.get("postData");

                        try {

                            final JSONObject inboundParams = new JSONObject(json);

                            if (inboundParams.has("name")) {
                                OGSystem.setSystemName(inboundParams.getString("name"));
                            }

                            if (inboundParams.has("locationWithinVenue")) {
                                OGSystem.setSystemLocation(inboundParams.getString("locationWithinVenue"));
                            }

                            JSONHTTPResponse result = OGCore.updateNameLocOnAsahi();

                            //TODO this should be 3 cases: null (500?), NOT_ACC, OK
                            //TODO HATE HATE this implementation. There should be a sweep service
                            //that updates Asahi periodically. No reason this should fault out
                            //if Asahi is down.
                            //implement an operations queue of synchronous requests plus a retry count
                            //in a seperate service
                            if (result == null || !result.isGoodResponse) {
                                responseStatus = NanoHTTPD.Response.Status.NOT_ACCEPTABLE;
                                return result.stringResponse;
                            } else {
                                responseStatus = NanoHTTPD.Response.Status.OK;
                                return OGSystem.getSystemInfo().toString();
                            }

                            //TODO add mechanism to add paired Settop box info

                        } catch (Exception e) {

                            Log.e(getClass().toString(), e.toString());
                            responseStatus = NanoHTTPD.Response.Status.INTERNAL_ERROR;
                            return makeErrorJson(e);

                        }
                        //endpoint to register with Asaho
                    case "regcode":

                        String code = session.getParms().get("regcode");

                        //check for JWT
                        //todo add more checks to determine if the JWT contains the correct information
                        if (!OGConstants.TEST_MODE && !JWTPresent(session)) {
                            responseStatus = NanoHTTPD.Response.Status.UNAUTHORIZED;
                            return "Unauthorized";
                        }

                        //if JWT not present then set responseStatus accordingly

                        if (code == null) {
                            responseStatus = NanoHTTPD.Response.Status.BAD_REQUEST;
                            return makeErrorJson("Please enter a registration code.");
                        }

                        //TODO add mechanism to add paired Settop box info

                        JSONHTTPResponse result = OGCore.registerWithAsahi(code);

                        //TODO this should be 3 cases: null (500?), NOT_ACC, OK
                        if (result == null || !result.isGoodResponse) {
                            responseStatus = NanoHTTPD.Response.Status.NOT_ACCEPTABLE;
                            String rcvdJson = result.stringResponse;
                            return rcvdJson;
                            //return makeErrorJson(result.stringResponse);
                        }

                        responseStatus = NanoHTTPD.Response.Status.OK;
                        return result.stringResponse;


                    //endpoint to discover installed apps, useful if there are new apps installed while running

                    case "refreshapps":
                        JSONArray installedApps = OGCore.installStockApps();
                        responseStatus = NanoHTTPD.Response.Status.OK;
                        return "Apps currently installed\n" + installedApps.toString();

                }
            default:
                // Only allowed verbs are GET/POST/PUT
                responseStatus = NanoHTTPD.Response.Status.NOT_ACCEPTABLE;
                return "";
        }


    }


}
