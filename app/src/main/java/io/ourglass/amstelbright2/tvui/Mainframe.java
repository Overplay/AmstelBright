package io.ourglass.amstelbright2.tvui;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import io.ourglass.amstelbright2.core.OGConstants;
import io.ourglass.amstelbright2.core.OGCore;
import io.ourglass.amstelbright2.services.amstelbright.AmstelBrightService;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by mkahn on 5/11/16.
 */
public class Mainframe implements OGBroadcastReceiver.OGBroadcastReceiverListener, OGBroadcastStatusReceiver.OGBroadcastReceiverListener {

    private static final String TAG = "Mainframe";
    private Context mContext;


    private static final boolean REPLACE_OURGLASS = true;

    private OkHttpClient client;

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

    private final String BASE_URL = OGConstants.USE_HTTPS ? "https://localhost:" : "http://localhost:";

    // TODO this should be combined with mAllApps so there is a single native array with all app info
    public ArrayList<AppIcon> appIcons = new ArrayList<>();


    public static final int SERVER_PORT = 9090;

    private JSONArray mAllApps;

    private MainframeListener mListener;


    public interface MainframeListener {

        public void moveWidgetFromTo(Point fromTranslation, Point toTranslation);
        public void moveCrawlerFrom(float fromY, float toY);
        public void adjustCrawler( float scale, int xAdjust, int yAdjust );
        public void adjustWidget( float scale, int xAdjust, int yAdjust );
        public void killCrawler();
        public void killWidget();
        public void launchCrawler(String urlPathToApp);
        public void launchWidget(String urlPathToApp, int width, int height);
        public void uiAlert(UIMessage message);
    }

