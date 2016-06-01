package io.ourglass.amstelbright.services.http.handlers;

import java.util.Map;

import io.ourglass.amstelbright.core.OGCore;
import io.ourglass.amstelbright.core.OGDevice;
import io.ourglass.amstelbright.services.http.NanoHTTPBase.NanoHTTPD;


/**
 * Created by mkahn on 5/9/16.
 */
public class JSONSystemHandler extends JSONHandler {



    public String getText(Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {

        switch (session.getMethod()){

            case GET:
                String cmd = urlParams.get("command");

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
            case PUT:

            default:
                // Only allowed verbs are GET/POST/PUT
                responseStatus = NanoHTTPD.Response.Status.NOT_ACCEPTABLE;
                return "";
        }


    }


}
