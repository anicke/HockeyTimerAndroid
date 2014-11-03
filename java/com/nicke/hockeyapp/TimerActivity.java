package com.nicke.hockeyapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.util.Log;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.Locale;


public class TimerActivity extends Activity {
    private static final String TAG = "TimerActivity";
    TextToSpeech ttobj;
    Resources r;
    public static final int UPDATE_INTERVAL = 1000;

    public static final int[] speakTimes = {5, 10, 15, 30, 45};
    int AwayTeamScore = 0;
    int HomeTeamScore = 0;
    int preamble_time;

    public TextView AwayScoreText;
    public TextView HomeScoreText;
    public TextView CurrentTimeField;

    boolean timer_active = false;
    boolean enable_game_preamble;
    boolean preamble_done = false;
    boolean use_custom_timer;
    LinearLayout mainLayout;
    SharedPreferences prefs;
    int custom_game_length;

    int currentPosition = 0;
    long CustomStartTime;

    MediaPlayer mPlayer = null;
    CountDownTimer timer;
    CountDownTimer preamble_timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);
        r = getResources();
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        AwayScoreText = (TextView) findViewById(R.id.awayTeam);
        HomeScoreText = (TextView) findViewById(R.id.homeTeam);
        CurrentTimeField = (TextView) findViewById(R.id.timerText);
        mainLayout = (LinearLayout) findViewById(R.id.mainLayout);

        Bundle bundle = getIntent().getExtras();
        use_custom_timer = bundle.getBoolean("run_custom_timer");
        // load settings.
        custom_game_length = Integer.parseInt(prefs.getString("custom_game_length",
                Integer.toString(r.getInteger(R.integer.custom_match_length))));
        enable_game_preamble = prefs.getBoolean("enable_game_preamble",
                r.getBoolean(R.bool.default_enable_preamble_time));
        preamble_time = Integer.parseInt(prefs.getString("match_preamble_time",
                Integer.toString(r.getInteger(R.integer.default_game_preamble_time))));
        if (use_custom_timer){
            Log.d(TAG, "Custom timer is enabled: " + custom_game_length + " minutes");
            if (enable_game_preamble){
                Log.d(TAG, "Preamble time enabled: " + preamble_time + " seconds.");
                CurrentTimeField.setText(String.format("-%02d:%02d",
                        preamble_time / 60, preamble_time % 60));
            }
            else{
                CurrentTimeField.setText(String.format("-%02d:00",
                        custom_game_length));
            }
        }

        mPlayer = MediaPlayer.create(TimerActivity.this, R.raw.timer1);

        CurrentTimeField.setOnLongClickListener(new View.OnLongClickListener(){
                  @Override
                  public boolean onLongClick(View v) {
                      stopPlayer();
                      catch_long_click();
                      return true;
                  }
              }
        );
        // set up tts.
        ttobj=new TextToSpeech(getApplicationContext(),
                new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        if(status != TextToSpeech.ERROR){
                            ttobj.setLanguage(Locale.UK);
                        }
                    }
        });
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

    public void clean_up_audio(){
        if(ttobj !=null){
            ttobj.stop();
            ttobj.shutdown();
        }
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");
        clean_up_audio();
        stop_timer();
        stopPlayer();
        super.onPause();
    }

    protected void onStop(){
        try{
            mPlayer.release();
            mPlayer = null;
            Log.d(TAG, "onStop mediaplayer relased");
        }
        catch (NullPointerException e) {
            Log.d(TAG, "onStop tried to release mediaplayer.");
        }
        super.onStop();
    }

    public void switch_to_main_activity(){
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "Back button pressed!");
        if (! timer_active) {
            Log.d(TAG, "Back button pressed. No active timer to cancel.");
            switch_to_main_activity();
        }
        else{
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.reset_dialog_question)
                    .setTitle(R.string.reset_dialog_title)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Log.d(TAG, "Back button pressed confirmed.");
                            switch_to_main_activity();
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Log.d(TAG, "Back button pressed cancelled.");
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
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
                        CurrentTimeField.setText("Press to start timer.");
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
        try{
            timer.cancel();
            Log.d(TAG, "Timer stopped.");
        }
        catch(NullPointerException e) {
            Log.d(TAG, "Tried to stop a timer that were not running.");
        }
        try{
            preamble_timer.cancel();
            Log.d(TAG, "Preamble timer stopped.");
        }
        catch (NullPointerException e){
            Log.d(TAG, "Preamble timer not running.");
        }
    }
    public void stopPlayer(){
        if (timer_active && ! use_custom_timer) {
            timer_active = false;
            mPlayer.pause();
            stop_timer();
            Log.d(TAG, "Player paused!");
        }
        else if (use_custom_timer && timer_active){
            timer_active = false;
            Log.d(TAG, "Pause the custom timer at duration: " + currentPosition);
            stop_timer();
        }
    }

    public void start_custom_timer(){
        Log.d(TAG, "Start custom timer!");
        if (currentPosition != 0){
            CustomStartTime = System.currentTimeMillis() - currentPosition * 1000;
            Log.d(TAG, "Resume custom timer at time: " + CustomStartTime);
        }
        else{
            CustomStartTime = System.currentTimeMillis();
        }

        int custom_match_length = Integer.parseInt(prefs.getString("custom_game_length",
                Integer.toString(r.getInteger(R.integer.custom_match_length))));
        // timer needs milliseconds.
        custom_match_length = custom_match_length * 60 * 1000;
        Log.d(TAG, "Start a custom timer with remaining time: " + custom_match_length);
        if (currentPosition == 0) {
            Log.d(TAG, "Start timer with remaining: " + custom_match_length);
            timer = new MediaCountDown(custom_match_length, UPDATE_INTERVAL);
        }
        else{
            int remaining_time = custom_match_length - currentPosition;
            Log.d(TAG, "Resume timer. Remaining time: " + remaining_time);
            timer = new MediaCountDown(remaining_time, UPDATE_INTERVAL);
        }
        timer.start();
    }

    public void start_timer(){
        // start the timer. set the timer duration at the remaining duration of the media file.
        int remainingTime = mPlayer.getDuration() - mPlayer.getCurrentPosition();
        Log.d(TAG, "Start the timer with remaining time: " + remainingTime);
        timer = new MediaCountDown(remainingTime, UPDATE_INTERVAL);
        timer.start();
    }

    public MediaPlayer play_resource_file(int resouce_id){
        Log.d(TAG, "Play media file with resource id: " + resouce_id);
        MediaPlayer mediaPlayer = MediaPlayer.create(this, resouce_id);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener(){
               @Override public void onCompletion(final MediaPlayer mp){
                    mp.release();
           }
        });
        mediaPlayer.start();
        return mediaPlayer;
    }

    public void toggleTimer(View view){
        if (! use_custom_timer) {
            Log.d(TAG, "Default 5 minute timer is used!");
            if (timer_active) {
                Log.d(TAG, "Timer is running! Pause it!");
                stopPlayer();
            } else {
                Log.d(TAG, "Timer is not running! Start it!");
                timer_active = true;
                mPlayer.start();
                if (prefs.getBoolean("enable_sound", true)) {
                    mPlayer.setVolume(1f, 1f);
                } else {
                    mPlayer.setVolume(0f, 0f);
                }
                start_timer();
            }
        } else{
            if (!timer_active) {
                Log.d(TAG, "Start a custom timer.");
                if (!preamble_done && enable_game_preamble){
                    timer_active = true;
                    Log.d(TAG, "Start the preamble timer with length: " + preamble_time + " s");
                    preamble_timer = new PreambleCountDown(preamble_time * 1000, UPDATE_INTERVAL);
                    preamble_timer.start();
                }
                else{
                    start_custom_timer();
                    timer_active = true;
                }
            }
            else {
                Log.d(TAG, "Stop a custom timer.");
                stopPlayer();
            }
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
            // that hierarchy.
            //NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    public void speak_string (String toSpeak){
        // If TTS is not speaking say the string in toSpeak.
        Log.d(TAG, "Say: " + toSpeak);
        if (! ttobj.isSpeaking()) {
            ttobj.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    public class PreambleCountDown extends CountDownTimer {
        public PreambleCountDown(long startTime, long interval) {
            super(startTime, interval);
        }

        @Override
        public void onFinish() {
            CurrentTimeField.setText("-00:00");
            Log.d(TAG, "Preamble timer is done!");
            preamble_done = true;
            play_resource_file(R.raw.start_whistle);
            start_custom_timer();
        }

        @Override
        public void onTick(long millisUntilFinished) {
            int remaining_seconds = (int) (millisUntilFinished / 1000) % 60;
            int remaining_minutes = (int) (millisUntilFinished / 1000) / 60;
            CurrentTimeField.setText(String.format("-%02d:%02d",
                    remaining_minutes, remaining_seconds));
        }
    }

    public class MediaCountDown extends CountDownTimer {
        public MediaCountDown(long startTime, long interval) {
            super(startTime, interval);
        }
        @Override
        public void onFinish() {
            CurrentTimeField.setText("Time's up!");
            timer_active = false;
            currentPosition = 0;
            play_resource_file(R.raw.police_siren);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            if (! use_custom_timer) {
                if (timer_active) {
                    currentPosition = mPlayer.getCurrentPosition() - 334000;
                    int minutes = (currentPosition / 1000) / 60 * -1;
                    int seconds = (currentPosition / 1000) % 60 * -1;
                    CurrentTimeField.setText(String.format("-%02d:%02d", minutes, seconds));
                }
            }
            else {
                int current_duration = (int) (System.currentTimeMillis() - CustomStartTime) / 1000;
                currentPosition = current_duration;
                int remaining_minutes = (custom_game_length * 60 - current_duration) / 60;
                int remaining_seconds = (custom_game_length * 60 - current_duration) % 60;
                Log.d(TAG, "Game length: " + custom_game_length + "m Remaining time: " + remaining_minutes + "m" + remaining_seconds + "s");
                // show the remaining  time
                CurrentTimeField.setText(String.format("-%02d:%02d",
                        remaining_minutes, remaining_seconds));
                if (remaining_seconds == 0 && remaining_minutes > 0) {
                    if (remaining_minutes == 1) {
                        speak_string("One minute remaining");
                    } else {
                        speak_string(remaining_minutes + " minutes remaining");
                    }
                } else if (remaining_minutes == 0){
                    for(int k = 0; k!= speakTimes.length;  k++){
                       if (speakTimes[k] == remaining_seconds){
                           speak_string(remaining_seconds + " seconds remaining");
                       }
                    }
                }
            }
        }
    }
}
