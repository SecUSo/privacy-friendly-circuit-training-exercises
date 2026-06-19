package org.secuso.privacyfriendlycircuittraining

import android.app.Activity
import android.util.Log
import androidx.work.Configuration
import org.secuso.pfacore.application.SQLiteHelperConfig
import org.secuso.pfacore.ui.PFApplication
import org.secuso.pfacore.ui.PFData
import org.secuso.privacyfriendlycircuittraining.activities.MainActivity
import org.secuso.privacyfriendlycircuittraining.database.PFASQLiteHelper

class PFCircuitTrainingApplication : PFApplication() {

    override val name: String
        get() = getString(R.string.app_name)

    override val database
        get() = SQLiteHelperConfig(baseContext, PFASQLiteHelper.DATABASE_NAME)

    override val data: PFData
        get() = PFApplicationData.instance(this).data

    override val mainActivity: Class<out Activity> = MainActivity::class.java

    override val workManagerConfiguration by lazy {
        Configuration.Builder().setMinimumLoggingLevel(Log.INFO).build()
    }
}