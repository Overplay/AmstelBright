package io.ourglass.amstelbright2.services.amstelbright;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

import io.ourglass.amstelbright2.BuildConfig;
import io.ourglass.amstelbright2.core.ABApplication;
import io.ourglass.amstelbright2.core.OGConstants;
import io.ourglass.amstelbright2.core.OGCore;
import io.ourglass.amstelbright2.services.ajpgsservice.AJPGSPollingService;
import io.ourglass.amstelbright2.services.applejack_comm.AdFetchService;
import io.ourglass.amstelbright2.services.applejack_comm.LogCleanAndPushService;
import io.ourglass.amstelbright2.services.cloudscraper.CloudScraperService;
import io.ourglass.amstelbright2.services.http.HTTPDService;
import io.ourglass.amstelbright2.services.ogdpservice.OGDPService;
import io.ourglass.amstelbright2.services.ogdpservice.UDPBeaconService;
import io.ourglass.amstelbright2.services.ssdpservice.SSDPBroadcastReceiver;
import io.ourglass.amstelbright2.services.ssdpservice.SSDPService;
import io.ourglass.amstelbright2.services.stbservice.STBPollingService;

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

    //public static Context context;

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

        boolean testMode = intent.getBooleanExtra("testMode", false);

        ABApplication.dbToast(this, "Starting Background Services");


        // Let's start the UDP service
        OGCore.sendStatusIntent("STATUS", "Starting Services",
                OGConstants.BootState.ABS_START.getValue());

        if(!testMode) {
            startChildServices();
        }
        else {
            Log.v(TAG, "Starting AmstelBright service in test mode, will not start child services");
        }

        return mStartMode;
    }

    private void startChildServices(){

        OGCore.installStockApps();
        OGCore.sendStatusIntent("STATUS", "Installing stock apps",
                OGConstants.BootState.UPGRADE_START.getValue());


        // Start both UDP discovery methods (won't harm anything now they are on different ports...MAK

        startService( new Intent(this, OGDPService.class));

        if (OGConstants.SEND_UDP_BEACONS){
            startService( new Intent(this, UDPBeaconService.class));
        }

        Intent httpIntent = new Intent(this, HTTPDService.class);
        startService(httpIntent);

        Intent csIntent = new Intent(this, CloudScraperService.class);
        startService(csIntent);


//        Intent upnpIntent = new Intent(this, OGDiscoService.class);
//        startService(upnpIntent);

        Intent logReapIntent = new Intent(this, LogCleanAndPushService.class);
        startService(logReapIntent);

        //start the heartbeat timertask
        mHeartbeatTimer = new Timer();
        //mHeartbeatTimer.cancel();
        mHeartbeatTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Log.v(TAG, "logging heartbeat now");
                OGCore.log_heartbeat(BuildConfig.VERSION_NAME, "", Build.VERSION.RELEASE);
            }
        }, 1, OGConstants.HEARTBEAT_TIMER_INTERVAL);

        Intent advertisementIntent = new Intent(this, AdFetchService.class);
        startService(advertisementIntent);

        Intent stbIntent = new Intent(this, STBPollingService.class);
        startService(stbIntent);

        Intent pgsIntent = new Intent(this, AJPGSPollingService.class);
        startService(pgsIntent);

        // These are here to try to the two different ways of talking to the SSDP discovery service
        //testSSDPBind();
        //testSSDPIntent();

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

    public void registerSSDPResponse(){

        SSDPBroadcastReceiver ssdpBR = new SSDPBroadcastReceiver(new SSDPBroadcastReceiver.SSDPBroadcastReceiverListener() {
            @Override
            public void receivedSSDPUpdate(Intent intent) {
                Log.d(TAG, "Got an SSDP update!");
            }
        });

        IntentFilter filter = new IntentFilter("tv.ourglass.amstelbrightserver.ssdpresponse");
        registerReceiver(ssdpBR, filter);
    }

    public void testSSDPIntent(){

        registerSSDPResponse();

        Intent ssdpi = new Intent(this, SSDPService.class);
        ssdpi.putExtra("deviceFilter", "DIRECTV");

        startService(ssdpi);

    }

    public void testSSDPBind(){

        registerSSDPResponse();

        Intent ssdpi = new Intent(this, SSDPService.class);
        bindService(ssdpi, mConnection, Context.BIND_AUTO_CREATE);


    }

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            SSDPService.LocalBinder binder = (SSDPService.LocalBinder) service;
            binder.getService().discover();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            //mBound = false;
        }
    };
}
