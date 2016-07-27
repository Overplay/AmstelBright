package io.ourglass.amstelbright2.services.amstelbright;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import io.ourglass.amstelbright2.core.ABApplication;
import io.ourglass.amstelbright2.core.OGConstants;
import io.ourglass.amstelbright2.core.OGCore;
import io.ourglass.amstelbright2.services.cloudscraper.CloudScraperService;
import io.ourglass.amstelbright2.services.http.HTTPDService;
import io.ourglass.amstelbright2.services.stbservice.STBService;
import io.ourglass.amstelbright2.services.udp.UDPBeaconService;


/**
 * This is the parent server class that kicks off everybody else: UDP, Bluetooth, HTTP
 *
 * (c) Ourglass
 * Mitch Kahn, May 2016
 */

public class AmstelBrightService extends Service  {

    // This is here as a reliable ref to context for the Realm stuff.
    public static Context context;

    public static final String TAG = "ABService";

    /** indicates how to behave if the service is killed */
    int mStartMode = START_STICKY;

    /** indicates whether onRebind should be used */
    //boolean mAllowRebind = true;

    //final Messenger mMessenger = new Messenger(new IncomingHandler());


    public AmstelBrightService() {
    }


    /** Called when the service is being created. */
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "In onCreate()");
    }

    /** The service is starting, due to a call to startService() */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        ABApplication.dbToast(this, "Starting Background Services");

        // Create global static ref for code that needs Context
        context = getApplicationContext();

        // Let's start the UDP service
        OGCore.sendStatusIntent("STATUS", "Starting Services",
                OGConstants.BootState.ABS_START.getValue());

        startChildServices();

        return mStartMode;
    }

    private void startChildServices(){

        OGCore.installStockApps();
        OGCore.sendStatusIntent("STATUS", "Installing stock apps",
                OGConstants.BootState.UPGRADE_START.getValue());

        Intent udpIntent = new Intent(this, UDPBeaconService.class);

        startService(udpIntent);

        Intent httpIntent = new Intent(this, HTTPDService.class);
        startService(httpIntent);

        Intent csIntent = new Intent(this, CloudScraperService.class);
        startService(csIntent);

        Intent stbIntent = new Intent(this, STBService.class);
        startService(stbIntent);

    }

    /** Called when The service is no longer used and is being destroyed */
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "In onDestroy()");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    // MAK: We aren't binding right now, but it's here for ref

//    /** A client is binding to the service with bindService() */
//    @Override
//    public IBinder onBind(Intent intent) {
//        return mMessenger.getBinder();
//    }
//
//    /** Called when all clients have unbound with unbindService() */
//    @Override
//    public boolean onUnbind(Intent intent) {
//        super.onUnbind(intent);
//        return mAllowRebind;
//    }




//
//
//
//    // TODO: Seriously with the leaks? This code is right from the Google site. FCOL
//    private class IncomingHandler extends Handler {
//        @Override
//        public void handleMessage(Message msg) {
//            switch (msg.what) {
//                case 1:
//                    Toast.makeText(getApplicationContext(), "hello!", Toast.LENGTH_SHORT).show();
//                    break;
//                case 2:
//                    Toast.makeText(getApplicationContext(), "beer thirty!", Toast.LENGTH_SHORT).show();
//                    break;
//                default:
//                    super.handleMessage(msg);
//            }
//        }
//    }
//
//    private void logTweets(JSONObject jobj){
//        Log.d(TAG, jobj.toString());
//    }




}
