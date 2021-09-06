package com.collisiondetector.project_anaptujh.Settings;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.collisiondetector.project_anaptujh.R;


public class Main2Activity extends AppCompatActivity  {
    //declarations
    private SeekBar frequency_Seekbar;
    private SeekBar sensivity_Seekbar;
    TextView frequency_progress;
    TextView sensivity_progress;
    int p_frequency ;
    int p_sensivity;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        android.support.v7.app.ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setLogo(R.drawable.app_logo);
        ab.setDisplayUseLogoEnabled(true);
        ab.setDisplayShowHomeEnabled(true);

        frequency_Seekbar = (SeekBar)findViewById(R.id.frequency_seekBar);
        sensivity_Seekbar = (SeekBar)findViewById(R.id.sensivity_seekBar);
        frequency_progress = (TextView)findViewById(R.id.textfrequency);
        sensivity_progress = (TextView)findViewById(R.id.textsensivity);


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

        //sensivity seekbar
        sensivity_Seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {


                p_sensivity = progress + 75;
                sensivity_progress.setText(" " + p_sensivity/100.0 + " %");
                sensivity_Seekbar.setProgress(progress);


            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                View c = findViewById(R.id.textsensivity);
                c.setVisibility(View.VISIBLE);

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                View c = findViewById(R.id.textsensivity);
                c.setVisibility(View.GONE);

            }
        });




    }

    //saves seekbar data on shared preferences
    @Override
    protected void onPause() {
        super.onPause();

        SharedPreferences prefs = this.getSharedPreferences(
                "com.collisiondetector.project_anaptujh.sharedPrefs", Context.MODE_PRIVATE);

        prefs.edit().putInt("frequency_Seekbar",p_frequency).apply();
        prefs.edit().putInt("sensivity_Seekbar",p_sensivity).apply();

    }

    //Loads seekbar data from shared preferences
    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences prefs = this.getSharedPreferences(
                "com.collisiondetector.project_anaptujh.sharedPrefs", Context.MODE_PRIVATE);

        p_frequency = prefs.getInt("frequency_Seekbar", p_frequency);
        p_sensivity = prefs.getInt("sensivity_Seekbar", p_sensivity);

        frequency_Seekbar.setProgress(p_frequency);
        sensivity_Seekbar.setProgress(p_sensivity-75);

    }

    //On back pressed returns seekbar data
    @Override
    public void onBackPressed() {

        Intent intent = new Intent();
        Bundle extras = new Bundle();
        extras.putInt("frequency",p_frequency);
        extras.putInt("sensivity",p_sensivity);
        intent.putExtras(extras);

        setResult(RESULT_OK, intent);
        finish();
    }
}

