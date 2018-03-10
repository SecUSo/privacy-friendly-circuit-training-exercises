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

package org.secuso.privacyfriendlycircuittraining.adapters;

/**
 * Adapter for Exercises
 *
 * @author Nils Schroth
 * @version 20180103
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.secuso.privacyfriendlycircuittraining.R;
import org.secuso.privacyfriendlycircuittraining.activities.ExerciseActivity;
import org.secuso.privacyfriendlycircuittraining.database.PFASQLiteHelper;
import org.secuso.privacyfriendlycircuittraining.fragments.ExerciseDialogFragment;
import org.secuso.privacyfriendlycircuittraining.models.Exercise;
import org.secuso.privacyfriendlycircuittraining.models.ExerciseSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.MyViewHolder> {

    private List<Exercise> exerciseList;
    private ExerciseActivity exerciseActivity;
    private PFASQLiteHelper db = null;

    public ExerciseAdapter(List<Exercise> exerciseList, Context ctx) {
        this.exerciseList = exerciseList;
        exerciseActivity = (ExerciseActivity) ctx;
        db = new PFASQLiteHelper(ctx);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_exercise, parent, false);

        return new MyViewHolder(itemView, exerciseActivity);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Exercise ex = exerciseList.get(position);
        holder.name.setText(ex.getName());
        holder.description.setText(ex.getDescription());
        holder.exerciseImg.setImageBitmap(getImage(ex.getImage()));

        if(!exerciseActivity.getIsInActionMode()){
            holder.checkbox.setVisibility(View.GONE);
        }
        else{
            holder.checkbox.setVisibility(View.VISIBLE);
            holder.checkbox.setChecked(false);
        }
    }

    @Override
    public int getItemCount() {
        return exerciseList.size();
    }


    public void updateAdapter(ArrayList<Exercise> list){
        exerciseList = db.getAllExercise();
        for(Exercise ex : list){
            exerciseList.remove(ex);
            updateExerciseSets(list);
        }
        notifyDataSetChanged();
    }

    private void updateExerciseSets(ArrayList<Exercise> list){
        List<ExerciseSet> tmp = db.getAllExerciseSet();
        for(ExerciseSet es : tmp){
            for(Exercise ex: list)
                if(es.getExercises().contains(ex.getID())){
                    ExerciseSet es_tmp = es;
                    es_tmp.getExercises().removeAll(Collections.singleton(ex.getID()));
                    try {
                        db.updateExerciseSet(es_tmp);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
        }
    }


    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private TextView name, description;
        private ImageView exerciseImg;
        private CheckBox checkbox;
        private ExerciseActivity exerciseActivity;
        private CardView cardView;
        public MyViewHolder(View view, ExerciseActivity exerciseActivity) {
            super(view);
            name = (TextView) view.findViewById(R.id.exercise_name);
            description = (TextView) view.findViewById(R.id.exercise_description);
            exerciseImg = (ImageView) view.findViewById(R.id.exercise_img);

            checkbox = (CheckBox) view.findViewById(R.id.check_exercise);
            checkbox.setOnClickListener(this);

            this.exerciseActivity = exerciseActivity;
            cardView = (CardView) itemView.findViewById(R.id.cardView_exercise);
            cardView.setOnLongClickListener(exerciseActivity);
            cardView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(exerciseActivity.getIsInActionMode())
                exerciseActivity.prepareSelection(v, getAdapterPosition());
            else {
                if (!ExerciseDialogFragment.isOpened()) {
                    ExerciseDialogFragment listDialogFragment = ExerciseDialogFragment.newEditInstance(getAdapterPosition());
                    listDialogFragment.show(exerciseActivity.getSupportFragmentManager(), "DialogFragment");
                }
            }
        }

    }

    // convert from byte array to bitmap
    public static Bitmap getImage(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }
}

