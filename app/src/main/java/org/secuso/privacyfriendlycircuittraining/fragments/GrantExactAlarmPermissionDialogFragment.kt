package org.secuso.privacyfriendlycircuittraining.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.annotation.RequiresApi
import android.app.DialogFragment
import org.secuso.privacyfriendlycircuittraining.R

class GrantExactAlarmPermissionDialogFragment : DialogFragment() {
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity.let {
            val builder = AlertDialog.Builder(it)
            builder.setMessage(R.string.request_schedule_exact_alarm_permission)
                .setPositiveButton(
                    R.string.okay
                ) { dialog, _ ->
                    val askForExactAlarmIntent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    dialog.dismiss()
                    context.startActivity(askForExactAlarmIntent)
                }
                .setNegativeButton(
                    R.string.cancel
                ) { dialog, _ ->
                    dialog.dismiss()
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    companion object {
        const val TAG = "GrantExactAlarmPermissionDialog"
    }
}