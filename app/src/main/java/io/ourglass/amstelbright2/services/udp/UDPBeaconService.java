package io.ourglass.amstelbright2.services.udp;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import io.ourglass.amstelbright2.core.ABApplication;
import io.ourglass.amstelbright2.core.OGConstants;
import io.ourglass.amstelbright2.core.OGCore;
import io.ourglass.amstelbright2.core.OGSystem;

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

    private void logd(String message) {
        if (VERBOSE) {
            Log.d(TAG, message);
        }
    }

    private void sendUDPPacket() {


        mMessage = OGSystem.getSystemInfo().toString();


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

        ABApplication.dbToast(this, "Starting UDP Beacon");

        udpLooperThread.start();
        mUdpThreadHandler = new Handler(udpLooperThread.getLooper());


        mMessage = OGSystem.getSystemInfo().toString();

        mPort = OGConstants.UDP_BEACON_PORT;
        mBeaconFreq = OGConstants.UDP_BEACON_FREQ;


        Log.d(TAG, mMessage + " " + mPort + " " + mBeaconFreq);

        startBeaconLooper();

        OGCore.sendStatusIntent("STATUS", "Starting beacon", OGConstants.BootState.UDP_START.getValue());

        return Service.START_STICKY;
    }

    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        mUdpThreadHandler.removeCallbacksAndMessages(null);
        udpLooperThread.quit();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}
