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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.secuso.privacyfriendlycircuittraining.R;
import org.secuso.privacyfriendlycircuittraining.activities.ExerciseActivity;
import org.secuso.privacyfriendlycircuittraining.database.PFASQLiteHelper;
import org.secuso.privacyfriendlycircuittraining.fragments.ExerciseDialogFragment;
import org.secuso.privacyfriendlycircuittraining.fragments.ExerciseSetDialogFragment;
import org.secuso.privacyfriendlycircuittraining.models.Exercise;

import java.util.ArrayList;
import java.util.List;

public class DialogAdapter extends RecyclerView.Adapter<DialogAdapter.MyViewHolder> {

    private ArrayList<Exercise> exerciseList;
    private ExerciseSetDialogFragment exerciseDialog;
    private PFASQLiteHelper db = null;
    private Context ctx;

    public DialogAdapter(ArrayList<Exercise> exerciseList, Context ctx) {
        this.exerciseList = exerciseList;
        this.ctx = ctx;
        //exerciseDialog = (ExerciseSetDialogFragment) ctx;
        db = new PFASQLiteHelper(ctx);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_dialog, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        Exercise ex = exerciseList.get(position);
        holder.name.setText(ex.getName());
        holder.exerciseImg.setImageBitmap(getImage(ex.getImage()));

        holder.deleteButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                exerciseList.remove(position);
                updateAdapter(exerciseList);
            }
        });
    }

    @Override
    public int getItemCount() {
        return exerciseList.size();
    }


    public void updateAdapter(ArrayList<Exercise> list){
        exerciseList = list;
        notifyDataSetChanged();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private TextView name;
        private ImageView exerciseImg;
        private ImageButton deleteButton;
        private CardView cardView;
        public MyViewHolder(View view) {
            super(view);
            name = (TextView) view.findViewById(R.id.exercise_name);
            exerciseImg = (ImageView) view.findViewById(R.id.exercise_img);
            deleteButton = (ImageButton) view.findViewById(R.id.deleteButton);

            cardView = (CardView) itemView.findViewById(R.id.cardView_dialog);
        }

        @Override
        public void onClick(View v) {
        }
    }

    // convert from byte array to bitmap
    public static Bitmap getImage(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }
}