    // Constructor
    public Mainframe(Context c, MainframeListener listener){

        mListener = listener;
        mContext = c;

        IntentFilter filter = new IntentFilter("com.ourglass.amstelbrightserver");
        mContext.registerReceiver(new OGBroadcastReceiver(this), filter);

        IntentFilter filter2 = new IntentFilter("com.ourglass.amstelbrightserver.status");
        mContext.registerReceiver(new OGBroadcastStatusReceiver(this), filter2);

        //if using https, then install custom certificate checker that trusts everything
        if(OGConstants.USE_HTTPS) {
            try {
                final TrustManager[] trustAllCerts = new TrustManager[]{
                        new X509TrustManager() {
                            @Override
                            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                            }

                            @Override
                            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                            }

                            @Override
                            public X509Certificate[] getAcceptedIssuers() {
                                return new X509Certificate[0];
                            }
                        }
                };
                final SSLContext sslContext = SSLContext.getInstance("SSL");
                sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
                //create an ssl socket facotry with our all-trusting manager
                final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

                OkHttpClient.Builder builder = new OkHttpClient.Builder();
                builder.sslSocketFactory(sslSocketFactory);
                builder.hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                });
                client = builder.build();
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        } else {

            // Ethan, you did not create a client for non-HTTPS mode, ffs!
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            client = builder.build();


        }

    }

    // Positioning Methods

    private float crawlerTranslationY(int slotNumber){

        return ((float)slotNumber * 0.915f ) *  mScreenRect.height;

    }

    private Point widgetTranslationXY(int slotNumber){

        float x;
        float y;

        //default values
        int widgetWidth = 300;
        int widgetHeight = 300;

        try{
            widgetWidth = mRunningWidget.getInt("width");
            widgetHeight = mRunningWidget.getInt("height");
        } catch (JSONException e){
            Log.e(TAG, "Couldn't get the running widget dimensions, widgets will probably be in the wrong place");
        }

        //y padding on widget is .12f (because of crawler)
        //x padding on widget is .013f

        float ytop = 0.08f * mScreenRect.height;
        float ybot = (0.88f * mScreenRect.height) - widgetHeight;

//        float xleft = 0.013f * mScreenRect.width;
//        float xright = 0.987f * mScreenRect.width - widgetWidth;

        // Pinning to edge
        float xleft = 0;
        float xright = mScreenRect.width - widgetWidth;

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

        return BASE_URL + SERVER_PORT + "/www/opp/" + appId + "/app/tv/index.html";
    }

    public String urlForAppInfo(String appId){

        return BASE_URL + SERVER_PORT + "/www/opp/" + appId + "/info/info.json";
    }

    public String urlForAppIcon(String appId, String iconName){

        return BASE_URL + SERVER_PORT + "/www/opp/" + appId + "/assets/icons/"+iconName;
    }

    public void getApps() throws Exception {

        // TODO: Should we do this thru service calls now??
        Request request = new Request.Builder()
                .url(BASE_URL + SERVER_PORT +"/api/system/apps")
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

        mListener.uiAlert(new UIMessage("Processing Apps"));
        for (int i = 0; i < mAllApps.length(); i++) {
            try {
                JSONObject app = mAllApps.getJSONObject(i);

                // Set what's running
                boolean running = app.getBoolean("running");
                if (running){
                    String type = app.getString("appType");
                    if (type.equalsIgnoreCase("crawler")){
                        setRunningCrawler(app);
                    } else {
                        setRunningWidget(app);
                    }
                }

                loadAppIconFor(app.getString("appId"));


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        mListener.uiAlert(new UIMessage(mAllApps.length()+" apps intalled. Nice!"));

    }


    private void loadAppIconFor( final String appId){


        Request request = new Request.Builder()
                .url(urlForAppInfo(appId))
                .build();

        client.newCall(request).enqueue(new Callback() {


            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                Log.e(TAG, "GET app info.json failed");
                raiseRedFlag("Unable to get app info from server!");
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {

                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                String jString = response.body().string();

                JSONObject aiObj;
                AppIcon ai = new AppIcon();
                try {
                    aiObj = new JSONObject(jString);
                    try {
                        //ai.primaryColor = aiObj.getInt("primaryColor"); //Color.parseColor(aiObj.getString("primaryColor"));
                        ai.primaryColor = Color.parseColor(aiObj.getString("primaryColorHex"));
                    } catch(IllegalArgumentException e){
                        Log.e(TAG, "There was an error parsing color, verify that the correct information is associated with your applications or change your info.json to HEX values");
                    }
                    try{
                        ai.secondaryColor = Color.parseColor(aiObj.getString("secondaryColorHex"));
                    } catch(IllegalArgumentException e){
                        Log.e(TAG, "There was an error parsing secondaryColor, verify that the correct information is associated with your applications or change your info.json to HEX values");
                    }
                    try {
                        ai.label = aiObj.getString("iconLabel");
                    } catch(JSONException e){
                        Log.w(TAG, "iconLabel was missing from json, using appName as default");
                        ai.label = appId;
                    }
                    ai.appId = appId;
                    ai.textColor = Color.WHITE; // TODO parse from JSON
                    ai.imageUrl = urlForAppIcon(appId, aiObj.getString("icon"));

                } catch (JSONException e) {
                    throw new IOException("Unexpected Json error " + e.toString());
                } finally {
                    appIcons.add(ai);
                }
            }
        });


    }

    // Feels like a hack to go two places to get similar info
    public void getAppInfo() throws Exception {



        Request request = new Request.Builder()
                .url(BASE_URL + SERVER_PORT + "/api/system/apps")
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

    public void getDirectvBoxesAndLaunchActivity(final Intent intent){
        Request request = new Request.Builder()
                .url(BASE_URL + SERVER_PORT + "/api/stb/available")
                .build();

        client.newCall(request).enqueue(new Callback() {


            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                //dtpa.setError("error");
                Log.e(TAG, "GET stbs failed");
                raiseRedFlag("Unable to get stb info from server");
                intent.putExtra("success", false);
                intent.putExtra("errMsg", e.getMessage());
                AmstelBrightService.context.startActivity(intent);
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {

                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                String jString = response.body().string();

                try {
                    JSONObject obj = new JSONObject(jString);
                    JSONArray devs = obj.getJSONArray("devices");

                    ArrayList<String> toAttach = new ArrayList<String>();
                    for(int i = 0; i < devs.length(); i++){
                        toAttach.add(devs.getString(i));
                    }
                    intent.putExtra("success", true);
                    intent.putExtra("devices", toAttach);
                } catch (JSONException e) {
                    intent.putExtra("success", false);
                    //throw new IOException("Unexpected Json error " + e.toString());
                } finally {
                    AmstelBrightService.context.startActivity(intent);
                }
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

        //if (destSlot!=mRunningWidgetSlot){
            // We're going to need to move it
            if (mListener!=null){
                mListener.moveWidgetFromTo(widgetTranslationXY(mRunningWidgetSlot), widgetTranslationXY(destSlot));
                mRunningWidgetSlot = destSlot;
            }
        //}

    }

    public boolean isRunning(String appId){

        // TODO another reason we need to dispense with JSONObjects quickly
        try {
            if (mRunningCrawler != null){
                String rcaid = mRunningCrawler.getString("appId");
                if (rcaid.equalsIgnoreCase(appId))
                    return true;
            }

            if (mRunningWidget != null){
                String rcaid = mRunningWidget.getString("appId");
                if (rcaid.equalsIgnoreCase(appId))
                    return true;
            }

        } catch (Exception e){
            Log.wtf(TAG, "JSONObject sucks");
        }

        return false;

    }

    private void setRunningWidget(JSONObject appJson){

        try {
            String appId = appJson.getString("appId");
            int slotNumber = appJson.getInt("slotNumber");

            //get the height and width and pass to launchWidget
            int width = appJson.getInt("width"), height = appJson.getInt("height");

            mRunningWidget = appJson;
            Log.d(TAG, "Widget set to: "+ appId);
            if (mListener!=null) {
                mListener.launchWidget(urlForApp(appId), width, height);
            }
            moveWidgetIfNeeded(slotNumber);

        } catch (JSONException e) {
            raiseRedFlag("Could not extract appId from appJson for widget");
            mRunningWidget = null;
        }

    }

    public void launchApp(JSONObject app){

        String appId = null;
        String appType = null;
        String appName = null;

        try {
            appId = app.getString("appId");
            appType = app.getString("appType");
            appName = app.getString("appName");

        } catch (Exception e){
            raiseRedFlag("Could not launch app. Error parsing JSON");
        }

        mListener.uiAlert(new UIMessage("Launching "+ appName));
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

    private void moveApp(JSONObject app) {

        try {
            int newSlot = app.getInt("slotNumber");

            if (app.getString("appType").equalsIgnoreCase("crawler")) {
                moveCrawlerIfNeeded(newSlot);
            } else {
                moveWidgetIfNeeded(newSlot);
            }

            //log the movement as placement override
            OGCore.log_placementOverride(OGCore.channel, OGCore.programId, app.getString("appId"), newSlot);
        } catch (Exception e) {
            Log.wtf(TAG, "WTF with the bad JSON again!");
            raiseRedFlag("WTF with the bad JSON again!");
        }
    }

    private void adjustApp(JSONObject app, float scale, int xAdjust, int yAdjust){

        try {

            if (app.getString("appType").equalsIgnoreCase("crawler")){
                mListener.adjustCrawler(scale, xAdjust, yAdjust);
            } else {
                mListener.adjustWidget(scale, xAdjust, yAdjust);
            }

        } catch (Exception e){
            Log.wtf(TAG, "WTF with the bad JSON again!");
            raiseRedFlag("WTF with the bad JSON again!");
        }


    }

    public void postCommand(Intent intent) {

        String command = intent.getStringExtra("command");

        if ( command.equalsIgnoreCase("NEW_CHANNEL")){

            // Channel change stuff
            String channel = intent.getStringExtra("channel");
            Log.d(TAG, "Got a channel change: "+channel);
            UIMessage m = new UIMessage("Changed to: "+channel);
            mListener.uiAlert(m);

            if (channel.startsWith("ES") || channel.startsWith("CNN") ){
                moveCrawlerIfNeeded(0); // top
            } else if ( channel.startsWith("beIN")){
                moveCrawlerIfNeeded(1); // bottom
            }


        } else {

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

                case "adjust":
                    float scale = intent.getFloatExtra("scale", 1f);
                    int xAdjust = intent.getIntExtra("xAdjust", 0);
                    int yAdjust = intent.getIntExtra("yAdjust", 0);
                    adjustApp(app, scale, xAdjust, yAdjust);
                    break;
            }

        }


    }

    public void launchViaHttp(String appId){

        MediaType JSON
                = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, "{}");

        Request request = new Request.Builder()
                .url(BASE_URL + SERVER_PORT + "/api/app/"+appId+"/launch")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {


            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                Log.e(TAG, "Launch apps failed");
                raiseRedFlag("Unable to launch app via server!");
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {

                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                Log.d(TAG, "App operation complete");

            }
        });


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

    // BROADCAST RECEIVER CALLBACKS

    /*******************************************
     * SYSTEM COMMAND MESSAGE BROADCAST RECEIVER
     *******************************************/

    @Override
    public void receivedCommand(Intent intent) {
        postCommand(intent);
    }

    /*******************************************
     * SYSTEM STATUS MESSAGE BROADCAST RECEIVER
     *******************************************/

    @Override
    public void receivedStatus(Intent intent) {

        if(intent.hasExtra("newAppData") && intent.hasExtra("appType")){
            String newAppData = intent.getStringExtra("newAppData"), appType = intent.getStringExtra("appType");
            if(newAppData != null){
                if("widget".equals(appType)) {
                    ((MainframeActivity)mContext).injectAppDataIntoWidget(newAppData);
                }
                else if("crawler".equals(appType)){
                    ((MainframeActivity)mContext).injectAppDataIntoCrawler(newAppData);
                }
            }
            return;
        }

        String command = intent.getStringExtra("command");

        String msg = intent.getStringExtra("message");
        ((MainframeActivity)mContext).uiAlert(new UIMessage(msg));

        int code = intent.getIntExtra("code", 0);

        if ( code == OGConstants.BootState.HTTP_START.getValue() ){
            Log.d(TAG, "HTTP server has started, going to get apps");
            try {
                getApps();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }



}
