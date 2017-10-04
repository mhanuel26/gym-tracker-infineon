package app.gymassistant.contest.com.gymassistantapp;

/**
 * Created by mhanuel on 9/29/17.
 */

import android.app.DialogFragment;
import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ai.api.android.AIConfiguration;
import ai.api.android.GsonFactory;
import ai.api.model.AIError;
import ai.api.model.AIResponse;
import ai.api.model.Metadata;
import ai.api.model.Result;
import ai.api.model.Status;
import ai.api.ui.AIDialog;
import ai.api.android.AIService;

import com.google.gson.reflect.TypeToken;
import com.infineon.sen.comm.Model.Mode;
import com.infineon.sen.comm.SensorEvent;
import com.infineon.sen.comm.SensorHub;
import com.infineon.sen.comm.SensorHubListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static java.lang.Math.abs;


public class AIDialogSampleActivity extends BaseActivity implements AIDialog.AIDialogListener {

    private static final String TAG = AIDialogSampleActivity.class.getName();

    private TextView resultTextView;
    private AIDialog aiDialog;
    private TextView tv_rel_altitude;

    SetCountDownTimer set_timer = null;
    private TextView SetCounter;

    //table
    private TableLayout workoutTable;
    private TextView tb_Index;

    class exer_reps_info {
        Integer reps;
        Integer weight;
    }

    class calibrated {
        Double threshold_min;
        Double threshold_max;
        Boolean State;
    }

    class exer_info  {
        String name;
        ArrayList<exer_reps_info> reps_info;
        Boolean done;
        Integer set_log_idx;
        ArrayList<exer_reps_info> log_info;
        calibrated cal;
    }

    private ArrayList<exer_info> exercise_list = new ArrayList<exer_info>();
    private Integer over_idx = 0;
    private Integer log_idx = 0;
    private Boolean workout_done = false;

    RelativeCalculator RelAltitude = new RelativeCalculator();

    SensorHubActivity hub = new SensorHubActivity();

    SensorHub nanoHub;

    Boolean sensor_started = false;

    private Gson gson = GsonFactory.getGson();

