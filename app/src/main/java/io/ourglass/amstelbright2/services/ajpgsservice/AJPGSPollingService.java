package io.ourglass.amstelbright2.services.ajpgsservice;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import io.ourglass.amstelbright2.core.ABApplication;
import io.ourglass.amstelbright2.core.AppSettings;
import io.ourglass.amstelbright2.core.ProgramGuide;
import io.ourglass.amstelbright2.core.TimeHelpers;
import io.ourglass.amstelbright2.realm.OGTVListing;
import io.ourglass.amstelbright2.realm.OGTVStation;
import io.realm.Realm;
import io.realm.RealmResults;


// TODO This service shouldn't run when not paired. It should be started only after a pair
// event occurs, or upon bootup if an existing pair is saved.


public class AJPGSPollingService extends Service {

    static final Boolean DONT_FETCH = false;
    static final String TAG = "AJPGSPollingService";
    static final boolean VERBOSE = true;

     static AJPGSPollingService sInstance;
    public static final String LAST_SYNC_SETTINGS_KEY = "lastPGSSync";

    public static AJPGSPollingService getInstance() {
        return sInstance;
    }

    // TODO: Count failed polls and take a reconnect action if the number of failed polls exceeds a
    // threshold. Several things could cause a disconnect: WiFi is down, etc.

    //public static STBPollStatus lastPollStatus;

    HandlerThread pgsLooperThread = new HandlerThread("pgsLooperThread");
    private Handler mPGSThreadHandler;

    private void logd(String message) {
        if (VERBOSE) {
            Log.d(TAG, message);
        }
    }


    private void stopPoll() {
        Log.d(TAG, "Stopping TV polling.");
    }


