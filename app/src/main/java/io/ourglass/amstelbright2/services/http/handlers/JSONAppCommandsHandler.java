package io.ourglass.amstelbright2.services.http.handlers;

import java.util.Map;

import io.ourglass.amstelbright2.core.OGCore;
import io.ourglass.amstelbright2.core.exceptions.OGServerException;
import io.ourglass.amstelbright2.realm.OGApp;
import io.ourglass.amstelbright2.services.http.NanoHTTPBase.NanoHTTPD;
import io.realm.Realm;


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

        switch (session.getMethod()) {

            case POST:

                switch (cmd) {


                    case "move":
                    {

                        OGApp movedApp;
                        Realm realm = Realm.getDefaultInstance();

                        try {

                            movedApp = OGCore.moveApp(realm, appId);
                            String appJsonString = movedApp.getAppAsJson().toString();
                            responseStatus = NanoHTTPD.Response.Status.OK;
                            return appJsonString;

                        } catch (OGServerException e) {

                            return processOGException(e);

                        } finally {
                            realm.close();
                        }

                    }




                    case "launch":

                    {

                        OGApp launchedApp;
                        Realm realm = Realm.getDefaultInstance();


                        try {

                            launchedApp = OGCore.launchApp(realm, appId);
                            responseStatus = NanoHTTPD.Response.Status.OK;
                            return launchedApp.getAppAsJson().toString();

                        } catch (OGServerException e) {

                            return processOGException(e);

                        } finally {
                            realm.close();
                        }


                    }


                    case "kill":

                    {

                        Realm realm = Realm.getDefaultInstance();


                        try {

                            OGApp killedApp = OGCore.killApp(realm, appId);
                            responseStatus = NanoHTTPD.Response.Status.OK;
                            return killedApp.getAppAsJson().toString();

                        } catch (OGServerException e) {

                            return processOGException(e);

                        } finally {
                            realm.close();
                        }

                    }




                    //TODO add scaling. Pass scale in JSON body so same slug can be used

                    case "scale":
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
