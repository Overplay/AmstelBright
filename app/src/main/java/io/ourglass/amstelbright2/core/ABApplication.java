package io.ourglass.amstelbright2.core;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import net.danlew.android.joda.JodaTimeAndroid;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import io.ourglass.amstelbright2.services.logcat.LogCatRotationService;
import io.ourglass.amstelbright2.tvui.RootActivity;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import okhttp3.OkHttpClient;

/**
 * Created by mkahn on 5/18/16.
 */
public class ABApplication extends Application {

    public static Context sharedContext;
    public static final String TAG = "ABApplication";

    // Shared by all!
    public static final OkHttpClient okclient = new OkHttpClient();

    private LogCatRotationService mLogCatService;

    @Override
    public void onCreate() {
        super.onCreate();
        // The realm file will be located in Context.getFilesDir() with name "default.realm"
        Log.d(TAG, "Loading AB application");

        sharedContext = getApplicationContext();

        bringUpEthernet();

        RealmConfiguration config = new RealmConfiguration.Builder(this)
                .name("ab.realm")
                .schemaVersion(1)
                .build();

        Realm.setDefaultConfiguration(config);

        PackageInfo pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
            int code = pInfo.versionCode;
            OGSystem.setABVersionName(version);
            OGSystem.setABVersionCode(code);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        // Logcat messages go to a file...
        if ( isExternalStorageWritable() && OGConstants.LOGCAT_TO_FILE ) {

            final Intent logCatServiceIntent = new Intent(this, LogCatRotationService.class);
//            //check if bucanero LogCatService is installed
//            boolean logCatServiceInstalled = ABApplication.packageExists(this, OGConstants.LOG_CAT_SERVICE_PACKAGE_NAME);
//            if(!logCatServiceInstalled){
//                Log.w(TAG, "log cat service is not installed and cannot run");
//            }
//            else {
//                Log.v(TAG, "Log cat service is installed!");
//
//                //start the external bucanero LogCatService
//                Intent logCatServiceIntent = new Intent();
//                logCatServiceIntent.setClassName(
//                        OGConstants.LOG_CAT_SERVICE_PACKAGE_NAME,
//                        OGConstants.LOG_CAT_SERVICE_PACKAGE_NAME + "." + OGConstants.LOG_CAT_SERVICE_SERVICE_NAME);
//
//                logCatServiceIntent.putExtra("uuid", OGSystem.uniqueDeviceId());
//                logCatServiceIntent.putExtra("asahiAddress", OGConstants.ASAHI_ADDRESS);
//            ComponentName c = startService(logCatServiceIntent);

            startService(logCatServiceIntent);


        }

        JodaTimeAndroid.init(this);

    }

    public static void dbToast(Context context, String message){
        if (OGConstants.SHOW_DB_TOASTS){
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if ( Environment.MEDIA_MOUNTED.equals( state ) ) {
            return true;
        }
        return false;
    }

    public void launchUDHCPd(){

        Log.d(TAG, "Firing up UDHCPD");

        ShellExecutor bringUpUdhcpd = new ShellExecutor(new ShellExecutor.ShellExecutorListener() {
            @Override
            public void results(String results) {
                Log.d(TAG, "UDHCPD>>>>> "+results);
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                OGSystem.checkHardSTBConnection();

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                OGSystem.checkHardSTBConnection();

                try {
                    Thread.sleep(15000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                OGSystem.checkHardSTBConnection();

                try {
                    Thread.sleep(60000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                OGSystem.checkHardSTBConnection();

                try {
                    Thread.sleep(120000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                OGSystem.checkHardSTBConnection();


            }
        });

        bringUpUdhcpd.exec("su -c /system/bin/busybox udhcpd /mnt/sdcard/wwwaqui/conf/udhcpd.conf");

    }

    public void bringUpEthernet(){

        Log.d(TAG, "Bringing up ethernet interface.");

        ShellExecutor bringUpEth = new ShellExecutor(new ShellExecutor.ShellExecutorListener() {
            @Override
            public void results(String results) {
                Log.d(TAG, "IFUP>>>>> "+results);
                launchUDHCPd();
            }
        });

        bringUpEth.exec("su -c /system/bin/busybox ifconfig eth0 10.21.200.1 netmask 255.255.255.0");
    }

}
