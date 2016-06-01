package io.ourglass.amstelbright.services.http.handlers;

import android.util.Log;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import io.ourglass.amstelbright.core.OGCore;
import io.ourglass.amstelbright.core.OGDevice;
import io.ourglass.amstelbright.services.http.NanoHTTPBase.NanoHTTPD;


/**
 * Created by mkahn on 5/9/16.
 */
public class JSONSystemHandler extends JSONHandler {



    public String getText(Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {

        String cmd;

        switch (session.getMethod()){

            case GET:
                cmd = urlParams.get("command");

                switch (cmd){

                    case "apps":
                        responseStatus = NanoHTTPD.Response.Status.OK;
                        return OGCore.getInstance().getAllApps();

                    case "device":
                        responseStatus = NanoHTTPD.Response.Status.OK;
                        return OGCore.getInstance().getDevice();
                    
		            default:
                        responseStatus = NanoHTTPD.Response.Status.NOT_ACCEPTABLE;
                        return "no such command: "+cmd;
                }


            case POST:
                cmd = urlParams.get("command");

                switch(cmd){
                    case "device":
                        OGCore core = OGCore.getInstance();

                        //check for JWT
                        /*Map<String, String> headers = session.getHeaders();
                        Iterator it = headers.entrySet().iterator();
                        while(it.hasNext()){
                            Map.Entry pair = (Map.Entry)it.next();
                            Log.wtf("TAGTAGTAG", pair.getKey() + " = " + pair.getValue());
                            it.remove();
                        }*/

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
            case PUT:

            default:
                // Only allowed verbs are GET/POST/PUT
                responseStatus = NanoHTTPD.Response.Status.NOT_ACCEPTABLE;
                return "";
        }


    }


}
