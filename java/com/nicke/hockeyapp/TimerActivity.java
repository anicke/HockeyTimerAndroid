package com.nicke.hockeyapp;

import com.nicke.hockeyapp.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;


public class TimerActivity extends Activity {
    private static final String TAG = "TimerActivity";
    int AwayTeamScore = 0;
    int HomeTeamScore = 0;
    public TextView AwayScoreText;
    public TextView HomeScoreText;
    boolean timer_active = false;
    LinearLayout mainLayout;
    SharedPreferences prefs;

    public TextView CurrentTimeField;
    int currentPosition;

    MediaPlayer mPlayer;
    CountDownTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        AwayScoreText = (TextView) findViewById(R.id.awayTeam);
        HomeScoreText = (TextView) findViewById(R.id.homeTeam);
        CurrentTimeField = (TextView) findViewById(R.id.timerText);
        mainLayout = (LinearLayout) findViewById(R.id.mainLayout);

        mPlayer = MediaPlayer.create(TimerActivity.this, R.raw.timer1);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        CurrentTimeField.setOnLongClickListener(new View.OnLongClickListener(){
                  @Override
                  public boolean onLongClick(View v) {
                      stopPlayer();
                      catch_long_click();
                      return true;
                  }
              }
        );
        AwayScoreText.setOnLongClickListener(new View.OnLongClickListener() {
                                                 @Override
                 public boolean onLongClick(View v) {
                     if (AwayTeamScore > 0) {
                         AwayTeamScore--;
                     }
                     AwayScoreText.setText(Integer.toString(AwayTeamScore));
                     return true;
                 }
             }
        );
        HomeScoreText.setOnLongClickListener(new View.OnLongClickListener(){
                 @Override
                 public boolean onLongClick(View v) {
                     if (HomeTeamScore > 0) {
                         HomeTeamScore--;
                     }
                     HomeScoreText.setText(Integer.toString(HomeTeamScore));
                     return true;
                 }
             }
        );
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
                        CurrentTimeField.setText("00:00");
                        stopPlayer();
                        timer_active = false;
                        mPlayer.seekTo(0);
                        reset_score();
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

    public void reset_score(){
        Log.d(TAG, "Reset score");
        AwayTeamScore = 0;
        HomeTeamScore = 0;
        HomeScoreText.setText(Integer.toString(HomeTeamScore));
        AwayScoreText.setText(Integer.toString(AwayTeamScore));
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    public void increase_home_team_score(View view){
        Log.d(TAG, "Increase home team score!");
        HomeTeamScore++;
        HomeScoreText.setText(Integer.toString(HomeTeamScore));
    }

    public void increase_away_team_score(View view){
        Log.d(TAG, "Increase away team score!");
        AwayTeamScore++;
        AwayScoreText.setText(Integer.toString(AwayTeamScore));
    }

    public void stop_timer(){
        // stop the timer
        Log.d(TAG, "Stop the timer!");
        timer.cancel();
    }
    public void stopPlayer(){
        if (timer_active) {
            timer_active = false;
            mPlayer.pause();
            stop_timer();
            Log.d(TAG,"Player paused!");
        }
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

    public void toggleTimer(View view){
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
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            // TODO: If Settings has multiple levels, Up should navigate up
            // that hierarchy.
            //NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
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
}
