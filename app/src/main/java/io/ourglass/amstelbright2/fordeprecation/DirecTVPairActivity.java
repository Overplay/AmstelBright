package io.ourglass.amstelbright2.fordeprecation;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import io.ourglass.amstelbright2.R;
import io.ourglass.amstelbright2.core.OGConstants;
import io.ourglass.amstelbright2.core.OGSystem;
import io.ourglass.amstelbright2.realm.OGDevice;
import io.ourglass.amstelbright2.services.ssdpservice.SSDPBroadcastReceiver;
import io.ourglass.amstelbright2.services.ssdpservice.SSDPService;
import io.ourglass.amstelbright2.services.stbservice.DirecTVSetTopBox;
import io.ourglass.amstelbright2.services.stbservice.STBService;
import io.ourglass.amstelbright2.services.stbservice.SetTopBox;
import io.ourglass.amstelbright2.tvui.stb.SetTopBoxAdapter;
import io.realm.Realm;

public class DirecTVPairActivity extends AppCompatActivity {

    public static final String TAG = "DirecTVPairActivity";

    static final int REQUEST_CODE = 43;

    TextView title;

    RelativeLayout contentWrapper;
    TextView errorMsg;
    TextView currentPair;

    ListView directvDevicesList;
    TextView emptyListMessage;
    DirectvDevicesAdapter devicesAdapter;
    View scanningMessage;

    Typeface font;
    Typeface boldFont;

    int contentMarginHor;
    int contentMarginVer;

    // Added by MAK, new SSDP
    ArrayList<SetTopBox> mFoundBoxes = new ArrayList<>();
    SetTopBoxAdapter mSTBArrayAdapter;

