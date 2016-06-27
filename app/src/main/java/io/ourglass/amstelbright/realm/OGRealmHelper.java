package io.ourglass.amstelbright.realm;

import org.json.JSONObject;

import io.realm.Realm;


/**
 *
 * This is a metric-shitton of Static helper functions to try to work with the Realm
 * threading/update model.
 *
 * MAK: June 16, 2016
 *
 *
 */

public class OGRealmHelper {

    public static final String TAG = "OGRealmHelper";

    public static boolean isAppInstalled(Realm realm, String appId){

        OGApp app = OGApp.getApp(realm, appId);
        boolean isInstalled = app!=null;
        realm.close();
        return isInstalled;

    }

    public static void updateAppData(final Realm realm, final String appId, final JSONObject dataJson) {

        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm bgRealm) {
                OGApp app = OGApp.getApp(bgRealm, appId);
                app.setPublicData(dataJson);
                realm.close();
            }
        }, null, null );



    }
}
