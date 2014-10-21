package com.nicke.hockeyapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.media.MediaPlayer;
import android.content.DialogInterface;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.os.Build;

import java.util.Locale;


public class MainActivity extends Activity {
    private static final String TAG = "HockeyTimer";
    MediaPlayer mPlayer;
    public TextView CurrentTimeField;
    public LinearLayout mainLayout;
    String time_string;
    int currentPosition;

    boolean timer_active = false;
    boolean play_sound;
    CountDownTimer timer;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //mPlayer = MediaPlayer.create(MainActivity.this, R.raw.timer1);
        CurrentTimeField = (TextView) findViewById(R.id.timerText);
        mainLayout = (LinearLayout) findViewById(R.id.mainLayout);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        mainLayout.setOnLongClickListener(new View.OnLongClickListener(){
            @Override
            public boolean onLongClick(View v) {
                stopPlayer();
                catch_long_click();
                return true;
                }
        }
        );
    }

    public void stop_timer(){
        // stop the timer
        Log.d(TAG, "Stop the timer!");
        timer.cancel();
    }

    public void start_timer(){
        // start the timer. set the timer duration at the remaining duration of the media file.

        int remainingTime = mPlayer.getDuration() - mPlayer.getCurrentPosition();
        int interval = Integer.parseInt(prefs.getString("update_interval", "600"));
        Log.d(TAG, "Start the timer with remaining time: " +
                remainingTime +" interval: " + interval);
        timer = new MediaCountDown(remainingTime, interval);
        timer.start();
    }

    public class MediaCountDown extends CountDownTimer {
        public MediaCountDown(long startTime, long interval) {
            super(startTime, interval);
        }
        @Override
        public void onFinish() {
            CurrentTimeField.setText("Time's up!");
        }
        @Override
        public void onTick(long millisUntilFinished) {
            if (timer_active) {
                currentPosition = mPlayer.getCurrentPosition() - 34000;
                //time_string
                //CurrentTimeField.setTextSize("80dp");
                if (currentPosition < 60) {
                    currentPosition = currentPosition * -1;
                    CurrentTimeField.setText(String.format("-00:%02d", currentPosition / 1000));
                }
                else{
                    if (currentPosition == 0) {
                        CurrentTimeField.setText(String.format("00:%02d", currentPosition / 1000));
                    }
                    else{
                        int minutes = (currentPosition / 1000) / 60;
                        int seconds = (currentPosition / 1000) - (minutes * 60);
                        CurrentTimeField.setText(String.format("%02d:%02d", minutes, seconds));
                    }
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
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

    public void openSettings(){
        Log.d(TAG, "Open settings.");
        stopPlayer();
        startActivity(new Intent("com.nicke.hockeyapp.SettingsActivity"));
    }

    public void catch_long_click(){
        stop_timer();
        // build alert dialog!
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.reset_dialog_question)
                .setTitle(R.string.reset_dialog_title)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // reset!
                        CurrentTimeField.setText("Press screen to start");
                        stopPlayer();
                        timer_active = false;
                        mPlayer.seekTo(0);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void stopPlayer(){
        if (timer_active) {
            timer_active = false;
            mPlayer.pause();
            stop_timer();
            Log.d(TAG,"Player paused!");
        }
    }

    public void toggleTimer(View view){
        startActivity(new Intent("com.nicke.hockeyapp.TimerActivity"));
        /*
        Log.d(TAG, "");
        if (timer_active) {
            Log.d(TAG, "Timer is running! Pause it!");
            stopPlayer();
        }
        else{
            Log.d(TAG, "Timer is not running! Start it!");
            timer_active = true;
            mPlayer.start();
            if (prefs.getBoolean("enable_sound", true)){
                mPlayer.setVolume(1f, 1f);
            }
            else{
                mPlayer.setVolume(0f,0f);
            }
            start_timer();
        }
        */
    }
}
