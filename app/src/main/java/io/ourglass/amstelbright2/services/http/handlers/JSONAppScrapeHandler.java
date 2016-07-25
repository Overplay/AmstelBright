package io.ourglass.amstelbright2.services.http.handlers;

import org.json.JSONObject;

import java.util.Map;

import io.ourglass.amstelbright2.realm.OGScraper;
import io.ourglass.amstelbright2.services.http.NanoHTTPBase.NanoHTTPD;
import io.realm.Realm;

/**
Scrape endpoint
 MAK June 2016
 */
public class JSONAppScrapeHandler extends JSONHandler {

    public String getText(Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {

        final String appId = urlParams.get("appid");


        // TODO this is not the right logic, just trying to work while listening to Treb
        if (appId==null){
            responseStatus = NanoHTTPD.Response.Status.NOT_ACCEPTABLE;
            return makeErrorJson("No such app installed.");
        }

        switch (session.getMethod()) {

            case GET: {

                Realm realm = Realm.getDefaultInstance();
                String results = OGScraper.getScrape(realm, appId);
                realm.close();
                responseStatus = NanoHTTPD.Response.Status.OK;
                return results;

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
