package io.ourglass.amstelbright2.core;

import android.content.Intent;

import io.ourglass.amstelbright2.realm.OGApp;

/**
 * Created by mkahn on 7/25/16.
 */
public class OGNotifications {

    private static final String TAG = "OGNotifications";

    private static void sendCommandIntent(String cmd, OGApp target) {

        // Notify
        Intent intent = new Intent();
        intent.setAction("com.ourglass.amstelbrightserver");
        intent.putExtra("command", cmd);
        intent.putExtra("appId", target.appId);
        intent.putExtra("app", target.toJson().toString());
        ABApplication.sharedContext.sendBroadcast(intent);

    }

    public static void sendStatusIntent(String cmd, String msg, int code) {

        // Notify
        Intent intent = new Intent();
        intent.setAction("com.ourglass.amstelbrightserver.status");
        intent.putExtra("command", cmd);
        intent.putExtra("message", msg);
        intent.putExtra("code", code);
        ABApplication.sharedContext.sendBroadcast(intent);

    }

}
