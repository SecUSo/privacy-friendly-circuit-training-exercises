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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import org.secuso.privacyfriendlycircuittraining.R;
import org.secuso.privacyfriendlycircuittraining.adapters.ExerciseAdapter;
import org.secuso.privacyfriendlycircuittraining.database.PFASQLiteHelper;
import org.secuso.privacyfriendlycircuittraining.fragments.ExerciseDialogFragment;
import org.secuso.privacyfriendlycircuittraining.models.Exercise;

import java.util.ArrayList;
import java.util.List;

/**
 * Exercise view
 *
 * @author Nils Schroth
 * @version 20180103
 */

public class ExerciseActivity extends BaseActivity implements View.OnLongClickListener{

    private List<Exercise> exerciseList = new ArrayList<>();
    private RecyclerView recyclerView;
    private ExerciseAdapter mAdapter;
    private FloatingActionButton newListFab;
    private FloatingActionButton deleteFab;
    private FloatingActionButton acceptFab;
    private LinearLayout noListsLayout;
    private boolean is_in_delete_mode = false;
    private boolean is_in_picker_mode = false;
    private ArrayList<Exercise> selection_list = new ArrayList<>();
    private PFASQLiteHelper db = new PFASQLiteHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent myIntent = getIntent(); // gets the previously created intent
        is_in_picker_mode = myIntent.getBooleanExtra("pickerMode", false);

        setContentView(R.layout.activity_exercise);

        // TODO: Database calls on main thread?! This should be changed
        exerciseList = db.getAllExercise();

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView_ex);
        recyclerView.setHasFixedSize(true);
        mAdapter = new ExerciseAdapter(exerciseList, ExerciseActivity.this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        //mLayoutManager.setReverseLayout(true);
        //mLayoutManager.setStackFromEnd(true);
        recyclerView.setAdapter(mAdapter);

        newListFab = (FloatingActionButton) findViewById(R.id.fab_new_list_ex);
        deleteFab = (FloatingActionButton) findViewById(R.id.fab_delete_item_ex);
        acceptFab = (FloatingActionButton) findViewById(R.id.fab_accept_item_ex);
        noListsLayout = (LinearLayout) findViewById(R.id.no_lists_layout_ex);

        deleteFab.setVisibility(View.GONE);
        if(is_in_picker_mode) {
            acceptFab.setVisibility(View.VISIBLE);
            newListFab.setVisibility(View.GONE);
        }else{
            acceptFab.setVisibility(View.GONE);
            newListFab.setVisibility(View.VISIBLE);
        }
        noListsLayout.setVisibility(View.VISIBLE);
        setNoExererciseMessage();

        newListFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if ( !ExerciseDialogFragment.isOpened() )
                {
                    ExerciseDialogFragment listDialogFragment = ExerciseDialogFragment.newAddInstance();
                    listDialogFragment.show(getSupportFragmentManager(), "DialogFragment");
                }
            }
        });

        deleteFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ExerciseActivity.this, 0);

                builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        for(Exercise ex : selection_list){
                            db.deleteExercise(ex);
                        }
                        mAdapter.updateAdapter(selection_list);
                        clearActionMode();
                        setNoExererciseMessage();
                    }
                });
                builder.setNegativeButton(R.string.cancel, null);
                builder.setMessage(R.string.dialog_exercise_set_confirm_delete_message);
                builder.create().show();
            }
        });

        acceptFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("result", selection_list);
                setResult(Activity.RESULT_OK, returnIntent);
                clearActionMode();
                finish();
            }
        });

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        setDrawerEnabled(!is_in_picker_mode);

        mDrawerToggle.setToolbarNavigationClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(is_in_picker_mode)
                    onBackPressed();
            }
        });
    }

    public void setDrawerEnabled(final boolean enabled) {

        int lockMode = enabled ?
                DrawerLayout.LOCK_MODE_UNLOCKED :
                DrawerLayout.LOCK_MODE_LOCKED_CLOSED;

        mDrawerLayout.setDrawerLockMode(lockMode);

        mDrawerToggle.setDrawerIndicatorEnabled(enabled);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(!enabled);
            actionBar.setDisplayShowHomeEnabled(enabled);
            actionBar.setHomeButtonEnabled(enabled);
        }

        mDrawerToggle.syncState();
    }


    public void addExercise(Exercise exercise) {
        exerciseList.add(exercise);
        ArrayList<Exercise> empty = new ArrayList<>();
        mAdapter.updateAdapter(empty);
        recyclerView.getLayoutManager().scrollToPosition(exerciseList.size()-1);
    }

    public void updateExercise(int position, Exercise exercise){
        exerciseList.set(position, exercise);
        ArrayList<Exercise> empty = new ArrayList<>();
        mAdapter.updateAdapter(empty);
        mAdapter.notifyItemChanged(position);
    }


    public void setNoExererciseMessage(){
        if(mAdapter.getItemCount() == 0){
            findViewById(R.id.no_lists_layout_ex).setVisibility(View.VISIBLE);
        }
        else{
            findViewById(R.id.no_lists_layout_ex).setVisibility(View.GONE);
        }
    }


    public void clearActionMode(){
        is_in_delete_mode = false;
        is_in_picker_mode = false;
        if(is_in_delete_mode)
            selection_list.clear();
        newListFab.setVisibility(View.VISIBLE);
        deleteFab.setVisibility(View.GONE);
        acceptFab.setVisibility(View.GONE);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimary)));
        this.getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary));
    }

    @Override
    public void onBackPressed(){
        if(is_in_delete_mode){
            clearActionMode();
            mAdapter.notifyDataSetChanged();
        }
        else{
            super.onBackPressed();
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if(!is_in_picker_mode) {
            is_in_delete_mode = true;
            mAdapter.notifyDataSetChanged();
            //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            newListFab.setVisibility(View.GONE);
            deleteFab.setVisibility(View.VISIBLE);
            acceptFab.setVisibility(View.GONE);
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.LTGRAY));
            this.getWindow().setStatusBarColor(Color.LTGRAY);
        }
        return true;
    }

    public void prepareSelection(View view, int position){
        if(view instanceof CheckBox) {
            if (((CheckBox) view).isChecked()) {
                selection_list.add(exerciseList.get(position));
            } else {
                selection_list.remove(exerciseList.get(position));
            }
        }
    }

    protected int getNavigationDrawerID() {
        return R.id.nav_exercises;
    }

    public FloatingActionButton getNewListFab()
    {
        return newListFab;
    }
    public FloatingActionButton getDeleteFab()
    {
        return deleteFab;
    }
    public LinearLayout getNoListsLayout() {return noListsLayout;}
    public boolean getIsInActionMode() {return is_in_delete_mode || is_in_picker_mode;}

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

}
