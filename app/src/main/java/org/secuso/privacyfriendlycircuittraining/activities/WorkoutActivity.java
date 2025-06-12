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

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.secuso.privacyfriendlycircuittraining.R;
import org.secuso.privacyfriendlycircuittraining.database.PFASQLiteHelper;
import org.secuso.privacyfriendlycircuittraining.models.Exercise;
import org.secuso.privacyfriendlycircuittraining.services.TimerService;
import org.secuso.privacyfriendlycircuittraining.tutorial.PrefManager;

import java.util.ArrayList;

/**
 * Workout view with a workout and rest timer and exercise image (if in exercise mode)
 * Timers can be paused and skipped. Once the workout is finished a message is shown and
 * the view navigates back to the main view.
 *
 * @author Alexander Karakuz, Nils Schroth
 * @version 20180321
 */
public class WorkoutActivity extends AppCompatActivity {

    // GUI Text
    private TextView currentSetsInfo = null;
    private TextView workoutTimer = null;
    private TextView workoutTitle = null;
    private TextView caloriesNumber = null;
    private TextView workoutName = null;
    private TextView workoutDescription = null;

    // GUI Buttons
    private FloatingActionButton fab = null;
    private ImageButton volumeButton = null;
    private ImageView prevTimer = null;
    private ImageView nextTimer = null;

    //GUI Elements
    private ProgressBar progressBar = null;
    private View finishedView = null;
    private ImageView workoutImage = null;

    // Service variables
    private final BroadcastReceiver timeReceiver = new BroadcastReceiver();
    private TimerService timerService = null;
    private boolean serviceBound = false;

