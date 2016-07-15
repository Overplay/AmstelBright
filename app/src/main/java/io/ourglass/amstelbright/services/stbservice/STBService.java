package io.ourglass.amstelbright.services.stbservice;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import io.ourglass.amstelbright.core.OGConstants;
import io.ourglass.amstelbright.core.OGCore;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class STBService extends Service {

    public static final String TAG = "STB";
    public static final boolean VERBOSE = true;

    int mTVPollInterval = OGConstants.TV_POLL_INTERVAL;

    HandlerThread mTVPollThread = new HandlerThread("tvPollLooper");
    private Handler mTVThreadHandler;

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

        OGConstants.dbToast(this, "STB: onStartCommand");

        mTVPollThread.start();
        mTVThreadHandler = new Handler(mTVPollThread.getLooper());

        startSTBPolling();

        return mStartMode;
    }


    /**
     * Called when The service is no longer used and is being destroyed
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        OGConstants.dbToast(this, "STB: onDestroy");

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
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

    private void pollSTB() {

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

                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

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
