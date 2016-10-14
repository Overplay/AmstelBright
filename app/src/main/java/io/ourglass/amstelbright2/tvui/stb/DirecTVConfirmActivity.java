package io.ourglass.amstelbright2.tvui.stb;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import io.ourglass.amstelbright2.R;
import io.ourglass.amstelbright2.core.OGConstants;

/**
 * Created by ethan on 10/7/16.
 */

public class DirecTVConfirmActivity extends AppCompatActivity {

    private TextView numberTextView;
    private TextView friendlyNameTextView;
    private TextView currentlyPlayingTextView;
    private TextView ipTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_direc_tv_confirm);

        View v = findViewById(R.id.outer_wrapper);

        Bundle extras = getIntent().getExtras();

        final int width = extras.getInt("width");
        final int marginHor = extras.getInt("marginHor");
        final int marginVer = extras.getInt("marginVer");
        final RelativeLayout inner = (RelativeLayout) findViewById(R.id.inner_wrapper);

        numberTextView = (TextView) findViewById(R.id.dtv_list_elem_idx_num);
        friendlyNameTextView = (TextView) findViewById(R.id.dtv_list_elem_friendlyName);
        currentlyPlayingTextView = (TextView) findViewById(R.id.dtv_list_elem_curPlaying);
        ipTextView = (TextView) findViewById(R.id.dtv_list_elem_ipAddr);


        String ip = extras.getString("ip");
        String friendlyName = extras.getString("friendlyName");
        String currentChannel = extras.getString("currentChannel");
        int number = extras.getInt("number");

        ipTextView.setText(ip);
        friendlyNameTextView.setText(friendlyName);
        currentlyPlayingTextView.setText("Current channel: " + (currentChannel == null ? "not available" : currentChannel));
        numberTextView.setText(String.format("%02d", number));

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) inner.getLayoutParams();
                params.width = width;
                params.setMargins(marginHor, 0, marginHor, marginVer);
                inner.setLayoutParams(params);
            }
        });
//        inner.getViewTreeObserver().removeOnGlobalLayoutListener(this);
    }

    public void onConfirmClicked(View view){
        setResult(OGConstants.DIRECTV_PAIR_CONFIRMED_RESULT_CODE);
        finish();
    }

    public void onCancelClicked(View view){
        setResult(OGConstants.DIRECTV_PAIR_CANCELED_RESULT_CODE);
        finish();
    }
}
