package io.ourglass.amstelbright.services.amstelbright;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import io.ourglass.amstelbright.core.OGConstants;
import io.ourglass.amstelbright.core.OGCore;
import io.ourglass.amstelbright.services.cloudscraper.CloudScraperService;
import io.ourglass.amstelbright.services.cloudscraper.OGTweetScraper;
import io.ourglass.amstelbright.services.http.HTTPDService;
import io.ourglass.amstelbright.services.stbservice.STBService;
import io.ourglass.amstelbright.services.udp.UDPBeaconService;


/**
 * This is the parent server class that kicks off everybody else: UDP, Bluetooth, HTTP
 *
 * (c) Ourglass
 * Mitch Kahn, May 2016
 */

public class AmstelBrightService extends Service  {

    public static final String TAG = "ABS";

    OGTweetScraper twatter;

    // For debug toasts
    public static final Boolean DEBUG = false;

    //Context mContext = getApplicationContext();
    final Messenger mMessenger = new Messenger(new IncomingHandler());

    // This is here as a reliable ref to context for the Realm stuff.
    public static Context context;

    public AmstelBrightService() {
    }

    /** indicates how to behave if the service is killed */
    int mStartMode = START_STICKY;

    /** indicates whether onRebind should be used */
    boolean mAllowRebind = true;

    /** Called when the service is being created. */
    @Override
    public void onCreate() {

    }

    /** The service is starting, due to a call to startService() */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        dbToastr("ABS: onStartCommand");

        // Create global static ref for code that needs Context
        context = getApplicationContext();

        // Let's start the UDP service
        OGCore.sendStatusIntent("STATUS", "Starting Services",
                OGConstants.BootState.ABS_START.getValue());


        startChildServices();

        return mStartMode;
    }

    /** A client is binding to the service with bindService() */
    @Override
    public IBinder onBind(Intent intent) {
        dbToastr("ABS: binding");
        return mMessenger.getBinder();
    }

    /** Called when all clients have unbound with unbindService() */
    @Override
    public boolean onUnbind(Intent intent) {
        super.onUnbind(intent);
        dbToastr("ABS: unbinding");
        return mAllowRebind;
    }


    /** Called when The service is no longer used and is being destroyed */
    @Override
    public void onDestroy() {
        super.onDestroy();
        dbToastr("ABS: onDestroy");

    }




    // TODO: Seriously with the leaks? This code is right from the Google site. FCOL
    private class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    Toast.makeText(getApplicationContext(), "hello!", Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    Toast.makeText(getApplicationContext(), "beer thirty!", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private void logTweets(JSONObject jobj){
        Log.d(TAG, jobj.toString());
    }

    private void startChildServices(){



        OGCore.installStockApps(this);
        OGCore.sendStatusIntent("STATUS", "Installing stock apps",
                OGConstants.BootState.UPGRADE_START.getValue());


        Intent udpIntent = new Intent(this, UDPBeaconService.class)
                .putExtra("data", "some data to broadcast")
                .putExtra("port", 9091)
                .putExtra("beaconFreq", 2000);

        startService(udpIntent);

        Intent httpIntent = new Intent(this, HTTPDService.class);
        startService(httpIntent);

        Intent csIntent = new Intent(this, CloudScraperService.class);
        startService(csIntent);

        Intent stbIntent = new Intent(this, STBService.class);
        startService(stbIntent);

    }


    private void dbToastr(String msg){

        if (DEBUG){
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        }

    }
}
