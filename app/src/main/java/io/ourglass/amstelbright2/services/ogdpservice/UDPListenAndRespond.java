package io.ourglass.amstelbright2.services.ogdpservice;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import io.ourglass.amstelbright2.core.ABApplication;
import io.ourglass.amstelbright2.core.OGSystem;

/**
 * Created by ethan on 8/3/16.
 */
public class UDPListenAndRespond extends Service {

    private String TAG = "UDPListenAndRespond";

    private String mMessage;
    private int mPort;

    private DatagramSocket mSocket;
    private Thread udpListenThread;
    private boolean threadAlive;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onCreate(){
        Log.d(TAG, "onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        ABApplication.dbToast(this, "starting udp listener");

        //get port
        mPort = intent != null ? intent.getIntExtra("port", 9091) :
                9091;

        threadAlive = true;
        udpListenThread = new Thread(new Runnable(){
            public void run(){
                while(threadAlive){
                    try {
                        listenForUDPBroadcast();
                    } catch(java.io.IOException e){
                        Log.e(TAG, "listen errored: " + e.getMessage());
                    }
                }
                Log.v(TAG, "Listen and Respond thread has been killed");
            }
        });
        udpListenThread.start();

        return Service.START_STICKY;
    }

    private void listenForUDPBroadcast() throws java.io.IOException{
        //TODO: 1500 came from an example, don't know why this many bytes would be needed
        byte[] buffer = new byte[1500];
        if(mSocket == null || mSocket.isClosed()){
            mSocket = new DatagramSocket(mPort);
            mSocket.setBroadcast(true);
        }

        DatagramPacket receivedPacket = new DatagramPacket(buffer, buffer.length);
        Log.v( TAG, "UDP listener sitting on port: "+mPort);
        mSocket.receive(receivedPacket);

        Log.v(TAG, "received UDP packet from " + receivedPacket.getAddress() + " "
                + new String(receivedPacket.getData()).trim());

        //TODO: add some parsing of the packet before issuing a response
        respondToUDP(receivedPacket.getAddress());
    }

    private void respondToUDP(InetAddress receivedFrom){
        try {
            mMessage = OGSystem.getSystemInfo().toString();
//            if (mMessage==null){
//                mMessage = "{ \"error\": \"Invalid system data\"";
//            }
            DatagramPacket packet = new DatagramPacket(mMessage.getBytes(), mMessage.length(), receivedFrom, mPort);
            mSocket.send(packet);
        } catch (IOException e){
            Log.e(TAG, "Couldn't respond to " + receivedFrom + " - " + e.getMessage());
        }
    }

    public void onDestroy(){
        Log.d(TAG, "onDestroy");
        threadAlive = false;
    }
}