    private final PFASQLiteHelper db = new PFASQLiteHelper(this);


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_workout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Bind to LocalService
        Intent intent = new Intent(this, TimerService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

        // Initialize all GUI variables
        this.caloriesNumber = (TextView) this.findViewById(R.id.workout_finished_calories_number);
        this.currentSetsInfo = (TextView) this.findViewById(R.id.current_sets_info);
        this.volumeButton = (ImageButton) this.findViewById(R.id.volume_button);
        this.prevTimer = (ImageView) this.findViewById(R.id.workout_previous);
        this.progressBar = (ProgressBar) this.findViewById(R.id.progressBar);
        this.workoutTimer = (TextView) this.findViewById(R.id.workout_timer);
        this.workoutTitle = (TextView) this.findViewById(R.id.workout_title);
        this.nextTimer = (ImageView) this.findViewById(R.id.workout_next);
        this.finishedView = findViewById(R.id.finishedView);
        this.finishedView.setVisibility(View.GONE);
        this.workoutImage = findViewById(R.id.workout_image);
        this.workoutName = findViewById(R.id.workout_name);
        this.workoutDescription = findViewById(R.id.workout_description);

        // Set the workout screen to remain on if so enabled in the settings
        if (isKeepScreenOnEnabled(this)) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        // Set the gui to workout gui colors if start timer wasn't enabled
        if (!isStartTimerEnabled(this)) {
            setWorkoutGuiColors(true);
        }

        // Register the function of the pause button
        fab = (FloatingActionButton) findViewById(R.id.fab_pause_resume);
        fab.setOnClickListener(view -> {
            fab.setSelected(!fab.isSelected());
            if (fab.isSelected() && timerService != null) {
                fab.setImageResource(R.drawable.ic_play_24dp);
                timerService.pauseTimer();
            } else if (timerService != null) {
                fab.setImageResource(R.drawable.ic_pause_24dp);
                timerService.resumeTimer();
            }
        });

        // Set image and flag of the volume button according to the current sound settings
        int volumeImageId = isSoundsMuted(this) ? R.drawable.ic_volume_mute_24dp : R.drawable.ic_volume_loud_24dp;
        volumeButton.setImageResource(volumeImageId);
        volumeButton.setSelected(isSoundsMuted(this));

        // Register the function of the volume button
        volumeButton.setOnClickListener(view -> {
            volumeButton.setSelected(!volumeButton.isSelected());
            if (volumeButton.isSelected()) {
                volumeButton.setImageResource(R.drawable.ic_volume_mute_24dp);
                muteAllSounds(true);
            } else {
                volumeButton.setImageResource(R.drawable.ic_volume_loud_24dp);
                muteAllSounds(false);
            }
        });

        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


    /**
     * Defines callbacks for service binding, passed to bindService()
     * Performs an initial GUI update when connection is established.
     **/
    private final ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            TimerService.LocalBinder binder = (TimerService.LocalBinder) service;
            timerService = binder.getService();
            serviceBound = true;

            timerService.setIsAppInBackground(false);
            updateGUI();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            serviceBound = false;
        }
    };


    /**
     * Receives the timer ticks from the service and updates the GUI accordingly
     **/
    public class BroadcastReceiver extends android.content.BroadcastReceiver {

        // Threshold when progressbar starts blinking in milliseconds and the blinking speed
        final int workoutBlinkingTime = 10000; //10 sec
        final int restBlinkingTime = 5000; // 5 sec

        // Timestamp to calculate when next progressBar color change should occur
        long oldTimeStamp = workoutBlinkingTime + 100;

        // Flags for the color switches of the GUI
        boolean workoutColors = false;

        /**
         * Updates the GUI depending on the message recived
         * <p>
         * onTickMillis - Updates the progressBar progress according to current timer millis and makes it blink
         * timer_title - Updates the GUI title and switches the GUI colors accordingly
         * countdown_seconds - Updates the current seconds in the GUI
         * current_set - Updates the current sets in the GUI
         * workout_finished - Shows the final message and navigates back to the main view
         * new_timer - Resets the progressBar
         **/
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getExtras() != null) {
                if (intent.getLongExtra("onTickMillis", 0) != 0) {

                    long millis = intent.getLongExtra("onTickMillis", 0);
                    progressBar.setProgress((int) millis);

                    if (isProgressBarBlinking(millis, context)) {
                        progressBarBlink();
                    }
                }
                if (intent.getStringExtra("timer_title") != null) {
                    String message = intent.getStringExtra("timer_title");

                    workoutColors = message.equals(getResources().getString(R.string.workout_headline_workout));
                    setWorkoutGuiColors(workoutColors);
                    workoutTitle.setText(message);
                }
                if (intent.getIntExtra("countdown_seconds", -1) != -1) {
                    int seconds = intent.getIntExtra("countdown_seconds", 0);
                    workoutTimer.setText(Integer.toString(seconds));
                }
                if (intent.getIntExtra("current_set", 0) != 0 && intent.getIntExtra("sets", 0) != 0) {
                    int currentSet = intent.getIntExtra("current_set", 0);
                    int sets = intent.getIntExtra("sets", 0);

                    currentSetsInfo.setText(getResources().getString(R.string.workout_info) + ": " + currentSet + "/" + sets);
                }
                if (intent.getBooleanExtra("workout_finished", false) != false) {
                    showFinishedView();
                }
                if (intent.getIntExtra("exercise_id", -1) != -1) {
                    int exerciseID = intent.getIntExtra("exercise_id", -1);
                    Exercise exercise = db.getExercise(exerciseID);
                    workoutName.setText(exercise.getName());
                    workoutDescription.setText(exercise.getDescription());
                    Glide.with(context).load(exercise.getImage()).into(workoutImage);
                }

                if (intent.getLongExtra("new_timer", 0) != 0) {
                    long timerDuration = intent.getLongExtra("new_timer", 0);
                    int seconds = (int) Math.ceil(timerDuration / 1000.0);
                    workoutTimer.setText(Integer.toString(seconds));
                    progressBar.setMax((int) timerDuration);
                    progressBar.setProgress((int) timerDuration);
                    this.oldTimeStamp = workoutBlinkingTime + workoutBlinkingTime;

                    progressBar.animate().cancel();
                    progressBar.setAlpha(1.0f);
                }
            }
        }

        /**
         * Check if the progressbar should be blinking
         *
         * @param millis  The current milliseconds
         * @param context Context
         * @return Boolean if the progressbar should be blinking
         */
        private boolean isProgressBarBlinking(long millis, Context context) {
            int buffer = 100; //Makes sure the animation has time to begin

            if (millis <= workoutBlinkingTime && workoutColors && isBlinkingProgressBarEnabled(context) && oldTimeStamp - buffer >= millis) {
                oldTimeStamp = millis;
                return true;
            } else if (millis <= restBlinkingTime && isBlinkingProgressBarEnabled(context) && oldTimeStamp - buffer >= millis) {
                oldTimeStamp = millis;
                return true;
            }
            return false;
        }
    }

    /**
     * Switches between colors for rest timer and workout timer
     * according to the boolean flag provided/
     *
     * @param guiFlip True for workout phase colors, false for rest phase colors
     */
    private void setWorkoutGuiColors(boolean guiFlip) {
        int textColor = guiFlip ? R.color.white : R.color.black;
        int backgroundColor = guiFlip ? R.color.lightblue : R.color.white;
        int progressBackgroundColor = guiFlip ? R.color.white : R.color.lightblue;
        int buttonColor = guiFlip ? R.color.white : R.color.darkblue;


        currentSetsInfo.setTextColor(getResources().getColor(textColor));
        workoutTitle.setTextColor(getResources().getColor(textColor));
        workoutTimer.setTextColor(getResources().getColor(textColor));
        prevTimer.setColorFilter(getResources().getColor(buttonColor));
        nextTimer.setColorFilter(getResources().getColor(buttonColor));

        View view = findViewById(R.id.workout_content);
        view.setBackgroundColor(getResources().getColor(backgroundColor));

        LayerDrawable progressBarDrawable = (LayerDrawable) progressBar.getProgressDrawable();
        Drawable backgroundDrawable = progressBarDrawable.getDrawable(0);
        Drawable progressDrawable = progressBarDrawable.getDrawable(1);
        progressDrawable.setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
        backgroundDrawable.setColorFilter(ContextCompat.getColor(this, progressBackgroundColor), PorterDuff.Mode.SRC_IN);

        //progressBar.setProgressBackgroundTintList(ColorStateList.valueOf(getResources().getColor(progressBackgroundColor)));

        if (guiFlip)
            workoutImage.setImageAlpha(255);
        else
            workoutImage.setImageAlpha(50);
    }

    /**
     * Lets the progressbar blink by changing the alpha value
     */
    private void progressBarBlink() {
        if (progressBar.getAlpha() == 1.0f)
            progressBar.animate().alpha(0.1f).setDuration(600).start();
        else if (progressBar.getAlpha() <= 0.15f)
            progressBar.animate().alpha(1.0f).setDuration(600).start();
    }

    /**
     * Click functions to go to previous or next timer and to finish the current workout.
     * On workout finish an alert is build followed by a navigation to the main view.
     *
     * @param view View
     */
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.workout_previous:
                if (timerService != null) {
                    timerService.prevTimer();
                }
                break;
            case R.id.workout_next:
                if (timerService != null) {
                    timerService.nextTimer();
                }
                break;
            case R.id.workout_finished_ok:
                cleanTimerServiceFinish();
                finish();
                break;
            case R.id.finish_workout:
                if (isCancelDialogEnabled(this)) {
                    showCancelAlert(true);
                } else {
                    showFinishedView();
                }
                break;
            default:
        }
    }


    /**
     * Update all GUI elements by getting the current timer values from the TimerService
     */
    private void updateGUI() {
        if (timerService != null) {
            boolean isPaused = timerService.getIsPaused();
            int currentSet = timerService.getCurrentSet();
            String title = timerService.getCurrentTitle();
            long savedTime = timerService.getSavedTime();
            int sets = timerService.getSets();
            long timerDuration = 0;
            Integer currentExerciseId = timerService.getCurrentExerciseId();


            if (timerService.getisExerciseMode()) {
                workoutImage.setVisibility(View.VISIBLE);
                workoutName.setVisibility(View.VISIBLE);
                workoutDescription.setVisibility(View.VISIBLE);
            } else {
                ConstraintLayout constraintLayout = findViewById(R.id.workout_center_view_constraint_layout);
                ConstraintSet constraintSet = new ConstraintSet();
                constraintSet.clone(constraintLayout);
                constraintSet.setVerticalBias(R.id.workout_image, 1f);
                constraintSet.applyTo(constraintLayout);
            }

            if (title.equals(getResources().getString(R.string.workout_headline_workout))) {
                timerDuration = timerService.getWorkoutTime();
                setWorkoutGuiColors(true);
            } else if (title.equals(getResources().getString(R.string.workout_headline_start_timer))) {
                timerDuration = timerService.getStartTime();
                setWorkoutGuiColors(false);
            } else if (title.equals(getResources().getString(R.string.workout_headline_rest))) {
                timerDuration = timerService.getRestTime();
                setWorkoutGuiColors(false);
            } else if (title.equals(getResources().getString(R.string.workout_block_periodization_headline))) {
                timerDuration = timerService.getBlockRestTime();
                setWorkoutGuiColors(false);
            }

            String time = Long.toString((int) Math.ceil(savedTime / 1000.0));

            currentSetsInfo.setText(getResources().getString(R.string.workout_info) + ": " + currentSet + "/" + sets);
            workoutTitle.setText(title);
            workoutTimer.setText(time);
            progressBar.setMax((int) timerDuration);
            progressBar.setProgress((int) savedTime);
            progressBar.setAlpha(1.0f);

            if (timerService.getisExerciseMode()) {
                Exercise exercise = db.getExercise(currentExerciseId);
                workoutName.setText(exercise.getName());
                workoutDescription.setText(exercise.getDescription());
                Glide.with(this).load(exercise.getImage()).into(workoutImage);
            }

            if (isPaused) {
                fab.setImageResource(R.drawable.ic_play_24dp);
                fab.setSelected(true);
            } else {
                fab.setImageResource(R.drawable.ic_pause_24dp);
                fab.setSelected(false);
            }

            if (title.equals(getResources().getString(R.string.workout_headline_done))) {
                showFinishedView();
            }
        }
    }


    /**
     * Build and show an AlertDialog for when the workout is canceled
     *
     * @return The AlertDialog
     */
    private void showCancelAlert(final boolean showFinish) {
        if (timerService != null) {
            timerService.pauseTimer();
            timerService.setCancelAlert(true);
        }

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);

        final CharSequence[] item = {getResources().getString(R.string.workout_canceled_check)};
        final boolean[] selection = {false};
        final ArrayList<Integer> selectedItem = new ArrayList<>();

        alertBuilder.setMultiChoiceItems(item, selection, (dialog, indexSelected, isChecked) -> {
            if (isChecked) {
                selectedItem.add(indexSelected);
                PrefManager.setCancelWorkoutCheck(getBaseContext(), false);
            } else if (selectedItem.contains(indexSelected)) {
                selectedItem.remove(Integer.valueOf(indexSelected));
                PrefManager.setCancelWorkoutCheck(getBaseContext(), true);
            }
        });

        alertBuilder.setTitle(R.string.workout_canceled_info);

        alertBuilder.setNegativeButton(getString(R.string.alert_confirm_dialog_negative), (dialog, id) -> {
            if (timerService != null) {
                timerService.resumeTimer();
                timerService.setCancelAlert(false);
            }
            updateGUI();
            dialog.dismiss();
        });


        alertBuilder.setPositiveButton(getString(R.string.alert_confirm_dialog_positive), (dialog, id) -> {
            if (showFinish) {
                showFinishedView();
            } else {
                cleanTimerServiceFinish();
                finish();
            }
        });

        alertBuilder.setOnCancelListener(dialog -> {
            if (timerService != null) {
                timerService.resumeTimer();
                timerService.setCancelAlert(false);
            }
            updateGUI();
            dialog.dismiss();
        });

        alertBuilder.create().show();
    }

    /**
     * Shows a transparent overlay over the workout screen.
     * Overlay displays that the workout is over and optionally how many calories were burned.
     */
    private void showFinishedView() {
        TextView finishButton = (TextView) this.findViewById(R.id.finish_workout);
        finishButton.setEnabled(false);

        if (timerService != null) {
            timerService.setCurrentTitle(getString(R.string.workout_headline_done));
            timerService.pauseTimer();
        }
        this.workoutTitle.setText(getResources().getString(R.string.workout_headline_done));
        this.workoutTimer.setText("0");
        this.fab.hide();
        this.finishedView.setVisibility(View.VISIBLE);

        finishedView.setOnTouchListener((v, event) -> true);

        if (isCaloriesEnabled(this)) {
            TextView txt = (TextView) findViewById(R.id.workout_finished_calories_text);
            TextView nmbr = (TextView) findViewById(R.id.workout_finished_calories_number);
            TextView unit = (TextView) findViewById(R.id.workout_finished_calories_unit);

            txt.setVisibility(View.VISIBLE);
            nmbr.setVisibility(View.VISIBLE);
            unit.setVisibility(View.VISIBLE);

            if (timerService != null) {
                String caloriesBurned = Integer.toString(timerService.getCaloriesBurned());
                this.caloriesNumber.setText(caloriesBurned);
            }
        }
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

    /**
     * Stop the notification and update the GUI with current values
     */
    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override
    public void onResume() {
        super.onResume();

        if (timerService != null) {
            timerService.setIsAppInBackground(false);
        }
        updateGUI();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(timeReceiver, new IntentFilter(TimerService.COUNTDOWN_BROADCAST), RECEIVER_EXPORTED);
        } else {
            registerReceiver(timeReceiver, new IntentFilter(TimerService.COUNTDOWN_BROADCAST));
        }
    }

    /**
     * Start the notification when activity goes into the background
     */
    @Override
    public void onPause() {
        super.onPause();

        if (timerService != null) {
            timerService.setIsAppInBackground(true);
        }
        unregisterReceiver(timeReceiver);
    }

    /**
     * Stop the notification when activity is destroyed
     */
    @Override
    public void onDestroy() {
        if (timerService != null) {
            timerService.workoutClosed();
            timerService.setCancelAlert(false);
        }
        super.onDestroy();
    }

    /*
     * Stop all timers and remove notification when navigating back to the main activity
     */
    @Override
    public void onBackPressed() {
        if (isCancelDialogEnabled(this)) {
            showCancelAlert(false);
        } else {
            cleanTimerServiceFinish();
            finish();
        }
        //super.onBackPressed();
    }


    /**
     * Calls the service to stop an clear all timer
     */
    private void cleanTimerServiceFinish() {
        if (timerService != null) {
            timerService.cleanTimerFinish();
        }
    }

    /**
     * Mutes or unmutes all sound output
     *
     * @param mute Flag to mute or unmute all sounds
     */
    private void muteAllSounds(boolean mute) {
        PrefManager.setSoundsMuted(getBaseContext(), mute);
    }

    /*
     * Multiple checks for what was enabled inside the settings
     */
    public boolean isKeepScreenOnEnabled(Context context) {
        return PrefManager.getKeepScreenOnSwitchEnabled(context);
    }

    public boolean isStartTimerEnabled(Context context) {
        return PrefManager.getStartTimerSwitchEnabled(context);
    }

    public boolean isBlinkingProgressBarEnabled(Context context) {
        return PrefManager.getBlinkingProgressBar(context);
    }

    public boolean isCaloriesEnabled(Context context) {
        return PrefManager.getCaloriesCounter(context);
    }

    public boolean isSoundsMuted(Context context) {
        return PrefManager.getSoundsMuted(context);
    }

    public boolean isCancelDialogEnabled(Context context) {
        return PrefManager.getCancelWorkoutCheck(context);
    }
}
