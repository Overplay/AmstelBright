package io.ourglass.amstelbright2.tvui.wifi;

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
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
import android.text.format.Time;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

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
//    TextView wifiListHeader;
    ListView wifiNetworksList;
    TextView emptyListMessage;

    RelativeLayout passwordBox;
    EditText passwordField;
    TextView passwordPrompt;
    Button wifiConnect;

    int passwordFieldMarginTop;

    WifiManager manager;
    WifiInfo currentConnectionInfo = null;

    WifiNetworkInfo selectedConnectionInfo = null;
    Integer selectedConnectionIndex = null;

    IntentFilter intentFilter;
    WifiReceiver wifiReceiver;

    Typeface font;
    Typeface boldFont;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        _this = this;

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_wifi_manage);

        final RelativeLayout outerLayout = (RelativeLayout) findViewById(R.id.activity_wifi_manage);
        final RelativeLayout contentWrapper = (RelativeLayout) findViewById(R.id.wifi_manage_content_wrapper);
        contentWrapper.setBackgroundColor(OGConstants.WIFI_MANAGE_ACTIVITY_BACKGROUND_ORANGE);
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
                final RelativeLayout.LayoutParams wifiNetworksListParams = (RelativeLayout.LayoutParams)wifiNetworksList.getLayoutParams();
                final RelativeLayout.LayoutParams passwordFieldParams = (RelativeLayout.LayoutParams)passwordBox.getLayoutParams();

                contentWrapperParams.setMargins(contentMarginHor, contentMarginVer, contentMarginHor, contentMarginVer);
                contentWrapperParams.width = contentWidth;
                contentWrapperParams.height = contentHeight;

                //set the wifiList to be 30% of the content wrapper with margin=35% each side
                wifiNetworksListParams.width = (int)(.5 * contentWidth);
                wifiNetworksListParams.setMargins((int)(.25 * contentWidth), 0, 0/*(int)(.25 * contentWidth)*/, 0);

                //position the password field correctly
                passwordFieldMarginTop = (int)(contentHeight * .45);
                passwordFieldParams.leftMargin = (int)(.60 * contentWidth);
                passwordFieldParams.topMargin = passwordFieldMarginTop;
                passwordFieldParams.width = (int)(contentWidth * .35);
                passwordBox.setLayoutParams(passwordFieldParams);

                contentWrapper.setLayoutParams(contentWrapperParams);
                //contentWrapper.setPadding(10, 10, 10, 10);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        contentWrapper.requestLayout();
                        wifiNetworksList.requestLayout();
                        passwordField.requestLayout();
                    }
                });
                contentWrapper.getViewTreeObserver().removeOnGlobalLayoutListener(this);


            }
        });

        //set the font to Exo
        AssetManager am = this.getApplicationContext().getAssets();
        Typeface poppins = font = Typeface.createFromAsset(am, String.format(Locale.US, "fonts/%s", "Poppins-Regular.ttf"));
        boldFont = Typeface.createFromAsset(am, String.format(Locale.US, "fonts/%s", "Poppins-Bold.ttf"));

        /* fetch all of the things from the xml */
        title = (TextView) findViewById(R.id.wifi_pair_title);
        currentConnection = (TextView) findViewById(R.id.wifi_current_connection);
        //wifiListHeader = (TextView) findViewById(R.id.wifi_list_header);
        wifiNetworksList = (ListView) findViewById(R.id.wifi_networks_list);
        emptyListMessage = (TextView) findViewById(R.id.empty_list_message);
        passwordBox = (RelativeLayout) findViewById(R.id.wifi_password_box);
        passwordField = (EditText) findViewById(R.id.wifi_password_edit_text);
        passwordPrompt = (TextView) findViewById(R.id.wifi_password_prompt);
        wifiConnect = (Button) findViewById(R.id.wifi_connect_button);

        title.setTypeface(boldFont);
        currentConnection.setTypeface(font);
        //wifiListHeader.setTypeface(font);
        emptyListMessage.setTypeface(font);
        passwordField.setTypeface(font);
        passwordPrompt.setTypeface(font);


        wifiNetworksList.setPadding(10, 10, 10, 10);

        manager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        //user wifiManager to get Scan Results of available networks
        List<ScanResult> results = manager.getScanResults();
        networks = new ArrayList<>();
        for(ScanResult scan : results){
            if(scan.SSID != null && scan.SSID.length() != 0) {
                networks.add(new WifiNetworkInfo(scan));
                scan.SSID = "FUARK";
                networks.add(new WifiNetworkInfo(scan));
                scan.SSID = "Overplay";
                networks.add(new WifiNetworkInfo(scan));
            }
        }

        Log.v(TAG, "There seem to be " + networks.size() + " on the network");

        //set the array adapter to the garbage string list
        //change this to a custom adapter with a custom view
        wifiNetworksAdapter = new WifiNetworksAdapter(this, networks);

        wifiNetworksList.setAdapter(wifiNetworksAdapter);
        wifiNetworksList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            long lastAcceptedClick = 0;

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                long currentTime = Calendar.getInstance().getTimeInMillis();
                if((currentTime - lastAcceptedClick) > OGConstants.BUTTON_CLICK_DEBOUNCE) {
                    //reset the debounce timer
                    lastAcceptedClick = currentTime;
                    int newLeftMargin;
                    //closing the password prompt
                    if(selectedConnectionIndex != null && position == selectedConnectionIndex){
                        selectedConnectionIndex = null;
                        selectedConnectionInfo = null;
                        view.setBackgroundColor(OGConstants.WIFI_MANAGE_ACTIVITY_BACKGROUND_ORANGE);
                        ((TextView)view.findViewById(R.id.wifi_list_elem_ssid)).setTextColor(Color.WHITE);
                        ((ImageView)view.findViewById(R.id.image_wifi_level_unlocked)).setColorFilter(Color.WHITE);
                        newLeftMargin = 250;
                    }
                    //opening the password prompt
                    else {
                        //if there is a previously highlighted view, then unhighlight
                        if(selectedConnectionIndex != null){
                            View previouslySelected = wifiNetworksList.getChildAt(selectedConnectionIndex);
                            if(previouslySelected != null){
                                previouslySelected.setBackgroundColor(OGConstants.WIFI_MANAGE_ACTIVITY_BACKGROUND_ORANGE);
                                ((TextView)previouslySelected.findViewById(R.id.wifi_list_elem_ssid)).setTextColor(Color.WHITE);
                                ((ImageView)previouslySelected.findViewById(R.id.image_wifi_level_unlocked)).setColorFilter(Color.WHITE);
                            }
                        }
                        //highlight the selected network
                        view.setBackgroundColor(Color.WHITE);
                        ((TextView)view.findViewById(R.id.wifi_list_elem_ssid)).setTextColor(OGConstants.WIFI_MANAGE_ACTIVITY_BACKGROUND_ORANGE);
                        ((ImageView)view.findViewById(R.id.image_wifi_level_unlocked)).setColorFilter(OGConstants.WIFI_MANAGE_ACTIVITY_BACKGROUND_ORANGE);

                        selectedConnectionIndex = position;
                        selectedConnectionInfo = wifiNetworksAdapter.getItem(position);
                        newLeftMargin = 50;
                    }


                    //move the listview accordingly
                    final ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) wifiNetworksList
                            .getLayoutParams();
                    final int newLeftMarginFinal = newLeftMargin;
                    if(layoutParams.leftMargin != newLeftMargin) {
                        ValueAnimator animator = ValueAnimator.ofInt(layoutParams.leftMargin, newLeftMargin);
                        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                layoutParams.leftMargin = (Integer) animation.getAnimatedValue();
                                if(layoutParams.leftMargin == 50)
                                    passwordBox.setVisibility(View.VISIBLE);
                                else {
                                    passwordBox.setVisibility(View.GONE);
                                }
                                wifiNetworksList.requestLayout();
                            }
                        });
                        animator.setDuration(500);
                        animator.start();
                    }
                    passwordField.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                            if(s.length() >= 8){
                                wifiConnect.setEnabled(true);
                            }
                            else {
                                wifiConnect.setEnabled(false);
                            }
                        }

                        @Override
                        public void afterTextChanged(Editable s) {

                        }
                    });
                    //set the onclick for the showpassword checkbox
                    CheckBox showPass = (CheckBox) findViewById(R.id.toggle_show_password);
                    showPass.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            //save cursor position
                            EditText passField = (EditText)findViewById(R.id.wifi_password_edit_text);
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
            }
        });

        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);

        wifiReceiver = new WifiReceiver();
        registerReceiver(wifiReceiver, intentFilter);

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

        wifiConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selectedConnectionInfo != null) {
                    if (selectedConnectionInfo.secured) {

                        String password = passwordField.getText().toString();
                        Log.v(TAG, "Trying to connect to " + selectedConnectionInfo.SSID + " with " + password);
                        if (selectedConnectionInfo.WEP) {
                            WifiUtils.connectToWEPSecuredNetwork(selectedConnectionInfo.SSID, password, manager);
                        } else if (selectedConnectionInfo.WPA || selectedConnectionInfo.WPA2) {
                            WifiUtils.connectToWPASecuredNetwork(selectedConnectionInfo.SSID, password, manager);
                        }
//                        might need a different case here but I think it probably is the same as WPA
//                        else if(info.WPA2){
//
//                        }
                    } else {
                        WifiUtils.connectToOpenNetwork(selectedConnectionInfo.SSID, manager);
                    }
                }
                else {
                    Log.w(TAG, "Tried to connect to a network, and there was not one currently selected");
                }
            }
        });
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        unregisterReceiver(wifiReceiver);
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

                if(ssid != null) {
                    OGNotifications.sendStatusIntent("STATUS", "Connected to " + ssid, 0);
                    ((Activity) WifiManageActivity._this).finish();
                }
            }
            else {
                String errMsg = "Could not connect";
                if(info.getReason() != null && !info.getReason().equals("null")){
                    errMsg += " - " + info.getReason();
                }
                OGNotifications.sendStatusIntent("STATUS", errMsg, 0);
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

//            EditText passField = (EditText) dialog.findViewById(R.id.wifi_password_edit_text);
//            passField.getBackground().mutate().setColorFilter(getResources().getColor(R.color.Crimson), PorterDuff.Mode.SRC_ATOP);

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
