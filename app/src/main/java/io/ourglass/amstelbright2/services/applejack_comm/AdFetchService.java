package io.ourglass.amstelbright2.services.applejack_comm;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

/**
 * Created by ethan on 8/25/16.
 */

public class AdFetchService extends Service {

    public final String TAG = "AdFetchService";
    private final OkHttpClient client = ABApplication.okclient;  // share it
    HandlerThread mScrapeThread = new HandlerThread("AdFetchLooper");
    private Handler mScrapeThreadHandler;

    private void getAndSaveAds() {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * starts the ad scraping thread which is set to run every 5 minutes
     * defines the thread to get all ads from applejack and then stores them in realm
     */
    private void startAdScraping() {
        Log.d(TAG, "starting ad scraping");

        Runnable run = new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "In ad scraping runnable");

                String url = OGConstants.ASAHI_ADDRESS + OGConstants.ASAHI_ACCEPTED_AD_ENDPOINT;

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
                        String responseString = response.body().string();
                        //needed to move this because there was some weird javassit error pertaining to realm and okhttp
                        handleGottenAds(responseString);
                    }
                });

                //todo replace with ogconstants
                mScrapeThreadHandler.postDelayed(this, 1000 * 60 * 5);

            }
        };

        mScrapeThreadHandler.post(run);


    }

    /**
     * function to store the retrieved ads into Realm as OGAdvertisements
     * @param responseString string (stringified json array containing all of the advertisement objects)
     */
    private void handleGottenAds(String responseString) {
        try {
            ArrayList<OGAdvertisement> receivedAds = new ArrayList<OGAdvertisement>();
            JSONArray resArray = new JSONArray(responseString);

            //iterate through the responded ad array
            for (int i = 0; i < resArray.length(); i++) {
                JSONObject adJSON = (JSONObject) resArray.get(i);
                String adId = adJSON.getString("id");

                //create a new ad for the Advertisement id
                OGAdvertisement newAd = new OGAdvertisement(adId);


                //added this for the new ad JSON from Asahi
                JSONObject advertJSON = adJSON.getJSONObject("advert");

                //iterate through the text ads on the JSON array and incrementally add them to the newAd
                JSONArray textAds = advertJSON.getJSONArray("text");
                for (int j = 0; j < textAds.length(); j++) {
                    try {
                        //may throw class cast exception if text array contains something other than strings
                        String ad = (String) textAds.get(j);
                        newAd.setNextText(ad);
                    } catch (Exception e) {
                        Log.w(TAG, "There was a problem retrieving string from text ad array, ignoring (" + e.getMessage() + ")");
                    }
                }


                JSONObject mediaJSON = null;
                try {
                    mediaJSON = advertJSON.getJSONObject("media");
                } catch (JSONException e) {
                    Log.w(TAG, "There seem to be no associated media. This is likely a problem");
                }
                if (mediaJSON != null) {
                    //fetch and add the widget image to the OGAdd
                    try {
                        String widgetImgId = mediaJSON.getString("widget");

                        byte[] img = widgetImgId == null || "null".equals(widgetImgId)
                                ? null
                                : imageFetcher(OGConstants.ASAHI_ADDRESS + OGConstants.ASAHI_MEDIA_ENDPOINT + widgetImgId);

                        newAd.setWidgetImg(img);
//                        newAd.setWidgetURL(OGConstants.ASAHI_ADDRESS + OGConstants.ASAHI_MEDIA_ENDPOINT + widgetImgId);
                    } catch (JSONException e) {
                        Log.w(TAG, "There was no widget image associated with the ad");
                        newAd.setWidgetImg(null);
                    } catch (IOException e) {
                        Log.w(TAG, "There was a problem reading the crawler image from asahi");
                    }

                    //fetch and add the crawler image to the OGAd
                    try {
                        String crawlerImgId = mediaJSON.getString("crawler");

                        byte[] img = crawlerImgId == null || "null".equals(crawlerImgId)
                                ? null
                                : imageFetcher(OGConstants.ASAHI_ADDRESS + OGConstants.ASAHI_MEDIA_ENDPOINT + crawlerImgId);

                        newAd.setCrawlerImg(img);
//                        newAd.setCrawlerURL(OGConstants.ASAHI_ADDRESS + OGConstants.ASAHI_MEDIA_ENDPOINT + crawlerImgId);
                    } catch (JSONException e) {
                        Log.w(TAG, "There was no crawler image associated with the ad");
                        newAd.setCrawlerImg(null);
                    } catch (IOException e) {
                        Log.w(TAG, "There was a problem reading the image from asahi");
                        newAd.setCrawlerImg(null);
                    }

                }
                //add the newAd to the arraylist
                receivedAds.add(newAd);
            }
            //todo this really should replace everything in the database with everything found because right now, old ads will not be removed
            //todo also can update what is above so that if we already have downloaded an associated image, not to do again
            //copy or update the ad objects created from the response
            Realm realm = Realm.getDefaultInstance();
            realm.beginTransaction();
            realm.copyToRealmOrUpdate(receivedAds);
            realm.commitTransaction();
        } catch (JSONException e) {
            Log.e(TAG, "Response of ads from Applejack/asahi seems to have been malformatted. " + e.getMessage());
        }
    }

    /**
     * retrieves an image from designated url and returns it in a byte[]
     *
     * @param path path to the image
     * @return a byte[] representing the image
     * @throws IOException if the URL is malformatted or can't read from the url
     */
    @Nullable
    private byte[] imageFetcher(String path) throws IOException {
        Log.v(TAG, "About to access " + path);
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
        } catch (IOException e) {
            Log.w(TAG, "error reading in the designated image. " + e.getMessage());
            return null;
        } finally {
            if (in != null)
                in.close();
        }
        return out.toByteArray();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");

        mScrapeThreadHandler.removeCallbacksAndMessages(null);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ABApplication.dbToast(this, "Starting ad scraper");

        Log.v(TAG, "In onStartCommand in AdFetchService");

        mScrapeThread.start();
        mScrapeThreadHandler = new Handler(mScrapeThread.getLooper());

        startAdScraping();

        return Service.START_STICKY;
    }
}
