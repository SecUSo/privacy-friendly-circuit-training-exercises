package org.secuso.privacyfriendlycircuittraining.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.secuso.privacyfriendlycircuittraining.R;
import org.secuso.privacyfriendlycircuittraining.activities.ExerciseSetActivity;
import org.secuso.privacyfriendlycircuittraining.database.PFASQLiteHelper;
import org.secuso.privacyfriendlycircuittraining.models.ExerciseSet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nils on 15.01.18.
 */

public class ExerciseSetDialogFragment extends DialogFragment {

    private static boolean editDialog;
    private static boolean opened;
    private View v;
    private ExerciseSetActivity ea;
    private static int adapterPosition;
    private static int adapterId;
    private static Spinner spinner1;
    private static Spinner spinner2;
    private static Spinner spinner3;
    private static Spinner spinner4;
    private static Spinner spinner5;
    private static Spinner spinner6;
    private static Toast toast;
    private PFASQLiteHelper db = null;

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
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppTheme_Dialog);
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(getActivity().LAYOUT_INFLATER_SERVICE);
        v = inflater.inflate(R.layout.exerciseset_dialog, null);

        builder.setView(v);

        spinner1 = (Spinner) v.findViewById(R.id.ex1);
        spinner2 = (Spinner) v.findViewById(R.id.ex2);
        spinner3 = (Spinner) v.findViewById(R.id.ex3);
        spinner4 = (Spinner) v.findViewById(R.id.ex4);
        spinner5 = (Spinner) v.findViewById(R.id.ex5);
        spinner6 = (Spinner) v.findViewById(R.id.ex6);

        List<ExerciseSet> exerciseSetsList = new ArrayList<>();
        exerciseSetsList = db.getAllExerciseSet();

        if(exerciseSetsList.size() > 0)
            adapterId = exerciseSetsList.get(adapterPosition).getID();
        else{
            adapterId = 0;
        }

        if(editDialog){
            TextView text = (TextView) v.findViewById(R.id.dialog_title);
            text.setText(getResources().getString(R.string.edit));

            EditText etext = (EditText) v.findViewById(R.id.list_name);

            etext.setText(db.getExerciseSet(adapterId).getName());

            ArrayList<String> exercisesList = db.getExerciseSet(adapterId).getExercises();
            ArrayList<Integer> exerciseId = new ArrayList<Integer>();
            for(String ex : exercisesList){
                if(ex.equals("squat"))
                    exerciseId.add(1);
                if(ex.equals("pushup"))
                    exerciseId.add(2);
            }
            if(exercisesList.size() > 0)
                spinner1.setSelection(exerciseId.get(0));
            if(exercisesList.size() > 1)
                spinner2.setSelection(exerciseId.get(1));
            if(exercisesList.size() > 2)
                spinner3.setSelection(exerciseId.get(2));
            if(exercisesList.size() > 3)
                spinner4.setSelection(exerciseId.get(3));
            if(exercisesList.size() > 4)
                spinner5.setSelection(exerciseId.get(4));
            if(exercisesList.size() > 5)
                spinner6.setSelection(exerciseId.get(5));
        }

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

                int selectedEx1 = spinner1.getSelectedItemPosition();
                int selectedEx2 = spinner2.getSelectedItemPosition();
                int selectedEx3 = spinner3.getSelectedItemPosition();
                int selectedEx4 = spinner4.getSelectedItemPosition();
                int selectedEx5 = spinner5.getSelectedItemPosition();
                int selectedEx6 = spinner6.getSelectedItemPosition();

                ArrayList<String> exercises = new ArrayList<String>();
                if(selectedEx1 > 0)
                    exercises.add(intToExerciseName(selectedEx1));
                if(selectedEx2 > 0)
                    exercises.add(intToExerciseName(selectedEx2));
                if(selectedEx3 > 0)
                    exercises.add(intToExerciseName(selectedEx3));
                if(selectedEx4 > 0)
                    exercises.add(intToExerciseName(selectedEx4));
                if(selectedEx5 > 0)
                    exercises.add(intToExerciseName(selectedEx5));
                if(selectedEx6 > 0)
                    exercises.add(intToExerciseName(selectedEx6));

                if( name.length() == 0 ) {
                    toast = Toast.makeText(ea, getResources().getString(R.string.valid_exercise_set_name), Toast.LENGTH_LONG);
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
                            ea.updateExerciseSet(adapterPosition, adapterId, name, exercises);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        ea.addExerciseSet(name, exercises);
                    }
                    ea.setNoExererciseSetsMessage();
                    dialog.dismiss();
                }
            }
        });


        return dialog;
    }

    private String intToExerciseName(int exerciseInt){
        if(exerciseInt == 1)
            return "squat";
        if(exerciseInt == 2)
            return "pushup";
        return "";
    }
}
