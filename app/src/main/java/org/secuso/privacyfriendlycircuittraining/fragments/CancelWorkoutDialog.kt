package org.secuso.privacyfriendlycircuittraining.fragments

import androidx.appcompat.app.AppCompatActivity
import org.secuso.pfacore.model.dialog.AbortElseDialog
import org.secuso.pfacore.ui.dialog.show
import org.secuso.privacyfriendlycircuittraining.R

object CancelWorkoutDialog {

    @JvmStatic
    fun show(
        activity: AppCompatActivity,
        onCancel: Runnable,
        onConfirm: Runnable
    ) {
        AbortElseDialog.build(activity) {
            title = {
                activity.getString(R.string.workout_cancel_dialog_title)
            }

            content = {
                activity.getString(R.string.workout_canceled_info)
            }

            acceptLabel =
                activity.getString(R.string.alert_confirm_dialog_positive)

            abortLabel =
                activity.getString(R.string.alert_confirm_dialog_negative)

            onAbort = {
                onCancel.run()
            }

            onElse = {
                onConfirm.run()
            }

            handleDismiss = true
        }.show()
    }
}