    @Override
    public void onCreate() {

        Log.d(TAG, "onCreate");
        sInstance = this;

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        ABApplication.dbToast(this, "Starting AJPGS Polling");

        if (!pgsLooperThread.isAlive()) {
            pgsLooperThread.start();
            mPGSThreadHandler = new Handler(pgsLooperThread.getLooper());
        }

        // HERE TO SAVE API CALL $$$ when debugging other stuff
        if (!DONT_FETCH)
            mPGSThreadHandler.postDelayed(mUpdateGridRunnable, 5000);

        mPGSThreadHandler.postDelayed(mUpdateGridCache, 12000);

        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        mPGSThreadHandler.removeCallbacksAndMessages(null);

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void setLastSyncNow(){
        AppSettings.putString(LAST_SYNC_SETTINGS_KEY, TimeHelpers.utcISOTimeStringWithOffset(0));
    }

    Runnable mUpdateGridCache = new Runnable() {
        @Override
        public void run() {
            //All this does is cache a fairly recent grid.
            Log.d(TAG, "Reloading grid cache");
            Realm realm = Realm.getDefaultInstance();
            ProgramGuide.currentGridForAllStations(realm);
            realm.close();
            mPGSThreadHandler.postDelayed(this, TimeHelpers.TEN_MINUTES_AS_MS);
        }
    };

    Runnable mUpdateGridRunnable = new Runnable() {

        //We need to patch up listings to fix the timestamp and add the stationID
        private JSONArray patchListings(JSONArray input, int stationID) {

            JSONArray rval = new JSONArray();

            for (int l = 0; l < input.length(); l++) {
                try {
                    JSONObject lobj = input.getJSONObject(l);
                    lobj.put("stationID", stationID);
                    String badTime = lobj.getString("listDateTime");
                    String goodTime = badTime.replace(" ", "T") + "Z";
                    lobj.put("listDateTime", goodTime);
                    DateTime startDt = new DateTime(goodTime);
                    DateTime endDt = startDt.plusMinutes(lobj.optInt("duration", 30));
                    lobj.put("endDateTime", endDt.toString());
                    rval.put(lobj);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            return rval;

        }

        @Override
        public void run() {

            Log.d(TAG, "UPDATING FULL GRID");

            JSONArray grid = TVMediaAPI.gridForLineupID("5266D");

            if (grid != null) {
                Log.d(TAG, "There are " + grid.length() + " grid entries");

                Realm realm = Realm.getDefaultInstance();

                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        //TODO the channel update should really look for channels dropped from a lineup
                        //It also has to mindful of those marked as FAVORITES
                        //and only nuke those. For now, no cleanup on stations.
                        //Log.d(TAG, "Deleting all stations, then replacing");
                        //realm.where(OGTVStation.class).findAll().deleteAllFromRealm();
                        Log.d(TAG, "Deleting crufty listings");
                        RealmResults<OGTVListing> oldListings = realm.where(OGTVListing.class)
                                .lessThan("endDateTime", new Date()).findAll();
                        Log.d(TAG, "Found "+oldListings.size()+" listings that are stale. Nuking.");
                        oldListings.deleteAllFromRealm();
                    }
                });

                // iterate the grid
                for (int j = 0; j < grid.length(); j++) {
                    Log.d(TAG, "Shoving grid entry "+j+" into Realm.");
                    try {
                        JSONObject gridEntry = grid.getJSONObject(j);
                        final JSONObject station = gridEntry.getJSONObject("channel");
                        JSONArray listings = gridEntry.getJSONArray("listings");
                        int stationID = station.getInt("stationID");
                        final JSONArray cleanListings = patchListings(listings, stationID);
                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                realm.createOrUpdateObjectFromJson(OGTVStation.class, station);
                                realm.createOrUpdateAllFromJson(OGTVListing.class, cleanListings);
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                ProgramGuide.currentGridForAllStations(realm); //tickle the cache
                realm.close();

            } else {
                Log.wtf(TAG, "Got null listings grid!!!");
            }

            setLastSyncNow();

            mPGSThreadHandler.postDelayed(this, TimeHelpers.ONE_HOUR_AS_MS);

        }
    };


}


/* THIS STUFF PROBABLY WON'T END UP BEING USED, BUT KEPT IN CASE WE NEED IT */

//    Runnable mUpdateStationsRunnable = new Runnable() {
//        @Override
//        public void run() {
//
//            Log.d(TAG, "UPDATING ALL STATIONS");
//
//            // Hardcoded to DirecTV Bay Area
//            JSONObject lineup = TVMediaAPI.lineupWithStations("5266D");
//
//            if (lineup != null) {
//
//                Realm realm = Realm.getDefaultInstance();
//
//                try {
//                    final JSONArray stations = lineup.getJSONArray("stations");
//                    realm.executeTransaction(new Realm.Transaction() {
//                        @Override
//                        public void execute(Realm realm) {
//                            realm.createOrUpdateAllFromJson(OGTVStation.class, stations);
//                        }
//                    });
//
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                } finally {
//                    realm.close();
//                }
//
//            } else {
//                Log.d(TAG, "Failure to get channel lineup!");
//            }
//        }
//    };


//    Runnable pgsSyncRunnable = new Runnable() {
//
//        int providerId = OGConstants.AJPGS_DIRECTV_PROVIDER_ID;
//
//        private String today() {
//
//            Date d = new Date();
//            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//            return sdf.format(d);
//
//        }
//
//        private int getNumPages() {
//
//
//            try {
//                //TODO hardcoded for South Bay, specifically Campbell
//                Request req = new Request.Builder()
//                        .url(OGConstants.AJPGS_BASE + "/lineup/pages?zip=95008&providerID=" + providerId)
//                        .build();
//
//                Log.d(TAG, "checking number of pages on AJPGS");
//                Response response = mClient.newCall(req).execute();
//
//                if (response.isSuccessful()) {
//                    JSONObject json = new JSONObject(response.body().string());
//                    return json.optInt("count", -1);
//                }
//
//            } catch (IOException e) {
//                Log.e(TAG, "IO Exception getting channel info");
//            } catch (JSONException e) {
//                Log.e(TAG, "JSON Exception getting channel info");
//            } catch (Exception e) {
//                Log.e(TAG, "Exception getting channel info");
//            }
//
//            return -1;
//        }
//
//        private JSONArray getPage(int pageNum) {
//
//            try {
//                //TODO hardcoded for South Bay, specifically Campbell
//                Request req = new Request.Builder()
//                        .url(OGConstants.AJPGS_BASE + "/lineup/getPage?zip=95008&providerID="
//                                + providerId + "&page=" + pageNum)
//                        .build();
//                Log.d(TAG, "Fecthing page " + pageNum + " of the progrmam guide");
//                Response response = mClient.newCall(req).execute();
//
//                if (response.isSuccessful()) {
//                    JSONArray jsonA = new JSONArray(response.body().string());
//                    return jsonA;
//                }
//
//            } catch (IOException e) {
//                Log.e(TAG, "IO Exception getting program page");
//            } catch (JSONException e) {
//                Log.e(TAG, "JSON Exception getting program page");
//            } catch (Exception e) {
//                Log.e(TAG, "Exception getting program page");
//            }
//
//            return null;
//
//        }
//
//        private void processPage(final JSONArray jsonArray) {
//
//
////            realm.executeTransaction(new Realm.Transaction() {
////                @Override
////                public void execute(Realm realm) {
////                    realm.createOrUpdateAllFromJson(OGProgram.class, jsonArray);
////                }
////            });
//
//            if (jsonArray == null) {
//                Log.e(TAG, "Got a bad (null) jsonArray tying to process page!!");
//                return;
//            }
//
//            Realm realm = Realm.getDefaultInstance();
//
//            for (int i = 0; i < jsonArray.length(); i++) {
//
//                try {
//                    JSONObject job = jsonArray.getJSONObject(i);
//                    OGProgram.addOrUpdate(realm, job);
//
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//
//            }
//
//            realm.close();
//
//        }
//
//        @Override
//        public void run() {
//
//            logd("Program Guide Update Loop");
//
//            int numPages = getNumPages();
//            logd("Number of pages of guide data: " + numPages);
//
//            if (numPages <= 0) {
//                mPGSThreadHandler.postDelayed(this, 5000);
//                Log.e(TAG, "Got bad number of pages, going to try again in 5 seconds.");
//            } else {
//
//                int programCount = 0;
//                for (int i = 0; i < numPages; i++) {
//                    processPage(getPage(i));
//                    try {
//                        Thread.sleep(5000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//
//                mPGSThreadHandler.postDelayed(this, 60 * 60 * 1000);
//            }
//
//
//        }
//    };

//    Runnable mUpdateListingsRunnable = new Runnable() {
//
//        //We need to patch up listings to fix the timestamp and add the stationID
//        private JSONArray patchListings(JSONArray input, int stationID) {
//
//            JSONArray rval = new JSONArray();
//
//            for (int l = 0; l < input.length(); l++) {
//                try {
//                    JSONObject lobj = input.getJSONObject(l);
//                    lobj.put("stationID", stationID);
//                    String badTime = lobj.getString("listDateTime");
//                    String goodTime = badTime.replace(" ", "T") + "Z";
//                    lobj.put("listDateTime", goodTime);
//                    rval.put(lobj);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//
//            return rval;
//
//        }
//
//        @Override
//        public void run() {
//
//            Log.d(TAG, "UPDATING ALL LISTINGS");
//
//            Realm realm = Realm.getDefaultInstance();
//
//            RealmResults<OGTVStation> stations = realm.where(OGTVStation.class).findAll()
//                    .sort("favorite", Sort.DESCENDING);
//
//            if (stations != null) {
//
//                Log.d(TAG, "There are " + stations.size() + " stations to get listings for.");
//                for (OGTVStation currentStation : stations) {
//
//                    int stationID = currentStation.stationID;
//
//                    Log.d(TAG, "Getting listings for: " + currentStation.name);
//                    JSONArray listings = TVMediaAPI.listingsForNext4HoursForStationID(currentStation.stationID);
//                    if (listings != null) {
//
//                        Log.d(TAG, "There are " + listings.length() + " listings for " + currentStation.name);
//                        final JSONArray cleanListings = patchListings(listings, stationID);
//
//                        realm.executeTransaction(new Realm.Transaction() {
//                            @Override
//                            public void execute(Realm realm) {
//                                realm.createOrUpdateAllFromJson(OGTVListing.class, cleanListings);
//
//                            }
//                        });
//
//                    } else {
//                        Log.wtf(TAG, "Got null listings!!!");
//                    }
//                }
//
//            } else {
//                Log.wtf(TAG, "There are no stations to get listings for!");
//            }
//
//            realm.close();
//
//        }
//    };
//
///**
// * All the sync logic is in this object
// */

