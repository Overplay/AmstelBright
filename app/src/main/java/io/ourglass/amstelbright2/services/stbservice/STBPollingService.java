package io.ourglass.amstelbright2.services.stbservice;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;

import io.ourglass.amstelbright2.core.ABApplication;
import io.ourglass.amstelbright2.core.OGConstants;
import io.ourglass.amstelbright2.core.OGCore;
import io.ourglass.amstelbright2.core.OGSystem;

/**
 * Created by atorres on 4/19/16.
 */

// TODO This service shouldn't event run when not paired. It should be started only after a pair
// event occurs, or upon bootup if an existing pair is saved.


public class STBPollingService extends Service {

    static final String TAG = "STBPollingService";
    static final boolean VERBOSE = true;
    static STBPollingService sInstance;

    public static STBPollingService getInstance(){
        return sInstance;
    }

    // TODO: Count failed polls and take a reconnect action if the number of failed polls exceeds a
    // threshold. Several things could cause a disconnect: IP address changed for box, WiFi is down,
    // etc.

    public static STBPollStatus lastPollStatus;

    HandlerThread stbLooperThread = new HandlerThread("stbPollLooper");
    private Handler mPollThreadHandler;

    private void logd(String message) {
        if (VERBOSE) {
            Log.d(TAG, message);
        }
    }

    private void startPollLooper() {
        Log.d(TAG, "Starting STB poll looper");

        Runnable txRunnable = new Runnable() {
            @Override
            public void run() {
                logd("STB Update Loop");
                SetTopBox stb = OGSystem.getPairedSTB();
                if (stb==null){
                    Log.d(TAG, "Not paired to STB, skipping update");
                } else {
                    Log.d(TAG, "Paired to: "+stb.ipAddress+", updating now.");
                    lastPollStatus = stb.updateAllSync();
                    // Feels clunky like the STB should notify the system if its channel changed...
                    OGCore.setCurrentlyOnTV(stb.nowPlaying);
                }

                mPollThreadHandler.postDelayed(this, OGConstants.TV_POLL_INTERVAL);
            }
        };

        mPollThreadHandler.post(txRunnable);

    }

    private void stopPoll() {
        Log.d(TAG, "Stopping TV polling.");
        mPollThreadHandler.removeCallbacksAndMessages(null);
    }


    @Override
    public void onCreate() {

        Log.d(TAG, "onCreate");
        sInstance = this;

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        ABApplication.dbToast(this, "Starting STB Polling");

        if (!stbLooperThread.isAlive()){
            stbLooperThread.start();
            mPollThreadHandler = new Handler(stbLooperThread.getLooper());
        }


        startPollLooper();

        //OGCore.sendStatusIntent("STATUS", "Starting beacon", OGConstants.BootState.UDP_START.getValue());

        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        stopPoll();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}
