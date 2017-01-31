package io.ourglass.amstelbright2.services.http.handlers;

import android.content.Intent;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import io.ourglass.amstelbright2.core.ABApplication;
import io.ourglass.amstelbright2.core.OGConstants;
import io.ourglass.amstelbright2.realm.OGApp;
import io.ourglass.amstelbright2.services.http.NanoHTTPBase.NanoHTTPD;
import io.ourglass.amstelbright2.services.http.ogutil.JWTHelper;
import io.realm.Realm;

/**
 * Created by mkahn on 5/9/16.
 */
public class JSONAppDataHandler extends JSONHandler {

    public static final String TAG = "JSONAppDataHandler";

    public String getText(Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {

        //these operations require patron level permissions
        String tok = session.getHeaders().get("Authorization");
        if(!OGConstants.USE_JWT && (tok == null || JWTHelper.getInstance().checkJWT(tok, OGConstants.AUTH_LEVEL.PATRON))) {
            responseStatus = NanoHTTPD.Response.Status.UNAUTHORIZED;
            return "";
        }

        final String appId = urlParams.get("appid");

        Realm realm = Realm.getDefaultInstance();
        final OGApp app = OGApp.getApp(realm, appId);

        if (app==null){
            realm.close();
            responseStatus = NanoHTTPD.Response.Status.NOT_ACCEPTABLE;
            return makeErrorJson("No such app installed.");
        }

        switch (session.getMethod()) {

            case GET:

                Log.d(TAG, "GET for appdata on "+ app.appId);
                //TODO a better locking mechanism would use request IP address...for Bucanero
                boolean shouldLock = session.getParms().containsKey("lock");
                long key = 0;

                if (shouldLock==true){
                    Log.d(TAG, "GET for is LOCKED");
                    realm.beginTransaction();
                    key = app.lockUpdates();
                    realm.commitTransaction();
                }

                JSONObject appData = app.getPublicData(realm);
                if (key!=0){
                    try {
                        appData.putOpt("lockKey", key);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                realm.close();
                responseStatus = NanoHTTPD.Response.Status.OK;
                return appData.toString();

            // These are treated the same for now

            case POST:
                Log.d(TAG, "POST for appdata on "+ app.appId);

                try {
                    final JSONObject dataJson = getBodyAsJSONObject(session);

                    long lockKey = dataJson.optLong("lockKey", 0);

                    dataJson.remove("lockKey");

                    if (app.checkKey(realm, lockKey)){

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
                                //AmstelBrightService.context.sendBroadcast(intent);
                                ABApplication.sharedContext.sendBroadcast(intent);

                            }
                        }, null, null );
                        realm.close();
                        responseStatus = NanoHTTPD.Response.Status.OK;

                        return dataJson.toString();
                    } else {
                        realm.close();
                        responseStatus = NanoHTTPD.Response.Status.CONFLICT;
                        return makeErrorJson("data locked by another client");
                    }


                } catch (Exception e) {
                    realm.close();
                    responseStatus = NanoHTTPD.Response.Status.INTERNAL_ERROR;
                    return makeErrorJson(e);
                }


            default:
                // Only allowed verbs are GET/POST/PUT
                realm.close();
                responseStatus = NanoHTTPD.Response.Status.NOT_ACCEPTABLE;
                return "";
        }


    }


}