    private AIService aiService;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aidialog_workout);

        resultTextView = (TextView) findViewById(R.id.resultTextView);
        workoutTable = (TableLayout)findViewById( R.id.workoutTableView );

        // display the first exercise
        tb_Index = (TextView)((TableRow) workoutTable.getChildAt(0)).getChildAt(1);
        tb_Index.setText("-");
        tb_Index = (TextView)((TableRow) workoutTable.getChildAt(2)).getChildAt(2);
        tb_Index.setText("-");
        tb_Index = (TextView)((TableRow) workoutTable.getChildAt(4)).getChildAt(2);
        tb_Index.setText("-");
        tb_Index = (TextView)((TableRow) workoutTable.getChildAt(6)).getChildAt(1);
        tb_Index.setText("-");

        SetCounter = (TextView) findViewById(R.id.countdownTimer);

        // NanoHub setup
        tv_rel_altitude = (TextView) findViewById(R.id.tv_rel_altitude);


        String deviceAddress = getIntent().getStringExtra("deviceAddress");
        nanoHub = new SensorHub(getApplicationContext(), deviceAddress);

        nanoHub.addSensorHubListener(hub);
        nanoHub.connect();

        final AIConfiguration config = new AIConfiguration(Config.ACCESS_TOKEN,
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);

        aiDialog = new AIDialog(this, config);
        aiDialog.setResultsListener(this);

    }

    private void FillTestWorkout(){
        String name = "Squats";
        Integer reps = 5;
        Integer weight = 320;
        FillTestWorkout(name, reps, weight);
        name = "Barbell Curls";
        reps = 5;
        weight = 150;
        FillTestWorkout(name, reps, weight);
        // Test Filling up initial exercise
        FillWorkoutTable(log_idx);  // Fill the table with exercise to perform
    }

    private void FillTestWorkout(String name, Integer reps, Integer weight){
        exer_info tmp_info = new exer_info();
        tmp_info.name = name;
        Log.i(TAG, "Adding Exercise: " + name);
        over_idx = 0;
        log_idx = 0;
        tmp_info.done = false;
        tmp_info.set_log_idx = 0;
        tmp_info.reps_info = new ArrayList<exer_reps_info>();
        tmp_info.log_info = new ArrayList<exer_reps_info>();
        tmp_info.cal = new calibrated();
        tmp_info.cal.State = false;
        tmp_info.cal.threshold_max = 0.4;   // fixing this now for tests
        tmp_info.cal.threshold_min = 0.15;   // fixing this now for tests
        for (int i = 0; i < 2; i++) {
            exer_reps_info tmp_reps_info = new exer_reps_info();
            exer_reps_info log_reps_info = new exer_reps_info();
            tmp_reps_info.reps = reps;
            tmp_reps_info.weight = weight;
            tmp_info.reps_info.add(tmp_reps_info);
            // adding log with reps in zero
            log_reps_info.reps = 0;
            log_reps_info.weight = weight;
            tmp_info.log_info.add(log_reps_info);
            tmp_reps_info = null;
            log_reps_info = null;
        }
        exercise_list.add(tmp_info);
//        Integer goal = exercise_list.get(0).reps_info.get(0).reps;
//        Log.i(TAG, "goal reps: " + goal.toString());
        RelAltitude.reset();
        FillPerfReps(0);
        sensor_started = true;
//        nanoHub.start();
        set_timer = new SetCountDownTimer(2*60*1000, 1000);
        set_timer.start();
    }

    private void FillWorkoutTable(Integer index) {
        // display the exercise at index if valid
        if (index < exercise_list.size()) {
            tb_Index = (TextView) ((TableRow) workoutTable.getChildAt(0)).getChildAt(1);
            tb_Index.setText(exercise_list.get(index).name);
            tb_Index = (TextView) ((TableRow) workoutTable.getChildAt(2)).getChildAt(2);
            tb_Index.setText(String.valueOf(exercise_list.get(index).reps_info.size()));
            tb_Index = (TextView) ((TableRow) workoutTable.getChildAt(4)).getChildAt(2);
            tb_Index.setText(exercise_list.get(index).reps_info.get(0).reps.toString());
            tb_Index = (TextView) ((TableRow) workoutTable.getChildAt(6)).getChildAt(1);
            tb_Index.setText(exercise_list.get(index).reps_info.get(0).weight.toString());
        }
    }

    private void FillPerfReps(Integer reps){
        tb_Index = (TextView) ((TableRow) workoutTable.getChildAt(4)).getChildAt(1);
        tb_Index.setText(reps.toString());
    }

    private void FillPerfSet(Integer reps){
        tb_Index = (TextView) ((TableRow) workoutTable.getChildAt(2)).getChildAt(1);
        tb_Index.setText(reps.toString());
    }

    @Override
    public void onResult(final AIResponse response) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "onResult gym");
                Log.d(TAG, "JSON : " + gson.toJson(response));

                Log.i(TAG, "Received success response");

                // this is example how to get different parts of result object
                final Status status = response.getStatus();
                Log.i(TAG, "Status code: " + status.getCode());
                Log.i(TAG, "Status type: " + status.getErrorType());

                final Result result = response.getResult();
                Log.i(TAG, "Resolved query: " + result.getResolvedQuery());

                Log.i(TAG, "Action: " + result.getAction());
                final String speech = result.getFulfillment().getSpeech();
                Log.i(TAG, "Speech: " + speech);
                if (!speech.isEmpty())
                    TTS.speak(speech);

                Map <String, JsonElement> Data = result.getFulfillment().getData();
                if (Data != null && !Data.isEmpty()) {
                    for(final Map.Entry<String, JsonElement> entry : Data.entrySet()){
                        try {
                            Log.i(TAG, "Key : " + entry.getKey());
                            if (entry.getKey().equals("Training_Number")) {
                                // not doing anything here with this number now but that was a
                                // required step of the state machine.
                                Log.i(TAG, "Training_Number " + entry.getValue().toString());
                            } else if (entry.getKey().equals("Exercise_List")) {
                                // The exercise list response
                                JSONArray ex_list = new JSONArray(entry.getValue().toString());
                                for (int j = 0; j < ex_list.length(); j++) {
                                    exer_info tmp_info = new exer_info();
                                    JSONObject exer_data = ex_list.getJSONObject(j);
                                    String name = exer_data.getString("name");
                                    tmp_info.name = name;
                                    over_idx = 0;
                                    tmp_info.done = false;
                                    tmp_info.set_log_idx = 0;
                                    tmp_info.reps_info = new ArrayList<exer_reps_info>();
                                    tmp_info.log_info = new ArrayList<exer_reps_info>();
                                    tmp_info.cal = new calibrated();
                                    tmp_info.cal.State = false;
                                    tmp_info.cal.threshold_max = 0.5;   // fixing this now for tests
                                    tmp_info.cal.threshold_min = 0.15;   // fixing this now for tests
                                    JSONArray reps_list = exer_data.getJSONArray("reps_list");
                                    JSONArray weight_list = exer_data.getJSONArray("weight_list");
                                    if (reps_list != null) {
                                        for (int i = 0; i < reps_list.length(); i++) {
                                            exer_reps_info tmp_reps_info = new exer_reps_info();
                                            exer_reps_info tmp_logs_info = new exer_reps_info();
                                            tmp_reps_info.reps = reps_list.getInt(i);
                                            tmp_reps_info.weight = weight_list.getInt(i);
                                            tmp_info.reps_info.add(tmp_reps_info);
                                            // adding log with reps in zero
                                            tmp_logs_info.reps = 0;
                                            tmp_logs_info.weight = weight_list.getInt(i);
                                            tmp_info.log_info.add(tmp_logs_info);
                                            tmp_reps_info = null;
                                            tmp_logs_info = null;
                                        }
                                    }
                                    exercise_list.add(tmp_info);
                                }
                                for (int j = 0; j < exercise_list.size(); j++) {
                                    Log.i(TAG, "EXERCISE #" + j + " : " + exercise_list.get(j).name);
                                    Log.i(TAG, "REPS/WEIGHTS: ");
                                    for (int i = 0; i < exercise_list.get(j).reps_info.size(); i++) {
                                        Log.i(TAG, exercise_list.get(j).reps_info.get(i).reps.toString() +
                                                "/" + exercise_list.get(j).reps_info.get(i).weight);
                                    }
                                }
                                // Fill out workout table with initial value
                                FillWorkoutTable (over_idx);
                            }else if(entry.getKey().equals("sequence")){
                                // This will handle navigation throu the exercise table.
                                Log.i(TAG, String.format("%s: %s", entry.getKey(), entry.getValue().toString()));
                                String sequence = entry.getValue().getAsString();
                                String gymapp_speech = sequence;
                                if (sequence.equals("first")){
                                    over_idx = 0;
                                    Log.i(TAG, "first");
                                    FillWorkoutTable (over_idx);
                                    gymapp_speech = "The first exercise is " + exercise_list.get(over_idx).name;
                                }else if(sequence.equals("next")){
                                    Log.i(TAG, "next");
                                    if(over_idx >= exercise_list.size() - 1) {
                                        gymapp_speech = exercise_list.get(over_idx).name + "is actually the last exercise";
                                    }else {
                                        over_idx++;
                                        gymapp_speech = "The next exercise is " + exercise_list.get(over_idx).name;
                                        FillWorkoutTable(over_idx);
                                    }
                                }else if(sequence.equals("last")){
                                    over_idx = exercise_list.size() - 1;
                                    Log.i(TAG, "last");
                                    FillWorkoutTable (over_idx);
                                    gymapp_speech = "The last exercise is " + exercise_list.get(over_idx).name;
                                }else if(sequence.equals("remaining")){
                                    Log.i(TAG, "remaining");
                                    Integer performed = exercise_list.get(over_idx).log_info.size();
                                    if(performed == 0){
                                        gymapp_speech = "You haven't started this exercise, come in don't be lazy";
                                    }else {
                                        Integer remaining = exercise_list.get(over_idx).reps_info.size() -
                                                performed;
                                        String sets = (remaining > 1) ? "sets" : "set";
                                        gymapp_speech = "You still have <say-as interpret-as=\"cardinal\"> " +
                                                remaining + "more " + sets + " to perform";
                                    }
                                }else{
                                    Log.i(TAG, "else other sequence unhandled");
                                    gymapp_speech = "";
                                }
                                TTS.speak(gymapp_speech);
                            }else if(entry.getKey().equals("action")){
                                // this will take over actions from trainee such as startinga  new set.
                                try{
                                    JSONObject action_list = new JSONObject(entry.getValue().toString());
                                    if(action_list.has("error")){
                                        String error = action_list.getString("error");
                                        if(error.equals("NoErr")){
                                            // decode received json action
                                            if(action_list.has("log_entry")){
                                                // Right now I am returning new always here so igring this value!
                                                Log.i(TAG, "Log Entry Action: " + action_list.getString("log_entry"));
                                                if(!exercise_list.get(log_idx).done){
                                                    // proceed if exercise was not previously performed
                                                    RelAltitude.reset();
                                                    FillPerfReps(0);
                                                    if (!sensor_started) {
                                                        sensor_started = true;
                                                        if(set_timer == null) {
                                                            set_timer = new SetCountDownTimer(60 * 1000, 1000);
                                                        }
                                                        set_timer.start();
                                                    }
                                                }else{
                                                    // done with that exercise

                                                }
                                            }else if(action_list.has("weight")){
                                                Log.i(TAG, "Weight Action: " + action_list.getString("weight"));
                                            }
                                        }else{
                                            Log.i(TAG, "An Error occur at webhook processing");
                                            // give error treatment here
                                        }
                                    }// if not error that should be an error
                                }catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }else if(entry.getKey().equals("perform")){
                                // this will force a new (in sequence) exercise to be performed by trainee
                                Log.i(TAG, "Perform: ");
                                try {
                                    Boolean isActive = entry.getValue().getAsBoolean();
                                    if (isActive) {
                                        Log.i(TAG, "True");
                                        FillWorkoutTable (log_idx); // Fill the table with exercise to perform
                                    }else {
                                        // Right now webhook is always returning True
                                        // if state machine fails it will return some error
                                        Log.i(TAG, "False");
                                    }
                                }catch (Exception ex){
                                    Log.i(TAG, "Exception parsing perform");
                                }
                            }


                        }catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }

                final Metadata metadata = result.getMetadata();
                if (metadata != null) {
                    Log.i(TAG, "Intent id: " + metadata.getIntentId());
                    Log.i(TAG, "Intent name: " + metadata.getIntentName());
                }

                final HashMap<String, JsonElement> params = result.getParameters();
                if (params != null && !params.isEmpty()) {
                    for (final Map.Entry<String, JsonElement> entry : params.entrySet()) {

                    }
                }
            }

        });
    }

    @Override
    public void onError(final AIError error) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                resultTextView.setText(error.toString());
            }
        });
    }

    @Override
    public void onCancelled() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                resultTextView.setText("");
            }
        });
    }

    @Override
    protected void onPause() {
        if (aiDialog != null) {
            aiDialog.pause();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (aiDialog != null) {
            aiDialog.resume();
        }
        super.onResume();
    }

    public void buttonListenOnClick(final View view) {
        aiDialog.showAndListen();
    }


    class SensorHubActivity implements SensorHubListener {
        @Override
        public void onConnected(SensorHub sensorHub) {
            Toast.makeText(getApplicationContext(), "Connected to " + sensorHub.getName(), Toast.LENGTH_LONG).show();
            sensorHub.setSelectedSensor(sensorHub.getSensorList().get(0).getId());
            sensorHub.setMode("mode", "bg");
            sensorHub.setMode("prs_mr", "8");
            sensorHub.setMode("prs_osr", "128");
//            FillTestWorkout();  // enable to test the sensor
            sensorHub.start();
        }


        @Override
        public void onDisconnected(SensorHub sensorHub) {
            Toast.makeText(getApplicationContext(), "Disconnected from " + sensorHub.getName(), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onConnectionError(SensorHub sensorHub) {
            Toast.makeText(getApplicationContext(), "Connection Error " + sensorHub.getName(), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onSensorDataReceived(SensorHub sensorHub, SensorEvent sensorEvent) {
            if(sensor_started) {
                if (!sensorEvent.getDataId().equals("a"))
                    return;
                RelAltitude.updateValue(sensorEvent.getSensorValue());
                tv_rel_altitude.setText(String.format("Rel Pos = %.2f", RelAltitude.getRelativeValue()));
                if (exercise_list.get(log_idx).cal.State) {
                    if (abs(RelAltitude.getRelativeValue()) <= exercise_list.get(log_idx).cal.threshold_min) {
                        Integer idx = exercise_list.get(log_idx).set_log_idx;
                        RelAltitude.reset();
                        exercise_list.get(log_idx).cal.State = false;
                        Integer count = exercise_list.get(log_idx).log_info.get(idx).reps;
                        //  for some reason I cannot do reps++
                        count = count + 1;
                        exercise_list.get(log_idx).log_info.get(idx).reps = count;
                        FillPerfReps(count);
                        Integer goal = exercise_list.get(log_idx).reps_info.get(idx).reps;
                        Log.i(TAG, "REPS: " + count.toString());
                        SetCounter.setText(count.toString());
                        if (count.equals(goal)) {
                            Log.i(TAG, "Set completed");
                            TTS.speak("good you are done!");
                            sensor_started = false;
                            idx = idx + 1;
                            // we cancel the timer since we completed the set
                            set_timer.cancel();
                            if(exercise_list.get(log_idx).reps_info.size() > idx){
                                // next time we need to log in the correct position (next Set)
                                exercise_list.get(log_idx).set_log_idx = idx;
                                Log.i(TAG, "There are pending sets");
                                TTS.speak("You have more sets to do, let me know when you are ready!");
                            }else{
                                // it means we  are done with exercise sets
                                exercise_list.get(log_idx).done = true;
                                if(log_idx >= exercise_list.size() - 1) {
                                    // it means we are done with last workout exercise
                                    workout_done = true;
                                    Log.i(TAG, "Finish the workout");
                                    TTS.speak("Glad you are done, go to rest and remember " +
                                              "Winners Train, Losers Complain ");
                                    final Intent mServiceIntent = new Intent();
                                    mServiceIntent.setClass(AIDialogSampleActivity.this, RSSPullService.class);
                                    String jsonLog = getJsonLog(exercise_list);
                                    mServiceIntent.putExtra("logData", jsonLog);
                                    AIDialogSampleActivity.this.startService(mServiceIntent);
                                }else{
                                    log_idx = log_idx + 1;
                                    if(!exercise_list.get(log_idx).done) {
                                        // proceed if exercise was not previously performed (just to be sure)
                                        FillWorkoutTable(log_idx); // Fill the table with exercise to perform
                                        Log.i(TAG, "Display next exercise");
                                        TTS.speak("You finish your " + exercise_list.get(log_idx-1).name + " sets");
                                        TTS.speak("Next exercise is " + exercise_list.get(log_idx).name );
                                    }
                                }
                            }
                            // this seems to not work here
//                        sensorHub.setSelectedSensor(sensorHub.getSensorList().get(0).getId());
//                        sensorHub.stop();
                        }
                    }
                } else {
                    // Initial state
                    if (abs(RelAltitude.getRelativeValue()) >= exercise_list.get(log_idx).cal.threshold_max) {
                        exercise_list.get(log_idx).cal.State = true;
                    }
                }
            } // if started
            else
            {
                tv_rel_altitude.setText("");
                SetCounter.setText("");
            }
        }

        @Override
        public void onModeChanged(SensorHub sensorHub, Mode mode) {

        }
    }

    private String getJsonLog(ArrayList<exer_info> exercise_list) {
        String jsonRes = "This is a Test";

        return jsonRes;
    }

    public class SetCountDownTimer extends CountDownTimer {

        public SetCountDownTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {

            int progress = (int) (millisUntilFinished/1000);

//            progressBar.setProgress(progressBar.getMax()-progress);
        }

        @Override
        public void onFinish() {
            Log.i(TAG, "Try to stop NanoHub");
            // stop the sensor sample acquisition after given time for performing the set
            // this actually doesn't stop the nanohub either
            sensor_started = false;
            nanoHub.stop();
        }
    }

//    static {
//        System.loadLibrary("hello-android-jni");
//    }
//    public native String getMsgFromJni();

}
