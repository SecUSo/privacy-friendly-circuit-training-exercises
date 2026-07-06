package org.secuso.privacyfriendlycircuittraining

import android.content.Context
import androidx.lifecycle.map
import org.secuso.pfacore.application.PFData
import org.secuso.pfacore.model.Theme
import org.secuso.pfacore.model.about.About
import org.secuso.pfacore.model.preferences.Preferable
import org.secuso.pfacore.ui.dialog.show
import org.secuso.pfacore.ui.help.Help
import org.secuso.pfacore.ui.preferences.appPreferences
import org.secuso.pfacore.ui.preferences.settings.action
import org.secuso.pfacore.ui.preferences.settings.appearance
import org.secuso.pfacore.ui.preferences.settings.general
import org.secuso.pfacore.ui.preferences.settings.input
import org.secuso.pfacore.ui.preferences.settings.preferenceFirstTimeLaunch
import org.secuso.pfacore.ui.preferences.settings.radio
import org.secuso.pfacore.ui.preferences.settings.settingDeviceInformationOnErrorReport
import org.secuso.pfacore.ui.preferences.settings.settingThemeSelector
import org.secuso.pfacore.ui.preferences.settings.switch
import org.secuso.pfacore.ui.preferences.settings.time
import org.secuso.pfacore.ui.tutorial.buildTutorial


class PFApplicationData private constructor(context: Context) {

    lateinit var theme: Preferable<String>
        private set
    lateinit var firstTimeLaunch: Preferable<Boolean>
        private set
    lateinit var includeDeviceDataInReport: Preferable<Boolean>
        private set

    lateinit var keepScreenOn: Preferable<Boolean>
        private set
    lateinit var startTimer: Preferable<Boolean>
        private set
    lateinit var blinkingProgressBar: Preferable<Boolean>
        private set
    lateinit var voiceCountdownWorkout: Preferable<Boolean>
        private set
    lateinit var voiceCountdownRest: Preferable<Boolean>
        private set
    lateinit var soundRythm: Preferable<Boolean>
        private set
    lateinit var voiceHalftime: Preferable<Boolean>
        private set
    lateinit var cancelWorkoutCheck: Preferable<Boolean>
        private set
    lateinit var caloriesCounter: Preferable<Boolean>
        private set
    lateinit var gender: Preferable<String>
        private set

    lateinit var age: Preferable<String>
        private set
    lateinit var weight: Preferable<String>
        private set

    lateinit var notificationTime: Preferable<Long>
        private set

