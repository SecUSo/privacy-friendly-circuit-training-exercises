/**
 * This file is part of Privacy Friendly Circuit Trainer.
 * Privacy Friendly Circuit Trainer is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or any later version.
 * Privacy Friendly Circuit Trainer is distributed in the hope
 * that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with Privacy Friendly Interval Timer. If not, see <http://www.gnu.org/licenses/>.
 */

package org.secuso.privacyfriendlycircuittraining.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import org.secuso.privacyfriendlycircuittraining.R;
import org.secuso.privacyfriendlycircuittraining.database.PFASQLiteHelper;
import org.secuso.privacyfriendlycircuittraining.helpers.BitMapUtility;
import org.secuso.privacyfriendlycircuittraining.models.Exercise;
import org.secuso.privacyfriendlycircuittraining.models.ExerciseSet;
import org.secuso.privacyfriendlycircuittraining.tutorial.PrefManager;
import org.secuso.privacyfriendlycircuittraining.tutorial.TutorialActivity;

import java.util.ArrayList;

/**
 * @author Karola Marky, Nils Schroth
 * @version 20180321
 */


public class SplashActivity extends AppCompatActivity {

    private PrefManager prefManager;
    private PFASQLiteHelper db = new PFASQLiteHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefManager = new PrefManager(this);
        if (prefManager.isFirstTimeLaunch()) {
            //add two example exercises

            Uri ic_squat = Uri.parse("android.resource://" + this.getPackageName() + "/" + R.drawable.ic_exercise_squat);
            Uri ic_pushup = Uri.parse("android.resource://" + this.getPackageName() + "/" + R.drawable.ic_exercise_pushup);
            Exercise defaultExercise1 = new Exercise(0, "Squat", "Example description", ic_squat);
            Exercise defaultExercise2 = new Exercise(0, "Pushup", "Example description", ic_pushup);
            db.addExercise(defaultExercise1);
            db.addExercise(defaultExercise2);
            ArrayList<Integer> tmp = new ArrayList<>();
            tmp.add(db.getAllExercise().get(0).getID());
            tmp.add(db.getAllExercise().get(1).getID());
            ExerciseSet defaultExerciseSet = new ExerciseSet(0, "Example", tmp);

            db.addExerciseSet(defaultExerciseSet);
            Intent mainIntent = new Intent(SplashActivity.this, TutorialActivity.class);
            SplashActivity.this.startActivity(mainIntent);
            SplashActivity.this.finish();
        }
        else{
            Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
            SplashActivity.this.startActivity(mainIntent);
            SplashActivity.this.finish();
        }

    }

}
