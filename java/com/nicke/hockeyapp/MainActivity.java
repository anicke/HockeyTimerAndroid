package com.nicke.hockeyapp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;


public class MainActivity extends Activity {
    private static final String TAG = "HockeyTimer";
    Resources r;
    public LinearLayout mainLayout;
    public LinearLayout custom_timer_box;
    public LinearLayout standard_timer_box;
    public LinearLayout bottom_half;
    public TextView customTimerTextView;
    public TextView timerText;
    SharedPreferences prefs;
    // custom timer settings
    boolean enable_preamble;
    boolean dragging = false;

    int y_coord;
    int start_y_coord;
    int preamble_game_time;
    int custom_game_length;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        r = getResources();

        setContentView(R.layout.activity_main);
        mainLayout = (LinearLayout) findViewById(R.id.mainLayout);
        customTimerTextView = (TextView) findViewById(R.id.Custom_timer_settings);
        timerText = (TextView) findViewById(R.id.timerText);
        timerText.setText("Select Timer");
        custom_timer_box = (LinearLayout) findViewById(R.id.custom_box);
        standard_timer_box = (LinearLayout) findViewById(R.id.standard_box);
        bottom_half = (LinearLayout) findViewById(R.id.bottom_half);
        standard_timer_box.setOnTouchListener(new DragListener());
        custom_timer_box.setOnTouchListener(new DragListener());
        update_settings();
    }
    private final class DragListener implements View.OnTouchListener {
        //
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                Log.d(TAG, "ACTION UP!");
                //start_default_timer();
                v.scrollTo(0, 0);
                dragging = false;
                timerText.setText("Select Timer");
                standard_timer_box.setVisibility(View.VISIBLE);
                custom_timer_box.setVisibility(View.VISIBLE);
                return true;
            } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                timerText.setText("Drag up to select");
                if (v.getId() == R.id.standard_box){
                    custom_timer_box.setVisibility(View.GONE);
                }
                else{
                    standard_timer_box.setVisibility(View.GONE);
                }

                start_y_coord = (int) event.getRawY();
                Log.d(TAG, "ACTION DOWN! Start x: " + start_y_coord);
                dragging = true;
                return true;
            } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                if (dragging) {
                    y_coord = (int) event.getRawY();
                    int movement = (start_y_coord - (int) event.getRawY()) / 5;
                    Log.d(TAG, "DRAGGING: " + movement);
                    if (movement > 0) {
                        v.scrollBy(0, movement);
                        if (movement > 35){
                            v.scrollBy(0, 0);
                            if (v.getId() == R.id.custom_box){
                                start_custom_timer();
                            }
                            else if (v.getId() == R.id.standard_box) {
                                start_default_timer();
                            }
                    }
                    }
                }
            }
            return false;
        }
    }

    public void update_settings(){
        // Reads the current preferences and sets the correct data in the textview.
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        enable_preamble = prefs.getBoolean("enable_game_preamble",
                r.getBoolean(R.bool.default_enable_preamble_time));
        preamble_game_time = Integer.parseInt(prefs.getString("match_preamble_time",
                Integer.toString(r.getInteger(R.integer.default_game_preamble_time))));
        custom_game_length = Integer.parseInt(prefs.getString("custom_game_length",
                Integer.toString(r.getInteger(R.integer.custom_match_length))));
        String custom_timer_settings = "Game length: " + custom_game_length + "min\n";
        if (enable_preamble){
            custom_timer_settings += "Preamble time:" + preamble_game_time + "s\n";
        }else{
            custom_timer_settings += "Preamble time: Disabled";
        }
        customTimerTextView.setText(custom_timer_settings);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onResume() {
        // Check if the settings have been changed.
        super.onResume();
        update_settings();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            openSettings();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    public void start_custom_timer(){
        Intent i = new Intent(getApplicationContext(), TimerActivity.class);
        i.putExtra("run_custom_timer", true);
        startActivity(i);
    }

    public void start_default_timer(){
        Intent i = new Intent(getApplicationContext(), TimerActivity.class);
        i.putExtra("run_custom_timer", false);
        startActivity(i);
    }

    public void open_settings_onclick(View v){
        Log.d(TAG, "TextView onclick");
        openSettings();
    }

    public void openSettings(){
        Log.d(TAG, "Open settings.");
        startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
    }

}
