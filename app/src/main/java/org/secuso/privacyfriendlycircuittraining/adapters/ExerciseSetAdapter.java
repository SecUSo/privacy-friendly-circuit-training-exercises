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
import android.widget.TextView;

import org.secuso.privacyfriendlycircuittraining.R;
import org.secuso.privacyfriendlycircuittraining.activities.ExerciseSetActivity;
import org.secuso.privacyfriendlycircuittraining.models.ExerciseSet;

import java.util.ArrayList;
import java.util.List;

public class ExerciseSetAdapter extends RecyclerView.Adapter<ExerciseSetAdapter.MyViewHolder> {

    private List<ExerciseSet> exerciseSetsList;
    ExerciseSetActivity exerciseSetActivity;

    public ExerciseSetAdapter(List<ExerciseSet> exerciseSetsList, Context ctx) {
        this.exerciseSetsList = exerciseSetsList;
        exerciseSetActivity = (ExerciseSetActivity) ctx;
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
        holder.exercises.setText(es.getExercises());

        if(!exerciseSetActivity.is_in_action_mode){
            holder.checkbox.setVisibility(View.GONE);
        }
        else{
            holder.checkbox.setVisibility(View.VISIBLE);
            holder.checkbox.setChecked(false);
        }
    }

    @Override
    public int getItemCount() {
        return exerciseSetsList.size();
    }


    public void updateAdapter(ArrayList<ExerciseSet> list){
        for(ExerciseSet es : list){
            exerciseSetsList.remove(es);
        }
        notifyDataSetChanged();
    }



    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView name, number, exercises;
        CheckBox checkbox;
        ExerciseSetActivity exerciseSetActivity;
        CardView cardView;
        public MyViewHolder(View view, ExerciseSetActivity exerciseSetActivity) {
            super(view);
            name = (TextView) view.findViewById(R.id.name);
            exercises = (TextView) view.findViewById(R.id.exercises);
            number = (TextView) view.findViewById(R.id.number);
            checkbox = (CheckBox) view.findViewById(R.id.check_list_item);
            checkbox.setOnClickListener(this);

            this.exerciseSetActivity = exerciseSetActivity;
            cardView = (CardView) itemView.findViewById(R.id.cardView);
            cardView.setOnLongClickListener(exerciseSetActivity);

        }

        @Override
        public void onClick(View v) {
            exerciseSetActivity.prepareSelection(v, getAdapterPosition());
        }
    }
}