    private val preferences = appPreferences(context) {
        preferences {
            firstTimeLaunch = preferenceFirstTimeLaunch
        }
        settings {
            category(context.getString(R.string.pref_header_notification)) {
                switch {
                    key = context.getString(R.string.pref_notification_motivation_alert_enabled)
                    default = false
                    backup = true
                    title { resource(R.string.pref_title_motivation_alert_switch) }
                    summary { resource(R.string.pref_summary_motivation_alert_switch) }
                    onUpdate = { enabled ->
                        if (enabled) {
                            org.secuso.privacyfriendlycircuittraining.helpers.NotificationHelper.setMotivationAlert(context)
                        } else {
                            org.secuso.privacyfriendlycircuittraining.helpers.NotificationHelper.cancelMotivationAlert(context)
                        }
                    }
                }
                notificationTime = time {
                    key = context.getString(R.string.pref_notification_motivation_alert_time)
                    default = 64_800_000L
                    backup = true
                    title { resource(R.string.pref_title_notification_motivation_alert_time) }
                    summary { transform { _, value ->
                        val totalMin = (value / 60_000L).toInt()
                        String.format("%02d:%02d", totalMin / 60, totalMin % 60)
                    } }
                    validation = { _, _ -> true }
                }
                action {
                    onClick = { activity ->
                        activity.startActivity(
                            android.content.Intent(
                                activity,
                                org.secuso.privacyfriendlycircuittraining.activities.MotivationAlertTextsActivity::class.java
                            )
                        )
                    }
                    title { resource(R.string.pref_notification_motivation_alert_texts_title) }
                }
            }
            category(context.getString(R.string.pref_header_workout)) {
                keepScreenOn = switch {
                    key = context.getString(R.string.pref_keep_screen_on_switch_enabled)
                    title { resource(R.string.pref_keep_screen_on_switch) }
                    summary { resource(R.string.pref_keep_screen_on_switch_summary) }
                    default = true
                    backup = true
                }
                startTimer = switch {
                    key = context.getString(R.string.pref_start_timer_switch_enabled)
                    title { resource(R.string.pref_start_timer_switch) }
                    summary { resource(R.string.pref_start_timer_switch_summary) }
                    default = true
                    backup = true
                }
                blinkingProgressBar = switch {
                    key = context.getString(R.string.pref_blinking_progress_bar)
                    title { resource(R.string.pref_blinking_progress_bar_title) }
                    summary { resource(R.string.pref_blinking_progress_bar_summary) }
                    default = false
                    backup = true
                }
                voiceCountdownWorkout = switch {
                    key = context.getString(R.string.pref_voice_countdown_workout)
                    title { resource(R.string.pref_voice_countdown_workout_title) }
                    summary { resource(R.string.pref_voice_countdown_workout_summary) }
                    default = true
                    backup = true
                }
                voiceCountdownRest = switch {
                    key = context.getString(R.string.pref_voice_countdown_rest)
                    title { resource(R.string.pref_voice_countdown_rest_title) }
                    summary { resource(R.string.pref_voice_countdown_rest_summary) }
                    default = true
                    backup = true
                }
                soundRythm = switch {
                    key = context.getString(R.string.pref_sound_rythm)
                    title { resource(R.string.pref_sound_rythm_title) }
                    summary { resource(R.string.pref_sound_rythm_summary) }
                    default = true
                    backup = true
                }
                voiceHalftime = switch {
                    key = context.getString(R.string.pref_voice_halftime)
                    title { resource(R.string.pref_voice_halftime_title) }
                    summary { resource(R.string.pref_voice_halftime_summary) }
                    default = true
                    backup = true
                }
                cancelWorkoutCheck = switch {
                    key = context.getString(R.string.pref_cancel_workout_check)
                    title { resource(R.string.pref_cancel_workout_check_title) }
                    summary { resource(R.string.pref_cancel_workout_check_summary) }
                    default = true
                    backup = true
                }
            }
            category(context.getString(R.string.pref_statistics_title)) {
                caloriesCounter = switch {
                    key = context.getString(R.string.pref_calories_counter)
                    title { resource(R.string.pref_calories_counter_title) }
                    summary { resource(R.string.pref_calories_counter_summary) }
                    default = true
                    backup = true
                }
                action {
                    onClick = { activity ->
                        org.secuso.pfacore.model.dialog.AbortElseDialog.build(activity) {
                            title = { activity.getString(R.string.pref_delete_statistics_dialog_title) }
                            content = { activity.getString(R.string.pref_delete_statistics_dialog_info) }
                            acceptLabel = activity.getString(R.string.alert_confirm_dialog_positive)
                            abortLabel = activity.getString(R.string.alert_confirm_dialog_negative)
                            onElse = {
                                org.secuso.privacyfriendlycircuittraining.database.PFASQLiteHelper(activity)
                                    .deleteAllWorkokutData()
                                android.widget.Toast.makeText(
                                    activity,
                                    R.string.pref_delete_statistics_dialog_toast,
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            }
                        }.show()
                    }
                    title { resource(R.string.pref_delete_statistics_dialog_title) }
                }
            }
            category(context.getString(R.string.pref_group_title_personal_settings)) {
                gender = radio {
                    key = context.getString(R.string.pref_gender)
                    default = "1"
                    backup = true
                    entries {
                        entries(R.array.pref_keys_gender)
                        values(resources.getStringArray(R.array.pref_values_gender).toList())
                    }
                    title { resource(R.string.pref_title_gender) }
                    summary { transform { state, value -> state.entries.find { it.value == value }!!.entry } }
                }
                age = input<String> {
                    key = context.getString(R.string.pref_age)
                    default = "25"
                    backup = true
                    title { resource(R.string.pref_title_age) }
                    summary { transform { _, value -> value } }
                    validation = { value ->
                        val n = value?.toIntOrNull()
                        if (n != null && n in 1..120) value else null
                    }
                }
                weight = input<String> {
                    key = context.getString(R.string.pref_weight)
                    default = "70"
                    backup = true
                    title { resource(R.string.pref_title_weight) }
                    summary { transform { _, value -> value } }
                    validation = { value ->
                        val n = value?.toIntOrNull()
                        if (n != null && n in 1..500) value else null
                    }
                }
            }
            appearance {
                theme = settingThemeSelector
            }
            general {
                includeDeviceDataInReport = settingDeviceInformationOnErrorReport
            }
        }
    }

    private val help = Help.build(context) {
        listOf(
            R.string.help_whatis to R.string.help_whatis_answer,
            R.string.help_feature_workout_timer to R.string.help_feature_workout_timer_answer,
            R.string.help_feature_motivation_alert to R.string.help_feature_motivation_alert_answer,
            R.string.help_feature_block_periodization to R.string.help_feature_block_periodization_answer,
            R.string.help_feature_workout_history to R.string.help_feature_workout_history_answer,
            R.string.help_feature_own_exercises to R.string.help_feature_own_exercises_answer,
            R.string.help_privacy to R.string.help_privacy_answer,
            R.string.help_permission to R.string.help_permission_answer,
        ).forEach { (q, a) ->
            item {
                title { resource(q) }
                description { resource(a) }
            }
        }
    }

    private val about = About(
        name = context.resources.getString(R.string.app_name),
        version = BuildConfig.VERSION_NAME,
        authors = "Betul Cuhadar, SECUSO",
        repo = context.resources.getString(org.secuso.pfacore.R.string.about_github)
    )

    private val tutorial = buildTutorial {
        stage {
            title = context.getString(R.string.slide1_heading)
            images = single(R.mipmap.ic_splash)
            description = context.getString(R.string.slide1_text1) + "\n\n" +
                    context.getString(R.string.slide1_text2)
        }
        stage {
            title = context.getString(R.string.slide4_heading)
            images = single(R.mipmap.ic_splash)
            description = context.getString(R.string.slide4_text_1) + "\n\n" +
                    context.getString(R.string.slide4_text_2)
        }
        stage {
            title = context.getString(R.string.slide3_heading)
            images = single(R.mipmap.ic_splash)
            description = context.getString(R.string.slide3_text)
        }
        stage {
            title = context.getString(R.string.slide2_heading)
            images = single(R.mipmap.ic_splash)
            description = context.getString(R.string.slide2_text)
        }
        stage {
            title = context.getString(R.string.slide5_heading)
            images = single(R.mipmap.ic_splash)
            description = context.getString(R.string.slide5_text)
        }
    }

    val data = PFData(
        about = about,
        help = help,
        preferences = preferences,
        tutorial = tutorial,
        theme = theme.state.map { Theme.valueOf(it) },
        firstLaunch = firstTimeLaunch,
        includeDeviceDataInReport = includeDeviceDataInReport,
    )

    companion object {
        private var _instance: PFApplicationData? = null
        fun instance(context: Context): PFApplicationData {
            if (_instance == null) {
                _instance = PFApplicationData(context)
            }
            return _instance!!
        }
    }
}