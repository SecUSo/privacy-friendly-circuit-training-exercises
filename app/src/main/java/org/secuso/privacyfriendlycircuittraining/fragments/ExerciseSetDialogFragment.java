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

package org.secuso.privacyfriendlycircuittraining.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.secuso.privacyfriendlycircuittraining.R;
import org.secuso.privacyfriendlycircuittraining.activities.ExerciseActivity;
import org.secuso.privacyfriendlycircuittraining.activities.ExerciseSetActivity;
import org.secuso.privacyfriendlycircuittraining.adapters.DialogAdapter;
import org.secuso.privacyfriendlycircuittraining.database.PFASQLiteHelper;
import org.secuso.privacyfriendlycircuittraining.models.Exercise;
import org.secuso.privacyfriendlycircuittraining.models.ExerciseSet;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

/**
 * Displays the Dialog for creating new Exercises Sets and edititn existing exercise sets
 *
 * @author Nils Schroth
 * @version 20180103
 */

public class ExerciseSetDialogFragment extends DialogFragment {

    private static boolean editDialog;
    private static boolean opened;
    private View v;
    private ExerciseSetActivity ea;
    private static int adapterPosition;
    private static int adapterId;
    private PFASQLiteHelper db = null;
    private RecyclerView mRecyclerView;
    private DialogAdapter mAdapter;
    private ArrayList<Exercise> exercises;
    private Toast toast;

    public static ExerciseSetDialogFragment newEditInstance(int i)
    {
        editDialog = true;
        adapterPosition = i;
        ExerciseSetDialogFragment dialogFragment = getListDialogFragment();
        return dialogFragment;
    }

    public static ExerciseSetDialogFragment newAddInstance()
    {
        editDialog = false;
        ExerciseSetDialogFragment dialogFragment = getListDialogFragment();
        return dialogFragment;
    }

    private static ExerciseSetDialogFragment getListDialogFragment()
    {
        ExerciseSetDialogFragment dialogFragment = new ExerciseSetDialogFragment();
        return dialogFragment;
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        opened = true; // flag to avoid opening this dialog twice
    }

    @Override
    public void onDismiss(DialogInterface dialog)
    {
        super.onDismiss(dialog);
        opened = false; // flag to avoid opening this dialog twice
    }

    public static boolean isOpened()
    {
        return opened;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        ea = (ExerciseSetActivity) getActivity();
        db = new PFASQLiteHelper(ea);
        exercises = new ArrayList<>();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppTheme_Dialog);
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(getActivity().LAYOUT_INFLATER_SERVICE);
        v = inflater.inflate(R.layout.exerciseset_dialog, null);


        //RECYCER
        mRecyclerView= (RecyclerView) v.findViewById(R.id.recyclerView_dialog);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));

        //ADAPTER
        mAdapter=new DialogAdapter(exercises, this.getActivity());
        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.setHasFixedSize(true);

        builder.setView(v);
        builder.setTitle(getResources().getString(R.string.new_exercise_set));

        FloatingActionButton addExerciseFab = (FloatingActionButton) v.findViewById(R.id.fab_add_exercise);

        addExerciseFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent exercisePickerIntent = new Intent(getActivity(), ExerciseActivity.class);
                exercisePickerIntent.putExtra("pickerMode", true);
                startActivityForResult(exercisePickerIntent, 1);
            }
        });

        List<ExerciseSet> exerciseSetsList = new ArrayList<>();
        exerciseSetsList = db.getAllExerciseSet();

        if(exerciseSetsList.size() > 0)
            adapterId = exerciseSetsList.get(adapterPosition).getID();
        else{
            adapterId = 0;
        }

        if(editDialog){

            builder.setTitle(getResources().getString(R.string.edit));

            EditText etext = (EditText) v.findViewById(R.id.list_name);

            etext.setText(db.getExerciseSet(adapterId).getName());
            for(Integer ex: db.getExerciseSet(adapterId).getExercises()){
                exercises.add(db.getExercise(ex));
            }
            mAdapter.updateAdapter(exercises);
        }

        setNoExererciseMessage();

        builder.setPositiveButton("okay", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {

            }
        });

        builder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
            }
        });

        final AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                EditText text = (EditText) v.findViewById(R.id.list_name);
                String name = text.getText().toString();

                if( name.length() == 0 ) {
                    toast = Toast.makeText(ea, getResources().getString(R.string.valid_name), Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
                else if(exercises.size() == 0){
                    toast = Toast.makeText(ea, getResources().getString(R.string.choose_one_exercise), Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
                else {
                    if (editDialog) {
                        try {
                            ea.updateExerciseSet(adapterPosition, adapterId, name, exercisesToIds(exercises));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        ea.addExerciseSet(name, exercisesToIds(exercises));
                    }
                    ea.setNoExererciseSetsMessage();
                    dialog.dismiss();
                }
            }
        });

        return dialog;
    }

    public void setNoExererciseMessage(){
        if(mAdapter.getItemCount() == 0){
            v.findViewById(R.id.no_exercises_layout).setVisibility(View.VISIBLE);
        }
        else{
            v.findViewById(R.id.no_exercises_layout).setVisibility(View.GONE);
        }
    }

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {
            ArrayList<Exercise> tmp =  (ArrayList<Exercise>) data.getSerializableExtra("result");
            exercises.addAll(tmp);
            mAdapter.updateAdapter(exercises);
            setNoExererciseMessage();
        }

    }

    private ArrayList<Integer> exercisesToIds (ArrayList<Exercise> exercises){
        ArrayList<Integer> ids = new ArrayList<>();
        for(Exercise ex : exercises){
            ids.add(ex.getID());
        }
        return ids;
    }
}
