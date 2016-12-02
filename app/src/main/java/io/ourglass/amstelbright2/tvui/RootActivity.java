package io.ourglass.amstelbright2.tvui;


import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import io.ourglass.amstelbright2.R;
import io.ourglass.amstelbright2.core.OGCore;
import io.ourglass.amstelbright2.core.OGSystem;
import io.ourglass.amstelbright2.core.OGSystemExceptionHander;
import io.ourglass.amstelbright2.services.amstelbright.AmstelBrightService;


/**
 *
 * Why is this here? This shim Activity is here simply to make sure the TV Surface is always
 * running even if Mainframe crashes. All it does is set up the TV surface ans start AB Service.
 *
 */
public class RootActivity extends Activity {

    private static final String TAG = "RootActivity";

    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;

    private SurfaceView surfaceView;
    private RelativeLayout mMainLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Thread.setDefaultUncaughtExceptionHandler(new OGSystemExceptionHander(this,
                RootActivity.class));

        checksCameraPermission();
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Hide both the navigation bar and the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        getWindow().getDecorView().setSystemUiVisibility(uiOptions);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.root_layout);

        Log.d(TAG, "OS Level: "+ OGSystem.osLevel());
        Log.d(TAG, "Is demo H/W? " + (OGSystem.isTronsmart()?"YES":"NO"));
        Log.d(TAG, "Is real OG H/W? " + (OGSystem.isRealOG()?"YES":"NO"));
        Log.d(TAG, "Is emulated H/W? " + (OGSystem.isEmulator()?"YES":"NO"));


       //check started in test mode
        Intent currentIntent = getIntent();
        boolean testMode = currentIntent.getBooleanExtra("testMode", false);

        //pass testMode onto abService, defaults to false
        Intent abServiceIntent = new Intent(getBaseContext(), AmstelBrightService.class);
        abServiceIntent.putExtra("testMode", testMode);

        startService(abServiceIntent);

     // surfaceView = (SurfaceView)findViewById(R.id.surfaceView);


        Log.v(TAG, "Creating system alert log for restart");
        //log the restart
        OGCore.log_alert("System cold boot", "Logged in onCreate of RootActivity");

        Log.d(TAG, "onCreate done");


    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d(TAG, "onResume done");
        Intent mainIntent = new Intent(this, MainframeActivity.class);
        startActivity(mainIntent);

    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onStop() {
        super.onStop();
     }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

//        // 82 = Menu button,
//
//        if (keyCode == 82 || keyCode == 41) {
//            toggleAppMenu();
//            return false;
//        }
//
//        // Launch settings from button 0 on remote
//        if ( keyCode == 7 || keyCode == 4 ){
//            startActivityForResult(new Intent(android.provider.Settings.ACTION_SETTINGS), 0);
//        }
//
//        if (keyCode == 8){
//            Intent intent = new Intent(this, SetTopBoxPairActivity.class);
//            startActivity(intent);
//        }
//
//        // Out until we can do more debug
////        if (keyCode == 9){
////            Intent intent = new Intent(this, WifiManageActivity.class);
////            startActivity(intent);
////        }
//
//
//        //showAlert(new UIMessage("You pushed key " + keyCode));

        return false;
    }



    /*****************************************
     * TOASTS and ALERTS
     *****************************************/

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

    }

    /*****************************************
     * Camera Permission code
     *****************************************/

    public void checksCameraPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.d("MyApp", "SDK >= 23");
            if (checkSelfPermission(Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.d("MyApp", "Request permission");
                requestPermissions(new String[] {Manifest.permission.CAMERA}, REQUEST_CODE_ASK_PERMISSIONS);

            }
            else {
                Log.d("og1rxhdmi", "Permission granted: taking pic");

            }
        }
        else {
            Log.d("og1rxhdmi", "Android < 6.0");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    Toast.makeText(this, "You did not allow camera usage :(", Toast.LENGTH_SHORT).show();

                }
                return;
            }
        }
    }
}