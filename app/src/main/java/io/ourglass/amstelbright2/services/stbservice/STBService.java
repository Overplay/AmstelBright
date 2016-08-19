package io.ourglass.amstelbright2.services.stbservice;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;

import io.ourglass.amstelbright2.core.ABApplication;
import io.ourglass.amstelbright2.core.OGConstants;
import io.ourglass.amstelbright2.core.OGCore;
import io.ourglass.amstelbright2.realm.OGDevice;
import io.realm.Realm;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static io.ourglass.amstelbright2.core.OGConstants.LOC_PATTERN;


public class STBService extends Service {

    public static final String TAG = "STB";
    public static final boolean VERBOSE = true;

    public static boolean hasSearched = false;
    public static ArrayList<String> foundIps;
    private boolean mListening = false;

    int mTVPollInterval = OGConstants.TV_POLL_INTERVAL;

    HandlerThread mTVPollThread = new HandlerThread("tvPollLooper");
    HandlerThread mTVDiscoveryThread = new HandlerThread("tvDiscoveryLooper");

    private Handler mTVThreadHandler;
    private Handler mTVThreadHandler2;

    public STBService() {
    }

    /**
     * indicates how to behave if the service is killed
     */
    int mStartMode = START_STICKY;

    /**
     * indicates whether onRebind should be used
     */
    boolean mAllowRebind = true;

    private void logd(String message){
        if (VERBOSE){
            Log.d(TAG, message);
        }
    }

    /**
     * Called when the service is being created.
     */
    @Override
    public void onCreate() {

    }

    /**
     * The service is starting, due to a call to startService()
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        ABApplication.dbToast(this, "STB: onStartCommand");

        mTVPollThread.start();
        mTVThreadHandler = new Handler(mTVPollThread.getLooper());

        mTVDiscoveryThread.start();
        mTVThreadHandler2 = new Handler(mTVDiscoveryThread.getLooper());

        startSTBFinding();

        startSTBPolling();

        return mStartMode;
    }


    /**
     * Called when The service is no longer used and is being destroyed
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        ABApplication.dbToast(this, "STB: onDestroy");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    /**
     * starts a looping runnable which discovers directv devices on the network
     */
    private void startSTBFinding(){
        Log.v(TAG, "finding stbs");

        Runnable findRunnable = new Runnable(){

            @Override
            public void run(){
                logd("Checking for STBs on network");
                findSTBs();

                mTVThreadHandler2.postDelayed(this, OGConstants.TV_DISCOVER_INTERVAL);

            }

        };
        mTVThreadHandler2.post(findRunnable);

    }

    private void startSTBPolling() {
        Log.d(TAG, "polling");

        Runnable pollRunnable = new Runnable() {


            @Override
            public void run() {

                logd("Checking for updates on STB");
                pollSTB();


                mTVThreadHandler.postDelayed(this, mTVPollInterval);
            }
        };

        mTVThreadHandler.post(pollRunnable);

    }

