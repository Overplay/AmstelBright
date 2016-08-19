package io.ourglass.amstelbright2.services.http.handlers;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.Map;

import io.ourglass.amstelbright2.core.OGConstants;
import io.ourglass.amstelbright2.realm.OGDevice;
import io.ourglass.amstelbright2.services.http.NanoHTTPBase.NanoHTTPD;
//import io.ourglass.amstelbright2.services.http.ogutil.JWTHelper;
import io.ourglass.amstelbright2.services.stbservice.STBService;
import io.realm.Realm;

/**
 * Created by ethan on 8/18/16.
 */

public class JSONSTBHandler extends JSONHandler {

    private static final String TAG = "JSONSTBHandler";

    @Override
    public String getText(Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {

        //these operations should definitely be restricted
        String tok = session.getHeaders().get("authorization");
//        if(!OGConstants.USE_JWT && (tok == null ||!JWTHelper.getInstance().checkJWT(tok, OGConstants.AUTH_LEVEL.OWNER))){
//            responseStatus = NanoHTTPD.Response.Status.UNAUTHORIZED;
//            return "";
//        }

        String cmd = urlParams.get("command");
        switch(session.getMethod()){
            case GET:
                switch (cmd){
                    //return the available devices on the network
                    case "available":
                        if(!STBService.hasSearched){
                            responseStatus = NanoHTTPD.Response.Status.OK;
                            return "{ message: 'device has not finished discovering devices', devices: [] }";
                        }
                        responseStatus = NanoHTTPD.Response.Status.OK;
                        String response = "{ devices: [";
                        for(String foundDevice : STBService.foundIps){
                            response += foundDevice + ", ";
                        }
                        response = response.substring(0, response.lastIndexOf(",")) + "] }";
                        return response;

                    //return the status of the current pairing
                    // (whether paired and if paired, then what its paired to)
                    case "pair":
                        try {
                            Realm realm = Realm.getDefaultInstance();
                            String stbAddr = OGDevice.getPairedSTBOrNull(realm);

                            JSONObject res = new JSONObject();

                            if (stbAddr != null) {
                                res.put("isPaired", true);
                                res.put("paired_to_stb_addr", stbAddr);
                            }
                            else {
                                res.put("isPaired", false);
                            }
                            responseStatus = NanoHTTPD.Response.Status.OK;
                            return res.toString();

                        } catch(JSONException e){
                            responseStatus = NanoHTTPD.Response.Status.INTERNAL_ERROR;
                            return "";
                        }

                    default:
                        responseStatus = NanoHTTPD.Response.Status.NOT_ACCEPTABLE;
                        return "no such command: " + cmd;
                }
            case POST:
                switch (cmd){
                    //takes the address of the box as a parameter and attempts to connect to it
                    //if connection is successful, it will set the appropriate fields in the OGDevice
                    case "pair":
                        String ipToPairWith;
                        HttpURLConnection huc = null;
                        try {
                            //extract the desired pairing ip address from the body
                            JSONObject body = getBodyAsJSONObject(session);
                            ipToPairWith = body.getString("ip");

                            if(ipToPairWith == null){
                                throw new Exception("Missing ip address");
                            }

                            //check that the ipaddress is formatted correctly, respond error if not
                            ipToPairWith = ipToPairWith.replace("https://", "");
                            ipToPairWith = ipToPairWith.replace("http://", "");

                            int idx;
                            if((idx = ipToPairWith.indexOf(":")) != -1){
                                ipToPairWith = ipToPairWith.substring(0, idx);
                            }
                            if((idx = ipToPairWith.indexOf("/")) != -1){
                                ipToPairWith = ipToPairWith.substring(0,idx);
                            }

                            Log.v(TAG, "attempting to connect with: " + ipToPairWith);
                            InetAddress toTest = InetAddress.getByName(ipToPairWith);
                            if(toTest == null){
                                throw new Exception("the entered ip address seems to be invalid");
                            }

                            //test info endpoint to make sure that this is a correct address
                            URL u = new URL("http://" + ipToPairWith + ":8080/info/getVersion");
                            huc = (HttpURLConnection)u.openConnection();
                            huc.setRequestMethod("GET");
                            huc.setConnectTimeout(2000);
                            huc.connect();

                            BufferedReader in = new BufferedReader(new InputStreamReader(huc.getInputStream()));
                            String response = "", line;
                            while((line = in.readLine()) != null){
                                response += line;
                            }
                            huc.disconnect();

                            //if all of this passes, then set pairing return ok
                            OGDevice.setPairedSTB(Realm.getDefaultInstance(), ipToPairWith);

                            responseStatus = NanoHTTPD.Response.Status.OK;
                            return response;
                        }
                        catch (Exception e){
                            if(huc != null){
                                huc.disconnect();
                            }
                            responseStatus = NanoHTTPD.Response.Status.INTERNAL_ERROR;
                            return e.getMessage();
                        }

                        //unpairs the currently paired stb, if no stb is paired, then nothing happens
                    case "unpair":
                        OGDevice.unpair(Realm.getDefaultInstance());
                        responseStatus = NanoHTTPD.Response.Status.OK;
                        return "{'message': 'successfully unpaired'}";

                    default:
                        responseStatus = NanoHTTPD.Response.Status.NOT_ACCEPTABLE;
                        return "no such command: " + cmd;
                }
            default:
                // Only allowed verbs are GET/POST/PUT
                responseStatus = NanoHTTPD.Response.Status.NOT_ACCEPTABLE;
                return "";
        }
    }
}
