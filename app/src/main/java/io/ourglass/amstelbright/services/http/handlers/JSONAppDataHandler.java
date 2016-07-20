package io.ourglass.amstelbright.services.http.handlers;

import android.content.Intent;

import org.json.JSONObject;

import java.util.Map;

import io.ourglass.amstelbright.realm.OGApp;
import io.ourglass.amstelbright.services.amstelbright.AmstelBrightService;
import io.ourglass.amstelbright.services.http.NanoHTTPBase.NanoHTTPD;
import io.ourglass.amstelbright.tvui.MainframeActivity;
import io.realm.Realm;

/**
 * Created by mkahn on 5/9/16.
 */
public class JSONAppDataHandler extends JSONHandler {

    public String getText(Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {

        final String appId = urlParams.get("appid");

        Realm realm = Realm.getDefaultInstance();
        OGApp app = OGApp.getApp(realm, appId);

        if (app==null){
            realm.close();
            responseStatus = NanoHTTPD.Response.Status.NOT_ACCEPTABLE;
            return makeErrorJson("No such app installed.");
        }

        switch (session.getMethod()) {

            case GET:

                JSONObject appData = app.getPublicData();
                realm.close();
                responseStatus = NanoHTTPD.Response.Status.OK;
                return appData.toString();

            // These are treated the same for now

            case POST:

                try {
                    final JSONObject dataJson = getBodyAsJSONObject(session);
                    realm.executeTransactionAsync(new Realm.Transaction() {
                        @Override
                        public void execute(Realm bgRealm) {
                            OGApp app = OGApp.getApp(bgRealm, appId);
                            app.setPublicData(dataJson);
                        }
                    }, null, null );
                    realm.close();
                    responseStatus = NanoHTTPD.Response.Status.OK;
                    Intent intent = new Intent();
                    intent.setAction("com.ourglass.amstelbrightserver.status");
                    intent.putExtra("newAppData", dataJson.toString());
                    AmstelBrightService.context.sendBroadcast(intent);

                    return dataJson.toString();

                } catch (Exception e) {
                    responseStatus = NanoHTTPD.Response.Status.INTERNAL_ERROR;
                    return makeErrorJson(e);
                }



            default:
                // Only allowed verbs are GET/POST/PUT
                responseStatus = NanoHTTPD.Response.Status.NOT_ACCEPTABLE;
                return "";
        }


    }


}