    SSDPBroadcastReceiver mSsdpBR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_direc_tvpair);

        final RelativeLayout outerLayout = (RelativeLayout) findViewById(R.id.activity_direc_tvpair);
        contentWrapper = (RelativeLayout) findViewById(R.id.content_wrapper);

        //wait for outerLayout to settle
        outerLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int width = outerLayout.getWidth(), height = outerLayout.getHeight();

                int contentWidth = (int)(width * .8);
                int contentHeight = (int)(height * .84);

                contentMarginHor = (int)(width * .1);
                contentMarginVer = (height - contentHeight) / 2;

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


        //set the font to Exo
        AssetManager am = this.getApplicationContext().getAssets();
        Typeface poppins = font = Typeface.createFromAsset(am, String.format(Locale.US, "fonts/%s", "Poppins-Regular.ttf"));
        boldFont = Typeface.createFromAsset(am, String.format(Locale.US, "fonts/%s", "Poppins-Bold.ttf"));

        title = (TextView) findViewById(R.id.directv_pair_title);
        errorMsg = (TextView) findViewById(R.id.error_msg);
        currentPair = (TextView) findViewById(R.id.current_pair);
        directvDevicesList = (ListView) findViewById(R.id.directv_devices_list);
        emptyListMessage = (TextView) findViewById(R.id.empty_list_message);
        scanningMessage = findViewById(R.id.scanning_message);

        title.setTypeface(boldFont);
        errorMsg.setTypeface(poppins);
        currentPair.setTypeface(poppins);
        emptyListMessage.setTypeface(boldFont);

        // TODO: this is not the new way of doing things
        //directvDevicesList.setAdapter(new DirectvDevicesAdapter(this, STBService.foundBoxes, poppins, boldFont));

        mSTBArrayAdapter = new SetTopBoxAdapter(this, mFoundBoxes, poppins, boldFont);

        directvDevicesList.setAdapter(mSTBArrayAdapter);


    }
    @Override
    public void onResume(){
        super.onResume();

        registerSSDPResponse();
        Intent ssdpi = new Intent(this, SSDPService.class);
        ssdpi.putExtra("deviceFilter", "DIRECTV");
        startService(ssdpi);

        // MAK: Replaced all the Realm based pairing with OGSystem calls
        if(OGSystem.isPairedToSTB()){
            setCurrentPair(OGSystem.getPairedSTBIpAddress());
        }
        else{
            nullifyCurrentPair();
        }

        startCheckLoop();
    }

    @Override
    protected void onPause(){
        unregisterReceiver(mSsdpBR);
        super.onPause();
    }

    public void setErrorMsg(String message){
        emptyListMessage.setVisibility(View.VISIBLE);
        emptyListMessage.setText(message);
    }

    public String lastIpAddressClicked;

    public void setDirectvDevicesList(){
//        devicesAdapter.notifyDataSetChanged();

        final Activity _this = this;
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                for(int i = 0; i < foundDevices.size(); i++){
//                    final int fi = i;
//                    STBService.DirectvBoxInfo info = foundDevices.get(i);
//                    if(info.channelIsNew) {
//                        try {
//                            View child = directvDevicesList.getChildAt(fi);
//                            TextView currentChannel = (TextView) child.findViewById(R.id.dtv_list_elem_curPlaying);
//                            ColorDrawable cd = (ColorDrawable) currentChannel.getBackground();
//
//                            int colorFrom = ContextCompat.getColor(_this, R.color.DodgerBlue);
//                            int colorTo = ContextCompat.getColor(_this, R.color.White);
//
//                            ValueAnimator colorAnim = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
//                            colorAnim.setDuration(2000);
//
//                            colorAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//                                @Override
//                                public void onAnimationUpdate(ValueAnimator animation) {
//                                    if (directvDevicesList == null) {
//                                        Log.w("brrrrr", "directv devices list is null");
//                                    } else {
//                                        View child = directvDevicesList.getChildAt(fi);
//                                        if (child != null) {
//                                            TextView currentChannel = (TextView) child.findViewById(R.id.dtv_list_elem_curPlaying);
//                                            if (currentChannel == null) {
//                                                Log.w("brrrrr", "could not get current channel textview");
//                                            } else {
//                                                currentChannel.setTextColor((int) animation.getAnimatedValue());
//                                            }
//                                        }
//                                    }
//                                }
//                            });
//                            colorAnim.start();
//                            info.channelIsNew = false;
//                        } catch (NullPointerException e){
//                            Log.v("DirectvPair activity", "Something was null, fuck");
//                        }
//                    }
//                }
//            }
//        });

        directvDevicesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                directvDevicesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        SetTopBox selectedBox = mFoundBoxes.get(position);
                        final String ip = selectedBox.ipAddress;
                        final String name = selectedBox.modelName;
                        //final String currentChannel = selectedBox.refreshWhatsPlaying();

                        view.setBackgroundColor(Color.WHITE);

                        TextView friendlyName = (TextView) view.findViewById(R.id.dtv_list_elem_friendlyName);
                        TextView curPlaying = (TextView) view.findViewById(R.id.dtv_list_elem_curPlaying);
                        TextView ipAddr = (TextView) view.findViewById(R.id.dtv_list_elem_ipAddr);
                        TextView idx = (TextView) view.findViewById(R.id.dtv_list_elem_idx_num);
                        View divider = view.findViewById(R.id.divider);

                        int green = OGConstants.DIRECTV_PAIR_ACTIVITY_BACKGROUND_GREEN;

                        friendlyName.setTextColor(green);
                        curPlaying.setTextColor(green);
                        ipAddr.setTextColor(green);
                        idx.setTextColor(green);
                        divider.setBackgroundColor(green);

                        int width = contentWrapper.getWidth();

                        Intent intent = new Intent(_this, DirecTVConfirmActivity.class);
                        intent.putExtra("width", width);
                        intent.putExtra("marginHor", contentMarginHor);
                        intent.putExtra("marginVer", contentMarginVer);
                        intent.putExtra("ip", ip);
                        intent.putExtra("friendlyName", name);
                        //intent.putExtra("currentChannel", currentChannel);
                        intent.putExtra("number", position + 1);

                        lastIpAddressClicked = ip;
                        startActivityForResult(intent, REQUEST_CODE);

                    }
                });

            }
        });

        //todo put this back in
