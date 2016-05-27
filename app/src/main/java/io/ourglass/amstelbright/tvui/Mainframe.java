package io.ourglass.amstelbright.tvui;

import android.content.Intent;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by mkahn on 5/11/16.
 */
public class Mainframe {

    private static final String TAG = "Mainframe";
    private static final boolean REPLACE_OURGLASS = true;

    private final OkHttpClient client = new OkHttpClient();

    private Rect mScreenRect;

    private int mWidgetMarginTop = 20;
    private int mWidgetMarginBottom = 20;
    private int mWidgetMarginSides = 20;

    private int mNumWidgetSlots = 4; // this may become programmable later
    private int mNumCrawlerSlots = 2; // this may become programmable later

    private Rect mCurrentWidgetRect;
    private Rect mCurrentCrawlerRect;

    private JSONObject mRunningCrawler;
    private int mRunningCrawlerSlot = 0;

    private JSONObject mRunningWidget;
    private int mRunningWidgetSlot = 0;


    public static final int SERVER_PORT = 9090;

    private JSONArray mAllApps;

    private MainframeListener mListener;

    public interface MainframeListener {

        public void moveWidgetFromTo(Point fromTranslation, Point toTranslation);
        public void moveCrawlerFrom(float fromY, float toY);
        public void killCrawler();
        public void killWidget();
        public void launchCrawler(String urlPathToApp);
        public void launchWidget(String urlPathToApp);
        public void uiAlert(UIMessage message);

    }

    // Constructor
    public Mainframe(MainframeListener listener){

        mListener = listener;

    }

    // Positioning Methods

    private float crawlerTranslationY(int slotNumber){

        return ((float)slotNumber * 0.945f ) *  mScreenRect.height;

    }

    private Point widgetTranslationXY(int slotNumber){

        float x;
        float y;

        float ytop = 0.12f * mScreenRect.height;
        float ybot = 0.55f * mScreenRect.height;

        float xleft = 0.013f * mScreenRect.width;
        float xright = 0.87f * mScreenRect.width;

        switch (slotNumber){

            case 0:

                x = xleft; // ~25px
                y = ytop; // ~90px
                break;

            case 1:

                x = xright;
                y = ytop; // ~90px
                break;

            case 2:
                x = xright;
                y = ybot;
                break;

            case 3:
                x = xleft;
                y = ybot;
                break;

            default:
                x = 0.5f * mScreenRect.width;
                y = 0.5f * mScreenRect.height;

        }

        return new Point(x,y);
    }


    private void raiseRedFlag(String message){

        if (mListener!=null)
            mListener.uiAlert(new UIMessage(message, UIMessage.UIMessageType.REDFLAG));

    }


    public void setTVScreenSize( float width, float height){

        mScreenRect = new Rect(width, height);
        Log.d(TAG, "TV size set to: "+mScreenRect.toString());

    }

    public String urlForApp(String appId){

        return "http://localhost:" + SERVER_PORT + "/www/opp/" + appId + "/app/tv/index.html";
    }

    public void getApps() throws Exception {

        // TODO: Should we do this thru service calls now??
        Request request = new Request.Builder()
                .url("http://localhost:9090/api/system/apps")
                .build();

        client.newCall(request).enqueue(new Callback() {


            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                Log.e(TAG, "GET apps failed");
                raiseRedFlag("Unable to get apps from server!");
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {

                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                String jString = response.body().string();

                try {
                    mAllApps = new JSONArray(jString);
                } catch (JSONException e) {
                    throw new IOException("Unexpected Json error " + e.toString());
                }

                Log.d(TAG, "GET all apps complete");
                processInboundApps();

            }
        });
    }


