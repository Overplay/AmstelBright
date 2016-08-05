package io.ourglass.amstelbright2.services.http.handlers;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import io.ourglass.amstelbright2.core.OGConstants;
import io.ourglass.amstelbright2.core.OGCore;
import io.ourglass.amstelbright2.realm.OGApp;
import io.ourglass.amstelbright2.realm.OGDevice;
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
        if(tok == null || !JWTHelper.getInstance().checkJWT(tok, OGConstants.AUTH_LEVEL.OWNER)) {
            responseStatus = NanoHTTPD.Response.Status.UNAUTHORIZED;
            return "";
        }

        String cmd = urlParams.get("command");

        switch (session.getMethod()) {

            case GET:

                switch (cmd) {

                    case "apps":

                        Realm realm = Realm.getDefaultInstance();
                        JSONArray arr = OGApp.getAllAppsAsJSON(realm);
                        realm.close();
                        responseStatus = NanoHTTPD.Response.Status.OK;
                        return arr.toString();

                    case "device":

                        Realm realm2 = Realm.getDefaultInstance();
                        JSONObject obj = OGDevice.getDeviceAsJSON(realm2);
                        realm2.close();
                        responseStatus = NanoHTTPD.Response.Status.OK;
                        return obj.toString();

                    case "channel":
                        responseStatus = NanoHTTPD.Response.Status.OK;
                        return OGCore.getCurrentChannel().toString();


                    default:
                        responseStatus = NanoHTTPD.Response.Status.NOT_ACCEPTABLE;
                        return "no such command: " + cmd;
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

                            Realm realm = Realm.getDefaultInstance();

                            final JSONObject obj = new JSONObject(json);

                            realm.executeTransactionAsync(new Realm.Transaction() {
                                @Override
                                public void execute(Realm bgRealm) {
                                    OGDevice thisDevice = OGDevice.getDevice(bgRealm);
                                    try {
                                        if (obj.has("name")) {
                                            thisDevice.name = obj.getString("name");
                                        }
                                        if (obj.has("locationWithinVenue")) {
                                            thisDevice.locationWithinVenue = obj.getString("locationWithinVenue");
                                        }
                                    } catch (Exception e) {
                                        Log.wtf("RealmUpdateDevice", "Fail updating realm");
                                    }
                                }
                            }, null, null);

                            realm.close();

                            //TODO add mechanism to add paired Settop box info
                            responseStatus = NanoHTTPD.Response.Status.OK;
                            return "{ \"status\":\"ok\"}";

                        } catch (Exception e) {

                            Log.e(getClass().toString(), e.toString());
                            responseStatus = NanoHTTPD.Response.Status.INTERNAL_ERROR;
                            return makeErrorJson(e);

                        }
                        //endpoint to discover installed apps, useful if there are new apps installed while running
                    case "discover-apps":
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
