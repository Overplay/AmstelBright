package io.ourglass.amstelbright2.tvui;


import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.squareup.picasso.Picasso;

import io.ourglass.amstelbright2.R;
import io.ourglass.amstelbright2.core.OGSystem;
import io.ourglass.amstelbright2.services.amstelbright.AmstelBrightService;

@SuppressLint("SetJavaScriptEnabled")

public class MainframeActivity extends Activity implements Mainframe.MainframeListener {

    private static final String TAG = "MFActivity";
    private static final boolean FLASHY = true;
    private static final long SCALE_ANIM_DURATION = 1000;

    private WebView mCrawlerWebView;
    private WebView mWidgetWebView;

    private TextView mPopupSystemMessageTV;
    private TextView mDebugMessageTV;

    private ImageView mBootBugImageView;

    private RelativeLayout mMainLayout;
    private LinearLayout appTray;

    private int mScreenWidth;
    private int mScreenHeight;

    private Mainframe mMf;

    private boolean mShowingMenu;
    private boolean inMenuDebounce = false; // debounce the menu button

    AnimatorSet mCurrentSelectAnim;

    /**
     * Messenger for communicating with the service.
     */
    Messenger mService = null;

    /**
     * Flag indicating whether we have called bind on the service.
     */
    boolean mBound;

    //private SurfaceView mSurfaceView;

