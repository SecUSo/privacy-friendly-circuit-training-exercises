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

package org.secuso.privacyfriendlycircuittraining.models;

import java.util.ArrayList;

/**
 * Exercise Set model
 *
 * @author Nils Schroth
 * @version 20180103
 */

public class ExerciseSet {
    private String NAME;
    private ArrayList<Integer> EXERCISE_IDS;
    private int ID;

    public ExerciseSet() {
    }

    public ExerciseSet(int id, String name, ArrayList<Integer> exercise_ids) {

        this.ID = id;
        this.NAME = name;
        this.EXERCISE_IDS = exercise_ids;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getName() {
        return NAME;
    }

    public void setName(String name) { this.NAME = name; }

    public ArrayList<Integer> getExercises() {
        return EXERCISE_IDS;
    }

    public void setExercises(ArrayList<Integer> exercise_ids) { this.EXERCISE_IDS = exercise_ids; }

    public int getNumber() { return EXERCISE_IDS.size(); }

}
