package io.ourglass.amstelbright2.core;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import net.danlew.android.joda.JodaTimeAndroid;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

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

            File appDirectory = new File( Environment.getExternalStorageDirectory() + "/ABLogs" );
            File logDirectory = new File( appDirectory + "/log" );
            Calendar now = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("HH-mm-ss-SSS");
            String time = sdf.format(now.getTime());

            File logFile = new File( logDirectory, "logcat" + time + ".txt" );

            // create app folder
            if ( !appDirectory.exists() ) {
                appDirectory.mkdir();
            }

            // create log folder
            if ( !logDirectory.exists() ) {
                logDirectory.mkdir();
            }

            // clear the previous logcat and then write the new one to the file
            try {
                Process process = Runtime.getRuntime().exec( "logcat -c");
                process = Runtime.getRuntime().exec( "logcat -f " + logFile + "");
            } catch ( IOException e ) {
                e.printStackTrace();
            }

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
