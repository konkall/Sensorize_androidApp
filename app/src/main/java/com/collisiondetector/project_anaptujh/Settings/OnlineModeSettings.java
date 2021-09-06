package com.collisiondetector.project_anaptujh.Settings;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.collisiondetector.project_anaptujh.R;


public class OnlineModeSettings extends AppCompatActivity {

    //declarations
    private SeekBar frequency_Seekbar;
    TextView frequency_progress;
    int p_frequency ;
    private EditText ip;
    String ipValue;
    private  EditText port;
    String portValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onlinesettings);
        android.support.v7.app.ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setLogo(R.drawable.app_logo);
        ab.setDisplayUseLogoEnabled(true);
        ab.setDisplayShowHomeEnabled(true);

        frequency_Seekbar = (SeekBar)findViewById(R.id.frequency_seekBar);
        frequency_progress = (TextView)findViewById(R.id.textfrequency);


        //frequency seekbar
        frequency_Seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {

                if (progress<20){
                    frequency_progress.setText( " " + 20 + " Hz");
                    frequency_Seekbar.setProgress(20);
                    p_frequency = 20;
                }
                else {
                    frequency_progress.setText(" " + progress + " Hz");
                    frequency_Seekbar.setProgress(progress);
                    p_frequency = progress;
                }



            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                View b = findViewById(R.id.textfrequency);
                b.setVisibility(View.VISIBLE);

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                View b = findViewById(R.id.textfrequency);
                b.setVisibility(View.GONE);

            }
        });

        //get Ip
        ip = (EditText) findViewById(R.id.ip);
        ip.setText(ipValue, TextView.BufferType.EDITABLE);

        //get POrt
        port = (EditText) findViewById(R.id.port);
        port.setText(portValue, TextView.BufferType.EDITABLE);
    }

    //saves seekbar data on shared preferences
    @Override
    protected void onPause() {
        super.onPause();

        SharedPreferences prefs = this.getSharedPreferences(
                "com.collisiondetector.project_anaptujh.sharedPrefs", Context.MODE_PRIVATE);
        ipValue= ip.getText().toString();
        portValue= port.getText().toString();

        prefs.edit().putInt("frequency_Seekbar",p_frequency).apply();
        prefs.edit().putString("ip", ipValue).apply();
        prefs.edit().putString("port", portValue).apply();

    }

    //Loads seekbar data from shared preferences
    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences prefs = this.getSharedPreferences(
                "com.collisiondetector.project_anaptujh.sharedPrefs", Context.MODE_PRIVATE);

        p_frequency = prefs.getInt("frequency_Seekbar", p_frequency);
        ipValue = prefs.getString("ip", ipValue);
        portValue = prefs.getString("port", portValue);


        frequency_Seekbar.setProgress(p_frequency);
        ip.setText(ipValue, TextView.BufferType.EDITABLE);
        port.setText(portValue, TextView.BufferType.EDITABLE);

    }

    //On back pressed returns seekbar data
    @Override
    public void onBackPressed() {

        Intent intent = new Intent();
        Bundle extras = new Bundle();
        extras.putInt("frequency",p_frequency);
        extras.putString("ip",ipValue);
        extras.putString("port",portValue);

        intent.putExtras(extras);

        setResult(RESULT_OK, intent);
        finish();
    }

}
