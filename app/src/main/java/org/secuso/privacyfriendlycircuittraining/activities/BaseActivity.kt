package org.secuso.privacyfriendlycircuittraining.activities

import android.content.Intent
import androidx.core.content.ContextCompat
import org.secuso.pfacore.model.DrawerElement
import org.secuso.pfacore.model.DrawerMenu
import org.secuso.pfacore.ui.activities.DrawerActivity
import org.secuso.privacyfriendlycircuittraining.R

abstract class BaseActivity : DrawerActivity() {

    override fun drawer() = DrawerMenu.build {
        name = ContextCompat.getString(this@BaseActivity, R.string.app_name)
        icon = R.mipmap.ic_launcher
        section {
            activity {
                name = ContextCompat.getString(this@BaseActivity, R.string.action_main)
                icon = R.drawable.ic_home_24dp
                clazz = MainActivity::class.java
                extras = { it.apply { flags = Intent.FLAG_ACTIVITY_CLEAR_TOP } }
            }
            activity {
                name = ContextCompat.getString(this@BaseActivity, R.string.action_exercisesets)
                icon = R.drawable.ic_exercise_pushup
                clazz = ExerciseSetActivity::class.java
            }
            activity {
                name = ContextCompat.getString(this@BaseActivity, R.string.action_exercises)
                icon = R.drawable.ic_workout_24dp
                clazz = ExerciseActivity::class.java
            }
            activity {
                name = ContextCompat.getString(this@BaseActivity, R.string.action_statistics)
                icon = R.drawable.ic_statistics_24dp
                clazz = StatisticsActivity::class.java
            }
        }
        defaultDrawerSection(this)
    }

    override fun isActiveDrawerElement(element: DrawerElement): Boolean = false

    companion object {
        const val MAIN_CONTENT_FADEOUT_DURATION = 150
        const val MAIN_CONTENT_FADEIN_DURATION = 250
    }
}