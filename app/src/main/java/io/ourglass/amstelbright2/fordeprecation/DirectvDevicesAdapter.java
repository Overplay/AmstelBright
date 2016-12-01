package io.ourglass.amstelbright2.fordeprecation;

import android.content.Context;
import android.graphics.Typeface;
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
    private Typeface font;
    private Typeface indexFont;

    public DirectvDevicesAdapter(Context context, ArrayList<STBService.DirectvBoxInfo> boxes, Typeface font, Typeface indexFont){
        super(context, 0, boxes);

        this.font = font;
        this.indexFont = indexFont;
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
        TextView idx = (TextView) convertView.findViewById(R.id.dtv_list_elem_idx_num);

        if(font != null) {
            friendlyName.setTypeface(font);
            curPlaying.setTypeface(font);
            ipAddr.setTypeface(font);
        }

        if(indexFont != null){
            idx.setTypeface(indexFont);
        }
        //SpannableString friendlyNameUnderlined = new SpannableString(box.friendlyName);
        //friendlyNameUnderlined.setSpan(new UnderlineSpan(), 0, friendlyNameUnderlined.length(), 0);

        friendlyName.setText(box.friendlyName);
        curPlaying.setText("Current channel: " + (box.curPlaying == null ? "not available" : box.curPlaying));

        String ip = box.ipAddr;
        ipAddr.setText(ip.replace("http://", "").replace("https://", ""));

        //format to contain leading 0 for better aesthetics
        idx.setText(String.format("%02d", position + 1));

        return convertView;
    }



}
