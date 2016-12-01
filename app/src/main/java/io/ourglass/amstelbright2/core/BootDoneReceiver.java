package io.ourglass.amstelbright2.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import io.ourglass.amstelbright2.tvui.MainframeActivity;

/**
 * Not sure this is needed any longer, but I'll leave it for now.
 */
public class BootDoneReceiver extends BroadcastReceiver {

    public static final String TAG = "BootDoneReceiver";

    public BootDoneReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d(TAG, "BootDoneReceiver received an intent");
        if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){
            Intent i = new Intent(context, MainframeActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
    }

}
