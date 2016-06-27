package io.ourglass.amstelbright.services.udp;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import io.ourglass.amstelbright.core.OGConstants;
import io.ourglass.amstelbright.core.OGCore;
import io.ourglass.amstelbright.realm.OGDevice;
import io.realm.Realm;

/**
 * Created by atorres on 4/19/16.
 */
public class UDPBeaconService extends Service {

    static final String TAG = "UDPBeaconService";
    static final boolean VERBOSE = true;

    String mMessage;
    int mPort;
    int mBeaconFreq;
    DatagramSocket mSocket;
    Boolean mSending = false;

    HandlerThread udpLooperThread = new HandlerThread("udpBeaconLooper");
    private Handler mUdpThreadHandler;

    private void logd(String message){
        if (VERBOSE){
            Log.d(TAG, message);
        }
    }

    private void sendUDPPacket() {

        Realm realm = Realm.getDefaultInstance();
        OGDevice device = OGCore.getDeviceAsObject(realm);

        WifiManager manager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        String mac = manager.getConnectionInfo().getMacAddress();

        mMessage = String.format("{\"name\": \"%s\", \"location\": \"%s\", \"mac\": \"%s\"}",
                device.name, device.locationWithinVenue, mac);

        realm.close();

        if (mSocket == null || mSocket.isClosed()) {
            try {
                mSocket = new DatagramSocket(mPort);
                mSocket.setReuseAddress(true);
                mSocket.setBroadcast(true);
            } catch (SocketException e) {
                Log.e(TAG, e.getLocalizedMessage());
                return;
            }
        }

        try {
            DatagramPacket packet = new DatagramPacket(mMessage.getBytes(), mMessage.length(),
                    getBroadcastAddress(), mPort);
            mSocket.send(packet);
        } catch (IOException e) {
            Log.e(TAG, e.getLocalizedMessage());
        }

    }

    private void startBeaconLooper() {
        Log.d(TAG, "startBeaconLooper");
        mSending = true;


        Runnable txRunnable = new Runnable() {
            @Override
            public void run() {
                logd("sending UDP packet from looper");
                sendUDPPacket();
                mUdpThreadHandler.postDelayed(this, mBeaconFreq);
            }
        };

        mUdpThreadHandler.post(txRunnable);

    }


    private void stopBeacon() {
        Log.d(TAG, "stopBeacon");
        mUdpThreadHandler.removeCallbacksAndMessages(null);
    }

    private InetAddress getBroadcastAddress() throws IOException {
        WifiManager manager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = manager.getDhcpInfo();

        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++) {
            quads[k] = (byte) ((broadcast >> k * 8) & 0xff);
        }

        return InetAddress.getByAddress(quads);
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        OGConstants.dbToast(this, "Starting UDP Beacon");

        udpLooperThread.start();
        mUdpThreadHandler = new Handler(udpLooperThread.getLooper());


        if (intent != null) {

            WifiManager manager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            String mac = manager.getConnectionInfo().getMacAddress();

            Realm realm = Realm.getDefaultInstance();

            OGDevice device = OGCore.getDeviceAsObject(realm);

            mMessage = String.format("{\"name\": \"%s\", \"location\": \"%s\", \"mac\": \"%s\"}",
                    device.name, device.locationWithinVenue, mac);

            realm.close();

            mPort = intent.getIntExtra("port", OGConstants.UDP_BEACON_PORT);
            mBeaconFreq = intent.getIntExtra("beaconFreq",OGConstants.UDP_BEACON_FREQ);

        } else {

            WifiManager manager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            String mac = manager.getConnectionInfo().getMacAddress();

            Realm realm = Realm.getDefaultInstance();
            OGDevice device = OGCore.getDeviceAsObject(realm);

            mMessage = String.format("{\"name\": \"%s\", \"location\": \"%s\", \"mac\": \"%s\"}",
                    device.name, device.locationWithinVenue, mac);

            realm.close();

            mPort = OGConstants.UDP_BEACON_PORT;
            mBeaconFreq = OGConstants.UDP_BEACON_FREQ;

        }

        Log.d(TAG, mMessage + " " + mPort + " " + mBeaconFreq);

        //startBeacon();
        startBeaconLooper();

        OGCore.sendStatusIntent("STATUS", "Starting beacon", OGConstants.BootState.UDP_START.getValue());

        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        stopBeacon();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private class UDPLooperThread extends Thread {
        public Handler mHandler;
        private static final String TAG = "UDPLooperThread";

        public void run() {
            Looper.prepare();

            mHandler = new Handler() {
                public void handleMessage(Message msg) {
                    Log.d(TAG, msg.toString());
                }
            };

            Looper.loop();
        }
    }

}
