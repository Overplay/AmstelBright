package io.ourglass.amstelbright2.services.amstelbright;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

import io.ourglass.amstelbright2.core.ABApplication;
import io.ourglass.amstelbright2.core.OGConstants;
import io.ourglass.amstelbright2.core.OGCore;
import io.ourglass.amstelbright2.realm.OGLog;
import io.ourglass.amstelbright2.services.applejack_comm.LogCleanAndPushService;
import io.ourglass.amstelbright2.services.cloudscraper.CloudScraperService;
import io.ourglass.amstelbright2.services.http.HTTPDService;
import io.ourglass.amstelbright2.services.stbservice.STBService;
import io.ourglass.amstelbright2.services.udp.UDPBeaconService;
import io.ourglass.amstelbright2.services.udp.UDPListenAndRespond;
import io.realm.Realm;
import io.realm.RealmResults;

//import io.ourglass.amstelbright2.services.udp.UDPBeaconService;

/**
 * This is the parent server class that kicks off everybody else: UDP, Bluetooth, HTTP
 *
 * (c) Ourglass
 * Mitch Kahn, May 2016
 */

public class AmstelBrightService extends Service  {

    //used to keep track of uptime
    public static final long bootTime = System.currentTimeMillis();

    // This is here as a reliable ref to context for the Realm stuff.
    public static Context context;

    public static final String TAG = "ABService";

    /** indicates how to behave if the service is killed */
    int mStartMode = START_STICKY;

    /** indicates whether onRebind should be used */
    //boolean mAllowRebind = true;

    //final Messenger mMessenger = new Messenger(new IncomingHandler());

    private Timer mHeartbeatTimer;

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



        /* Choose either new (UPNP) or old (shout in the dark) discovery method */
        Intent udpIntent = OGConstants.USE_UPNP_DISCOVERY ? new Intent(this, UDPListenAndRespond.class) :
                new Intent(this, UDPBeaconService.class);
        startService(udpIntent);

        Intent httpIntent = new Intent(this, HTTPDService.class);
        startService(httpIntent);

        Intent csIntent = new Intent(this, CloudScraperService.class);
        startService(csIntent);

        Intent stbIntent = new Intent(this, STBService.class);
        startService(stbIntent);

        Intent logReapIntent = new Intent(this, LogCleanAndPushService.class);
        startService(logReapIntent);

        //start the heartbeat timertask
        mHeartbeatTimer = new Timer();
        //mHeartbeatTimer.cancel();
        mHeartbeatTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Log.v(TAG, "logging heartbeat now");
                OGCore.log_heartbeat("", "", Build.VERSION.RELEASE);
            }
        }, 1, OGConstants.HEARTBEAT_TIMER_INTERVAL);
    }

    /** Called when The service is no longer used and is being destroyed */
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "In onDestroy()");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }



}
