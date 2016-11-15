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

    final OkHttpClient client = ABApplication.okclient;  // share it

    public LogCleanAndPushService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        ABApplication.dbToast(this, "Log Process Starting");

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

                //find all logs that haven't been uploaded
                RealmResults<OGLog> logs = realm.where(OGLog.class).equalTo("uploaded", -1L).findAll();
                if(logs.size() != 0){
                    uploadLogs(logs);
                }
                else {
                    Log.v(TAG, "There are no logs to upload");
                }

                logs = getOldLogs();

                if(logs.size() != 0) {
                    realm.beginTransaction();
                    reapLogs(logs);
                    realm.commitTransaction();
                }
                else
                    Log.v(TAG, "There are no logs ready to be reaped");

                realm.close();

                //todo replace this with constant or settings, right now set to 15 minutes
                mWorkerThreadHandler.postDelayed(this, 1000 * 60 * 15);

            }

        };
        mWorkerThreadHandler.post(run);
    }

    public RealmResults<OGLog> getOldLogs(){
        //todo in the future, change week calculation to configurable setting

        Realm realm = Realm.getDefaultInstance();

        long weekAgo = System.currentTimeMillis() - (1000 * 60 * 60 * 24 * 7);
        RealmResults<OGLog> oldLogs = realm.where(OGLog.class).lessThan("uploaded", weekAgo).findAll();

        return oldLogs;
    }

    public void uploadLogs(RealmResults<OGLog> logs){
        OGLog[] logArr = logs.toArray(new OGLog[logs.size()]);
        int numUploaded = 0;

        final Realm realm = Realm.getDefaultInstance();
        final MediaType type = MediaType.parse("application/json");
        for(final OGLog log : logArr){
            String postBody = log.getLogAsJSON().toString();

            RequestBody body = RequestBody.create(type, postBody);
            Log.v(TAG, postBody);

            //todo replace with OGConstants upon merge with ads branch
            Request request = new Request.Builder()
                    .url("http://104.131.145.36/OGLog/upload")
                    .post(body)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                if(!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                Log.v(TAG, "successfully uploaded log to Asahi, will now mark the upload time");
                realm.beginTransaction();
                log.setUploaded(System.currentTimeMillis());
                realm.commitTransaction();

                numUploaded++;
            } catch (Exception e){
                Log.w(TAG, "there was an error uploading log (" + e.getMessage() + "), will not mark as uploaded");
            }
        }
        realm.close();
        Log.v(TAG, "Uploaded a total of " + numUploaded + " logs");
    }

    /**
     * NOTE: make sure that you are inside a transaction
     * @param logs OGLogs to delete
     */
    public void reapLogs(RealmResults<OGLog> logs){
        int numToReap = logs.size();
        logs.deleteAllFromRealm();

        Log.v(TAG, "Successfully reaped " + numToReap + " old logs from the database");
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        mWorkerThreadHandler.removeCallbacksAndMessages(null);
    }

}
