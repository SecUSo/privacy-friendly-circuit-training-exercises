package org.secuso.privacyfriendlycircuittraining.fragments

import androidx.appcompat.app.AppCompatActivity
import org.secuso.pfacore.model.dialog.AbortElseDialog
import org.secuso.pfacore.ui.dialog.show
import org.secuso.privacyfriendlycircuittraining.R

object DeleteItemsDialog {

    @JvmStatic
    fun show(
        activity: AppCompatActivity,
        onDelete: Runnable
    ) {
        AbortElseDialog.build(activity) {
            title = {
                activity.getString(R.string.delete)
            }

            content = {
                activity.getString(
                    R.string.dialog_exercise_set_confirm_delete_message
                )
            }

            acceptLabel =
                activity.getString(R.string.delete)

            abortLabel =
                activity.getString(R.string.cancel)

            icon = android.R.drawable.ic_dialog_alert

            onElse = {
                onDelete.run()
            }
        }.show()
    }
}