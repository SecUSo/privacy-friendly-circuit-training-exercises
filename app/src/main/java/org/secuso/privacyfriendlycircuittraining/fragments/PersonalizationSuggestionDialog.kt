package org.secuso.privacyfriendlycircuittraining.fragments

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import org.secuso.pfacore.model.dialog.AbortElseDialog
import org.secuso.pfacore.ui.activities.SettingsActivity
import org.secuso.pfacore.ui.dialog.show
import org.secuso.privacyfriendlycircuittraining.R

object PersonalizationSuggestionDialog {
    @JvmStatic
    fun show(activity: AppCompatActivity) {
        AbortElseDialog.build(activity) {
            title       = { activity.getString(R.string.alert_personalization_title) }
            content     = { activity.getString(R.string.alert_personalization_message) }
            acceptLabel = activity.getString(R.string.alert_confirm_dialog_positive)
            abortLabel  = activity.getString(R.string.alert_confirm_dialog_negative)
            onElse = {
                activity.startActivity(Intent(activity, SettingsActivity::class.java))
            }
        }.show()
    }
}