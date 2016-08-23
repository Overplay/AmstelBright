package io.ourglass.amstelbright2.tvui;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

import io.ourglass.amstelbright2.R;
import io.ourglass.amstelbright2.realm.OGDevice;
import io.ourglass.amstelbright2.services.amstelbright.AmstelBrightService;
import io.ourglass.amstelbright2.services.stbservice.STBService;
import io.realm.Realm;
import okhttp3.OkHttpClient;

public class DirecTVPairActivity extends AppCompatActivity {

    TextView errorMsg;
    TextView currentPair;
    TextView deviceListHeader;
    ListView directvDevicesList;
    TextView emptyListMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_direc_tvpair);

        errorMsg = (TextView) findViewById(R.id.error_msg);
        currentPair = (TextView) findViewById(R.id.current_pair);
        deviceListHeader = (TextView) findViewById(R.id.device_list_header);
        directvDevicesList = (ListView) findViewById(R.id.directv_devices_list);
        emptyListMessage = (TextView) findViewById(R.id.empty_list_message);

        String pairedSTB = OGDevice.getPairedSTBOrNull(Realm.getDefaultInstance());
        if(pairedSTB != null){
            setCurrentPair(pairedSTB);
        }
        else{
            nullifyCurrentPair();
        }

//        Intent intent = getIntent();
//        boolean success = intent.getBooleanExtra("success", false);
        boolean ready = STBService.hasSearched;
        if(ready){
//            List<String> list = intent.getStringArrayListExtra("devices");

            setDirectvDevicesList(STBService.foundIps);
        }
        else {
//            String err = intent.getStringExtra("errMsg");
//            err = err == null ? "There was an error retrieving devices" : err;
            setErrorMsg("Not ready yet");
            startCheckLoop();
        }

        //setTitle("DirecTV pairing");
    }

    public void setErrorMsg(String message){
        errorMsg.setVisibility(View.VISIBLE);
        errorMsg.setText(message);
    }

    public void setDirectvDevicesList(final List<String> foundDevices){
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, foundDevices);
        directvDevicesList.setAdapter(adapter);

        final Activity _this = this;
        directvDevicesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                directvDevicesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        final String ip = foundDevices.get(position);
                        //final String ip = info.substring(0, info.indexOf('\n'));

                        AlertDialog.Builder builder = new AlertDialog.Builder(_this);

                        builder.setMessage("Are you sure you want to pair with " + ip + "?");
                        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                OGDevice.setPairedSTB(Realm.getDefaultInstance(), ip);
                                setCurrentPair(ip);
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

        if(foundDevices.size() == 0){
            emptyListMessage.setVisibility(View.VISIBLE);
        }

    }

    public void setCurrentPair(String ip){
        this.currentPair.setText("Currently paired with: " + ip);
    }

    public void nullifyCurrentPair(){
        this.currentPair.setText("Currently not paired");
    }

    public void startCheckLoop(){
        final Handler h = new Handler();
        h.postDelayed(new Runnable()
        {
            private long time = 0;

            @Override
            public void run()
            {
                if(STBService.hasSearched){
                    errorMsg.setText("");
                    errorMsg.setVisibility(View.GONE);
                    setDirectvDevicesList(STBService.foundIps);
                }
                else {
                    String msg = errorMsg.getText().toString();
                    int idx;
                    if((idx = msg.indexOf("...")) != -1){
                        msg = msg.substring(0, idx);
                    }
                    else {
                        msg += ".";
                    }
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
