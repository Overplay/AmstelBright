package io.ourglass.amstelbright.services.http;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.widget.Toast;

import java.io.IOException;

import io.ourglass.amstelbright.core.OGConstants;
import io.ourglass.amstelbright.core.OGCore;
import io.ourglass.amstelbright.services.http.ogutil.AssetExtractor;
import io.ourglass.amstelbright.services.udp.UDPBeaconService;

/**
 * This is the parent server class that kicks off everybody else: UDP, Bluetooth, HTTP
 *
 * (c) Ourglass
 * Mitch Kahn, May 2016
 */

public class HTTPDService extends Service {

    public static final String TAG = "HTTPServer";
    // For debug toasts
    public static final Boolean DEBUG = false;


    private OGNanolets server;

    final Messenger mMessenger = new Messenger(new IncomingHandler());

    public HTTPDService() {
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
        dbToastr(TAG+": onStartCommand");

        AssetExtractor asx = new AssetExtractor(getApplicationContext());
        asx.update();
        OGCore.sendStatusIntent("STATUS", "Checking for upgrades",
                OGConstants.BootState.UPGRADE_START.getValue());


        // Let's start the HTTP service
        try {
            server = new OGNanolets(this);
            OGCore.sendStatusIntent("STATUS", "Starting webserver",
                    OGConstants.BootState.HTTP_START.getValue());

        } catch (IOException e) {
            e.printStackTrace();
        }

        return mStartMode;
    }

    /** A client is binding to the service with bindService() */
    @Override
    public IBinder onBind(Intent intent) {
        dbToastr( TAG+"binding" );
        return mMessenger.getBinder();
    }

    /** Called when all clients have unbound with unbindService() */
    @Override
    public boolean onUnbind(Intent intent) {
        super.onUnbind(intent);
        dbToastr(TAG+"binding");
        return mAllowRebind;
    }


    /** Called when The service is no longer used and is being destroyed */
    @Override
    public void onDestroy() {
        super.onDestroy();
        dbToastr(TAG+": onDestroy");
        if (server != null) {
            server.stop();
        }

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


    private void startChildServices(){


        Intent intent = new Intent(this, UDPBeaconService.class)
                .putExtra("data", "some data to broadcast")
                .putExtra("port", 1234)
                .putExtra("beaconFreq", 2000);

        startService(intent);

    }


    private void dbToastr(String msg){

        if (DEBUG){
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        }

    }

}
