package io.ourglass.amstelbright2.tvui.stb;

import android.content.Context;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import io.ourglass.amstelbright2.R;
import io.ourglass.amstelbright2.services.stbservice.STBService;

/**
 * Created by ethan on 9/8/16.
 */

public class DirectvDevicesAdapter extends ArrayAdapter<STBService.DirectvBoxInfo> {
    public DirectvDevicesAdapter(Context context, ArrayList<STBService.DirectvBoxInfo> boxes){
        super(context, 0, boxes);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        STBService.DirectvBoxInfo box = getItem(position);

        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.dtv_list_elem_layout, parent, false);
        }

        TextView friendlyName = (TextView) convertView.findViewById(R.id.dtv_list_elem_friendlyName);
        TextView curPlaying = (TextView) convertView.findViewById(R.id.dtv_list_elem_curPlaying);
        TextView ipAddr = (TextView) convertView.findViewById(R.id.dtv_list_elem_ipAddr);

        SpannableString friendlyNameUnderlined = new SpannableString(box.friendlyName);
        friendlyNameUnderlined.setSpan(new UnderlineSpan(), 0, friendlyNameUnderlined.length(), 0);

        friendlyName.setText(friendlyNameUnderlined );
        curPlaying.setText("Current channel: " + (box.curPlaying == null ? "not available" : box.curPlaying));

        String ip = box.ipAddr;
        ipAddr.setText(ip.replace("http://", "").replace("https://", ""));

        return convertView;
    }

}