    private void findSTBs(){
        try {
            final DatagramSocket mListenSocket = new DatagramSocket(null);
            mListenSocket.setReuseAddress(true);
            SocketAddress sa = new InetSocketAddress(OGConstants.UPNP_UDP_BROADCAST_PORT);
            mListenSocket.bind(sa);
            mListening = true;

            mListenSocket.setSoTimeout(10000);
            final ArrayList<String> foundDeviceList = new ArrayList<String>();
            final ArrayList<String> verifiedDeviceList = new ArrayList<String>();
            new Thread(new Runnable(){
                @Override
                public void run() {
                    try {
                        byte[] buffer = new byte[2048];
                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                        while(mListening) {
                            mListenSocket.receive(packet);
                            String recInfo = new String(packet.getData(), 0, packet.getLength());
                            Matcher match = LOC_PATTERN.matcher(recInfo);
                            if(match.find()){
                                String loc = match.group();
                                loc = loc.substring(loc.indexOf("http"));//, loc.lastIndexOf(":"));
                                if(!foundDeviceList.contains(loc)) {
                                    foundDeviceList.add(loc);
                                }
                            }
                            buffer = new byte[2048];
                            packet = new DatagramPacket(buffer, buffer.length);
                        }
                    } catch (SocketTimeoutException e){
                        mListening = false;
                        mListenSocket.close();
                        new Thread(new Runnable(){
                            @Override
                            public void run(){
                                for(final String ip  : foundDeviceList){
                                    verifyBoxAndAdd(ip, verifiedDeviceList);
                                }
                                hasSearched = true;
                                foundIps = verifiedDeviceList;
                            }
                        }).start();

                    }catch (Exception e){
                        Log.e(TAG, e.getMessage());
                    }

                }
            }).start();

            //now send broadcast
            new Thread(new Runnable(){

                @Override
                public void run() {
                    try {
                        StringBuffer packet = new StringBuffer();

                        for(String packetLine : OGConstants.discoverPacket){
                            packet.append(packetLine);
                        }

                        String toSend = packet.toString();
                        byte[] pk = toSend.getBytes();

                        DatagramPacket out = new DatagramPacket(pk, pk.length, InetAddress.getByName(OGConstants.UPNP_UDP_BROADCAST_ADDR), OGConstants.UPNP_UDP_BROADCAST_PORT);
                        mListenSocket.send(out);

                    } catch(Exception e){
                        e.printStackTrace();
                    }

                }
            }).start();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    /**
     * accesses ip address (through the assumed directv api) and if connection is refused returns null,
     * else returns information about the box
     * @param ip the ip address (including path to xml file)
     * @param verifiedDeviceList list to add DirecTV ip addresses to
     */
    private void verifyBoxAndAdd(String ip, ArrayList<String> verifiedDeviceList){
        String baseIp = ip.substring(ip.indexOf("http"), ip.lastIndexOf(":"));
        if(verifiedDeviceList.contains(baseIp))
            return;

        String response = "";
        try {
            URL u = new URL(ip);

            HttpURLConnection huc = (HttpURLConnection)u.openConnection();
            huc.setConnectTimeout(2000);
            huc.connect() ;

            BufferedReader in = new BufferedReader(new InputStreamReader(
                    huc.getInputStream()));

            String inputLine;
            while ((inputLine = in.readLine()) != null){
                response += inputLine;
            }
            huc.disconnect();
            if(response.contains("<manufacturer>DIRECTV</manufacturer>")) {
                verifiedDeviceList.add(baseIp);
            }
        } catch(Exception e){
            Log.e(TAG, e.getMessage());
            return;
        }
    }
    private void pollSTB() {

        Realm realm = Realm.getDefaultInstance();
        String stbAddr = OGDevice.getPairedSTBOrNull(realm);
        if(stbAddr != null) {
            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(OGConstants.STB_ENDPOINT)
                    .build();

            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    Log.wtf(TAG, "Couldn't GET from STB!");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                    if (!response.isSuccessful())
                        throw new IOException("Unexpected code " + response);

                    //                Headers responseHeaders = response.headers();
                    //                for (int i = 0, size = responseHeaders.size(); i < size; i++) {
                    //                    System.out.println(responseHeaders.name(i) + ": " + responseHeaders.value(i));
                    //                }
                    //
                    //                System.out.println(response.body().string());

                    // TODO feels brittle, need more error checking
                    String resJson = response.body().string();
                    try {

                        JSONObject direcTVJson = new JSONObject(resJson);
                        //                    OGCore.channel = (direcTVJson.getString("callsign"));
                        //                    OGCore.programId = (direcTVJson.getString("programId"));
                        //                    OGCore.programTitle= (direcTVJson.getString("title"));
                        OGCore.setChannelInfo(direcTVJson.getString("callsign"),
                                direcTVJson.getString("programId"),
                                direcTVJson.getString("title"));

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }



}
