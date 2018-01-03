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

package org.secuso.privacyfriendlycircuittraining.activities;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import org.secuso.privacyfriendlycircuittraining.R;
import org.secuso.privacyfriendlycircuittraining.adapters.ExerciseSetAdapter;
import org.secuso.privacyfriendlycircuittraining.database.PFASQLiteHelper;
import org.secuso.privacyfriendlycircuittraining.models.ExerciseSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Exercise Sets view
 *
 * @author Nils Schroth
 * @version 20180103
 */

public class ExerciseSetActivity extends BaseActivity implements View.OnLongClickListener{

    private List<ExerciseSet> exerciseSetsList = new ArrayList<>();
    private RecyclerView recyclerView;
    private ExerciseSetAdapter mAdapter;
    private FloatingActionButton newListFab;
    private FloatingActionButton deleteFab;
    private LinearLayout noListsLayout;
    public boolean is_in_action_mode = false;
    ArrayList<ExerciseSet> selection_list = new ArrayList<>();
    PFASQLiteHelper db = new PFASQLiteHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_exercisesets);

        exerciseSetsList = db.getAllExerciseSet();

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        mAdapter = new ExerciseSetAdapter(exerciseSetsList, ExerciseSetActivity.this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        newListFab = (FloatingActionButton) findViewById(R.id.fab_new_list);
        deleteFab = (FloatingActionButton) findViewById(R.id.fab_delete_item);
        noListsLayout = (LinearLayout) findViewById(R.id.no_lists_layout);

        deleteFab.setVisibility(View.GONE);
        noListsLayout.setVisibility(View.VISIBLE);
        setNoExererciseSetsMessage();

        newListFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Random rand = new Random();
                int n = rand.nextInt(10);
                String name = "Übungsset" + n;
                String exercises = "Squat, Push-ups, Sit-Ups";

                int lastId = (int) db.addExerciseSet(new ExerciseSet(0, name, exercises));
                exerciseSetsList.add(new ExerciseSet(lastId, name, exercises));

                mAdapter.updateAdapter(selection_list);
                setNoExererciseSetsMessage();
                }
        });

        deleteFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAdapter.updateAdapter(selection_list);
                for(ExerciseSet es : selection_list){
                    db.deleteExerciseSet(es);
                }
                clearActionMode();
                setNoExererciseSetsMessage();
            }
        });

    }

    private void setNoExererciseSetsMessage(){
        if(mAdapter.getItemCount() == 0){
            findViewById(R.id.no_lists_layout).setVisibility(View.VISIBLE);
        }
        else{
            findViewById(R.id.no_lists_layout).setVisibility(View.GONE);
        }
    }


    public void clearActionMode(){
        is_in_action_mode = false;
        selection_list.clear();
        newListFab.setVisibility(View.VISIBLE);
        deleteFab.setVisibility(View.GONE);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimary)));
    }

    @Override
    public void onBackPressed(){
        if(is_in_action_mode){
            clearActionMode();
            mAdapter.notifyDataSetChanged();
        }
        else{
            super.onBackPressed();
        }
    }

    @Override
    public boolean onLongClick(View v) {
        is_in_action_mode = true;
        mAdapter.notifyDataSetChanged();
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        newListFab.setVisibility(View.GONE);
        deleteFab.setVisibility(View.VISIBLE);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.LTGRAY));
        return true;
    }

    public void prepareSelection(View view, int position){
        if(((CheckBox) view).isChecked()){
            selection_list.add(exerciseSetsList.get(position));
        }
        else{
            selection_list.remove(exerciseSetsList.get(position));
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
    public LinearLayout getNoListsLayout()
    {
        return noListsLayout;
    }

}
