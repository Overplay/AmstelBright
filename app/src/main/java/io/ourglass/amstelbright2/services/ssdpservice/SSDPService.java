package io.ourglass.amstelbright2.services.ssdpservice;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import io.ourglass.amstelbright2.core.ABApplication;
import io.ourglass.amstelbright2.services.amstelbright.AmstelBrightService;

/**
 * Created by mitch on 11/10/16.
 * <p>
 * Does periodic upnp discovery for settop boxes, etc.
 */

public class SSDPService extends Service implements SSDPHandlerThread.SSDPListener {

    public static final String TAG = "SSDPService";
    public static final long CONSIDERED_FRESH = 15000; // This is conservative

    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public SSDPService getService() {
            // Return this instance of SSDPService so clients can call public methods
            return SSDPService.this;
        }
    }

    private SSDPHandlerThread mSSDPDicoveryThread;

    public HashMap<String, String> mAllDevices = new HashMap<>();
    public HashSet<String> mAllAddresses = new HashSet<>();

    private long mLastDiscovery = 0;

    // Stock stuff that needs to be here for all services

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        ABApplication.dbToast(this, "Starting SSDP Enumerator");

        return Service.START_STICKY;

    }

    private void prepThread(){

        if (mSSDPDicoveryThread==null){
            mSSDPDicoveryThread = new SSDPHandlerThread("ssdpdicso");
            mSSDPDicoveryThread.start(this, this);
        }
    }
    // Triggers a discovery pass
    public void discover(){

        prepThread();

        long deltaT = System.currentTimeMillis() - mLastDiscovery;
        if ( deltaT > CONSIDERED_FRESH ) {
            mLastDiscovery = System.currentTimeMillis();
            mSSDPDicoveryThread.discover();
        } else
        {
            // we got reasonably fresh data, just spit it back
            notifyNewDevices();
        }

    }

    public HashSet<String> getFilteredAddresses(String filterTerm){

        HashSet<String> filteredAddresses = new HashSet<>();

        for(Map.Entry<String, String> device : mAllDevices.entrySet()){
            if ( device.getValue().contains(filterTerm)){
                filteredAddresses.add(device.getKey());
            }
        }

        return filteredAddresses;

    }

    public HashMap<String, String> getFilteredDevices(String filterTerm){

        HashMap<String, String> filteredDevices = new HashMap<>();

        for(Map.Entry<String, String> device : mAllDevices.entrySet()){
            if ( device.getValue().contains(filterTerm)){
                filteredDevices.put(device.getKey(), device.getValue());
            }
        }

        return filteredDevices;

    }

    public void onDestroy() {

        Log.d(TAG, "onDestroy");
        mSSDPDicoveryThread.quit();
        super.onDestroy();

    }

    private void notifyNewDevices(){

        Intent intent = new Intent();
        intent.setAction("com.ourglass.amstelbrightserver.ssdp");
        intent.putExtra("devices", mAllDevices);
        intent.putExtra("addresses", mAllAddresses);
        AmstelBrightService.context.sendBroadcast(intent);

    }

    // Discovery thread interface
    @Override
    public void foundDevices(HashMap<String, String> devices, HashSet<String> addresses) {
        mAllDevices = devices;
        mAllAddresses = addresses;
        notifyNewDevices();
    }

    @Override
    public void encounteredError(String errString) {
        Log.e(TAG, "Error enumerating SSDP: "+errString);
    }


}

