/**
 * This file is part of Privacy Friendly Circuit Trainer.
 * Privacy Friendly Circuit Trainer is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or any later version.
 * Privacy Friendly Interval Timer is distributed in the hope
 * that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with Privacy Friendly Circuit Trainer. If not, see <http://www.gnu.org/licenses/>.
 */

package org.secuso.privacyfriendlycircuittraining.adapters;

/**
 * Adapter for Exercise Sets
 *
 * @author Nils Schroth
 * @version 20180103
 */

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import org.secuso.privacyfriendlycircuittraining.R;
import org.secuso.privacyfriendlycircuittraining.activities.ExerciseSetActivity;
import org.secuso.privacyfriendlycircuittraining.database.PFASQLiteHelper;
import org.secuso.privacyfriendlycircuittraining.fragments.ExerciseSetDialogFragment;
import org.secuso.privacyfriendlycircuittraining.helpers.BitMapUtility;
import org.secuso.privacyfriendlycircuittraining.models.ExerciseSet;

import java.util.ArrayList;
import java.util.List;

public class ExerciseSetAdapter extends RecyclerView.Adapter<ExerciseSetAdapter.MyViewHolder> {

    private List<ExerciseSet> exerciseSetsList;
    private ExerciseSetActivity exerciseSetActivity;
    private PFASQLiteHelper db = null;

    public ExerciseSetAdapter(List<ExerciseSet> exerciseSetsList, Context ctx) {
        this.exerciseSetsList = exerciseSetsList;
        exerciseSetActivity = (ExerciseSetActivity) ctx;
        db = new PFASQLiteHelper(ctx);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_exerciseset, parent, false);

        return new MyViewHolder(itemView, exerciseSetActivity);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        ExerciseSet es = exerciseSetsList.get(position);
        holder.name.setText(es.getName());
        setExerciseImages(holder, es);

        if(!exerciseSetActivity.getIsInActionMode()){
            holder.checkbox.setVisibility(View.GONE);
        }
        else{
            holder.checkbox.setVisibility(View.VISIBLE);
            holder.checkbox.setChecked(false);
        }
    }

    public void setExerciseImages(MyViewHolder holder, ExerciseSet es){
        int i = 0;
        for(Integer ex : es.getExercises()){
            if(i>=6){
                holder.imageViews[5].setImageResource(R.drawable.ic_black_dots);
                holder.imageViews[5].setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                holder.imageViews[5].setScaleX((float)0.7);
                holder.imageViews[5].setScaleY((float)0.7);
                return;
            }
            else {
                holder.imageViews[i].setImageBitmap(BitMapUtility.getImage(db.getExercise(ex).getImage()));
            }
            i++;
        }
        for(int j = i; j<6; j++){
            holder.imageViews[j].setImageResource(0);
        }
        if(holder.imageViews[5].getScaleX() < 1){
            holder.imageViews[5].setScaleX((float)1);
            holder.imageViews[5].setScaleY((float)1);
        }
    }

    @Override
    public int getItemCount() {
        return exerciseSetsList.size();
    }


    public void updateAdapter(ArrayList<ExerciseSet> list){
        exerciseSetsList = db.getAllExerciseSet();
        for(ExerciseSet es : list){
            exerciseSetsList.remove(es);
        }
        notifyDataSetChanged();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private TextView name, number;
        private ImageView[] imageViews;
        private ImageView exercise1, exercise2, exercise3, exercise4, exercise5, exercise6;
        private CheckBox checkbox;
        private ExerciseSetActivity exerciseSetActivity;
        private CardView cardView;
        public MyViewHolder(View view, ExerciseSetActivity exerciseSetActivity) {
            super(view);
            name = (TextView) view.findViewById(R.id.name);
            exercise1 = (ImageView) view.findViewById(R.id.exercise_img_1);
            exercise2 = (ImageView) view.findViewById(R.id.exercise_img_2);
            exercise3 = (ImageView) view.findViewById(R.id.exercise_img_3);
            exercise4 = (ImageView) view.findViewById(R.id.exercise_img_4);
            exercise5 = (ImageView) view.findViewById(R.id.exercise_img_5);
            exercise6 = (ImageView) view.findViewById(R.id.exercise_img_6);

            imageViews = new ImageView[]{exercise1, exercise2, exercise3, exercise4, exercise5, exercise6};

            number = (TextView) view.findViewById(R.id.number);
            checkbox = (CheckBox) view.findViewById(R.id.check_list_item);
            checkbox.setOnClickListener(this);

            this.exerciseSetActivity = exerciseSetActivity;
            cardView = (CardView) itemView.findViewById(R.id.cardView);
            cardView.setOnLongClickListener(exerciseSetActivity);
            cardView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(exerciseSetActivity.getIsInActionMode())
                exerciseSetActivity.prepareSelection(v, getAdapterPosition());
            else {
                if (!ExerciseSetDialogFragment.isOpened()) {
                    ExerciseSetDialogFragment listDialogFragment = ExerciseSetDialogFragment.newEditInstance(getAdapterPosition());
                    listDialogFragment.show(exerciseSetActivity.getSupportFragmentManager(), "DialogFragment");
                }
            }
        }

    }
}

