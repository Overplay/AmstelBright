package io.ourglass.amstelbright2.core;

import android.content.Intent;

import io.ourglass.amstelbright2.realm.OGApp;
import io.ourglass.amstelbright2.services.amstelbright.AmstelBrightService;

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
        intent.putExtra("app", target.getAppAsJson().toString());
        AmstelBrightService.context.sendBroadcast(intent);

    }

    public static void sendStatusIntent(String cmd, String msg, int code) {

        // Notify
        Intent intent = new Intent();
        intent.setAction("com.ourglass.amstelbrightserver.status");
        intent.putExtra("command", cmd);
        intent.putExtra("message", msg);
        intent.putExtra("code", code);
        AmstelBrightService.context.sendBroadcast(intent);

    }

}
