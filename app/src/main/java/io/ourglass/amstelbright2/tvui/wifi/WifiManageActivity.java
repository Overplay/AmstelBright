package io.ourglass.amstelbright2.tvui.wifi;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.ourglass.amstelbright2.R;
import io.ourglass.amstelbright2.core.OGConstants;
import io.ourglass.amstelbright2.core.OGNotifications;
import io.ourglass.amstelbright2.tvui.wifi.WifiNetworksAdapter;

/**
 * Created by ethan on 9/14/16.
 */

public class WifiManageActivity extends AppCompatActivity{
    final String TAG = "WifiManageActivity";

    public static String CURRENT_CONNECTION_STRING = "";
    public static Context _this;
    ArrayList<WifiNetworkInfo> networks;
    WifiNetworksAdapter wifiNetworksAdapter;
    
    TextView title;
    TextView currentConnection;
    TextView wifiListHeader;
    ListView wifiNetworksList;
    TextView emptyListMessage;

    WifiManager manager;
    WifiInfo currentConnectionInfo = null;

    IntentFilter intentFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        _this = this;

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_wifi_manage);

        final RelativeLayout outerLayout = (RelativeLayout) findViewById(R.id.activity_wifi_manage);
        final RelativeLayout contentWrapper = (RelativeLayout) findViewById(R.id.wifi_manage_content_wrapper);

        //wait for outerLayout to settle
        outerLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int width = outerLayout.getWidth(), height = outerLayout.getHeight();

                int contentWidth = (int)(width * .8);
                int contentHeight = (int)(height * .84);

                int contentMarginHor = (int)(width * .1);
                int contentMarginVer = (height - contentHeight) / 2;

                RelativeLayout.LayoutParams contentWrapperParams = (RelativeLayout.LayoutParams)contentWrapper.getLayoutParams();

                contentWrapperParams.setMargins(contentMarginHor, contentMarginVer, contentMarginHor, contentMarginVer);
                contentWrapperParams.width = contentWidth;
                contentWrapperParams.height = contentHeight;

                contentWrapper.setLayoutParams(contentWrapperParams);
                //contentWrapper.setPadding(10, 10, 10, 10);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        contentWrapper.requestLayout();
                    }
                });
                contentWrapper.getViewTreeObserver().removeOnGlobalLayoutListener(this);

            }
        });
        /* fetch all of the things from the xml */
        title = (TextView) findViewById(R.id.wifi_pair_title);
        currentConnection = (TextView) findViewById(R.id.wifi_current_connection);
        wifiListHeader = (TextView) findViewById(R.id.wifi_list_header);
        wifiNetworksList = (ListView) findViewById(R.id.wifi_networks_list);
        emptyListMessage = (TextView) findViewById(R.id.empty_list_message);

        wifiNetworksList.setPadding(10, 10, 10, 10);

        manager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        //user wifiManager to get Scan Results of available networks
        List<ScanResult> results = manager.getScanResults();
        networks = new ArrayList<>();
        for(ScanResult scan : results){
            networks.add(new WifiNetworkInfo(scan));
        }

        Log.v(TAG, "There seem to be " + networks.size() + " on the network");

        //set the array adapter to the garbage string list
        //change this to a custom adapter with a custom view
        wifiNetworksAdapter = new WifiNetworksAdapter(this, networks);

        wifiNetworksList.setAdapter(wifiNetworksAdapter);
        wifiNetworksList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //todo flush out the onclick code
                WifiNetworkInfo info = networks.get(position);
                if(info != null) {
                    Log.v(TAG, "Attempting to connect to " + info.SSID);
                    connectToNetwork(info);
                }
            }
        });

        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
        registerReceiver(new WifiReceiver(), intentFilter);

        ConnectivityManager connManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (networkInfo.isConnected()) {
            final WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
            final WifiInfo connectionInfo = wifiManager.getConnectionInfo();
            if (connectionInfo != null && !TextUtils.isEmpty(connectionInfo.getSSID())) {
                WifiManageActivity.CURRENT_CONNECTION_STRING = connectionInfo.getSSID().replace("\"", "");
                currentConnection.setText("Connected to " + CURRENT_CONNECTION_STRING);
            }
            else {
                currentConnection.setText("Not connected");
            }
        }
    }

    /**
     * class to capture wifi state change event
     */
    public static class WifiReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if(info != null && info.isConnected()) {
                // Do your work.
                Log.v("WifiReceiver", "received a wifi state change");

                // e.g. To check the Network Name or other info:
                WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                String ssid = wifiInfo.getSSID();

                OGNotifications.sendStatusIntent("STATUS", "Connected to " + ssid, 0);
                ((Activity)WifiManageActivity._this).finish();
            }
            else {
                OGNotifications.sendStatusIntent("STATUS", "Could not connect - " + info.getReason(), 0);
            }
        }
    }

    public class WifiNetworkInfo {
        public String SSID;
        public int signalStrength; //Poor, Fair, Good, Excellent
        public boolean WEP;
        public boolean WPA;
        public boolean WPA2;
        public boolean secured;
        public String capabilitiesFull;
        public String capabilitiesShort;

        public WifiNetworkInfo(ScanResult scan){
            SSID = scan.SSID;

            String capabilities = scan.capabilities;

            WPA = capabilities.contains("WPA-");
            WPA2 = capabilities.contains("WPA2-");

            //todo figure out what WEP looks like in capabilities (none of the devices on our network seem to offer it)

            secured = WPA || WPA2 || WEP;

            signalStrength = WifiManager.calculateSignalLevel(scan.level, OGConstants.WIFI_STRENGTH_LEVELS.length);
            if(secured && signalStrength != 0) signalStrength += 4;

            this.capabilitiesFull = scan.capabilities;
            this.capabilitiesShort = this.capabilitiesFull.replace("[", "");
            this.capabilitiesShort = this.capabilitiesShort.replace("-", " ");
            this.capabilitiesShort = this.capabilitiesShort.substring(0, this.capabilitiesShort.indexOf("]"));
        }

        public String getSignalStrengthString(){
            int strength = this.signalStrength;
            if(strength > 4) strength -= 4;
            switch(strength){
                case 0: return "None";
                case 1: return "Poor";
                case 2: return "Fair";
                case 3: return "Good";
                case 4: return "Excellent";
                default: return "";
            }
        }
    }

    //todo flush out the refresh loop
    //todo actually probably won't need this, and can get away with what we have at activityLaunchTime
