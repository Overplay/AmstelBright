package io.ourglass.amstelbright.tvui;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mstar.android.tv.TvCommonManager;
import com.mstar.android.tvapi.common.TvManager;
import com.mstar.android.tvapi.common.exception.TvCommonException;
import com.mstar.android.tvapi.common.vo.TvOsType;

import io.ourglass.amstelbright.R;
import io.ourglass.amstelbright.services.amstelbright.AmstelBrightServer;

@SuppressLint("SetJavaScriptEnabled")

public class MainframeActivity extends Activity implements OGBroadcastReceiver.OGBroadcastReceiverListener, Mainframe.MainframeListener {

    private static final String TAG = "MFActivity";

    private WebView mCrawlerWebView;
    private WebView mWidgetWebView;

    private ProgressBar mProgressSpinner;
    private TextView mTextView;

    private ImageView mBootBugImageView;

    private RelativeLayout mMainLayout;

    private int mScreenWidth;
    private  int mScreenHeight;

    private Mainframe mMf;

    /**
     * Messenger for communicating with the service.
     */
    Messenger mService = null;

    /**
     * Flag indicating whether we have called bind on the service.
     */
    boolean mBound;
    boolean isEmulator = Build.FINGERPRINT.startsWith("generic");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainframe_layout);



        mMf = new Mainframe(this);

        startService(new Intent(getBaseContext(), AmstelBrightServer.class));

        mCrawlerWebView = (WebView)findViewById(R.id.crawlerWebView);
        mWidgetWebView = (WebView)findViewById(R.id.widgetWebView);

        mProgressSpinner = (ProgressBar)findViewById(R.id.progressBar);
        mProgressSpinner.setVisibility(View.INVISIBLE);

        mTextView = (TextView)findViewById(R.id.textViewMsg);
        mTextView.setVisibility(View.INVISIBLE);

        mMainLayout = (RelativeLayout)findViewById(R.id.mainframeLayout);
        mMainLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Log.d(TAG, "Layout done, updating Mainframe");
                mScreenHeight = mMainLayout.getHeight();
                mScreenWidth = mMainLayout.getWidth();
                mMf.setTVScreenSize(mScreenWidth, mScreenHeight);
                mMainLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                // Give webserver some time to finish firing up
                // TODO, I hate magic delays, we need some indication from Nano it is Done loading
                mMainLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            mMf.getApps();
                        } catch (Exception e) {
                            redFlag("Getting the apps shit itself.");
                        }
                    }
                }, 5000);
            }
        });

        if (isEmulator){
            mMainLayout.setBackgroundColor(getResources().getColor(R.color.Palette4a));
            Log.d(TAG, "Running in emulator, skipping HDMI passthru.");

        } else {
            enableHDMI();
        }

        mBootBugImageView = (ImageView)findViewById(R.id.bootBugIV);

        setupCrawler();
        setupWidget();

        Log.d(TAG, "onCreate done");

    }

    @Override
    protected void onResume(){
        super.onResume();

        // Binding not needed for now since we are using http and broadcast intents

//        bindService(new Intent(this, AmstelBrightServer.class), mConnection,
//                Context.BIND_AUTO_CREATE);

        IntentFilter filter = new IntentFilter("com.ourglass.amstelbrightserver");
        registerReceiver(new OGBroadcastReceiver(this), filter);
        Log.d(TAG, "onReceive done");

        mBootBugImageView.animate()
                .scaleX(0f)
                .scaleY(0f)
                .setDuration(3000)
                .setStartDelay(2000)
                .start();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to the service
        bindService(new Intent(this, AmstelBrightServer.class), mConnection,
                Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }



    @Override
    public void receivedCommand(Intent i) {
        //Toast.makeText(this, command+" for app "+appId, Toast.LENGTH_SHORT).show();
        mMf.postCommand(i);
    }

    public void animateIn(View v, float finalAlpha){

        ObjectAnimator anim = ObjectAnimator.ofFloat(v, "alpha", 0f, finalAlpha);
        anim.setDuration(1000);
        anim.start();

    }

    public void animateOut(View v){

        ObjectAnimator anim = ObjectAnimator.ofFloat(v, "alpha", v.getAlpha(), 0);
        anim.setDuration(1000);
        anim.start();

    }

    public void setupCrawler(){

        mCrawlerWebView.getSettings().setJavaScriptEnabled(true);
        mCrawlerWebView.setBackgroundColor(Color.TRANSPARENT);
        mCrawlerWebView.setAlpha(0f);

        mCrawlerWebView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                // Activities and WebViews measure progress with different scales.
                // The progress meter will automatically disappear when we reach 100%
            }
        });

        mCrawlerWebView.setWebViewClient(new WebViewClient() {
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                toast("Error loading app into crawler slot");
            }

            public void onPageFinished(WebView view, String url){
                animateIn(view, 1);
            }

        });

        // mCrawlerWebView.loadUrl("http://localhost:9090/www/opp/io.overplay.pubcrawler/app/tv/index.html");
        // mCrawlerWebView.loadUrl("http://159.203.242.99/opp/io.overplay.pubcrawler/app/tv/index.html");
        // mCrawlerWebView.loadUrl("http://www.overplay.io");

   }

    public void setupWidget(){

        mWidgetWebView.getSettings().setJavaScriptEnabled(true);
        mWidgetWebView.setBackgroundColor(Color.TRANSPARENT);
        mWidgetWebView.setAlpha(0f);  // initially invisible

        mWidgetWebView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                // Activities and WebViews measure progress with different scales.
                // The progress meter will automatically disappear when we reach 100%
            }
        });

        mWidgetWebView.setWebViewClient(new WebViewClient() {
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            }

            public void onPageFinished(WebView view, String url){
                animateIn(view, 1);
            }

        });


    }

    private void toast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

    }


    @Override
    public void moveWidgetFromTo(Point fromTranslation, Point toTranslation) {

        mWidgetWebView.animate().y(toTranslation.y).setDuration(1000);
        mWidgetWebView.animate().x(toTranslation.x).setDuration(1000);

    }

    @Override
    public void moveCrawlerFrom(float fromY, float toY) {

        mCrawlerWebView.animate().y(toY).setDuration(1000);

    }

    @Override
    public void killCrawler() {

        animateOut(mCrawlerWebView);
    }

    @Override
    public void killWidget() {

        animateOut(mWidgetWebView);

    }

    @Override
    public void launchCrawler(final String urlPathToApp) {

        // Must promote this to UI thread if coming from background thread
        mCrawlerWebView.post( new Runnable(){

            @Override
            public void run() {
                mCrawlerWebView.loadUrl(urlPathToApp);

            }

        });

    }

    @Override
    public void launchWidget(final String urlPathToApp) {

        mWidgetWebView.post( new Runnable(){

            @Override
            public void run() {
                mWidgetWebView.loadUrl(urlPathToApp);

            }

        });

    }

    @Override
    public void redFlag(String message) {
        Toast.makeText(this, "RED FLAG: "+message, Toast.LENGTH_LONG).show();
    }

    // MAK this is not currently used since we ar enot binding the service
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the object we can use to
            // interact with the service.  We are communicating with the
            // service using a Messenger, so here we get a client-side
            // representation of that from the raw IBinder object.
            mService = new Messenger(service);
            mBound = true;
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mService = null;
            mBound = false;
        }
    };

    // From old example code, needs to go...eventually
    public void sayHello(int msgnum) {
        if (!mBound) return;
        // Create and send a message to the service, using a supported 'what' value
        Message msg = Message.obtain(null, msgnum, 0, 0);
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static void changeInputSource(TvOsType.EnumInputSource eis)
    {

        TvCommonManager commonService = TvCommonManager.getInstance();

        if (commonService != null)
        {
            TvOsType.EnumInputSource currentSource = commonService.getCurrentInputSource();
            if (currentSource != null)
            {
                if (currentSource.equals(eis))
                {
                    return;
                }

                commonService.setInputSource(eis);
            }

        }


    }

    public static boolean enableHDMI()
    {
        boolean bRet = false;
        try
        {
            changeInputSource(TvOsType.EnumInputSource.E_INPUT_SOURCE_STORAGE);
            //changeInputSource(TvOsType.EnumInputSource.E_INPUT_SOURCE_HDMI);
            bRet = TvManager.getInstance().getPlayerManager().isSignalStable();
        } catch (TvCommonException e)
        {
            e.printStackTrace();
        }
        return bRet;
    }



}