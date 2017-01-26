package io.ourglass.amstelbright2.services.http.handlers;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;

import io.ourglass.amstelbright2.core.OGConstants;
import io.ourglass.amstelbright2.core.ProgramGuide;
import io.ourglass.amstelbright2.realm.OGTVStation;
import io.ourglass.amstelbright2.services.http.NanoHTTPBase.NanoHTTPD;
import io.ourglass.amstelbright2.services.http.ogutil.JWTHelper;
import io.realm.Realm;


/**
 * Created by mkahn on 5/9/16.
 */
public class JSONPGSHandler extends JSONHandler {

    private void markChannelFavorite(Realm realm, final OGTVStation station, final boolean isFav){

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                station.favorite = isFav;
            }
        });

    }

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

                    case "grid4channel": {

                        String chanS = session.getParms().get("channel");
                        if (chanS!=null){
                            int channel = Integer.parseInt(chanS);
                            Realm realm = Realm.getDefaultInstance();
                            JSONObject grid = ProgramGuide.currentGridForStation(realm, channel);
                            realm.close();
                            responseStatus = NanoHTTPD.Response.Status.OK;
                            return grid.toString();
                        }
                        responseStatus = NanoHTTPD.Response.Status.NOT_ACCEPTABLE;
                        return makeErrorJson("bad parameter, no channel");


                    }

                    case "grid": {

                        Realm realm = Realm.getDefaultInstance();
                        String resp = ProgramGuide.currentGridForAllStationsCached(realm);
                        realm.close();

                        responseStatus = NanoHTTPD.Response.Status.OK;
                        return resp;

                    }

                    case "channels": {
                        Realm realm = Realm.getDefaultInstance();
                        JSONArray arr = OGTVStation.getAllAsJSON(realm);
                        realm.close();
                        responseStatus = NanoHTTPD.Response.Status.OK;
                        return arr.toString();
                    }


                    default:
                        responseStatus = NanoHTTPD.Response.Status.NOT_ACCEPTABLE;
                        return "no such command: " + cmd;
                }


            case POST:
            case PUT:


                switch (cmd) {
                    case "unfavorite":
                    case "favorite": {
                        String channel = urlParams.get("channel");

                        //check for JWT
                        //todo add more checks to determine if the JWT contains the correct information
                        if (!OGConstants.TEST_MODE && !JWTPresent(session)) {
                            responseStatus = NanoHTTPD.Response.Status.UNAUTHORIZED;
                            return makeErrorJson("Unauthorized");
                        }

                        Realm realm = Realm.getDefaultInstance();
                        final OGTVStation station = OGTVStation.getByChannelNumber(realm, Integer.parseInt(channel));

                        if (station == null ){
                            responseStatus = NanoHTTPD.Response.Status.NOT_ACCEPTABLE;
                            realm.close();
                            return makeErrorJson("No such channel");
                        }

                        markChannelFavorite(realm, station, cmd.equalsIgnoreCase("favorite"));

                        responseStatus = NanoHTTPD.Response.Status.OK;
                        return station.toJson().toString();
                    }


                }
            default:
                // Only allowed verbs are GET/POST/PUT
                responseStatus = NanoHTTPD.Response.Status.NOT_ACCEPTABLE;
                return "";
        }


    }


}
