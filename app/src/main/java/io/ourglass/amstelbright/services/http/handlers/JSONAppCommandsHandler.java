package io.ourglass.amstelbright.services.http.handlers;

import java.util.Map;

import io.ourglass.amstelbright.core.OGCore;
import io.ourglass.amstelbright.core.exceptions.OGServerException;
import io.ourglass.amstelbright.realm.OGApp;
import io.ourglass.amstelbright.services.http.NanoHTTPBase.NanoHTTPD;


// TODO Maybe this should handle appData too?

/**
 * Created by mkahn on 5/9/16.
 *
 * Handles the following:
 *
 * /api/app/:appid/:command
 *
 * /api/app/:appid/launch
 * /api/app/:appid/kill
 * /api/app/:appid/move
 *
 * Idea?
 *
 * /api/app/:appid/data
 *
 *
 *
 */

public class JSONAppCommandsHandler extends JSONHandler {


    public String getText(Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {

        String appId = urlParams.get("appid");
        String cmd = urlParams.get("command");

        switch (session.getMethod()){

            case POST:

                switch (cmd){


                    case "move":

                        OGApp movedApp;

                        try {

                            movedApp = OGCore.getInstance().moveApp(appId);
                            String appJsonString = movedApp.getAppAsJson().toString();
                            responseStatus = NanoHTTPD.Response.Status.OK;
                            return appJsonString;

                        } catch (OGServerException e) {

                            processOGException(e);

                        }



                    case "launch":

                        OGApp launchedApp;

                        try {
                            launchedApp = OGCore.getInstance().launchApp(appId);
                            responseStatus = NanoHTTPD.Response.Status.OK;
                            return launchedApp.getAppAsJson().toString();
                        } catch (OGServerException e) {
                            processOGException(e);

                        }


                    case "kill":


                        try {
                            OGApp killedApp  = OGCore.getInstance().killApp(appId);
                            responseStatus = NanoHTTPD.Response.Status.OK;
                            return killedApp.getAppAsJson().toString();

                        } catch (OGServerException e) {
                            responseStatus = NanoHTTPD.Response.Status.BAD_REQUEST;
                            return e.toJson();
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
