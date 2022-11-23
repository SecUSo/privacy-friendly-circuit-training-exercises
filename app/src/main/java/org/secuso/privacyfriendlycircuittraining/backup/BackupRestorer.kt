package org.secuso.privacyfriendlycircuittraining.backup

import android.content.Context
import android.content.SharedPreferences
import android.util.JsonReader
import android.util.Log
import org.secuso.privacyfriendlybackup.api.backup.DatabaseUtil
import org.secuso.privacyfriendlybackup.api.backup.FileUtil
import org.secuso.privacyfriendlybackup.api.pfa.IBackupRestorer
import org.secuso.privacyfriendlycircuittraining.R
import org.secuso.privacyfriendlycircuittraining.database.PFASQLiteHelper
import org.secuso.privacyfriendlycircuittraining.tutorial.PrefManager
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import kotlin.system.exitProcess


class BackupRestorer : IBackupRestorer {
    @Throws(IOException::class)
    private fun readDatabase(reader: JsonReader, context: Context) {
        reader.beginObject()
        val n1: String = reader.nextName()
        if (n1 != "version") {
            throw RuntimeException("Unknown value $n1")
        }
        val version: Int = reader.nextInt()
        val n2: String = reader.nextName()
        if (n2 != "content") {
            throw RuntimeException("Unknown value $n2")
        }

        Log.d(TAG, "Restoring database...")
        val restoreDatabaseName = "restoreDatabase"

        // delete if file already exists
        val restoreDatabaseFile = context.getDatabasePath(restoreDatabaseName)
        if (restoreDatabaseFile.exists()) {
            DatabaseUtil.deleteRoomDatabase(context, restoreDatabaseName)
        }

        // create new restore database
        val db = DatabaseUtil.getSupportSQLiteOpenHelper(context, restoreDatabaseName, version).writableDatabase

        db.beginTransaction()
        db.version = version

        Log.d(TAG, "Copying database contents...")
        DatabaseUtil.readDatabaseContent(reader, db)
        db.setTransactionSuccessful()
        db.endTransaction()
        db.close()

        reader.endObject()

        // copy file to correct location
        val actualDatabaseFile = context.getDatabasePath(PFASQLiteHelper.DATABASE_NAME)

        DatabaseUtil.deleteRoomDatabase(context, PFASQLiteHelper.DATABASE_NAME)

        FileUtil.copyFile(restoreDatabaseFile, actualDatabaseFile)
        Log.d(TAG, "Database restored")

        // delete restore database
        DatabaseUtil.deleteRoomDatabase(context, restoreDatabaseName)
    }

    @Throws(IOException::class)
    private fun readPreferences(reader: JsonReader, preferences: SharedPreferences.Editor, context: Context) {
        reader.beginObject()
        Log.d(TAG, "Restoring preferences...")
        while (reader.hasNext()) {
            when (val name: String = reader.nextName()) {
                context.getString(R.string.pref_is_first_time_launch),
                context.getString(R.string.pref_workout_mode),
                context.getString(R.string.pref_blinking_progress_bar),
                context.getString(R.string.pref_block_periodization_switch_button),
                context.getString(R.string.pref_keep_screen_on_switch_enabled),
                context.getString(R.string.pref_start_timer_switch_enabled),
                context.getString(R.string.pref_calories_counter),
                context.getString(R.string.pref_sounds_muted),
                context.getString(R.string.pref_cancel_workout_dialog),
                context.getString(R.string.pref_notification_motivation_alert_enabled),
                context.getString(R.string.pref_voice_countdown_workout),
                context.getString(R.string.pref_voice_countdown_rest),
                context.getString(R.string.pref_sound_rythm),
                context.getString(R.string.pref_voice_halftime),
                context.getString(R.string.pref_cancel_workout_check) -> preferences.putBoolean(name, reader.nextBoolean())
                context.getString(R.string.pref_age),
                context.getString(R.string.pref_height),
                context.getString(R.string.pref_weight),
                context.getString(R.string.pref_gender) -> preferences.putString(name, reader.nextString())

                context.getString(R.string.pref_timer_workout),
                context.getString(R.string.pref_timer_rest),
                context.getString(R.string.pref_timer_set),
                context.getString(R.string.pref_timer_periodization_set),
                context.getString(R.string.pref_timer_periodization_time) -> preferences.putInt(name, reader.nextInt())

                context.getString(R.string.pref_notification_motivation_alert_time) -> preferences.putLong(name, reader.nextLong())

                context.getString(R.string.pref_notification_motivation_alert_texts) -> preferences.putStringSet(name, readPreferenceSet(reader))
                else -> throw RuntimeException("Unknown preference $name")
            }
        }
        reader.endObject()
        Log.d(TAG, "Preferences restored")
    }

    private fun readPreferenceSet(reader: JsonReader): Set<String> {
        val preferenceSet = mutableSetOf<String>()

        reader.beginArray()
        while (reader.hasNext()) {
            preferenceSet.add(reader.nextString())
        }
        reader.endArray()
        return preferenceSet
    }

    override fun restoreBackup(context: Context, restoreData: InputStream): Boolean {
        return try {
            val isReader = InputStreamReader(restoreData)
            val reader = JsonReader(isReader)
            val preferences = PrefManager.getPreferences(context).edit()

            // START
            reader.beginObject()
            while (reader.hasNext()) {
                when (val type: String = reader.nextName()) {
                    "database" -> readDatabase(reader, context)
                    "preferences" -> readPreferences(reader, preferences, context)
                    else -> throw RuntimeException("Can not parse type $type")
                }
            }
            reader.endObject()
            preferences.commit()

            exitProcess(0)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    companion object {
        const val TAG = "PFABackupRestorer"
    }
}