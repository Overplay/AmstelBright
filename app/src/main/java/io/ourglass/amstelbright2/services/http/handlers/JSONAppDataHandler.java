package io.ourglass.amstelbright2.services.http.handlers;

import org.json.JSONObject;

import java.util.Map;

import io.ourglass.amstelbright2.realm.OGApp;
import io.ourglass.amstelbright2.services.http.NanoHTTPBase.NanoHTTPD;
import io.realm.Realm;
import android.content.Intent;
import io.ourglass.amstelbright2.services.amstelbright.AmstelBrightService;

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

                            String appType = app.appType;
                            Intent intent = new Intent();
                            intent.setAction("com.ourglass.amstelbrightserver.status");
                            intent.putExtra("newAppData", dataJson.toString());
                            intent.putExtra("appType", appType);
                            AmstelBrightService.context.sendBroadcast(intent);

                        }
                    }, null, null );
                    realm.close();
                    responseStatus = NanoHTTPD.Response.Status.OK;


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
