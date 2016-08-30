package io.ourglass.amstelbright2.services.applejack_comm;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;

import io.ourglass.amstelbright2.core.ABApplication;
import io.ourglass.amstelbright2.realm.OGLog;
import io.ourglass.amstelbright2.services.cloudscraper.OGTweetScraper;
import io.realm.Realm;
import io.realm.RealmResults;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LogCleanAndPushService extends Service {
    static final String TAG = "LogCleanAndPush";

    HandlerThread mWorkerThread = new HandlerThread("cleanAndPush");
    private Handler mWorkerThreadHandler;

    public LogCleanAndPushService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        ABApplication.dbToast(this, "Starting Cloud Proxy");

        mWorkerThread.start();
        mWorkerThreadHandler = new Handler(mWorkerThread.getLooper());

        startLoop();

        return Service.START_STICKY;
    }

    private void startLoop(){
        Log.d(TAG, "starting log reaping");

        Runnable run = new Runnable(){
            @Override
            public void run(){
                Log.d(TAG, "In log reaping runnable");

                Realm realm = Realm.getDefaultInstance();
                RealmResults<OGLog> logs = realm.where(OGLog.class).findAll();
                if(logs.size() != 0){
                    uploadLogs(logs);
                }
                else {
                    Log.v(TAG, "There are no logs to upload");
                }

                realm.close();

                //todo replace this with constant or settings, right now set to 15 minutes
                mWorkerThreadHandler.postDelayed(this, 1000 * 60 * 15);

            }

        };
        mWorkerThreadHandler.post(run);
    }

    private void uploadLogs(RealmResults<OGLog> logs){
        OGLog[] logArr = logs.toArray(new OGLog[logs.size()]);
        int numReaped = 0;

        final Realm realm = Realm.getDefaultInstance();
        final OkHttpClient client = new OkHttpClient();
        final MediaType type = MediaType.parse("application/json");
        for(final OGLog log : logArr){
            String postBody = log.getLogAsJSON().toString();

            //todo replace with OGConstants upon merge with ads branch
            Request request = new Request.Builder()
                    .url("104.131.145.36/OGLog/upload")
                    .post(RequestBody.create(type, postBody))
                    .build();
            try {
                Response response = client.newCall(request).execute();
                if(!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                //this was successful, now going to delete log
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm){
                        log.deleteFromRealm();
                        Log.v(TAG, "successfully reaped log from database");
                    }
                });
                numReaped++;
            } catch (Exception e){
                Log.w(TAG, "there was an error uploading log will not delete");
                //error uploading do not delete this log
            }
        }
        realm.close();
        Log.v(TAG, "Reaped a total of " + numReaped + " logs");
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        mWorkerThreadHandler.removeCallbacksAndMessages(null);
    }

}
