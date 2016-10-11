package io.ourglass.amstelbright2.services.http.handlers;

import org.json.JSONObject;

import java.util.Map;

import io.ourglass.amstelbright2.core.OGConstants;
import io.ourglass.amstelbright2.realm.OGScraper;
import io.ourglass.amstelbright2.services.http.NanoHTTPBase.NanoHTTPD;
import io.ourglass.amstelbright2.services.http.ogutil.JWTHelper;
import io.realm.Realm;

/**
Scrape endpoint
 MAK June 2016
 */
public class JSONAppScrapeHandler extends JSONHandler {

    public String getText(Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {

        //these operations require patron level permissions
        String tok = session.getHeaders().get("Authorization");
        if(!OGConstants.USE_JWT && (tok == null || JWTHelper.getInstance().checkJWT(tok, OGConstants.AUTH_LEVEL.PATRON))) {
            responseStatus = NanoHTTPD.Response.Status.UNAUTHORIZED;
            return "";
        }

        final String appId = urlParams.get("appid");


        // TODO this is not the right logic, just trying to work while listening to Treb
        if (appId==null){
            responseStatus = NanoHTTPD.Response.Status.NOT_ACCEPTABLE;
            return makeErrorJson("No such app installed.");
        }

        switch (session.getMethod()) {

            case GET: {

                if (appId.equalsIgnoreCase("all")){

                    Realm realm = Realm.getDefaultInstance();
                    String results = OGScraper.getAllScrapes(realm).toString();
                    realm.close();

                    if (results==null){
                        results = "[]";
                        responseStatus = NanoHTTPD.Response.Status.NO_CONTENT;
                    } else {
                        responseStatus = NanoHTTPD.Response.Status.OK;
                    }
                    return results;


                } else {

                    Realm realm = Realm.getDefaultInstance();
                    String results = OGScraper.getScrape(realm, appId);
                    realm.close();

                    if (results==null){
                        results = "{}";
                        responseStatus = NanoHTTPD.Response.Status.NO_CONTENT;
                    } else {
                        responseStatus = NanoHTTPD.Response.Status.OK;
                    }
                    return results;

                }


            }


            // These are treated the same for now

            case POST: {

                Realm realm = Realm.getDefaultInstance();

                try {
                    final JSONObject dataJson = getBodyAsJSONObject(session);
                    OGScraper.setQueryFor(realm, appId, dataJson.getString("query"));
                    responseStatus = NanoHTTPD.Response.Status.OK;
                    return dataJson.toString();

                } catch (Exception e) {
                    responseStatus = NanoHTTPD.Response.Status.INTERNAL_ERROR;
                    return makeErrorJson(e);
                } finally {
                    realm.close();
                }

            }




            default:
                // Only allowed verbs are GET/POST/PUT
                responseStatus = NanoHTTPD.Response.Status.NOT_ACCEPTABLE;
                return "";
        }


    }


}
