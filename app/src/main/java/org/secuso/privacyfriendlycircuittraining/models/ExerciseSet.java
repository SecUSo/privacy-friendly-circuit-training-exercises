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

package org.secuso.privacyfriendlycircuittraining.models;

/**
 * Exercise Set model
 *
 * @author Nils Schroth
 * @version 20180103
 */

public class ExerciseSet {
    private String NAME, EXERCISES;
    private int ID;

    public ExerciseSet() {
    }

    public ExerciseSet(int id, String name, String exercises) {

        this.ID = id;
        this.NAME = name;
        this.EXERCISES = exercises;
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

    public String getExercises() {
        return EXERCISES;
    }

    public void setExercises(String exercises) {
        this.EXERCISES = exercises;
    }
}
