/**
 * This file is part of Privacy Friendly Interval Timer.
 * Privacy Friendly Interval Timer is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or any later version.
 * Privacy Friendly Interval Timer is distributed in the hope
 * that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with Privacy Friendly Interval Timer. If not, see <http://www.gnu.org/licenses/>.
 */

package org.secuso.privacyfriendlycircuittraining.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.secuso.privacyfriendlycircuittraining.models.Exercise;
import org.secuso.privacyfriendlycircuittraining.models.ExerciseSet;
import org.secuso.privacyfriendlycircuittraining.models.WorkoutSessionData;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Karola Marky, Alexander Karakuz
 * @version 20161223
 * Structure based on http://www.androidhive.info/2011/11/android-sqlite-database-tutorial/
 * accessed at 16th June 2016
 *
 * This class defines the structure of our database.
 */

public class PFASQLiteHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    /**
     * Use the following pattern for the name of the database
     * PF_[Name of the app]_DB
     */
    public static final String DATABASE_NAME = "PF_TRAINING_DB";

    //Name of the table in the database
    private static final String TABLE_DATA = "WORKOUT_SESSION";
    private static final String TABLE_DATA_ES = "EXERCISE_SET";
    private static final String TABLE_DATA_EX = "EXERCISES";

    //Names of columns in the databases in this example we only use one table
    private static final String KEY_ID = "id";
    private static final String KEY_WORKOUT_TIME = "workoutTime";
    private static final String KEY_CALORIES = "calories";
    private static final String KEY_TIMESTAMP = "time";

    private static final String KEY_ID_ES = "id";
    private static final String KEY_NAME_ES = "name";
    private static final String KEY_EXERCISES_ES = "exercises";

    private static final String KEY_ID_EX = "id";
    private static final String KEY_NAME_EX = "name";
    private static final String KEY_DESCIRPTION_EX = "description";
    private static final String KEY_IMAGE_EX = "image";

    public PFASQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        /**
         * Create the table data on the first start
         * */
        String EXERCISE_SET_TABLE = "CREATE TABLE " + TABLE_DATA_ES +
                "(" +
                KEY_ID_ES + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                KEY_NAME_ES + " TEXT," +
                KEY_EXERCISES_ES + " TEXT);";

        sqLiteDatabase.execSQL(EXERCISE_SET_TABLE);

        String EXERCISE_TABLE = "CREATE TABLE " + TABLE_DATA_EX +
                "(" +
                KEY_ID_EX + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                KEY_NAME_EX + " TEXT," +
                KEY_DESCIRPTION_EX + " TEXT," +
                KEY_IMAGE_EX + " BLOB);";

        sqLiteDatabase.execSQL(EXERCISE_TABLE);

        String WORKOUT_SESSION_TABLE = "CREATE TABLE " + TABLE_DATA +
                "(" +
                KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                KEY_WORKOUT_TIME + " LONG," +
                KEY_CALORIES + " INTEGER," +
                KEY_TIMESTAMP + " INTEGER);";

        sqLiteDatabase.execSQL(WORKOUT_SESSION_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_DATA);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_DATA_ES);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_DATA_EX);
        onCreate(sqLiteDatabase);
    }


    /**
     * Adds a single sampleData to our Table
     * As no ID is provided and KEY_ID is autoincremented (see line 50)
     * the last available key of the table is taken and incremented by 1
     * @param sampleData data that will be added
     */
    public void addWorkoutData(WorkoutSessionData sampleData) {
        SQLiteDatabase database = this.getWritableDatabase();

        //To adjust this class for your own data, please add your values here.
        ContentValues values = new ContentValues();
        values.put(KEY_WORKOUT_TIME, sampleData.getWORKOUTTIME());
        values.put(KEY_CALORIES, sampleData.getCALORIES());

        database.insert(TABLE_DATA, null, values);
        database.close();
    }


    /**
     * Adds a single sampleData to our Table
     * As no ID is provided and KEY_ID is autoincremented (see line 50)
     * the last available key of the table is taken and incremented by 1
     * @param sampleData data that will be added
     */
    public long addExerciseSet(ExerciseSet sampleData) {
        SQLiteDatabase database = this.getWritableDatabase();


        String exerciseList = "";
        try {
            JSONObject json = new JSONObject();
            json.put("uniqueArrays", new JSONArray(sampleData.getExercises()));
            exerciseList = json.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //To adjust this class for your own data, please add your values here.
        ContentValues values = new ContentValues();
        values.put(KEY_NAME_ES, sampleData.getName());
        values.put(KEY_EXERCISES_ES, exerciseList);

        long id = database.insert(TABLE_DATA_ES, null, values);
        database.close();
        return id;
    }

    /**
     * Adds a single sampleData to our Table
     * As no ID is provided and KEY_ID is autoincremented (see line 50)
     * the last available key of the table is taken and incremented by 1
     * @param sampleData data that will be added
     */
    public long addExercise(Exercise sampleData) {
        SQLiteDatabase database = this.getWritableDatabase();

        //To adjust this class for your own data, please add your values here.
        ContentValues values = new ContentValues();
        values.put(KEY_NAME_EX, sampleData.getName());
        values.put(KEY_DESCIRPTION_EX, sampleData.getDescription());
        values.put(KEY_IMAGE_EX, sampleData.getImage());

        long id = database.insert(TABLE_DATA_EX, null, values);
        database.close();
        return id;
    }

    /**
     * Adds a single sampleData to our Table
     * This method can be used for re-insertion for example an undo-action
     * Therefore, the key of the sampleData will also be written into the database
     * @param sampleData data that will be added
     * Only use this for undo options and re-insertions
     */
    public void addWorkoutDataWithID(WorkoutSessionData sampleData) {
        SQLiteDatabase database = this.getWritableDatabase();

        //To adjust this class for your own data, please add your values here.
        ContentValues values = new ContentValues();
        values.put(KEY_ID, sampleData.getID());
        values.put(KEY_WORKOUT_TIME, sampleData.getWORKOUTTIME());
        values.put(KEY_CALORIES, sampleData.getCALORIES());

        database.insert(TABLE_DATA, null, values);

        //always close the database after insertion
        database.close();
    }


    /**
     * Adds a single sampleData to our Table
     * This method can be used for re-insertion for example an undo-action
     * Therefore, the key of the sampleData will also be written into the database
     * @param sampleData data that will be added
     * Only use this for undo options and re-insertions
     */
    public void addExerciseSetWithID(ExerciseSet sampleData) {
        SQLiteDatabase database = this.getWritableDatabase();

        String exerciseList = "";
        try {
            JSONObject json = new JSONObject();
            json.put("uniqueArrays", new JSONArray(sampleData.getExercises()));
            exerciseList = json.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //To adjust this class for your own data, please add your values here.
        ContentValues values = new ContentValues();
        values.put(KEY_ID_ES, sampleData.getID());
        values.put(KEY_NAME_ES, sampleData.getName());
        values.put(KEY_EXERCISES_ES, exerciseList);

        database.insert(TABLE_DATA_ES, null, values);

        //always close the database after insertion
        database.close();
    }


    /**
     * Adds a single sampleData to our Table
     * This method can be used for re-insertion for example an undo-action
     * Therefore, the key of the sampleData will also be written into the database
     * @param sampleData data that will be added
     * Only use this for undo options and re-insertions
     */
    public void addExerciseWithID(Exercise sampleData) {
        SQLiteDatabase database = this.getWritableDatabase();

        //To adjust this class for your own data, please add your values here.
        ContentValues values = new ContentValues();
        values.put(KEY_ID_EX, sampleData.getID());
        values.put(KEY_NAME_EX, sampleData.getName());
        values.put(KEY_DESCIRPTION_EX, sampleData.getDescription());
        values.put(KEY_IMAGE_EX, sampleData.getImage());

        database.insert(TABLE_DATA_EX, null, values);

        //always close the database after insertion
        database.close();
    }


    /**
     * This method gets a single sampleData entry based on its ID
     * @param id of the sampleData that is requested, could be get by the get-method
     * @return the sampleData that is requested.
     */
    public WorkoutSessionData getWorkoutData(int id) {
        SQLiteDatabase database = this.getWritableDatabase();

        Log.d("DATABASE", Integer.toString(id));

        Cursor cursor = database.query(TABLE_DATA, new String[]{KEY_ID,
                        KEY_WORKOUT_TIME, KEY_CALORIES, KEY_TIMESTAMP}, KEY_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);

        WorkoutSessionData data = new WorkoutSessionData();

        if( cursor != null && cursor.moveToFirst() ){
            data.setID(Integer.parseInt(cursor.getString(0)));
            data.setWORKOUTTIME(Integer.parseInt(cursor.getString(1)));
            data.setCALORIES(Integer.parseInt(cursor.getString(2)));

            Log.d("DATABASE", "Read " + cursor.getString(1) + " from DB");
            
            cursor.close();
        }

        return data;

    }

    /**
     * This method gets a single sampleData entry based on its ID
     * @param id of the sampleData that is requested, could be get by the get-method
     * @return the sampleData that is requested.
     */
    public ExerciseSet getExerciseSet(int id) {
        SQLiteDatabase database = this.getWritableDatabase();

        Log.d("DATABASE", Integer.toString(id));

        Cursor cursor = database.query(TABLE_DATA_ES, new String[]{KEY_ID_ES,
                        KEY_NAME_ES, KEY_EXERCISES_ES}, KEY_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);

        ExerciseSet data = new ExerciseSet();

        if( cursor != null && cursor.moveToFirst() ){
            data.setID(Integer.parseInt(cursor.getString(0)));
            data.setName(cursor.getString(1));

            ArrayList<Integer> exerciseList = new ArrayList<Integer>();
            try {
                JSONObject json = new JSONObject(cursor.getString(2));
                JSONArray jArray = json.optJSONArray("uniqueArrays");
                for (int i = 0; i < jArray.length(); i++) {
                    exerciseList.add(Integer.parseInt(jArray.getString(i)));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            data.setExercises(exerciseList);

            Log.d("DATABASE", "Read " + cursor.getString(1) + " from  ES DB");

            cursor.close();
        }

        return data;

    }


    /**
     * This method gets a single sampleData entry based on its ID
     * @param id of the sampleData that is requested, could be get by the get-method
     * @return the sampleData that is requested.
     */
    public Exercise getExercise(int id) {
        SQLiteDatabase database = this.getWritableDatabase();

        Log.d("DATABASE", Integer.toString(id));

        Cursor cursor = database.query(TABLE_DATA_EX, new String[]{KEY_ID_EX,
                        KEY_NAME_EX, KEY_DESCIRPTION_EX, KEY_IMAGE_EX}, KEY_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);

        Exercise data = new Exercise(0, null, null, null);

        if( cursor != null && cursor.moveToFirst() ){
            data.setID(Integer.parseInt(cursor.getString(0)));
            data.setName(cursor.getString(1));
            data.setDescription(cursor.getString(2));
            data.setImage(cursor.getBlob(3));

            Log.d("DATABASE", "Read " + cursor.getString(1) + " from  EX DB");

            cursor.close();
        }

        return data;

    }

    /**
     * This method returns all data from the DB as a list
     * This could be used for instance to fill a recyclerView
     * @return A list of all available sampleData in the Database
     */
    public List<WorkoutSessionData> getAllWorkoutData() {
        List<WorkoutSessionData> sampleDataList = new ArrayList<WorkoutSessionData>();

        String selectQuery = "SELECT  * FROM " + TABLE_DATA;

        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);

        WorkoutSessionData sampleData = null;

        if (cursor.moveToFirst()) {
            do {
                //To adjust this class for your own data, please add your values here.
                //be careful to use the right get-method to get the data from the cursor
                sampleData = new WorkoutSessionData();
                sampleData.setID(Integer.parseInt(cursor.getString(0)));
                sampleData.setWORKOUTTIME(Integer.parseInt(cursor.getString(1)));
                sampleData.setCALORIES(Integer.parseInt(cursor.getString(2)));

                sampleDataList.add(sampleData);
            } while (cursor.moveToNext());
        }

        return sampleDataList;
    }


    /**
     * This method returns all data from the DB as a list
     * This could be used for instance to fill a recyclerView
     * @return A list of all available sampleData in the Database
     */
    public List<ExerciseSet> getAllExerciseSet() {
        List<ExerciseSet> sampleDataList = new ArrayList<ExerciseSet>();

        String selectQuery = "SELECT  * FROM " + TABLE_DATA_ES;

        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);

        ExerciseSet sampleData = null;

        if (cursor.moveToFirst()) {
            do {
                //To adjust this class for your own data, please add your values here.
                //be careful to use the right get-method to get the data from the cursor
                sampleData = new ExerciseSet();
                sampleData.setID(Integer.parseInt(cursor.getString(0)));
                sampleData.setName(cursor.getString(1));

                ArrayList<Integer> exerciseList = new ArrayList<Integer>();
                try {
                    JSONObject json = new JSONObject(cursor.getString(2));
                    JSONArray jArray = json.optJSONArray("uniqueArrays");
                    for (int i = 0; i < jArray.length(); i++) {
                        exerciseList.add(Integer.parseInt(jArray.getString(i)));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                sampleData.setExercises(exerciseList);

                sampleDataList.add(sampleData);
                Log.d("DATABASE", "Read " + cursor.getString(1) + " from  ES DB");

            } while (cursor.moveToNext());
        }

        return sampleDataList;
    }


    /**
     * This method returns all data from the DB as a list
     * This could be used for instance to fill a recyclerView
     * @return A list of all available sampleData in the Database
     */
    public List<Exercise> getAllExercise() {
        List<Exercise> sampleDataList = new ArrayList<Exercise>();

        String selectQuery = "SELECT  * FROM " + TABLE_DATA_EX;

        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);

        Exercise sampleData = null;

        if (cursor.moveToFirst()) {
            do {
                //To adjust this class for your own data, please add your values here.
                //be careful to use the right get-method to get the data from the cursor
                sampleData = new Exercise(0, null, null, null);
                sampleData.setID(Integer.parseInt(cursor.getString(0)));
                sampleData.setName(cursor.getString(1));
                sampleData.setDescription(cursor.getString(2));
                sampleData.setImage(cursor.getBlob(3));

                sampleDataList.add(sampleData);
                Log.d("DATABASE", "Read " + cursor.getString(1) + " from  ES DB");

            } while (cursor.moveToNext());
        }

        return sampleDataList;
    }



    /**
     * Updates a database entry.
     * @param workoutData
     * @return actually makes the update
     */
    public int updateWorkoutData(WorkoutSessionData workoutData) {
        SQLiteDatabase database = this.getWritableDatabase();

        //To adjust this class for your own data, please add your values here.
        ContentValues values = new ContentValues();
        values.put(KEY_WORKOUT_TIME, workoutData.getWORKOUTTIME());
        values.put(KEY_CALORIES, workoutData.getCALORIES());

        return database.update(TABLE_DATA, values, KEY_ID + " = ?",
                new String[] { String.valueOf(workoutData.getID()) });
    }

    /**
     * Updates a database entry.
     * @param exerciseSet
     * @return actually makes the update
     */
    public int updateExerciseSet(ExerciseSet exerciseSet) throws JSONException {
        SQLiteDatabase database = this.getWritableDatabase();

        JSONObject json = new JSONObject();
        json.put("uniqueArrays", new JSONArray(exerciseSet.getExercises()));
        String exerciseList = json.toString();

        //To adjust this class for your own data, please add your values here.
        ContentValues values = new ContentValues();
        values.put(KEY_NAME_ES, exerciseSet.getName());
        values.put(KEY_EXERCISES_ES, exerciseList);

        return database.update(TABLE_DATA_ES, values, KEY_ID_ES + " = ?",
                new String[] { String.valueOf(exerciseSet.getID()) });
    }


    /**
     * Updates a database entry.
     * @param exercise
     * @return actually makes the update
     */
    public int updateExercise(Exercise exercise){
        SQLiteDatabase database = this.getWritableDatabase();

        //To adjust this class for your own data, please add your values here.
        ContentValues values = new ContentValues();
        values.put(KEY_NAME_EX, exercise.getName());
        values.put(KEY_DESCIRPTION_EX, exercise.getDescription());
        values.put(KEY_IMAGE_EX, exercise.getImage());

        return database.update(TABLE_DATA_EX, values, KEY_ID_EX + " = ?",
                new String[] { String.valueOf(exercise.getID()) });
    }

    /**
     * Deletes sampleData from the DB
     * This method takes the sampleData and extracts its key to build the delete-query
     * @param sampleData that will be deleted
     */
    public void deleteWorkoutData(WorkoutSessionData sampleData) {
        SQLiteDatabase database = this.getWritableDatabase();
        database.delete(TABLE_DATA, KEY_ID + " = ?",
                new String[] { Integer.toString(sampleData.getID()) });
        //always close the DB after deletion of single entries
        database.close();
    }

    /**
     * Deletes sampleData from the DB
     * This method takes the sampleData and extracts its key to build the delete-query
     * @param sampleData that will be deleted
     */
    public void deleteExerciseSet(ExerciseSet sampleData) {
        SQLiteDatabase database = this.getWritableDatabase();
        database.delete(TABLE_DATA_ES, KEY_ID_ES + " = ?",
                new String[] { Integer.toString(sampleData.getID()) });
        //always close the DB after deletion of single entries
        database.close();
    }

    /**
     * Deletes sampleData from the DB
     * This method takes the sampleData and extracts its key to build the delete-query
     * @param sampleData that will be deleted
     */
    public void deleteExercise(Exercise sampleData) {
        SQLiteDatabase database = this.getWritableDatabase();
        database.delete(TABLE_DATA_EX, KEY_ID_EX + " = ?",
                new String[] { Integer.toString(sampleData.getID()) });
        //always close the DB after deletion of single entries
        database.close();
    }

    /**
     * deletes all sampleData from the table.
     * This could be used in case of a reset of the app.
     */
    public void deleteAllWorkokutData() {
        SQLiteDatabase database = this.getWritableDatabase();
        database.execSQL("delete from "+ TABLE_DATA);
    }

    /**
     * deletes all sampleData from the table.
     * This could be used in case of a reset of the app.
     */
    public void deleteAllExerciseSet() {
        SQLiteDatabase database = this.getWritableDatabase();
        database.execSQL("delete from "+ TABLE_DATA_ES);
    }

    /**
     * deletes all sampleData from the table.
     * This could be used in case of a reset of the app.
     */
    public void deleteAllExercise() {
        SQLiteDatabase database = this.getWritableDatabase();
        database.execSQL("delete from "+ TABLE_DATA_EX);
    }

}
