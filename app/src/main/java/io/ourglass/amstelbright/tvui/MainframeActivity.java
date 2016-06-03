package io.ourglass.amstelbright.tvui;

import android.animation.AnimatorSet;
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
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewTreeObserver;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.mstar.android.tv.TvCommonManager;
import com.mstar.android.tvapi.common.TvManager;
import com.mstar.android.tvapi.common.exception.TvCommonException;
import com.mstar.android.tvapi.common.vo.TvOsType;

import io.ourglass.amstelbright.R;
import io.ourglass.amstelbright.services.amstelbright.AmstelBrightService;

@SuppressLint("SetJavaScriptEnabled")

public class MainframeActivity extends Activity implements OGBroadcastReceiver.OGBroadcastReceiverListener, Mainframe.MainframeListener, OGBroadcastStatusReceiver.OGBroadcastReceiverListener {

    private static final String TAG = "MFActivity";
    private static final boolean FLASHY = true;

    private WebView mCrawlerWebView;
    private WebView mWidgetWebView;
    private WebView mSubfloorWebView;

    VideoView videoView;


    private ProgressBar mProgressSpinner;
    private TextView mTextView;

    private ImageView mBootBugImageView;

    private RelativeLayout mMainLayout;
    private LinearLayout appTray;

    private int mScreenWidth;
    private  int mScreenHeight;

    private Mainframe mMf;

    private boolean mShowingMenu;

    /**
     * Messenger for communicating with the service.
     */
    Messenger mService = null;

    /**
     * Flag indicating whether we have called bind on the service.
     */
    boolean mBound;
    boolean isEmulator = Build.FINGERPRINT.startsWith("generic");
    private SurfaceView mSurfaceView;
    int colors[] = { R.color.Palette1, R.color.Palette2a, R.color.Palette3a, R.color.Palette4a, R.color.Palette5a, R.color.Palette4 };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainframe_layout);



        mMf = new Mainframe(this);

        startService(new Intent(getBaseContext(), AmstelBrightService.class));

        mSurfaceView = (SurfaceView)findViewById(R.id.surfaceView);
        //mSurfaceView.setVisibility(View.GONE);


        mCrawlerWebView = (WebView)findViewById(R.id.crawlerWebView);
        mWidgetWebView = (WebView)findViewById(R.id.widgetWebView);
        mSubfloorWebView = (WebView)findViewById(R.id.subfloorWebView);


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
                            showAlert(new UIMessage("Getting the apps shit itself.",
                                    UIMessage.UIMessageType.REDFLAG));
                        }
                    }
                }, 5000);
            }
        });

        if (isEmulator){
            mMainLayout.setBackgroundColor(getResources().getColor(R.color.Black));
            Log.d(TAG, "Running in emulator, skipping HDMI passthru.");

        } else {
            enableHDMI();
        }

        mBootBugImageView = (ImageView)findViewById(R.id.bootBugIV);

        setupCrawler();
        setupWidget();
        //setupSubfloor();


        /*
        appTray = (LinearLayout)findViewById(R.id.appTray);
        LayoutInflater inflater = LayoutInflater.from(this);




        for (int zed=0; zed<6; zed++){
            LinearLayout appIcon = new LinearLayout(this);
            inflater.inflate(R.layout.appcell, appIcon);
            ((TextView)appIcon.findViewById(R.id.textView)).setText("App #"+zed);
            appIcon.setBackgroundColor(getResources().getColor(colors[zed]));
            appIcon.setTag(zed);
            appIcon.setFocusable(true);
            appIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toast("Clicked button: "+v.getTag());
                }
            });

            appIcon.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus){
                        int focusColor = getResources().getColor(R.color.Beige);
                        v.setBackgroundColor(focusColor);
                        v.animate().scaleY(1.1f).scaleX(1.1f).alpha(1.0f).setDuration(100).start();
                    } else {
                        int pCol = getResources().getColor( colors[(int)v.getTag()] );
                        v.setBackgroundColor(pCol);
                        v.animate().scaleY(1.0f).scaleX(1.0f).alpha(0.90f).setDuration(100).start();

                    }
                }
            });


            appTray.addView(appIcon);
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams)appIcon.getLayoutParams();

            lp.rightMargin = 2;
            lp.leftMargin = 4;


        }

        appTray.requestLayout();

        appTray.setAlpha(0f);
        */
        mShowingMenu = false;


