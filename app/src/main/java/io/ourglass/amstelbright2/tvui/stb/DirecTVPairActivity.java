package io.ourglass.amstelbright2.tvui.stb;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Locale;

import io.ourglass.amstelbright2.R;
import io.ourglass.amstelbright2.core.OGNotifications;
import io.ourglass.amstelbright2.realm.OGDevice;
import io.ourglass.amstelbright2.services.stbservice.STBService;
import io.realm.Realm;

public class DirecTVPairActivity extends AppCompatActivity {

    TextView title;

    RelativeLayout contentWrapper;
    TextView errorMsg;
    TextView currentPair;
    TextView deviceListHeader;
    ListView directvDevicesList;
    TextView emptyListMessage;
    DirectvDevicesAdapter devicesAdapter;

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


        //set the font to Exo
        AssetManager am = this.getApplicationContext().getAssets();
        Typeface exo2Typeface = Typeface.createFromAsset(am, String.format(Locale.US, "fonts/%s", "Exo2-Medium.ttf"));

        title = (TextView) findViewById(R.id.directv_pair_title);
        errorMsg = (TextView) findViewById(R.id.error_msg);
        currentPair = (TextView) findViewById(R.id.current_pair);
        deviceListHeader = (TextView) findViewById(R.id.device_list_header);
        directvDevicesList = (ListView) findViewById(R.id.directv_devices_list);
        emptyListMessage = (TextView) findViewById(R.id.empty_list_message);

        title.setTypeface(exo2Typeface);
        errorMsg.setTypeface(exo2Typeface);
        currentPair.setTypeface(exo2Typeface);
        deviceListHeader.setTypeface(exo2Typeface);
        emptyListMessage.setTypeface(exo2Typeface);

        directvDevicesList.setAdapter(new DirectvDevicesAdapter(this, STBService.foundBoxes));

        String pairedSTB = OGDevice.getPairedSTBOrNull(Realm.getDefaultInstance());
        if(pairedSTB != null){
            setCurrentPair(pairedSTB);
        }
        else{
            nullifyCurrentPair();
        }
        startCheckLoop();
    }

    public void setErrorMsg(String message){
        emptyListMessage.setVisibility(View.VISIBLE);
        emptyListMessage.setText(message);
    }

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
                        final String ip = STBService.foundBoxes.get(position).ipAddr;
                        //final String ip = info.substring(0, info.indexOf('\n'));

                        AlertDialog.Builder builder = new AlertDialog.Builder(_this);

                        builder.setMessage("Are you sure you want to pair with " + ip + "?");
                        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                OGDevice.setPairedSTB(Realm.getDefaultInstance(), ip);
                                setCurrentPair(ip);

                                OGNotifications.sendStatusIntent("","Successfully paired with " + ip, 0);
                                finish();
                            }
                        });
                        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //cancel the dialog
                            }
                        });
                        AlertDialog dialog = builder.create();

                        dialog.show();
                    }
                });

            }
        });

        if(STBService.foundBoxes.size() == 0){
            emptyListMessage.setText("There seem to be no boxes on the network");
            emptyListMessage.setVisibility(View.VISIBLE);
        }

    }

    public void setCurrentPair(String ip){
        this.currentPair.setText("paired with " + ip);
    }

    public void nullifyCurrentPair(){
        this.currentPair.setText("currently not paired");
    }

    public void startCheckLoop(){
        final Handler h = new Handler();
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
                    h.postDelayed(this, 1000);
                }
                else {
                    String msg = emptyListMessage.getText().toString();
                    String msgTemplate = "Not ready yet";

                    if(msg.contains("...")){
                        msg = msgTemplate + "   ";
                    }
                    else if(msg.contains("..")){
                        msg = msgTemplate + "...";
                    }
                    else if(msg.contains(".")){
                        msg = msgTemplate + ".. ";
                    }
                    else{
                        msg = msgTemplate + ".  ";
                    }
//                    int idx;
//                    if((idx = msg.indexOf("...")) != -1){
//                        msg = msg.substring(0, idx);
//                    }
//                    else {
//                        msg += ".";
//                    }
                    setErrorMsg(msg);
                    h.postDelayed(this, 500);
                }
            }
        }, 500);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        //the '1' button. Button press that opened this activity should also return to previous
        //activity if pressed again
        if (keyCode == 8){
            finish();
        }
        return false;
    }
}
