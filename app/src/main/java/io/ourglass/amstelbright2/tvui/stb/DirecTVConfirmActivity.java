package io.ourglass.amstelbright2.tvui.stb;

import android.app.Activity;
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

import io.ourglass.amstelbright2.R;
import io.ourglass.amstelbright2.core.OGConstants;

/**
 * Created by ethan on 10/7/16.
 */

public class DirecTVConfirmActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_direc_tv_confirm);

        View v = findViewById(R.id.outer_wrapper);

        final int width = getIntent().getExtras().getInt("width");
        final int marginHor = getIntent().getExtras().getInt("marginHor");
        final int marginVer = getIntent().getExtras().getInt("marginVer");
        final RelativeLayout inner = (RelativeLayout) findViewById(R.id.inner_wrapper);

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