//        for (int zed=0; zed<6; zed++){
//            Button appIcon = new Button(this);
//            appIcon.setText("App #"+zed);
//            appIcon.setBackgroundColor(getResources().getColor(colors[zed]));
//            appIcon.setTag(zed);
//            appIcon.setLayoutParams(new ViewGroup.LayoutParams(200,300));
//            appIcon.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.man, 0, 0);
//            appIcon.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    toast("Clicked button: "+v.getTag());
//                }
//            });
//            appIcon.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//                @Override
//                public void onFocusChange(View v, boolean hasFocus) {
//                    if (hasFocus){
//                        int focusColor = getResources().getColor(R.color.AntiqueWhite);
//                        v.setBackgroundColor(focusColor);
//                    } else {
//                        int pCol = getResources().getColor( colors[(int)v.getTag()] );
//                        v.setBackgroundColor(pCol);
//                    }
//                }
//            });
//            appTray.addView(appIcon);
//        }


        Log.d(TAG, "onCreate done");

    }

    @Override
    protected void onResume(){
        super.onResume();

//        mSurfaceView.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                mSurfaceView.setVisibility(View.VISIBLE);
//            }
//        }, 1000);

        IntentFilter filter = new IntentFilter("com.ourglass.amstelbrightserver");
        registerReceiver(new OGBroadcastReceiver(this), filter);

        IntentFilter filter2 = new IntentFilter("com.ourglass.amstelbrightserver.status");
        registerReceiver(new OGBroadcastStatusReceiver(this), filter2);


        mBootBugImageView.animate()
                .scaleX(0f)
                .scaleY(0f)
                .rotationY(90f)
                .setDuration(1000)
                .setStartDelay(5000)
                .start();

        Log.d(TAG, "onResume done");

        //appTray.getChildAt(0).requestFocus();

    }

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to the service
        bindService(new Intent(this, AmstelBrightService.class), mConnection,
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
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        // 82 = Menu button,

        if (keyCode==82){
            toggleAppMenu();
        }


        //Toast.makeText(this, "pressed "+event.toString(), Toast.LENGTH_SHORT).show();

        return false;
    }

    private void toggleAppMenu(){

        float destAlpha = mShowingMenu ? 0f:1f;
        float destAlphaApp = mShowingMenu ? 1f:0.5f;
        appTray.animate().alpha(destAlpha).setDuration(1000).start();
        mCrawlerWebView.animate().alpha(destAlphaApp).setDuration(1000).start();
        mWidgetWebView.animate().alpha(destAlphaApp).setDuration(1000).start();
        mShowingMenu = !mShowingMenu;

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

        // TODO this is a hack for now...probably need to redo css on ABLime
        mWidgetWebView.setScaleX(0.5f);
        mWidgetWebView.setScaleY(0.5f);


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

    public void setupSubfloor(){

        mSubfloorWebView.getSettings().setJavaScriptEnabled(true);
        mSubfloorWebView.setBackgroundColor(Color.TRANSPARENT);
        mSubfloorWebView.setAlpha(1f);

        mSubfloorWebView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                // Activities and WebViews measure progress with different scales.
                // The progress meter will automatically disappear when we reach 100%
            }
        });

        mSubfloorWebView.setWebViewClient(new WebViewClient() {
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                toast("Error loading app into crawler slot");
            }

            public void onPageFinished(WebView view, String url){
                animateIn(view, 1);
            }

        });

        String youtubeId = "Xu_plv2CTEM";
        String video = "<iframe class=\"youtube-player\" style=\"border: 0; width: 100%; height: 100%; padding:0px; margin:0px\" id=\"ytplayer\" type=\"text/html\" src=\"http://www.youtube.com/embed/"
                + youtubeId +
                "?autoplay=1"
                + "&fs=0\" frameborder=\"0\">\n"
                + "</iframe>\n";
        mSubfloorWebView.getSettings().setPluginState(WebSettings.PluginState.ON);
        mSubfloorWebView.setWebChromeClient(new WebChromeClient());
        mSubfloorWebView.getSettings().setJavaScriptEnabled(true);
        mSubfloorWebView.setHorizontalScrollBarEnabled(false);
        mSubfloorWebView.setVerticalScrollBarEnabled(false);
        mSubfloorWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        mSubfloorWebView.getSettings().setBuiltInZoomControls(false);
        mSubfloorWebView.getSettings().setAppCacheEnabled(true);
        mSubfloorWebView.setInitialScale(0);
        mSubfloorWebView.getSettings().setLoadWithOverviewMode(true);
        mSubfloorWebView.getSettings().setUseWideViewPort(true);
        mSubfloorWebView.loadData(video,"text/html","UTF-8");


        //mSubfloorWebView.loadUrl("https://www.youtube.com/embed/8V_1ZxCN3nI?autoplay=1&origin=http://example.com%22%20frameborder=%220%22");
        // mCrawlerWebView.loadUrl("http://159.203.242.99/opp/io.overplay.pubcrawler/app/tv/index.html");
        // mCrawlerWebView.loadUrl("http://www.overplay.io");

    }


    private void toast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

    }

    private void showAlert(UIMessage message){


        mTextView.setText(message.message);

        switch (message.type){

            case REDFLAG:
                    mTextView.setBackgroundColor(0xff0000);
                break;

            case INFO:
                    mTextView.setBackgroundColor(getResources().getColor(R.color.Palette2a));
                break;


            case BOOT:

                break;
        }

        mTextView.setVisibility(View.VISIBLE);

        mTextView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mTextView.setVisibility(View.INVISIBLE);
            }
        }, 2000);

    }


    @Override
    public void moveWidgetFromTo(Point fromTranslation, Point toTranslation) {

        mWidgetWebView.animate().y(toTranslation.y).setDuration(1000);
        mWidgetWebView.animate().x(toTranslation.x).setDuration(1000);

    }

    @Override
    public void moveCrawlerFrom(final float fromY, final float toY) {


        mCrawlerWebView.post(new Runnable() {
            @Override
            public void run() {

                if (FLASHY){

                    ObjectAnimator close = ObjectAnimator.ofFloat(mCrawlerWebView, "rotationX", 0, 50 );
                    close.setDuration(100);

                    ObjectAnimator slide = ObjectAnimator.ofFloat(mCrawlerWebView, "y", fromY, toY );
                    slide.setDuration(333);

                    ObjectAnimator open = ObjectAnimator.ofFloat(mCrawlerWebView, "rotationX", 50, 0 );
                    open.setDuration(100);

                    AnimatorSet animSet = new AnimatorSet();
                    //animSet.setInterpolator(new BounceInterpolator());
                    animSet.play(close).before(slide);
                    animSet.play(open).after(slide);
                    animSet.start();

                } else {
                    mCrawlerWebView.animate().y(toY).setDuration(1000);
                }
            }
        });


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
    public void uiAlert(UIMessage message) {
        showAlert(message);
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
            changeInputSource(TvOsType.EnumInputSource.E_INPUT_SOURCE_HDMI);
            bRet = TvManager.getInstance().getPlayerManager().isSignalStable();
        } catch (TvCommonException e)
        {
            e.printStackTrace();
        }
        return bRet;
    }


    @Override
    public void receivedStatus(Intent intent) {

        String command = intent.getStringExtra("command");
        String msg = intent.getStringExtra("message");
        uiAlert(new UIMessage(msg));


    }
}