//        if(STBService.foundBoxes.size() == 0){
//            emptyListMessage.setText("No boxes found");//("There seem to be no boxes on the network");
//            emptyListMessage.setVisibility(View.VISIBLE);
//        }
    }

    @Override
    protected void onActivityResult(int requestCode, int result, Intent data){
        if(requestCode == REQUEST_CODE){
            //regardless of the result, we will want to restore the color of the list
            int length = directvDevicesList.getChildCount();
            for(int i = 0; i < length; i++){
                View child = directvDevicesList.getChildAt(i);
                child.setBackgroundColor(OGConstants.DIRECTV_PAIR_ACTIVITY_BACKGROUND_GREEN);

                TextView friendlyName = (TextView) child.findViewById(R.id.dtv_list_elem_friendlyName);
                TextView curPlaying = (TextView) child.findViewById(R.id.dtv_list_elem_curPlaying);
                TextView ipAddr = (TextView) child.findViewById(R.id.dtv_list_elem_ipAddr);
                TextView idx = (TextView) child.findViewById(R.id.dtv_list_elem_idx_num);
                View divider = child.findViewById(R.id.divider);

                friendlyName.setTextColor(Color.WHITE);
                curPlaying.setTextColor(Color.WHITE);
                ipAddr.setTextColor(Color.WHITE);
                idx.setTextColor(Color.WHITE);
                divider.setBackgroundColor(Color.WHITE);
            }

            if(result == OGConstants.DIRECTV_PAIR_CONFIRMED_RESULT_CODE){
                Realm realm = Realm.getDefaultInstance();
                OGDevice device = realm.where(OGDevice.class).findFirst();

                realm.beginTransaction();
                device.pairedSTBAddr = lastIpAddressClicked;
                device.isPairedToSTB = true;
                realm.commitTransaction();
                setCurrentPair(lastIpAddressClicked);
                lastIpAddressClicked = "";
                finish();
            }
            else if(result == OGConstants.DIRECTV_PAIR_CANCELED_RESULT_CODE){
                lastIpAddressClicked = "";
            }
        }
    }

    public void setCurrentPair(String ip){
        this.currentPair.setText(ip);
    }

    public void nullifyCurrentPair(){
        this.currentPair.setText("N/A");
    }

    public void startCheckLoop(){

        final Handler h = new Handler();
        final Context _this = (Context)this;
        h.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                if(STBService.hasSearched){
                    emptyListMessage.setText("");
                    emptyListMessage.setVisibility(View.GONE);

                    setDirectvDevicesList();

                    ((DirectvDevicesAdapter)directvDevicesList.getAdapter()).notifyDataSetChanged();

                    //hide the scanning message
                    scanningMessage.setVisibility(View.GONE);
                    h.postDelayed(this, 1000);
                }
                else {
                    h.postDelayed(this, 500);
                }
            }
        }, 500);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        //the '1' button. Button press that opened this activity should also return to previous
        //activity if pressed again
        if (keyCode == 8 || keyCode == 4){
            finish();
        }

        return false;
    }

    public void uiListRefresh(){

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mSTBArrayAdapter.notifyDataSetChanged();
            }
        });

    }

    // All this shit should go in an intermediary service after Alpha 0!!!
    // TODO: Make dat service

    public void updateDTVArray(HashMap<String, String> devices){

        mSTBArrayAdapter.clear();

        for(Map.Entry<String, String> device : devices.entrySet()){

            DirecTVSetTopBox newSTB = new DirecTVSetTopBox(null, device.getKey(), SetTopBox.STBConnectionType.IPGENERIC, device.getValue());
            newSTB.ssdpResponse = device.getValue(); // Save it for getting the model name
            //newSTB.updateWhatsOn();
            mSTBArrayAdapter.add(newSTB);

        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                emptyListMessage.setText("");
                emptyListMessage.setVisibility(View.GONE);

                setDirectvDevicesList();

                //hide the scanning message
                scanningMessage.setVisibility(View.GONE);
                mSTBArrayAdapter.refreshDevices(new SetTopBoxAdapter.UpdateListener() {
                    @Override
                    public void done() {
                        uiListRefresh();
                    }
                });
            }
        });


    }

    // TODO Handle case where no boxes are found
    public void registerSSDPResponse(){

         mSsdpBR = new SSDPBroadcastReceiver(new SSDPBroadcastReceiver.SSDPBroadcastReceiverListener() {
            @Override
            public void receivedSSDPUpdate(Intent intent) {
                Log.d(TAG, "Got an SSDP update!");
                HashMap<String, String> devices = (HashMap<String, String>)intent.getSerializableExtra("devices");
                if (devices!=null){
                    Log.d(TAG, "Got some DirecTV boxes, updating ArrayList");
                    updateDTVArray(devices);
                }
            }
        });

        IntentFilter filter = new IntentFilter("tv.ourglass.amstelbrightserver.ssdpresponse");
        registerReceiver(mSsdpBR, filter);
    }

}
