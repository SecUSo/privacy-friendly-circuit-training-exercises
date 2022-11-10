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

package org.secuso.privacyfriendlycircuittraining.tutorial;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import org.secuso.privacyfriendlycircuittraining.R;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;

/**
 * Class structure taken from tutorial at http://www.androidhive.info/2016/05/android-build-intro-slider-app/
 */

public class PrefManager {
    public static final int PREFERENCE_MODE = Context.MODE_PRIVATE;

    // Default values for the timers
    private static final int workoutTimeDefault = 60;
    private static final int restTimeDefault = 30;
    public static final int setsDefault = 6;
    private static final int blockPeriodizationTimeDefault = 90;
    private static final int blockPeriodizationSetsDefault = 1;

    public PrefManager(Context context) {
    }

    public static void performMigrations(Context context) {
        migratePreferences(context);
    }

    /**
     * Migrate Preferences from app version <= v1.1
     *
     * @param context
     */
    private static void migratePreferences(Context context) {
        // pre v1.1 attributes
        int PRIVATE_MODE = 0;

        final String PREF_NAME = "androidhive-welcome";
        final String IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch";

        SharedPreferences prefs = getPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();

        //migrate firstTimeLaunch
        if (context.getSharedPreferences(PREF_NAME, PRIVATE_MODE).contains(IS_FIRST_TIME_LAUNCH)) {
            setFirstTimeLaunch(context, context.getSharedPreferences(PREF_NAME, PRIVATE_MODE).getBoolean(IS_FIRST_TIME_LAUNCH, true));
            context.getSharedPreferences(PREF_NAME, PRIVATE_MODE).edit().remove(IS_FIRST_TIME_LAUNCH).apply();
        }

        //migrate translated preferences

        if (prefs.contains("Fortschrittsbalken blinkt")) {
            editor.putBoolean(context.getString(R.string.pref_blinking_progress_bar), prefs.getBoolean("Fortschrittsbalken blinkt", false));
            editor.remove("Fortschrittsbalken blinkt");
        }
        if (prefs.contains("Blinking progress bar")) {
            editor.putBoolean(context.getString(R.string.pref_blinking_progress_bar), prefs.getBoolean("Blinking progress bar", false));
            editor.remove("Blinking progress bar");
        }

        if (prefs.contains("Motivationstexte")) {
            Set<String> defaultStringSet = new HashSet<>(Arrays.asList(context.getResources().getStringArray(R.array.pref_default_notification_motivation_alert_messages)));
            editor.putStringSet(context.getString(R.string.pref_notification_motivation_alert_texts), prefs.getStringSet("Motivationstexte", defaultStringSet));
            editor.remove("Motivationstexte");
        }

        if (prefs.contains("Motivation texts")) {
            Set<String> defaultStringSet = new HashSet<>(Arrays.asList(context.getResources().getStringArray(R.array.pref_default_notification_motivation_alert_messages)));
            editor.putStringSet(context.getString(R.string.pref_notification_motivation_alert_texts), prefs.getStringSet("Motivation texts", defaultStringSet));
            editor.remove("Motivation texts");
        }

        editor.apply();
    }

    /**
     * Only use this method to access the SharedPreferences object.
     * To access the preference values use the corresponding getter/setter functions of this class.
     *
     * @param context
     * @return the default SharedPreferences for this app
     */
    public static SharedPreferences getPreferences(@NonNull Context context) {
        return context.getSharedPreferences(context.getString(R.string.preference_file_name), PREFERENCE_MODE);
    }

    private static SharedPreferences.Editor getEditor(Context context) {
        return getPreferences(context).edit();
    }

    public static void setFirstTimeLaunch(Context context, boolean value) {
        getEditor(context).putBoolean(context.getString(R.string.pref_is_first_time_launch), value).apply();
    }

    public static void setBlockPeriodizationSwitchButton(Context context, boolean value) {
        getEditor(context).putBoolean(context.getString(R.string.pref_block_periodization_switch_button), value).apply();
    }

    public static void setWorkoutMode(Context context, boolean value) {
        getEditor(context).putBoolean(context.getString(R.string.pref_workout_mode), value).apply();
    }

    public static void setCancelWorkoutCheck(Context context, boolean value) {
        getEditor(context).putBoolean(context.getString(R.string.pref_cancel_workout_check), value).apply();
    }

    public static void setSoundsMuted(Context context, boolean value) {
        getEditor(context).putBoolean(context.getString(R.string.pref_sounds_muted), value).apply();
    }

    public static void setKeepScreenOnSwitchEnabled(Context context, boolean value) {
        getEditor(context).putBoolean(context.getString(R.string.pref_keep_screen_on_switch_enabled), value).apply();
    }

    public static void setTimerWorkout(Context context, int value) {
        getEditor(context).putInt(context.getString(R.string.pref_timer_workout), value).apply();
    }

    public static void setTimerRest(Context context, int value) {
        getEditor(context).putInt(context.getString(R.string.pref_timer_rest), value).apply();
    }

