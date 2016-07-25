package io.ourglass.amstelbright2.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import io.ourglass.amstelbright2.tvui.MainframeActivity;

public class BootDoneReceiver extends BroadcastReceiver {
    public BootDoneReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){
            Intent i = new Intent(context, MainframeActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
    }
}
