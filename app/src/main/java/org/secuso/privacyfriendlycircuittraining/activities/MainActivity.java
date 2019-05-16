/**
 * This file is part of Privacy Friendly Circuit Trainer.
 * Privacy Friendly Circuit Trainer is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or any later version.
 * Privacy Friendly Circuit Trainer is distributed in the hope
 * that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with Privacy Friendly Interval Timer. If not, see <http://www.gnu.org/licenses/>.
 */

package org.secuso.privacyfriendlycircuittraining.activities;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.secuso.privacyfriendlycircuittraining.R;
import org.secuso.privacyfriendlycircuittraining.database.PFASQLiteHelper;
import org.secuso.privacyfriendlycircuittraining.helpers.NotificationHelper;
import org.secuso.privacyfriendlycircuittraining.models.Exercise;
import org.secuso.privacyfriendlycircuittraining.models.ExerciseSet;
import org.secuso.privacyfriendlycircuittraining.services.TimerService;
import org.secuso.privacyfriendlycircuittraining.tutorial.PrefManager;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * Main view that lets the user choose the timer intervals, training mode and has a button to start the workout
 *
 * @author Alexander Karakuz, Nils Schroth
 * @version 20180321
 */
public class MainActivity extends BaseActivity {

    // CONFIGURE TIMER VARIABLES HERE
    // Max and min values for the workout and rest timer as well as the sets
    private int workoutMaxTime = 300; // 5 min
    private int workoutMinTime = 10; // 10 sec
    private int restMaxTime = 300; // 5 min
    private int restMinTime = 10; // 10 sec
    private int maxSets = 16;
    private int minSets = 1;

    // Default values for the timers
    private final int workoutTimeDefault = 60;
    private final int restTimeDefault = 30;
    private final int setsDefault = 6;
    private final int blockPeriodizationTimeDefault = 90;
    private final int blockPeriodizationSetsDefault = 1;

    // General
    private SharedPreferences settings = null;
    private PrefManager prefManager = null;
    private Intent intent = null;

    // Block periodization values and Button
    private final long blockPeriodizationTimeMax = 300; // 5:00 min
    private boolean isBlockPeriodization = false;
    private long blockPeriodizationTime = 0;
    private int blockPeriodizationSets = 0;
    private Switch blockPeriodizationSwitchButton;

    // Timer values
    private final long startTime = 10; // Starttimer 10 sec
    private long workoutTime = 0;
    private long restTime = 0;
    private int sets = 0;
    private int setsPerRound = 0;
    private boolean blockPeriodizationSwitchState = false;
    private boolean workoutModeSwitchState = false;

    // GUI text
    private TextView workoutIntervalText = null;
    private TextView restIntervalText = null;
    private TextView setsText = null;

    //Timer service variables
    private TimerService timerService = null;
    private boolean serviceBound = false;

    private Spinner exerciseSetSpinner;
    private Switch workoutMode;
    private PFASQLiteHelper db = new PFASQLiteHelper(this);
    ArrayList<Integer> exerciseIds = null;
    ArrayList<Integer> ExerciseIdsForRounds = null;

    private boolean isExerciseMode = false;
    private static Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Init preferences
        PreferenceManager.setDefaultValues(this, R.xml.pref_notification, true);
        PreferenceManager.setDefaultValues(this, R.xml.pref_personalization, true);
        PreferenceManager.setDefaultValues(this, R.xml.pref_statistics, true);
        PreferenceManager.setDefaultValues(this, R.xml.pref_workout, true);

        this.settings = PreferenceManager.getDefaultSharedPreferences(this);

        //Set default values for the timer configurations
        setDefaultTimerValues();

        //Set the GUI text
        this.workoutIntervalText = this.findViewById(R.id.main_workout_interval_time);
        this.restIntervalText = this.findViewById(R.id.main_rest_interval_time);
        this.setsText = this.findViewById(R.id.main_sets_amount);
        this.workoutMode = findViewById(R.id.workout_mode_switch);
        this.exerciseSetSpinner = findViewById(R.id.exerciseSets);
        this.blockPeriodizationSwitchButton = findViewById(R.id.main_block_periodization_switch);

        this.workoutIntervalText.setText(formatTime(workoutTime));
        this.restIntervalText.setText(formatTime(restTime));
        this.setsText.setText(Integer.toString(sets));
        this.workoutMode.setChecked(workoutModeSwitchState);
        this.blockPeriodizationSwitchButton.setChecked(blockPeriodizationSwitchState);

