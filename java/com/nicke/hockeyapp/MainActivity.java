package com.nicke.hockeyapp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;


public class MainActivity extends Activity {
    private static final String TAG = "HockeyTimer";
    Resources r;
    public LinearLayout mainLayout;
    public TextView customTimerTextView;
    SharedPreferences prefs;
    // custom timer settings
    boolean enable_preamble;
    int preamble_game_time;
    int custom_game_length;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        r = getResources();

        setContentView(R.layout.activity_main);
        mainLayout = (LinearLayout) findViewById(R.id.mainLayout);
        customTimerTextView = (TextView) findViewById(R.id.Custom_timer_settings);
        update_settings();
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
            custom_timer_settings += "Preamble time is disabled";
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
    public void start_custom_timer(View view){
        Intent i = new Intent("com.nicke.hockeyapp.TimerActivity");
        i.putExtra("run_custom_timer", true);
        startActivity(i);
    }

    public void start_default_timer(View view){
        Intent i = new Intent("com.nicke.hockeyapp.TimerActivity");
        i.putExtra("run_custom_timer", false);
        startActivity(i);
    }

    public void openSettings(){
        Log.d(TAG, "Open settings.");
        startActivity(new Intent("com.nicke.hockeyapp.SettingsActivity"));
    }

}
