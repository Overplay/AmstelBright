package io.ourglass.amstelbright2.services.applejack_comm;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;

import org.json.JSONObject;

import java.io.IOException;

import io.ourglass.amstelbright2.core.ABApplication;
import io.ourglass.amstelbright2.core.OGConstants;
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

        if (!mWorkerThread.isAlive()){
            mWorkerThread.start();
            mWorkerThreadHandler = new Handler(mWorkerThread.getLooper());
        }

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
                RealmResults<OGLog> logs = realm.where(OGLog.class)
                        .equalTo("uploadedAt", 0).findAll();

                if(logs.size() != 0){
                    uploadLogs(realm, logs);
                }
                else {
                    Log.v(TAG, "There are no logs to upload");
                }


                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        long weekAgo = System.currentTimeMillis() - (1000 * 60 * 60 * 24 * 7);
                        RealmResults<OGLog> oldLogs = realm.where(OGLog.class)
                                .lessThan("uploadedAt", weekAgo).findAll();
                        oldLogs.deleteAllFromRealm();
                    }
                });

                realm.close();

                //TODO replace this with constant or settings, right now set to 15 minutes
                mWorkerThreadHandler.postDelayed(this, OGConstants.LOG_UPLOAD_INTERVAL);

            }

        };
        mWorkerThreadHandler.post(run);
    }


    public void uploadLogs(Realm realm, RealmResults<OGLog> logs){

        int numUploaded = 0;

        final MediaType type = MediaType.parse("application/json");

        for(final OGLog log : logs){

            JSONObject logJSON = log.toJson();

            String postBody = logJSON.toString();

            RequestBody body = RequestBody.create(type, postBody);
            Log.v(TAG, postBody);

            //todo replace with OGConstants upon merge with ads branch
            Request request = new Request.Builder()
                    .url(OGConstants.ASAHI_ADDRESS + "/OGLog/upload")
                    .post(body)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                if(!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                Log.v(TAG, "successfully uploaded log to Asahi, will now mark the upload time");
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        log.setUploaded();
                    }
                });

                numUploaded++;
            } catch (Exception e){
                Log.w(TAG, "there was an error uploading log (" + e.getMessage() + "), will not mark as uploaded");
            }
        }

        Log.v(TAG, "Uploaded a total of " + numUploaded + " logs");

    }


    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        mWorkerThreadHandler.removeCallbacksAndMessages(null);
    }

}
