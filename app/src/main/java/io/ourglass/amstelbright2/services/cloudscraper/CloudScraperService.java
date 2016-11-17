package io.ourglass.amstelbright2.services.cloudscraper;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;

import io.ourglass.amstelbright2.core.ABApplication;
import io.ourglass.amstelbright2.core.OGConstants;
import io.ourglass.amstelbright2.realm.OGScraper;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 *  MAK
 *  Some time in July, Two-thousand-and-sixteen
 *
 *  Only does Twitter right now, but could be extended for IG and FB I imagine...
 *
 */
public class CloudScraperService extends Service {

    static final String TAG = "CloudScraper";
    static final boolean VERBOSE = true;

    OGTweetScraper mTweetScraper;

    int mScrapeInterval = OGConstants.CLOUD_SCRAPE_INTERVAL;

    HandlerThread mScrapeThread = new HandlerThread("scrapeLooper");
    private Handler mScrapeThreadHandler;


    private void logd(String message){
        if (VERBOSE){
            Log.d(TAG, message);
        }
    }


    private void startScrapeLooper() {
        Log.d(TAG, "startScrapeLooper");

        // TODO: This feels horribly clunky. Need a way to have a child of HandlerThread to encapsulate
        // and DRY this out.

        Runnable txRunnable = new Runnable() {


            @Override
            public void run() {
                logd("sending UDP packet from looper");

                final Realm realm = Realm.getDefaultInstance();
                RealmResults<OGScraper> mScrapeTasks = realm.where(OGScraper.class).findAll();


                for (final OGScraper s : mScrapeTasks) {
                    String appis = s.appId;
                    Log.d(TAG, "Gonna scrape for " + appis );
                    Log.d(TAG, "Query is: "+s.getQuery());
                    mTweetScraper.getTweets(s.getQuery(), new OGTweetScraper.TwitterDataCallback() {
                        @Override
                        public void results(final String results) {
                            Log.d(TAG+"RESULT", results);
                            // TODO make compatible types
                            mScrapeThreadHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Realm realm = Realm.getDefaultInstance();
                                    realm.beginTransaction();
                                    s.lastUpdate = (int)System.currentTimeMillis();
                                    s.data = results;
                                    realm.commitTransaction();
                                    realm.close();
                                }
                            });


                        }

                        @Override
                        public void error(IOException err) {
                            Log.d(TAG+"FETCH ERR", "Houston, we have a problem");
                        }
                    });
                }

                mScrapeThreadHandler.postDelayed(this, mScrapeInterval);
            }
        };

        mScrapeThreadHandler.post(txRunnable);

    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        ABApplication.dbToast(this, "Starting Cloud Proxy");

        mScrapeThread.start();
        mScrapeThreadHandler = new Handler(mScrapeThread.getLooper());

        mTweetScraper = new OGTweetScraper();
        mTweetScraper.authorize(new OGTweetScraper.TwitterAuthCallback() {
            @Override
            public void authorized(boolean authorized) {
                if (authorized){

                    // TODO this method won't work when we are scraping from multiple services, but for now it's ok
                    Log.d(TAG, "OGTwitter authorized OK, gonna spin up the looper.");
                    startScrapeLooper();
                } else {

                    Log.wtf(TAG, "Could not authorize Twitter.");

                }
            }
        });

        //OGCore.sendStatusIntent("STATUS", "Starting beacon", OGConstants.BootState.UDP_START.getValue());

        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        mScrapeThreadHandler.removeCallbacksAndMessages(null);
        mScrapeThread.quit();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}
