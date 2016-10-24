package io.ourglass.amstelbright2.tvui.wifi;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

import io.ourglass.amstelbright2.R;
import io.ourglass.amstelbright2.services.stbservice.STBService;
import io.ourglass.amstelbright2.tvui.wifi.WifiManageActivity;

/**
 * Created by ethan on 9/8/16.
 */

public class WifiNetworksAdapter extends ArrayAdapter<WifiManageActivity.WifiNetworkInfo> {
    Context _this;
    boolean listDisabled = false;

    public WifiNetworksAdapter(Context context, ArrayList<WifiManageActivity.WifiNetworkInfo> networks){
        super(context, 0, networks);
        _this = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        WifiManageActivity.WifiNetworkInfo wifiNetworkInfo = getItem(position);

        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.wifi_list_elem_layout, parent, false);
            convertView.setPadding(40,10,20,10);
        }

        TextView ssid = (TextView) convertView.findViewById(R.id.wifi_list_elem_ssid);
//        TextView securityDescription = (TextView) convertView.findViewById(R.id.wifi_list_elem_security_desc);
        ImageView wifiLevel = (ImageView) convertView.findViewById(R.id.image_wifi_level_unlocked);

        //set the font to Exo
        AssetManager am = _this.getApplicationContext().getAssets();
        Typeface poppins = Typeface.createFromAsset(am, String.format(Locale.US, "fonts/%s", "Poppins-Regular.ttf"));

        ssid.setTypeface(poppins);
//        securityDescription.setTypeface(poppins);

        wifiLevel.setImageLevel(wifiNetworkInfo.signalStrength);

        ssid.setText(wifiNetworkInfo.SSID);

        String securityString = "";
        if(wifiNetworkInfo.WPA && wifiNetworkInfo.WPA2){
            securityString = "Secured with WPA/WPA2";
        }
        else if(wifiNetworkInfo.WPA){
            securityString = "Secured with WPA";
        }
        else if(wifiNetworkInfo.WPA2){
            securityString = "Secured with WPA2";
        }

        if(WifiManageActivity.CURRENT_CONNECTION_STRING.length() != 0 && wifiNetworkInfo.SSID.equals(WifiManageActivity.CURRENT_CONNECTION_STRING)){
            securityString = "Connected";
        }
//        securityDescription.setText(securityString);

        return convertView;
    }

    @Override
    public boolean isEnabled(int position){
        return !listDisabled;
    }

}
