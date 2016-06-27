package io.ourglass.amstelbright.services.stbservice;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

import io.ourglass.amstelbright.core.OGConstants;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class STBService extends Service {

    public static final String TAG = "STB";

    public boolean mPolling = true;
    public int mPollInterval = 5000;

    // For debug toasts
    public static final Boolean DEBUG = false;

    //Context mContext = getApplicationContext();
    final Messenger mMessenger = new Messenger(new IncomingHandler());

    // This is here as a reliable ref to context for the Realm stuff.
    public static Context context;

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

        dbToastr("STB: onStartCommand");

        // Create global static ref for code that needs Context
        context = getApplicationContext();
        //This is commented out because of odd failures
        //startPolling();

        return mStartMode;
    }

    /**
     * A client is binding to the service with bindService()
     */
    @Override
    public IBinder onBind(Intent intent) {
        dbToastr("ABS: binding");
        return mMessenger.getBinder();
    }

    /**
     * Called when all clients have unbound with unbindService()
     */
    @Override
    public boolean onUnbind(Intent intent) {
        super.onUnbind(intent);
        dbToastr("STB: unbinding");
        return mAllowRebind;
    }


    /**
     * Called when The service is no longer used and is being destroyed
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        dbToastr("ABS: onDestroy");

    }


    private void startPolling() {
        Log.d(TAG, "polling");

        Thread STBPollingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (mPolling) {
                    Log.d(TAG, "polling STB");
                    pollSTB();
                    try {
                        Thread.sleep(mPollInterval);
                    } catch (InterruptedException e) {
                        Log.e(TAG, e.getLocalizedMessage());
                    }
                }
            }
        });

        STBPollingThread.start();
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
//                String resJson = response.body().string();
//                try {
//
////                    JSONObject direcTVJson = new JSONObject(resJson);
////                    OGCore.setChannel(direcTVJson.getString("callsign"));
////                    OGCore.setProgramId(direcTVJson.getString("programId"));
////                    OGCore.getInstance().setProgramTitle(direcTVJson.getString("title"));
//
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
            }
        });
    }


    // TODO: Seriously with the leaks? This code is right from the Google site. FCOL
    private class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
//                case 1:
//                    Toast.makeText(getApplicationContext(), "hello!", Toast.LENGTH_SHORT).show();
//                    break;
//                case 2:
//                    Toast.makeText(getApplicationContext(), "beer thirty!", Toast.LENGTH_SHORT).show();
//                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }


    private void dbToastr(String msg) {

        if (DEBUG) {
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        }

    }
}