        if(workoutModeSwitchState) {
            findViewById(R.id.exerciesetsRow).setVisibility(View.VISIBLE);
        }

        //Start timer service
        overridePendingTransition(0, 0);
        startService(new Intent(this, TimerService.class));

        //Schedule the next motivation notification (necessary if permission was not granted)
        if(NotificationHelper.isMotivationAlertEnabled(this)){
            NotificationHelper.setMotivationAlert(this);
        }

        //Set the change listener for the switch button to turn block periodization on and off
        blockPeriodizationSwitchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isBlockPeriodization = isChecked;
                blockPeriodizationSwitchState = isChecked;
                settings.edit().putBoolean("blockPeriodizationSwitchButton", isChecked).apply();
            }
        });

        //Suggest the user to enter his body data
        prefManager = new PrefManager(this);
        if(prefManager.isFirstTimeLaunch()){
            prefManager.setFirstTimeLaunch(false);
            showPersonalizationAlert();
        }

        final List<ExerciseSet> exerciseSetslist = db.getAllExerciseSet();

        workoutMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isExerciseMode = isChecked;
                workoutModeSwitchState = isChecked;
                settings.edit().putBoolean("workoutMode", isChecked).apply();
                if(isExerciseMode){
                    if(exerciseSetslist.size() == 0){
                        toast = Toast.makeText(getApplication(), getResources().getString(R.string.no_exercise_sets), Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER, 0, 60);
                        toast.show();
                        workoutMode.setChecked(false);
                    }
                    else {
                        buttonView.getRootView().findViewById(R.id.exerciesetsRow).setVisibility(View.VISIBLE);
                        sets = 1;
                        setsText.setText(Integer.toString(sets));
                    }
                }
                else{
                    buttonView.getRootView().findViewById(R.id.exerciesetsRow).setVisibility(View.GONE);
                    sets = setsDefault;
                    setsText.setText(Integer.toString(sets));
                    exerciseIds = null;
                }
            }
        });
        exerciseIds = new ArrayList<>();
        final List<String> exerciseSetsNames = new ArrayList<>();
        for(ExerciseSet ex : exerciseSetslist){
            exerciseSetsNames.add(ex.getName());
        }
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, exerciseSetsNames);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        exerciseSetSpinner.setAdapter(dataAdapter);
        exerciseSetSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                setsPerRound = exerciseSetslist.get(pos).getNumber();
                exerciseIds = exerciseSetslist.get(pos).getExercises();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) { }
        });
    }

    /**
     * This method connects the Activity to the menu item
     * @return ID of the menu item it belongs to
     */
    @Override
    protected int getNavigationDrawerID() {
        return R.id.nav_main;
    }

    /**
     * Click functions for timer values, block periodization AlertDialog and workout start button
     */
    public void onClick(View view) {
        SharedPreferences.Editor editor = this.settings.edit();

        switch(view.getId()) {
            case R.id.main_workout_interval_minus:
                workoutTime = (workoutTime <= workoutMinTime) ? workoutMaxTime : workoutTime - 10;
                workoutIntervalText.setText(formatTime(workoutTime));
                editor.putInt(this.getString(R.string.pref_timer_workout),(int) this.workoutTime);
                editor.apply();
                break;
            case R.id.main_workout_interval_plus:
                this.workoutTime = (workoutTime >= workoutMaxTime) ? workoutMinTime : this.workoutTime + 10;
                this.workoutIntervalText.setText(formatTime(workoutTime));
                editor.putInt(this.getString(R.string.pref_timer_workout),(int) this.workoutTime);
                editor.apply();
                break;
            case R.id.main_rest_interval_minus:
                this.restTime = (restTime <= restMinTime) ? restMaxTime : this.restTime - 10;
                this.restIntervalText.setText(formatTime(restTime));
                editor.putInt(this.getString(R.string.pref_timer_rest),(int) this.restTime);
                editor.apply();
                break;
            case R.id.main_rest_interval_plus:
                this.restTime = (restTime >= restMaxTime) ? restMinTime : this.restTime + 10;
                this.restIntervalText.setText(formatTime(restTime));
                editor.putInt(this.getString(R.string.pref_timer_rest),(int) this.restTime);
                editor.apply();
                break;
            case R.id.main_sets_minus:
                this.sets = (sets <= minSets) ? maxSets : this.sets - 1;
                this.setsText.setText(Integer.toString(sets));
                editor.putInt(this.getString(R.string.pref_timer_set), this.sets);
                editor.apply();
                break;
            case R.id.main_sets_plus:
                this.sets = (sets >= maxSets) ? minSets : this.sets + 1;
                this.setsText.setText(Integer.toString(sets));
                editor.putInt(this.getString(R.string.pref_timer_set), this.sets);
                editor.apply();
                break;
            case R.id.main_block_periodization:
                AlertDialog blockAlert = buildBlockAlert();
                blockAlert.show();
                break;
            case R.id.main_block_periodization_text:
                this.blockPeriodizationSwitchButton.setChecked(!this.blockPeriodizationSwitchButton.isChecked());
                editor.putBoolean("blockPeriodizationSwitchButton", this.blockPeriodizationSwitchButton.isChecked());
                editor.apply();
                break;
            case R.id.main_use_exercise_sets_text:
                this.workoutMode.setChecked(!this.workoutMode.isChecked());
                editor.putBoolean("workoutMode", this.workoutMode.isChecked());
                editor.apply();
                break;
            case R.id.start_workout:
                intent = new Intent(this, WorkoutActivity.class);

                if(isExerciseMode){
                    ExerciseIdsForRounds = getExercisesForRounds(exerciseIds, sets);
                    setsPerRound = sets * exerciseIds.size();
                } else {
                    ExerciseIdsForRounds = exerciseIds;
                    setsPerRound = sets;
                }

                if(setsPerRound == 0 || ExerciseIdsForRounds.size() == 0) {
                    Toast.makeText(this, R.string.exercise_set_has_no_exercises, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (isStartTimerEnabled(this)) {
                    timerService.startWorkout(workoutTime, restTime, startTime, setsPerRound,
                            isBlockPeriodization, blockPeriodizationTime, blockPeriodizationSets, ExerciseIdsForRounds, isExerciseMode);
                } else {
                    timerService.startWorkout(workoutTime, restTime, 0, setsPerRound,
                            isBlockPeriodization, blockPeriodizationTime, blockPeriodizationSets, ExerciseIdsForRounds, isExerciseMode);
                }

                this.startActivity(intent);
                break;
            default:
        }
    }

    /**
     * Defines callbacks for service binding, passed to bindService()
     **/
    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            TimerService.LocalBinder binder = (TimerService.LocalBinder) service;
            timerService = binder.getService();
            serviceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            serviceBound = false;
        }
    };



   /**
    * Build an AlertDialog for the block periodization configurations
    */
    private AlertDialog buildBlockAlert(){
        LayoutInflater inflater = getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.block_periodization, null);

        Button timeButtonPlus = (Button)dialogLayout.findViewById(R.id.main_block_periodization_time_plus);
        Button timeButtonMinus = (Button)dialogLayout.findViewById(R.id.main_block_periodization_time_minus);
        Button setButtonPlus = (Button)dialogLayout.findViewById(R.id.main_block_periodization_sets_plus);
        Button setButtonMinus = (Button)dialogLayout.findViewById(R.id.main_block_periodization_sets_minus);

        final TextView setsText = (TextView)dialogLayout.findViewById(R.id.main_block_periodization_sets_amount);
        final TextView timeText = (TextView)dialogLayout.findViewById(R.id.main_block_periodization_time);
        blockPeriodizationSets = (blockPeriodizationSets >= sets) ? sets-1 : blockPeriodizationSets;
        blockPeriodizationSets = (blockPeriodizationSets <= 0) ? 1 : blockPeriodizationSets;

        setsText.setText(Integer.toString(blockPeriodizationSets));
        timeText.setText(formatTime(blockPeriodizationTime));

        setButtonPlus.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SharedPreferences.Editor editor = settings.edit();

                if(blockPeriodizationSets < sets-1){ blockPeriodizationSets += 1; }
                setsText.setText(Integer.toString(blockPeriodizationSets));
                editor.putInt(getString(R.string.pref_timer_periodization_set), blockPeriodizationSets);
                editor.commit();
            }
        });

        setButtonMinus.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SharedPreferences.Editor editor = settings.edit();

                if(blockPeriodizationSets > 1){ blockPeriodizationSets -= 1; }
                setsText.setText(Integer.toString(blockPeriodizationSets));
                editor.putInt(getString(R.string.pref_timer_periodization_set), blockPeriodizationSets);
                editor.commit();
            }
        });


        timeButtonPlus.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SharedPreferences.Editor editor = settings.edit();

                if(blockPeriodizationTime < blockPeriodizationTimeMax){ blockPeriodizationTime += 10; }
                timeText.setText(formatTime(blockPeriodizationTime));
                editor.putInt(getString(R.string.pref_timer_periodization_time), (int) blockPeriodizationTime);
                editor.commit();

            }
        });

        timeButtonMinus.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SharedPreferences.Editor editor = settings.edit();

                if(blockPeriodizationTime > 10){ blockPeriodizationTime -= 10; }
                timeText.setText(formatTime(blockPeriodizationTime));
                editor.putInt(getString(R.string.pref_timer_periodization_time), (int) blockPeriodizationTime);
                editor.commit();
            }
        });


        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setView(dialogLayout);

        alertBuilder.setTitle(getResources().getString(R.string.main_block_periodization_headline)).setPositiveButton(
                    "Ok",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                });

        return alertBuilder.create();
    }

    private void showPersonalizationAlert(){
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);

        alertBuilder.setTitle(R.string.alert_personalization_title);
        alertBuilder.setMessage(R.string.alert_personalization_message);
        alertBuilder.setNegativeButton(getString(R.string.alert_confirm_dialog_negative), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        alertBuilder.setPositiveButton(getString(R.string.alert_confirm_dialog_positive), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Intent i = new Intent( MainActivity.this, SettingsActivity.class );
                i.putExtra( PreferenceActivity.EXTRA_SHOW_FRAGMENT, SettingsActivity.PersonalizationPreferenceFragment.class.getName() );
                i.putExtra( PreferenceActivity.EXTRA_NO_HEADERS, true );
                startActivity(i);
            }
        });
        alertBuilder.create().show();
    }


    /**
     * Initializes the timer values for the GUI. Previously chosen setup is retrieved if one exists.
     */
    private void setDefaultTimerValues(){
        if(settings != null ) {
            this.workoutTime = settings.getInt(this.getString(R.string.pref_timer_workout), workoutTimeDefault);
            this.restTime = settings.getInt(this.getString(R.string.pref_timer_rest), restTimeDefault);
            this.sets = settings.getInt(this.getString(R.string.pref_timer_set), setsDefault);
            this.blockPeriodizationTime = settings.getInt(this.getString(R.string.pref_timer_periodization_time), blockPeriodizationTimeDefault);
            this.blockPeriodizationSets = settings.getInt(this.getString(R.string.pref_timer_periodization_set), blockPeriodizationSetsDefault);
            this.blockPeriodizationSwitchState = settings.getBoolean("blockPeriodizationSwitchButton", false);
            this.workoutModeSwitchState = settings.getBoolean("workoutMode", false);
        } else {
            this.workoutTime = workoutTimeDefault;
            this.restTime = restTimeDefault;
            this.sets = setsDefault;
            this.blockPeriodizationTime = blockPeriodizationTimeDefault;
            this.blockPeriodizationSets = blockPeriodizationSetsDefault;
            this.blockPeriodizationSwitchState = false;
            this.workoutModeSwitchState = false;
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        // Bind to LocalService
        Intent intent = new Intent(this, TimerService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (serviceBound) {
            unbindService(serviceConnection);
            serviceBound = false;
        }
    }

    @Override
    public void onDestroy() {
        timerService.setIsAppInBackground(false);
        stopService(new Intent(this, TimerService.class));
        super.onDestroy();
    }


    /**
     * Helper methods and preference checks
     */
    public boolean isStartTimerEnabled(Context context) {
        if (this.settings != null) {
            return settings.getBoolean(context.getString(R.string.pref_start_timer_switch_enabled), true);
        }
        return false;
    }

    private String formatTime(long seconds) {
        long min = seconds/60;
        long sec = seconds%60;

        return String.format("%02d : %02d", min,sec);
    }

    private ArrayList<Integer> getExercisesForRounds(ArrayList<Integer> exerciseIds, int rounds) {
        ArrayList<Integer> temp = new ArrayList<>();
        for (int i = 0; i < rounds; i++)
            temp.addAll(exerciseIds);
        return temp;
    }
}
