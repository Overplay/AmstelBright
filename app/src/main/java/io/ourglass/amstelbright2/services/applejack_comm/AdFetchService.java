package io.ourglass.amstelbright2.services.applejack_comm;

import android.app.Service;
import android.content.Intent;
import android.media.Image;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import io.ourglass.amstelbright2.core.ABApplication;
import io.ourglass.amstelbright2.core.OGConstants;
import io.ourglass.amstelbright2.realm.OGAdvertisement;
import io.realm.Realm;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by ethan on 8/25/16.
 */

public class AdFetchService extends Service {
    public final String TAG = "AdFetchService";

    HandlerThread mScrapeThread = new HandlerThread("AdFetchLooper");
    private Handler mScrapeThreadHandler;

    private final OkHttpClient client = new OkHttpClient();

    private void getAndSaveAds(){

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void startAdScraping(){
        Log.d(TAG, "starting ad scraping");

        Runnable run = new Runnable(){
            @Override
            public void run(){
                Log.d(TAG, "In ad scraping runnable");

                //todo replace this with OGConstants
                String url = OGConstants.ASAHI_ADDRESS + OGConstants.ASAHI_API_ENDPOINT +"ad";

                Request request = new Request.Builder()
                        .url(url)
                        .get()
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e(TAG, "Error, could not get ads from Asahi/Applejack. " + e.getMessage());
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        Realm realm = Realm.getDefaultInstance();
                        String responseString = response.body().string();
                        try {
                            JSONArray resArray = new JSONArray(responseString);

                            //create an arraylist to capture the OGAd objects created from the response
                            ArrayList<OGAdvertisement> receivedAds = new ArrayList<OGAdvertisement>();

                            //iterate through the responded ad array
                            for(int i = 0; i < resArray.length(); i++){
                                JSONObject adJSON = (JSONObject) resArray.get(i);
                                String adId = adJSON.getString("id");

                                //create a new ad for the Advertisement id
                                OGAdvertisement newAd = new OGAdvertisement(adId);

                                //iterate through the text ads on the JSON array and incrementally add them to the newAd
                                JSONArray textAds = adJSON.getJSONArray("text");
                                for(int j = 0; j < textAds.length(); j++){
                                    try {
                                        //may throw class cast exception if text array contains something other than strings
                                        String ad = (String) textAds.get(i);
                                        newAd.setNextText(ad);
                                    } catch(Exception e){
                                        Log.w(TAG, "There was a problem retrieving string from text ad array, ignoring");
                                    }
                                }

                                JSONObject mediaJSON = adJSON.getJSONObject("media");

                                //fetch and add the widget image to the OGAdd
                                try {
                                    String widgetImgId = mediaJSON.getString("widget");
                                    byte[] img = imageFetcher(OGConstants.ASAHI_ADDRESS + OGConstants.ASAHI_MEDIA_ENDPOINT + widgetImgId);

                                    newAd.widgetImg = img;
                                } catch(JSONException e){
                                    Log.w(TAG, "There was no widget image associated with the ad");
                                    newAd.widgetImg = null;
                                }

                                //fetch and add the crawler image to the OGAd
                                try {
                                    String crawlerImgId = mediaJSON.getString("crawler");
                                    byte[] img = imageFetcher(OGConstants.ASAHI_ADDRESS + OGConstants.ASAHI_MEDIA_ENDPOINT + crawlerImgId);

                                    newAd.crawlerImg = img;
                                } catch (JSONException e){
                                    Log.w(TAG, "There was no crawler image associated with the ad");
                                    newAd.crawlerImg = null;
                                }

                                //add the newAd to the arraylist
                                receivedAds.add(newAd);
                            }
                            //copy or update the ad objects created from the response
                            realm.beginTransaction();
                            realm.copyToRealmOrUpdate(receivedAds);
                            realm.commitTransaction();
                        } catch(JSONException e){
                            Log.e(TAG, "Response of ads from Applejack/asahi seems to have been malformatted");
                        }
                    }
                });
            }
        };
    }

    /**
     * retrieves an image from designated url and returns it in a byte[]
     * @param path path to the image
     * @return a byte[] representing the image
     * @throws IOException if the URL is malformatted or can't read from the url
     */
    @Nullable
    private byte[] imageFetcher(String path) throws IOException {
        URL url = new URL(path);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        InputStream in = null;
        try {
            in = url.openStream();

            byte[] buff = new byte[4096];
            int n;

            while ((n = in.read(buff)) > 0) {
                out.write(buff, 0, n);
            }
        } catch(IOException e){
            Log.w(TAG, "error reading in the designated image. " + e.getMessage());
            return null;
        } finally {
            if(in != null)
                in.close();
        }
        return out.toByteArray();
    }

    @Override
    public void onDestroy(){
        Log.d(TAG, "onDestroy");

        mScrapeThreadHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        ABApplication.dbToast(this, "Starting ad scraper");

        mScrapeThread.start();
        mScrapeThreadHandler = new Handler(mScrapeThread.getLooper());

        startAdScraping();

        return Service.START_STICKY;
    }
}
