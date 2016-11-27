package io.ourglass.amstelbright2.tvui;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;
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

    private SurfaceView surfaceView;

    private RelativeLayout mMainLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Thread.setDefaultUncaughtExceptionHandler(new OGSystemExceptionHander(this,
                RootActivity.class));


        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

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


        if (!OGSystem.enableHDMI()) {
            // The color change doesn't seem to do anything...:(.. not worth stressing.

            surfaceView = (SurfaceView)findViewById(R.id.surfaceView);
            surfaceView.setVisibility(View.INVISIBLE);

            mMainLayout.setBackgroundColor(getResources().getColor(R.color.Turquoise));
            Log.d(TAG, "Running in emulator or on OG H/W without libs, skipping HDMI passthru.");


        }

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



}