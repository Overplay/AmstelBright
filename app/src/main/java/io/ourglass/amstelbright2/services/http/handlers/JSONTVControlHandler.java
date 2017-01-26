package io.ourglass.amstelbright2.services.http.handlers;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import io.ourglass.amstelbright2.core.OGConstants;
import io.ourglass.amstelbright2.core.OGSystem;
import io.ourglass.amstelbright2.core.ProgramGuide;
import io.ourglass.amstelbright2.services.http.NanoHTTPBase.NanoHTTPD;
import io.ourglass.amstelbright2.services.http.ogutil.JWTHelper;
import io.ourglass.amstelbright2.services.stbservice.DirecTVAPI;
import io.ourglass.amstelbright2.services.stbservice.DirecTVSetTopBox;
import io.ourglass.amstelbright2.services.stbservice.TVShow;
import io.realm.Realm;


/**
 * Created by mkahn on 5/9/16.
 */
public class JSONTVControlHandler extends JSONHandler {


    public String getText(Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {

        //these operations require owner level permissions
        String tok = session.getHeaders().get("authorization");
        if (!OGConstants.USE_JWT && (tok == null || !JWTHelper.getInstance().checkJWT(tok, OGConstants.AUTH_LEVEL.OWNER))) {
            responseStatus = NanoHTTPD.Response.Status.UNAUTHORIZED;
            return makeErrorJson("Unauthorized");
        }

        if (!OGSystem.isPairedToSTB() && !OGSystem.isEmulator()){
            responseStatus = NanoHTTPD.Response.Status.NOT_ACCEPTABLE;
            return makeErrorJson("System not paired to set top box.");
        }

        if (session.getUri().contains("currentgrid") && session.getMethod()==NanoHTTPD.Method.GET) {
            // get the current grid for the displayed channel

            if (!OGSystem.isPairedToSTB()){
                responseStatus = NanoHTTPD.Response.Status.NOT_ACCEPTABLE;
                return makeErrorJson("Not paired to STB");
            }

            DirecTVSetTopBox stb = OGSystem.getPairedSTB();
            if (stb==null){
                responseStatus = NanoHTTPD.Response.Status.INTERNAL_ERROR;
                return makeErrorJson("Failed retrieving paired STB");
            }

            TVShow nowPlaying = OGSystem.getPairedSTB().nowPlaying;
            if (nowPlaying==null){
                responseStatus = NanoHTTPD.Response.Status.INTERNAL_ERROR;
                return makeErrorJson("Failed retrieving now playing from paired STB");
            }

            String channelString = nowPlaying.channelNumber;
            int channelNumber = Integer.parseInt(channelString);

            Realm realm = Realm.getDefaultInstance();
            JSONObject currentGrid = ProgramGuide.currentGridForStation(realm, channelNumber);
            realm.close();

            JSONObject rval = new JSONObject();
            try {
                rval.put("grid", currentGrid);
                rval.put("nowPlaying", nowPlaying.title );
            } catch (JSONException e) {
                e.printStackTrace();
            }

            responseStatus = NanoHTTPD.Response.Status.OK;
            return rval.toString();


        } else if ( session.getUri().contains("change") &&  session.getMethod()==NanoHTTPD.Method.POST) {

            int channel = Integer.parseInt(urlParams.get("newchannel"));

            JSONObject rval = DirecTVAPI.changeChannel(OGSystem.getPairedSTBIpAddress(), channel);
            responseStatus = NanoHTTPD.Response.Status.OK;
            return rval.toString();
            //return makeErrorJson("gonna change to "+channel);

        } else {

            responseStatus = NanoHTTPD.Response.Status.NOT_ACCEPTABLE;
            return makeErrorJson("that's just not right");
        }


    }


}
