package io.ourglass.amstelbright2.services.stbservice;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import io.ourglass.amstelbright2.core.ABApplication;
import io.ourglass.amstelbright2.core.OGConstants;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by mkahn on 11/11/16.
 */

public class DirecTVAPI {

    public static final String TAG = "DirecTVAPI";

    public static OkHttpClient mClient = ABApplication.okclient.newBuilder()
            .connectTimeout(OGConstants.DIRECTV_API_CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS)
            .writeTimeout(OGConstants.DIRECTV_API_CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS)
            .readTimeout(OGConstants.DIRECTV_API_CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS)
            .build();

    public static ArrayList<DirecTVSetTopBox> availableSystems = new ArrayList<>();

    public interface DirecTVAPIInterface {
        public void availableSystems(ArrayList<SetTopBox> systems);
    }

    public static void enumerate(){


    }

    public static JSONObject whatsOn(String ipAddress){

        JSONObject rval = null;

        try {
            Request req = new Request.Builder()
                    .url(ipAddress + ":" + OGConstants.DIRECTV_PORT + OGConstants.DIRECTV_CHANNEL_GET_ENDPOINT)
                    .build();
            Log.d(TAG, "checking channel info on "+ipAddress);
            Response response = mClient.newCall(req).execute();

            if(response.isSuccessful()) {
                rval = new JSONObject(response.body().string());
            }

        } catch (IOException e){
            Log.e(TAG, "IO Exception getting channel info");
        } catch (JSONException e){
            Log.e(TAG, "JSON Exception getting channel info");
        } catch (Exception e){
            Log.e(TAG, "Exception getting channel info");
        }

        return rval;
    }


    private void findSTBs(){

//        try {
//
//            final DatagramSocket mListenSocket = new DatagramSocket(null);
//            mListenSocket.setReuseAddress(true);
//            SocketAddress sa = new InetSocketAddress(UPNP_UDP_BROADCAST_PORT);
//            mListenSocket.bind(sa);
//            mListening = true;
//
//            mListenSocket.setSoTimeout(10000);
//            final ArrayList<String> foundDeviceList = new ArrayList<String>();
//            final ArrayList<String> verifiedDeviceIps = new ArrayList<String>();
//
//            new Thread(new Runnable(){
//                @Override
//                public void run() {
//
//                    try {
//
//                        byte[] buffer = new byte[2048];
//                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
//                        while(mListening) {
//                            mListenSocket.receive(packet);
//                            String recInfo = new String(packet.getData(), 0, packet.getLength());
//                            Matcher match = LOC_PATTERN.matcher(recInfo);
//                            if(match.find()){
//                                String loc = match.group();
//                                loc = loc.substring(loc.indexOf("http"));//, loc.lastIndexOf(":"));
//                                if(!foundDeviceList.contains(loc)) {
//                                    foundDeviceList.add(loc);
//                                }
//                            }
//                            buffer = new byte[2048];
//                            packet = new DatagramPacket(buffer, buffer.length);
//                        }
//                    } catch (SocketTimeoutException e){
//                        mListening = false;
//                        mListenSocket.close();
//                        new Thread(new Runnable(){
//                            @Override
//                            public void run(){
//                                ArrayList<STBService.DirectvBoxInfo> newFoundBoxes = new ArrayList<STBService.DirectvBoxInfo>();
//                                for(final String ip  : foundDeviceList){
//                                    verifyBoxAndAdd(ip, newFoundBoxes);
//                                }
//
//                                hasSearched = true;
//                                while(foundBoxes.size() > 0){
//                                    foundBoxes.remove(0);
//                                }
//                                for(STBService.DirectvBoxInfo info : newFoundBoxes){
//                                    foundBoxes.add(info);
//                                }
//                            }
//                        }).start();
//
//                    }catch (Exception e){
//                        Log.e(TAG, e.getMessage());
//                    }
//
//                }
//            }).start();
//
//            //now send broadcast
//            new Thread(new Runnable(){
//
//                @Override
//                public void run() {
//                    try {
//                        StringBuffer packet = new StringBuffer();
//
//                        for(String packetLine : OGConstants.discoverPacket){
//                            packet.append(packetLine);
//                        }
//
//                        String toSend = packet.toString();
//                        byte[] pk = toSend.getBytes();
//
//                        DatagramPacket out = new DatagramPacket(pk, pk.length, InetAddress.getByName(OGConstants.UPNP_UDP_BROADCAST_ADDR), OGConstants.UPNP_UDP_BROADCAST_PORT);
//                        mListenSocket.send(out);
//
//                    } catch(Exception e){
//                        e.printStackTrace();
//                    }
//
//                }
//            }).start();
//        } catch (SocketException e) {
//            e.printStackTrace();
//        }
    }
}