//    private void refreshWifiNetworksList(){
//        String currentConnectionInformationStr = "Not connected";
//
//        networks = manager.getScanResults();
//
//        int state = manager.getWifiState();
//
//        this.currentConnectionInfo = manager.getConnectionInfo();
//        if(currentConnectionInfo != null){;
//            currentConnectionInformationStr = "Connected to " + currentConnectionInfo.getSSID().replace("\"", "");
//        }
//
//        while(nets.size() > 0)
//            nets.remove(0);
//        for(ScanResult net : networks){
//            nets.add(net.SSID);
//        }
//
//        final String connectInfo = currentConnectionInformationStr;
//        runOnUiThread(new Runnable(){
//            @Override
//            public void run(){
//                currentConnection.setText(connectInfo);
//                arrayAdapter.notifyDataSetChanged();
//            }
//        });
//    }

    private void getCurrentConnection(){
        String currentConnectionInformationStr = "Not connected";

        this.currentConnectionInfo = manager.getConnectionInfo();
        if(this.currentConnectionInfo != null){
            currentConnectionInformationStr = "Connected to " + currentConnectionInfo.getSSID().replace("\"", "");
        }

        final String connectInfo = currentConnectionInformationStr;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                currentConnection.setText(connectInfo);
            }
        });
    }

    //todo flush out connect to network code - this is probably gonna be tricky as fuck
    private void connectToNetwork(final WifiNetworkInfo info){
        final String SSID = info.SSID;
        final WifiConfiguration conf = new WifiConfiguration();
        conf.SSID = SSID; //needs to be surrounded by quotations but should already be coming from a scan object
        if(info.secured){
            //prompt for password
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setPositiveButton("Connect", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(info.secured){
                        EditText editText = (EditText)((AlertDialog)dialog).findViewById(R.id.wifi_password_edit_text);

                        String password = editText.getText().toString();
                        if(info.WEP) {
                            WifiUtils.connectToWEPSecuredNetwork(SSID, password, manager);
                        }
                        else if(info.WPA || info.WPA2){
                            WifiUtils.connectToWPASecuredNetwork(SSID, password, manager);
                        }
//                        might need a different case here but I think it probably is the same as WPA
//                        else if(info.WPA2){
//
//                        }
                    }
                    else {
                        WifiUtils.connectToOpenNetwork(SSID, manager);
                    }
                }
            });

            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            LayoutInflater inflater = this.getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dialog_wifi_manage_password, null);
            builder.setView(dialogView);
            final AlertDialog dialog = builder.create();
            dialog.show();

            dialog.getButton(AlertDialog.BUTTON1).setEnabled(false);

            TextView signalStrength = (TextView) dialog.findViewById(R.id.signal_strength_field);
            signalStrength.setText(info.getSignalStrengthString());

            TextView securityField = (TextView) dialog.findViewById(R.id.security_field);
            securityField.setText(info.capabilitiesShort);

            EditText passField = (EditText) dialog.findViewById(R.id.wifi_password_edit_text);
            passField.getBackground().mutate().setColorFilter(getResources().getColor(R.color.Crimson), PorterDuff.Mode.SRC_ATOP);
            passField.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if(s.length() >= 8){
                        dialog.getButton(AlertDialog.BUTTON1).setEnabled(true);
                    }
                    else {
                        dialog.getButton(AlertDialog.BUTTON1).setEnabled(false);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            TextView title = (TextView) dialog.findViewById(R.id.wifi_password_entry_title);
            title.setText(info.SSID);

            CheckBox showPass = (CheckBox) dialog.findViewById(R.id.toggle_show_password);
            showPass.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    //save cursor position
                    EditText passField = (EditText)dialog.findViewById(R.id.wifi_password_edit_text);
                    if(passField == null) {
                        Log.w(TAG, "Password field could not be found");
                        return;
                    }

                    int start = passField.getSelectionStart(), end = passField.getSelectionEnd();

                    if(!isChecked){
                        passField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    }
                    else{
                        passField.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    }
                    passField.setSelection(start,end);
                }
            });

        }
        else {
            conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            manager.addNetwork(conf);
            List<WifiConfiguration> list = manager.getConfiguredNetworks();
            for( WifiConfiguration i : list ) {
                if(i.SSID != null && i.SSID.equals(SSID)) {
                    manager.disconnect();
                    manager.enableNetwork(i.networkId, true);
                    manager.reconnect();
                    break;
                }
            }
        }
    }

}
