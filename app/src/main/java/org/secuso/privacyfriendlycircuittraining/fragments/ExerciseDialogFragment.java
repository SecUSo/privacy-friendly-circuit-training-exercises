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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.secuso.privacyfriendlycircuittraining.R;
import org.secuso.privacyfriendlycircuittraining.activities.ExerciseActivity;
import org.secuso.privacyfriendlycircuittraining.database.PFASQLiteHelper;
import org.secuso.privacyfriendlycircuittraining.helpers.BitMapUtility;
import org.secuso.privacyfriendlycircuittraining.models.Exercise;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

/**
 * Displays the Dialog for creating new exercises and edit existing exercises
 *
 * @author Nils Schroth
 * @version 20180103
 */

public class ExerciseDialogFragment extends DialogFragment {

    private static boolean editDialog;
    private static boolean opened;
    private View v;
    private ExerciseActivity ea;
    private static int adapterPosition;
    private static int adapterId;
    private static Toast toast;
    private PFASQLiteHelper db = null;

    public static ExerciseDialogFragment newEditInstance(int i)
    {
        editDialog = true;
        adapterPosition = i;
        ExerciseDialogFragment dialogFragment = getListDialogFragment();
        return dialogFragment;
    }

    public static ExerciseDialogFragment newAddInstance()
    {
        editDialog = false;
        ExerciseDialogFragment dialogFragment = getListDialogFragment();
        return dialogFragment;
    }

    private static ExerciseDialogFragment getListDialogFragment()
    {
        ExerciseDialogFragment dialogFragment = new ExerciseDialogFragment();
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
        ea = (ExerciseActivity) getActivity();
        db = new PFASQLiteHelper(ea);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppTheme_Dialog);
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(getActivity().LAYOUT_INFLATER_SERVICE);
        v = inflater.inflate(R.layout.exercise_dialog, null);

        builder.setView(v);
        builder.setTitle(getResources().getString(R.string.new_exercise));

        List<Exercise> exerciseList = new ArrayList<>();
        exerciseList = db.getAllExercise();

        if(exerciseList.size() > 0)
            adapterId = exerciseList.get(adapterPosition).getID();
        else{
            adapterId = 0;
        }

        ImageView fragment_img = (ImageView) v.findViewById(R.id.exercise_fragment_img);

        fragment_img.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, 1);
            }
        });

        if(editDialog){
            builder.setTitle(getResources().getString(R.string.edit));

            EditText name = (EditText) v.findViewById(R.id.exercise_name);

            EditText description = (EditText) v.findViewById(R.id.exercise_description);

            fragment_img.setImageBitmap(BitMapUtility.getImage(db.getExercise(adapterId).getImage()));
            name.setText(db.getExercise(adapterId).getName());
            description.setText(db.getExercise(adapterId).getDescription());
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
                EditText EditName = (EditText) v.findViewById(R.id.exercise_name);
                EditText EditDesciption = (EditText) v.findViewById(R.id.exercise_description);
                String name = EditName.getText().toString();
                String desciption = EditDesciption.getText().toString();

                ImageView fragment_img = (ImageView) v.findViewById(R.id.exercise_fragment_img);
                Bitmap bitmap = ((BitmapDrawable)fragment_img.getDrawable()).getBitmap();
                byte[] image = BitMapUtility.getBytes(bitmap);


                if( name.length() == 0 ) {
                    toast = Toast.makeText(ea, getResources().getString(R.string.valid_name), Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
                else {
                    if (editDialog) {
                        ea.updateExercise(adapterPosition, adapterId, name, desciption, image);
                    } else {
                        ea.addExercise(name, desciption, image);
                    }
                    ea.setNoExererciseMessage();
                    dialog.dismiss();
                }
            }
        });

        return dialog;
    }

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {
            try {
                final Uri imageUri = data.getData();
                final InputStream imageStream = getActivity().getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                ImageView fragment_img = (ImageView) v.findViewById(R.id.exercise_fragment_img);
                fragment_img.setImageBitmap(selectedImage);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(ea, getResources().getString(R.string.load_image_error), Toast.LENGTH_LONG).show();
            }

        }else {
            Toast.makeText(ea, getResources().getString(R.string.no_image_picked),Toast.LENGTH_LONG).show();
        }
    }
}