    public static void setTimerSet(Context context, int value) {
        getEditor(context).putInt(context.getString(R.string.pref_timer_set), value).apply();
    }

    public static void setTimerPeriodizationSet(Context context, int value) {
        getEditor(context).putInt(context.getString(R.string.pref_timer_periodization_set), value).apply();
    }

    public static void setTimerPeriodizationTime(Context context, int value) {
        getEditor(context).putInt(context.getString(R.string.pref_timer_periodization_time), value).apply();
    }

    public static void setNotificationMotivationAlertTexts(Context context, Set<String> value) {
        getEditor(context).putStringSet(context.getString(R.string.pref_notification_motivation_alert_texts), value).apply();
    }

    public static boolean isFirstTimeLaunch(Context context) {
        return getPreferences(context).getBoolean(context.getString(R.string.pref_is_first_time_launch), true);
    }

    public static boolean getBlockPeriodizationSwitchButton(Context context) {
        return getPreferences(context).getBoolean(context.getString(R.string.pref_block_periodization_switch_button), false);
    }

    public static boolean getWorkoutMode(Context context) {
        return getPreferences(context).getBoolean(context.getString(R.string.pref_workout_mode), false);
    }

    public static boolean getKeepScreenOnSwitchEnabled(Context context) {
        return getPreferences(context).getBoolean(context.getString(R.string.pref_keep_screen_on_switch_enabled), true);
    }

    public static boolean getStartTimerSwitchEnabled(Context context) {
        return getPreferences(context).getBoolean(context.getString(R.string.pref_start_timer_switch_enabled), true);
    }

    public static boolean getBlinkingProgressBar(Context context) {
        return getPreferences(context).getBoolean(context.getString(R.string.pref_blinking_progress_bar), false);
    }

    public static boolean getCaloriesCounter(Context context) {
        return getPreferences(context).getBoolean(context.getString(R.string.pref_calories_counter), true);
    }

    public static boolean getSoundsMuted(Context context) {
        return getPreferences(context).getBoolean(context.getString(R.string.pref_sounds_muted), true);
    }

    public static boolean getCancelWorkoutCheck(Context context) {
        return getPreferences(context).getBoolean(context.getString(R.string.pref_cancel_workout_check), true);
    }

    public static boolean getNotificationMotivationAlertEnabled(Context context) {
        return getPreferences(context).getBoolean(context.getString(R.string.pref_notification_motivation_alert_enabled), false);
    }

    public static boolean getVoiceCountdownWorkout(Context context) {
        return getPreferences(context).getBoolean(context.getString(R.string.pref_voice_countdown_workout), false);
    }

    public static boolean getVoiceCountdownRest(Context context) {
        return getPreferences(context).getBoolean(context.getString(R.string.pref_voice_countdown_rest), false);
    }

    public static boolean getSoundRythm(Context context) {
        return getPreferences(context).getBoolean(context.getString(R.string.pref_sound_rythm), false);
    }

    public static boolean getVoiceHalftime(Context context) {
        return getPreferences(context).getBoolean(context.getString(R.string.pref_voice_halftime), false);
    }

    public static int getTimerWorkout(Context context) {
        return getPreferences(context).getInt(context.getString(R.string.pref_timer_workout), workoutTimeDefault);
    }

    public static int getTimerRest(Context context) {
        return getPreferences(context).getInt(context.getString(R.string.pref_timer_rest), restTimeDefault);
    }

    public static int getTimerSet(Context context) {
        return getPreferences(context).getInt(context.getString(R.string.pref_timer_set), setsDefault);
    }

    public static int getTimerPeriodizationSet(Context context) {
        return getPreferences(context).getInt(context.getString(R.string.pref_timer_periodization_set), blockPeriodizationSetsDefault);
    }

    public static int getTimerPeriodizationTime(Context context) {
        return getPreferences(context).getInt(context.getString(R.string.pref_timer_periodization_time), blockPeriodizationTimeDefault);
    }

    public static long getNotificationMotivationAlertTime(Context context) {
        return getPreferences(context).getLong(context.getString(R.string.pref_notification_motivation_alert_time), 64800000);
    }

    public static String getAge(Context context) {
        return getPreferences(context).getString(context.getString(R.string.pref_age), "25");
    }

    public static String getHeight(Context context) {
        return getPreferences(context).getString(context.getString(R.string.pref_height), "170");
    }

    public static String getWeight(Context context) {
        return getPreferences(context).getString(context.getString(R.string.pref_weight), "70");
    }

    public static Set<String> getNotificationMotivationAlertTexts(Context context) {
        Set<String> defaultStringSet = new HashSet<>(Arrays.asList(context.getResources().getStringArray(R.array.pref_default_notification_motivation_alert_messages)));
        return getPreferences(context).getStringSet(context.getString(R.string.pref_notification_motivation_alert_texts), defaultStringSet);
    }
}
