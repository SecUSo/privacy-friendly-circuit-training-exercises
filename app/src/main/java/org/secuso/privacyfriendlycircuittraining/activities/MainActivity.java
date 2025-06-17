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

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.secuso.privacyfriendlycircuittraining.R;
import org.secuso.privacyfriendlycircuittraining.database.PFASQLiteHelper;
import org.secuso.privacyfriendlycircuittraining.fragments.GrantExactAlarmPermissionDialogFragment;
import org.secuso.privacyfriendlycircuittraining.helpers.NotificationHelper;
import org.secuso.privacyfriendlycircuittraining.models.ExerciseSet;
import org.secuso.privacyfriendlycircuittraining.services.TimerService;
import org.secuso.privacyfriendlycircuittraining.tutorial.PrefManager;

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
    private static final int workoutMaxTime = 300; // 5 min
    private static final int workoutMinTime = 10; // 10 sec
    private static final int restMaxTime = 300; // 5 min
    private static final int restMinTime = 10; // 10 sec
    private static final int maxSets = 16;
    private static final int minSets = 1;

    // Block periodization values and Button
    private final long blockPeriodizationTimeMax = 300; // 5:00 min
    private boolean isBlockPeriodization = false;
    private long blockPeriodizationTime = 0;
    private int blockPeriodizationSets = 0;
    private Switch blockPeriodizationSwitchButton;

    // Timer values
    private static final long startTime = 10; // Starttimer 10 sec
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
    private final PFASQLiteHelper db = new PFASQLiteHelper(this);
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
        this.isBlockPeriodization = blockPeriodizationSwitchState;

        if (workoutModeSwitchState) {
            findViewById(R.id.exerciesetsRow).setVisibility(View.VISIBLE);
            isExerciseMode = true;
        }

        //Start timer service
        overridePendingTransition(0, 0);
        startService(new Intent(this, TimerService.class));

        //Schedule the next motivation notification (necessary if permission was not granted)
        if (NotificationHelper.isMotivationAlertEnabled(this)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                AlarmManager am = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
                if (!am.canScheduleExactAlarms()) { //Check permission to schedule exact alarm on versions >= Android S
                    new GrantExactAlarmPermissionDialogFragment().show(getFragmentManager(), GrantExactAlarmPermissionDialogFragment.TAG);
                } else {
                    NotificationHelper.setMotivationAlert(this);
                }
            } else {
                NotificationHelper.setMotivationAlert(this);
            }
        }

        //Set the change listener for the switch button to turn block periodization on and off
        blockPeriodizationSwitchButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isBlockPeriodization = isChecked;
            blockPeriodizationSwitchState = isChecked;
            PrefManager.setBlockPeriodizationSwitchButton(getBaseContext(), isChecked);
        });

        //Suggest the user to enter their body data
        PrefManager.performMigrations(getBaseContext());
        if (PrefManager.isFirstTimeLaunch(getBaseContext())) {
            PrefManager.setFirstTimeLaunch(getBaseContext(), false);
            showPersonalizationAlert();
        }

        final List<ExerciseSet> exerciseSetslist = db.getAllExerciseSet();

        workoutMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isExerciseMode = isChecked;
            workoutModeSwitchState = isChecked;
            PrefManager.setWorkoutMode(getBaseContext(), isChecked);
            if (isExerciseMode) {
                if (exerciseSetslist.size() == 0) {
                    toast = Toast.makeText(getApplication(), getResources().getString(R.string.no_exercise_sets), Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 60);
                    toast.show();
                    workoutMode.setChecked(false);
                    isExerciseMode = false;
                    workoutModeSwitchState = false;
                    PrefManager.setWorkoutMode(getBaseContext(), false);
                } else {
                    buttonView.getRootView().findViewById(R.id.exerciesetsRow).setVisibility(View.VISIBLE);
                    sets = 1;
                    setsText.setText(Integer.toString(sets));
                }
            } else {
                buttonView.getRootView().findViewById(R.id.exerciesetsRow).setVisibility(View.GONE);
                sets = PrefManager.setsDefault;
                setsText.setText(Integer.toString(sets));
            }
        });
        exerciseIds = new ArrayList<>();
        final List<String> exerciseSetsNames = new ArrayList<>();
        for (ExerciseSet ex : exerciseSetslist) {
            exerciseSetsNames.add(ex.getName());
        }
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, exerciseSetsNames);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        exerciseSetSpinner.setAdapter(dataAdapter);
        exerciseSetSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                setsPerRound = exerciseSetslist.get(pos).getNumber();
                exerciseIds = exerciseSetslist.get(pos).getExercises();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });
    }

    /**
     * This method connects the Activity to the menu item
     *
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
        int id = view.getId();
        if (id == R.id.main_workout_interval_minus) {
            workoutTime = (workoutTime <= workoutMinTime) ? workoutMaxTime : workoutTime - 10;
            workoutIntervalText.setText(formatTime(workoutTime));
            PrefManager.setTimerWorkout(getBaseContext(), (int) this.workoutTime);
        } else if (id == R.id.main_workout_interval_plus) {
            this.workoutTime = (workoutTime >= workoutMaxTime) ? workoutMinTime : this.workoutTime + 10;
            this.workoutIntervalText.setText(formatTime(workoutTime));
            PrefManager.setTimerWorkout(getBaseContext(), (int) this.workoutTime);
        } else if (id == R.id.main_rest_interval_minus) {
            this.restTime = (restTime <= restMinTime) ? restMaxTime : this.restTime - 10;
            this.restIntervalText.setText(formatTime(restTime));
            PrefManager.setTimerRest(getBaseContext(), (int) this.restTime);
        } else if (id == R.id.main_rest_interval_plus) {
            this.restTime = (restTime >= restMaxTime) ? restMinTime : this.restTime + 10;
            this.restIntervalText.setText(formatTime(restTime));
            PrefManager.setTimerRest(getBaseContext(), (int) this.restTime);
        } else if (id == R.id.main_sets_minus) {
            this.sets = (sets <= minSets) ? maxSets : this.sets - 1;
            this.setsText.setText(Integer.toString(sets));
            PrefManager.setTimerSet(getBaseContext(), this.sets);
        } else if (id == R.id.main_sets_plus) {
            this.sets = (sets >= maxSets) ? minSets : this.sets + 1;
            this.setsText.setText(Integer.toString(sets));
            PrefManager.setTimerSet(getBaseContext(), this.sets);
        } else if (id == R.id.main_block_periodization) {
            AlertDialog blockAlert = buildBlockAlert();
            blockAlert.show();
        } else if (id == R.id.main_block_periodization_text) {
            this.blockPeriodizationSwitchButton.setChecked(!this.blockPeriodizationSwitchButton.isChecked());
            PrefManager.setBlockPeriodizationSwitchButton(getBaseContext(), this.blockPeriodizationSwitchButton.isChecked());
        } else if (id == R.id.main_use_exercise_sets_text) {
            this.workoutMode.setChecked(!this.workoutMode.isChecked());
            PrefManager.setWorkoutMode(getBaseContext(), this.workoutMode.isChecked());
        } else if (id == R.id.start_workout) {
            Intent intent = new Intent(this, WorkoutActivity.class);

            if (isExerciseMode) {
                ExerciseIdsForRounds = getExercisesForRounds(exerciseIds, sets);
                setsPerRound = sets * exerciseIds.size();
            } else {
                ExerciseIdsForRounds = exerciseIds;
                setsPerRound = sets;
            }

            if (setsPerRound == 0) {
                if (isExerciseMode && ExerciseIdsForRounds.size() == 0) {
                    Toast.makeText(this, R.string.exercise_set_has_no_exercises, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, R.string.exercise_set_has_no_sets, Toast.LENGTH_SHORT).show();
                }
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
        }
    }

    /**
     * Defines callbacks for service binding, passed to bindService()
     **/
    private final ServiceConnection serviceConnection = new ServiceConnection() {

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
    private AlertDialog buildBlockAlert() {
        LayoutInflater inflater = getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.block_periodization, null);

        Button timeButtonPlus = (Button) dialogLayout.findViewById(R.id.main_block_periodization_time_plus);
        Button timeButtonMinus = (Button) dialogLayout.findViewById(R.id.main_block_periodization_time_minus);
        Button setButtonPlus = (Button) dialogLayout.findViewById(R.id.main_block_periodization_sets_plus);
        Button setButtonMinus = (Button) dialogLayout.findViewById(R.id.main_block_periodization_sets_minus);

        final TextView setsText = (TextView) dialogLayout.findViewById(R.id.main_block_periodization_sets_amount);
        final TextView timeText = (TextView) dialogLayout.findViewById(R.id.main_block_periodization_time);
        blockPeriodizationSets = (blockPeriodizationSets >= sets) ? sets - 1 : blockPeriodizationSets;
        blockPeriodizationSets = (blockPeriodizationSets <= 0) ? 1 : blockPeriodizationSets;

        setsText.setText(Integer.toString(blockPeriodizationSets));
        timeText.setText(formatTime(blockPeriodizationTime));

        setButtonPlus.setOnClickListener(v -> {
            if (blockPeriodizationSets < sets - 1) {
                blockPeriodizationSets += 1;
            }
            setsText.setText(Integer.toString(blockPeriodizationSets));
            PrefManager.setTimerPeriodizationSet(getBaseContext(), blockPeriodizationSets);
        });

        setButtonMinus.setOnClickListener(v -> {
            if (blockPeriodizationSets > 1) {
                blockPeriodizationSets -= 1;
            }
            setsText.setText(Integer.toString(blockPeriodizationSets));
            PrefManager.setTimerPeriodizationSet(getBaseContext(), blockPeriodizationSets);
        });


        timeButtonPlus.setOnClickListener(v -> {
            if (blockPeriodizationTime < blockPeriodizationTimeMax) {
                blockPeriodizationTime += 10;
            }
            timeText.setText(formatTime(blockPeriodizationTime));
            PrefManager.setTimerPeriodizationTime(getBaseContext(), (int) blockPeriodizationTime);
        });

        timeButtonMinus.setOnClickListener(v -> {
            if (blockPeriodizationTime > 10) {
                blockPeriodizationTime -= 10;
            }
            timeText.setText(formatTime(blockPeriodizationTime));
            PrefManager.setTimerPeriodizationTime(getBaseContext(), (int) blockPeriodizationTime);
        });


        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setView(dialogLayout);

        alertBuilder.setTitle(getResources().getString(R.string.main_block_periodization_headline)).setPositiveButton(
                "Ok",
                (dialog, id) -> dialog.cancel());

        return alertBuilder.create();
    }

    private void showPersonalizationAlert() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);

        alertBuilder.setTitle(R.string.alert_personalization_title);
        alertBuilder.setMessage(R.string.alert_personalization_message);
        alertBuilder.setNegativeButton(getString(R.string.alert_confirm_dialog_negative), (dialog, id) -> dialog.dismiss());
        alertBuilder.setPositiveButton(getString(R.string.alert_confirm_dialog_positive), (dialog, id) -> {
            Intent i = new Intent(MainActivity.this, SettingsActivity.class);
            i.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT, SettingsActivity.PersonalizationPreferenceFragment.class.getName());
            i.putExtra(PreferenceActivity.EXTRA_NO_HEADERS, true);
            startActivity(i);
        });
        alertBuilder.create().show();
    }


    /**
     * Initializes the timer values for the GUI. Previously chosen setup is retrieved if one exists.
     */
    private void setDefaultTimerValues() {
        this.workoutTime = PrefManager.getTimerWorkout(getBaseContext());
        this.restTime = PrefManager.getTimerRest(getBaseContext());
        this.sets = PrefManager.getTimerSet(getBaseContext());
        this.blockPeriodizationTime = PrefManager.getTimerPeriodizationTime(getBaseContext());
        this.blockPeriodizationSets = PrefManager.getTimerPeriodizationSet(getBaseContext());
        this.blockPeriodizationSwitchState = PrefManager.getBlockPeriodizationSwitchButton(getBaseContext());
        this.workoutModeSwitchState = PrefManager.getWorkoutMode(getBaseContext());
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
        return PrefManager.getStartTimerSwitchEnabled(getBaseContext());
    }

    private String formatTime(long seconds) {
        long min = seconds / 60;
        long sec = seconds % 60;

        return String.format("%02d : %02d", min, sec);
    }

    private ArrayList<Integer> getExercisesForRounds(ArrayList<Integer> exerciseIds, int rounds) {
        ArrayList<Integer> temp = new ArrayList<>();
        for (int i = 0; i < rounds; i++)
            temp.addAll(exerciseIds);
        return temp;
    }
}
