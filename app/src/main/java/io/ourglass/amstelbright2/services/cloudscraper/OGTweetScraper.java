package io.ourglass.amstelbright2.services.cloudscraper;

import android.util.Base64;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import io.ourglass.amstelbright2.core.ABApplication;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by mkahn on 6/7/16.
 */
public class OGTweetScraper {

    private static final String TAG = "OGTweetScraper";

    private static final String CONSUMER_KEY = "ZKGjeMcDZT3BwyhAtCgYtvrb5";
    private static final String CONSUMER_SECRET = "iXnv6zwFfvHzZr0Y8pvnEJM9hPT0mYV1HquNCzbPrGb5aHUAtk";
    private static final String CONCAT_KEYS = CONSUMER_KEY+":"+CONSUMER_SECRET;
    private static final String B64_CONCAT = Base64.encodeToString(CONCAT_KEYS.getBytes(), Base64.NO_WRAP);

    private final OkHttpClient client = ABApplication.okclient;  // share it

    private String mToken;

    public interface TwitterAuthCallback {

        void authorized(boolean authorized);

    }

    public interface TwitterDataCallback {
        void results(String results);
        void error(IOException err);
    }

    public OGTweetScraper(){

    }

    public boolean mIsAuthorized = false;

    public void authorize(final TwitterAuthCallback cb){

        //grant_type=client_credentials
        RequestBody formBody = new FormBody.Builder()
                .add("grant_type", "client_credentials")
                .build();

        String auth = "Basic "+B64_CONCAT;

        Request request = new Request.Builder()
                .header("Authorization" , auth)
                .header("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8")
                .url("https://api.twitter.com/oauth2/token")
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {


            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                Log.e(TAG, "Could not authorize with Twitter");

                mIsAuthorized = false;

                if (cb!=null)
                    cb.authorized(false);
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {

                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                Log.d(TAG, "Twitter auth successful");

                String jString = response.body().string();

                try {
                    JSONObject robj = new JSONObject(jString);
                    mToken = robj.getString("access_token");
                    mIsAuthorized = true;
                    if (cb!=null) cb.authorized(true);
                } catch (JSONException e) {
                    e.printStackTrace();
                    if (cb!=null) cb.authorized(false);
                }
            }
        });

    }

    public void getTweets(String query, final TwitterDataCallback cb){

        String q = null;
        String baseUrl = "https://api.twitter.com/1.1/search/tweets.json?q=";

        // Fucking Java and it's never ending exception bullshit
        try {
            q = URLEncoder.encode(query, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            q = URLEncoder.encode(query);
        }

        String fUrl = baseUrl + q + "&lang=en";
        fUrl += "&result_type=mixed";
        fUrl += "&include_entities=false";
        fUrl += "&include_entities=false";

        Request request = new Request.Builder()
                .header("Authorization" , "Bearer "+mToken)
                .url(fUrl)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {


            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                Log.e(TAG, "Twitter scrape failed");
                if (cb!=null) cb.error(e);
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {

                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                Log.d(TAG, "Twitter scrape successful");

                String results = response.body().string();
                if (cb!=null) cb.results(results);

            }
        });

    }


}