    private void processInboundApps(){

        for (int i = 0; i < mAllApps.length(); i++) {
            try {
                JSONObject app = mAllApps.getJSONObject(i);
                boolean running = app.getBoolean("running");
                if (running){
                    String type = app.getString("appType");
                    if (type.equalsIgnoreCase("crawler")){
                        setRunningCrawler(app);
                    } else {
                        setRunningWidget(app);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


    }


    // Feels like a hack to go two places to get similar info
    public void getAppInfo() throws Exception {



        Request request = new Request.Builder()
                .url("http://localhost:9090/api/system/apps")
                .build();

        client.newCall(request).enqueue(new Callback() {


            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                Log.e(TAG, "GET apps failed");
                raiseRedFlag("Unable to get apps from server!");
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {

                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                String jString = response.body().string();

                try {
                    mAllApps = new JSONArray(jString);
                } catch (JSONException e) {
                    throw new IOException("Unexpected Json error " + e.toString());
                }

                Log.d(TAG, "GET all apps complete");
                processInboundApps();

            }
        });
    }

    private void moveCrawlerIfNeeded(int destSlot){

        if (destSlot!=mRunningCrawlerSlot){
            // We're going to need to move it
                mListener.moveCrawlerFrom(crawlerTranslationY(mRunningCrawlerSlot), crawlerTranslationY(destSlot));
                mRunningCrawlerSlot = destSlot;

        }

    }

    private void setRunningCrawler(JSONObject appJson){

        try {
            String appId = appJson.getString("appId");
            int slotNumber = appJson.getInt("slotNumber");

            mRunningCrawler = appJson;
            Log.d(TAG, "Crawler set to: "+ appId);
            if (mListener!=null)
                mListener.launchCrawler(urlForApp(appId));

            moveCrawlerIfNeeded(slotNumber);

        } catch (JSONException e) {
            raiseRedFlag("Could not extract appId from appJson for crawler");
            mRunningCrawler = null;
        }

    }

    private void moveWidgetIfNeeded(int destSlot){

        if (destSlot!=mRunningWidgetSlot){
            // We're going to need to move it
            if (mListener!=null){
                mListener.moveWidgetFromTo(widgetTranslationXY(mRunningWidgetSlot), widgetTranslationXY(destSlot));
                mRunningWidgetSlot = destSlot;
            }
        }

    }


    private void setRunningWidget(JSONObject appJson){

        try {
            String appId = appJson.getString("appId");
            int slotNumber = appJson.getInt("slotNumber");

            mRunningWidget = appJson;
            Log.d(TAG, "Widget set to: "+ appId);
            if (mListener!=null)
                mListener.launchWidget(urlForApp(appId));

            moveWidgetIfNeeded(slotNumber);

        } catch (JSONException e) {
            raiseRedFlag("Could not extract appId from appJson for widget");
            mRunningWidget = null;
        }

    }

    private void launchApp(JSONObject app){

        String appId = null;
        String appType = null;
        String screenName = null;

        try {
            appId = app.getString("appId");
            appType = app.getString("appType");
            screenName = app.getString("screenName");

        } catch (Exception e){
            raiseRedFlag("Could not launch app. Error parsing JSON");
        }

        mListener.uiAlert(new UIMessage("Launching "+ screenName));
        // OK, so at this point we need to concern ourselves with the type, URL, and slotNumber
        switch (appType){
            case "crawler":
                setRunningCrawler(app);
                break;

            case "widget":
                setRunningWidget(app);
                break;
        }

    }

    private void killApp(JSONObject app){

        // TODO: Clearly we need to wrap apps in their own object that does all this try/catch busy work!
        // TODO: Or we need to make the UI part of the overall server app and share that code...

        try {
            String appType = app.getString("appType");
            switch (appType){
                case "crawler":
                    mListener.killCrawler();
                    mRunningCrawler = null;
                    break;
                case "widget":
                    mListener.killWidget();
                    mRunningWidget = null;
                    break;
            }

        } catch (Exception e){
            Log.wtf(TAG, "Should not have a JSON failure here.");
        }
    }

    private void moveApp(JSONObject app){

        try {
            int newSlot = app.getInt("slotNumber");

            if (app.getString("appType").equalsIgnoreCase("crawler")){
                moveCrawlerIfNeeded(newSlot);
            } else {
                moveWidgetIfNeeded(newSlot);
            }

        } catch (Exception e){
            Log.wtf(TAG, "WTF with the bad JSON again!");
            raiseRedFlag("WTF with the bad JSON again!");
        }
    }

    public void postCommand(Intent intent) {

        String command = intent.getStringExtra("command");

        JSONObject app;


        try {
            app = new JSONObject(intent.getStringExtra("app"));

        } catch (JSONException e) {
            raiseRedFlag("Error parsing inbound intent JSON");
            return;
        }

        switch (command){

            case "launch":

                launchApp(app);
                break;

            case "move":
                moveApp(app);
                break;

            case "kill":
                killApp(app);
                break;
        }
    }

    public ArrayList<AppDisplayInfo> getLauncherApps(){

        ArrayList<AppDisplayInfo> rval = new ArrayList<>();

        AppDisplayInfo shuff = new AppDisplayInfo();
        shuff.appId = "io.ourglass.shuffleboard";
        shuff.displayName = "Shuffleboard";
        shuff.primaryColor = 0x2019f5;
        shuff.secondaryColor = 0x110c9d;
        rval.add(shuff);

        AppDisplayInfo pub = new AppDisplayInfo();
        shuff.appId = "io.ourglass.pubcrawler";
        shuff.displayName = "PubCrawler";
        shuff.primaryColor = 0x68566B;
        shuff.secondaryColor = 0x987D9C;
        rval.add(pub);

        AppDisplayInfo bud = new AppDisplayInfo();
        shuff.appId = "io.ourglass.budboard2";
        shuff.displayName = "BudBoard";
        shuff.primaryColor = 0xff0000;
        shuff.secondaryColor = 0xD03C3C;
        rval.add(bud);

        return rval;

    }


}