    // New permissions crap added to API 23+
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainframe_layout_demo);

        Log.d(TAG, "OS Level: "+ OGSystem.osLevel());
        Log.d(TAG, "Is demo H/W? " + (OGSystem.isTronsmart()?"YES":"NO"));
        Log.d(TAG, "Is real OG H/W? " + (OGSystem.isRealOG()?"YES":"NO"));
        Log.d(TAG, "Is emulated H/W? " + (OGSystem.isEmulator()?"YES":"NO"));


        // Cross fingers and toes that this works the permissions magic for SDCARD on OG1
        verifyStoragePermissions(this);

        mMf = new Mainframe(this, this);

        startService(new Intent(getBaseContext(), AmstelBrightService.class));

        //mSurfaceView = (SurfaceView) findViewById(R.id.surfaceView);

        setupCrawler();
        setupWidget();

        mPopupSystemMessageTV = (TextView) findViewById(R.id.textViewMsg);
        mPopupSystemMessageTV.setText("");
        mPopupSystemMessageTV.setAlpha(0);

        mDebugMessageTV = (TextView) findViewById(R.id.textViewDebug);
        mDebugMessageTV.setText("");
        mDebugMessageTV.setAlpha(0);

        mMainLayout = (RelativeLayout) findViewById(R.id.mainframeLayout);
        mMainLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                Log.d(TAG, "Layout done, updating Mainframe screen sizing.");
                mScreenHeight = mMainLayout.getHeight();
                mScreenWidth = mMainLayout.getWidth();
                mMf.setTVScreenSize(mScreenWidth, mScreenHeight);
                mMainLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);

            }
        });

        if (!OGSystem.enableHDMI()) {
            // The color change doesn't seem to do anything...:(.. not worth stressing.
            mMainLayout.setBackgroundColor(getResources().getColor(R.color.Turquoise));
            Log.d(TAG, "Running in emulator or on OG H/W without libs, skipping HDMI passthru.");

            VideoView vv = (VideoView) findViewById(R.id.videoView);
            if ( vv != null ){
                vv.setVideoPath("/sdcard/gswandroid.mp4");
                vv.start();
            }
        }

        mBootBugImageView = (ImageView) findViewById(R.id.bootBugIV);

        appTray = (LinearLayout) findViewById(R.id.appTray);
        appTray.setAlpha(0f);
        appTray.setRotationX(90f);
        appTray.setTranslationX(-1000f);
        mShowingMenu = false;

        Log.d(TAG, "onCreate done");

    }

    @Override
    protected void onResume() {
        super.onResume();




        mBootBugImageView.animate()
                .scaleX(0f)
                .scaleY(0f)
                .rotationY(90f)
                .setDuration(1000)
                .setStartDelay(5000)
                .start();

        Log.d(TAG, "onResume done");

        //appTray.getChildAt(0).requestFocus();

        mBootBugImageView.postDelayed(new Runnable() {
            @Override
            public void run() {
                //buildAppTray();
            }
        }, 5000);

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

        if (keyCode == 82 || keyCode == 41) {
            toggleAppMenu();
            return false;
        }

        // Launch settings from button 0 on remote or right mouse
        if (keyCode == 7 || keyCode == 4){
            startActivityForResult(new Intent(android.provider.Settings.ACTION_SETTINGS), 0);
        }


        showAlert(new UIMessage("You pushed key " + keyCode));

        return false;
    }

    /**
     * Checks if the app has permission to write to device storage
     *
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }


    private void buildAppTray() {

        appTray.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(this);
        for (final AppIcon ai : mMf.appIcons) {

            LinearLayout appIcon = new LinearLayout(this);
            inflater.inflate(R.layout.appcell, appIcon);
            appIcon.setBackgroundColor(ai.primaryColor);
            appIcon.setTag(R.string.app_icon_tag, ai);
            appIcon.setFocusable(true);

            TextView label = (TextView) appIcon.findViewById(R.id.textView);
            label.setText(ai.label);
            label.setTextColor(ai.textColor);
            label.setTypeface(null, Typeface.BOLD);

            ImageView iconView = (ImageView) appIcon.findViewById(R.id.imageViewIcon);
            Picasso.with(this).load(ai.imageUrl).into(iconView);

            ImageView rv = (ImageView) appIcon.findViewById(R.id.imageViewRunning);
            rv.setVisibility(mMf.isRunning(ai.appId) ? View.VISIBLE : View.INVISIBLE);

            appIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AppIcon ai = (AppIcon) v.getTag(R.string.app_icon_tag);
                    toast("Clicked button: " + ai.label);
                    mMf.launchViaHttp(ai.appId);
                    toggleAppMenu();
                    //killAppConfirm(ai);
                }
            });

            appIcon.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    AppIcon ai = (AppIcon) v.getTag(R.string.app_icon_tag);
                    if (hasFocus) {
                        int focusColor = ai.secondaryColor;
                        v.setBackgroundColor(focusColor);
                        v.animate().scaleY(1.02f).scaleX(1.02f).alpha(1.0f).setDuration(100).start();
                    } else {
                        int pCol = ai.primaryColor;
                        v.setBackgroundColor(pCol);
                        v.animate().scaleY(1.0f).scaleX(1.0f).alpha(0.90f).setDuration(100).start();

                    }
                }
            });

            appTray.addView(appIcon);
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) appIcon.getLayoutParams();

            lp.rightMargin = 2;
            lp.leftMargin = 4;


        }

        appTray.requestLayout();
        appTray.setAlpha(0f);
        appTray.setRotationX(90f);
        appTray.setTranslationX(-1000f);

    }

    private void toggleAppMenu() {

        if (inMenuDebounce)
            return; // do nothing, we are debouncing

        inMenuDebounce = true;
        // Allow pressing menu button 1 second later
        mCrawlerWebView.postDelayed(new Runnable() {
            @Override
            public void run() {
                inMenuDebounce = false;
            }
        }, 1000);

        mShowingMenu = !mShowingMenu;

        if (mShowingMenu) {
            buildAppTray();
        }

        // If about to show, my dest Alpha should be 1
        float destAlpha = mShowingMenu ? 1f : 0f;
        float destRot = mShowingMenu ? 0f : 90f;
        float destTransX = mShowingMenu ? 0 : -1000;
        float destAlphaApp = mShowingMenu ? 0.25f : 1f;


        appTray.animate()
                .alpha(destAlpha)
                .rotationX(destRot)
                .translationX(destTransX)
                .setDuration(500)
                .start();

        mCrawlerWebView.animate().alpha(destAlphaApp).setDuration(500).start();
        mWidgetWebView.animate().alpha(destAlphaApp).setDuration(500).start();
        //mFullScreenWebView.animate().alpha(destAlphaApp).setDuration(500).start();


    }


    /***********************
     * ANIMATIONS
     ***********************/

    public void animateIn(View v, float finalAlpha) {

        ObjectAnimator anim = ObjectAnimator.ofFloat(v, "alpha", 0f, finalAlpha);
        anim.setDuration(1000);
        anim.start();

    }

    public void animateOut(View v) {

        ObjectAnimator anim = ObjectAnimator.ofFloat(v, "alpha", v.getAlpha(), 0);
        anim.setDuration(1000);
        anim.start();

    }

    public AnimatorSet animatePulse(View v) {

        float startWidth = v.getScaleX();
        float startHeight = v.getScaleX();
        float delta = 0.9f;

        ObjectAnimator animWidthIn = ObjectAnimator.ofFloat(v, "scaleX", startWidth, delta*startWidth);
        animWidthIn.setDuration(250);

        ObjectAnimator animWidthOut = ObjectAnimator.ofFloat(v, "scaleX", delta*startWidth, startWidth );
        animWidthOut.setDuration(250);


        ObjectAnimator animHeightIn = ObjectAnimator.ofFloat(v, "scaleY", startHeight, delta*startHeight);
        animHeightIn.setDuration(250);

        ObjectAnimator animHeightOut = ObjectAnimator.ofFloat(v, "scaleY",  delta*startHeight, startHeight);
        animHeightOut.setDuration(250);

        final AnimatorSet pulser = new AnimatorSet();
        pulser.play(animWidthIn).before(animHeightIn);
        pulser.play(animWidthOut).with(animHeightIn);
        pulser.play(animHeightOut).with(animWidthIn);
        pulser.play(animWidthOut);


        pulser.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                pulser.start();
            }

        });

        //pulser.setDuration(5000);
        pulser.start();
        return pulser;


    }


    /************************
     * WEBVIEW SETUP
     ************************/

    public void setupAppWebView(WebView wv) {

        wv.getSettings().setJavaScriptEnabled(true);
        wv.getSettings().setDomStorageEnabled(true);
        wv.setBackgroundColor(Color.TRANSPARENT);
        wv.setAlpha(0f);
        wv.setFocusable(false);

        wv.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                // Activities and WebViews measure progress with different scales.
                // The progress meter will automatically disappear when we reach 100%
            }
        });

        wv.setWebViewClient(new WebViewClient() {
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                toast("Error loading app into crawler slot");
            }

            public void onPageFinished(WebView view, String url) {
                animateIn(view, 1);
            }

        });


        WebSettings settings = wv.getSettings();
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);

    }

    public void setupCrawler() {

        mCrawlerWebView = (WebView) findViewById(R.id.crawlerWebView);
        setupAppWebView(mCrawlerWebView);

    }

    public void setupWidget() {

        mWidgetWebView = (WebView) findViewById(R.id.widgetWebView);
        setupAppWebView(mWidgetWebView);

    }

    public void setupFullscreen() {

        //mFullScreenWebView = (WebView) findViewById(R.id.subfloorWebView);
        //setupAppWebView(mFullScreenWebView);

    }


    /****************************************
     * CALLBACKS from MAINFRAME
     ****************************************/


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

                if (FLASHY) {

                    ObjectAnimator close = ObjectAnimator.ofFloat(mCrawlerWebView, "rotationX", 0, 50);
                    close.setDuration(100);

                    ObjectAnimator slide = ObjectAnimator.ofFloat(mCrawlerWebView, "y", fromY, toY);
                    slide.setDuration(333);

                    ObjectAnimator open = ObjectAnimator.ofFloat(mCrawlerWebView, "rotationX", 50, 0);
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

    private void loadWebView(final WebView wv, final String url) {

        wv.post(new Runnable() {

            @Override
            public void run() {
                wv.loadUrl(url);
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
    public void adjustWidget(float scale, int xAdjust, int yAdjust){
        AnimationSet moveAndScale = new AnimationSet(true);

        Animation translate = new TranslateAnimation(mWidgetWebView.getX(), mWidgetWebView.getX() + xAdjust, mWidgetWebView.getY(), mWidgetWebView.getY() + yAdjust);
        translate.setDuration(SCALE_ANIM_DURATION);
        translate.setFillAfter(true);

        moveAndScale.addAnimation(translate);

        ScaleAnimation scaling = new ScaleAnimation(
                mWidgetWebView.getScaleX(), scale,
                mWidgetWebView.getScaleY(), scale,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        scaling.setFillAfter(true);
        scaling.setDuration(1000);

        moveAndScale.addAnimation(scaling);
        moveAndScale.setFillAfter(true);
        mWidgetWebView.startAnimation(moveAndScale);
    }

    @Override
    public void adjustCrawler(float scale, int xAdjust, int yAdjust){
        /*AnimationSet moveAndScale = new AnimationSet(true);

        Animation translate = new TranslateAnimation(mCrawlerWebView.getX(), mCrawlerWebView.getX() + xAdjust, mCrawlerWebView.getY(), mCrawlerWebView.getY() + yAdjust);
        translate.setDuration(SCALE_ANIM_DURATION);
        translate.setFillAfter(true);

        moveAndScale.addAnimation(translate);

        ScaleAnimation scaling = new ScaleAnimation(
                mCrawlerWebView.getScaleX(), scale,
                mCrawlerWebView.getScaleY(), scale,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        scaling.setFillAfter(true);
        scaling.setDuration(1000);

        moveAndScale.addAnimation(scaling);
        moveAndScale.setFillAfter(true);

        mCrawlerWebView.startAnimation(moveAndScale); */
        mCrawlerWebView.setX(mCrawlerWebView.getX() + xAdjust);
        mCrawlerWebView.setY(mCrawlerWebView.getY() + yAdjust);
        mCrawlerWebView.setScaleX(scale);
        mCrawlerWebView.setScaleY(scale);
    }

    @Override
    public void launchCrawler(final String urlPathToApp) {

        loadWebView(mCrawlerWebView, urlPathToApp);

    }

    @Override
    public void launchWidget(final String urlPathToApp, int width, int height) {
        int curWidth = mWidgetWebView.getLayoutParams().width;
        int curHeight = mWidgetWebView.getLayoutParams().height;
        if(curWidth != width || curHeight != height){
            mWidgetWebView.getLayoutParams().height = height;
            mWidgetWebView.getLayoutParams().width = width;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mWidgetWebView.requestLayout();
                }
            });
        }

        loadWebView(mWidgetWebView, urlPathToApp);

    }

    @Override
    public void uiAlert(UIMessage message) {
        showAlert(message);
    }




    /*****************************************
     * TOASTS and ALERTS
     *****************************************/

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

    }

    private void showAlert(final UIMessage message) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                mPopupSystemMessageTV.setText(message.message);

                switch (message.type) {

                    case REDFLAG:
                        //mPopupSystemMessageTV.setBackgroundColor(0xff0000);
                        break;

                    case INFO:
                        //mPopupSystemMessageTV.setBackgroundColor(getResources().getColor(R.color.Palette2a));
                        break;


                    case BOOT:

                        break;
                }

                //mPopupSystemMessageTV.setVisibility(View.VISIBLE);
                mPopupSystemMessageTV.setAlpha(0);
                mPopupSystemMessageTV.animate().alpha(1).setDuration(250);

                mPopupSystemMessageTV.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mPopupSystemMessageTV.animate().alpha(0).setDuration(250);
                        //mPopupSystemMessageTV.setVisibility(View.INVISIBLE);
                    }
                }, 2000);

            }
        });


    }



    /*************************************
     * PLACEHOLDER CODE
     * Not currently used.
     ************************************/

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

    public void injectAppDataIntoWidget(String appdata){
        mWidgetWebView.loadUrl("javascript:GLOBAL_UPDATE_TARGET(" + appdata + ")");
    }

    public void injectAppDataIntoCrawler(String appdata){
        mCrawlerWebView.loadUrl("javascript:GLOBAL_UPDATE_TARGET(" + appdata + ")");
    }

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


}