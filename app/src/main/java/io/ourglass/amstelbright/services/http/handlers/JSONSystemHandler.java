package io.ourglass.amstelbright.services.http.handlers;

import android.util.Log;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import io.ourglass.amstelbright.core.OGConstants;
import io.ourglass.amstelbright.core.OGCore;
import io.ourglass.amstelbright.realm.OGDevice;
import io.ourglass.amstelbright.services.http.NanoHTTPBase.NanoHTTPD;


/**
 * Created by mkahn on 5/9/16.
 */
public class JSONSystemHandler extends JSONHandler {



    public String getText(Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {

        String cmd = urlParams.get("command");

        switch (session.getMethod()){

            case GET:

                switch (cmd){

                    case "apps":
                        responseStatus = NanoHTTPD.Response.Status.OK;
                        return OGCore.getInstance().getAllApps();

                    case "device":
                        responseStatus = NanoHTTPD.Response.Status.OK;
                        return OGCore.getInstance().getDevice();

		            default:
                    case "device":
                        responseStatus = NanoHTTPD.Response.Status.OK;
                        return OGCore.getInstance().getDevice();

                    default:
                        responseStatus = NanoHTTPD.Response.Status.NOT_ACCEPTABLE;
                        return "no such command: "+cmd;
                }


            case POST:
            case PUT:

                switch(cmd){
                    case "device":
                        OGCore core = OGCore.getInstance();

                        //check for JWT
                        //todo add more checks to determine if the JWT contains the correct information
                        if(!OGConstants.TEST_MODE && !JWTPresent(session)){
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
                        final String json = files.get("postData");
                        try {
                            JSONObject obj = new JSONObject(json);
                            if(obj.has("name")){
                                core.updateDevice("name", obj.getString("name"));
                            }
                            if(obj.has("locationWithinVenue")){
                                core.updateDevice("locationWithinVenue", obj.getString("locationWithinVenue"));
                            }
                            responseStatus = NanoHTTPD.Response.Status.OK;
                            return core.getDevice();
                        }catch(Exception e){
                            Log.e(getClass().toString(), e.toString());
                            responseStatus = NanoHTTPD.Response.Status.INTERNAL_ERROR;
                            return e.toString();
                        }
                }
            default:
                // Only allowed verbs are GET/POST/PUT
                responseStatus = NanoHTTPD.Response.Status.NOT_ACCEPTABLE;
                return "";
        }


    }


}
