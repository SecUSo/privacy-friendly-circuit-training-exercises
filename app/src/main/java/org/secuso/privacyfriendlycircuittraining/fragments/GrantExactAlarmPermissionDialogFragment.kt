package org.secuso.privacyfriendlycircuittraining.fragments

import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import org.secuso.pfacore.model.dialog.AbortElseDialog
import org.secuso.pfacore.ui.dialog.show
// import org.secuso.pfacore.ui... .show   ← statistics'te kullandığın show extension, aynısı
import org.secuso.privacyfriendlycircuittraining.R

object ExactAlarmPermissionDialog {
    @JvmStatic
    @RequiresApi(Build.VERSION_CODES.S)
    fun show(activity: AppCompatActivity) {
        AbortElseDialog.build(activity) {
            title       = { activity.getString(R.string.request_schedule_exact_alarm_permission_title) }
            content     = { activity.getString(R.string.request_schedule_exact_alarm_permission) }
            acceptLabel = activity.getString(R.string.okay)
            abortLabel  = activity.getString(R.string.cancel)
            onElse = {
                activity.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
            }
        }.show()
    }
}