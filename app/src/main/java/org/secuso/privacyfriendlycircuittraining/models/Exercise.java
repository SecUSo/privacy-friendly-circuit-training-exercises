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

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * This class holds the "data type" of a single exercise.
 * A exercise consists of a name, a description, an image and the id of the exercise.
 *
 * @author Nils Schroth
 * @version 20180103
 */

public class Exercise implements Parcelable {
    private String NAME;
    private String DESCRIPTION;
    private Uri IMAGE;
    private int ID;

    private static final long serialVersionUID = 2L;

    public Exercise(int id, String name, String description, Uri image) {
        this.ID = id;
        this.NAME = name;
        this.DESCRIPTION = description;
        this.IMAGE = image;
    }

    protected Exercise(Parcel in) {
        NAME = in.readString();
        DESCRIPTION = in.readString();
        IMAGE = in.readParcelable(Uri.class.getClassLoader());
        ID = in.readInt();
    }

    public static final Creator<Exercise> CREATOR = new Creator<Exercise>() {
        @Override
        public Exercise createFromParcel(Parcel in) {
            return new Exercise(in);
        }

        @Override
        public Exercise[] newArray(int size) {
            return new Exercise[size];
        }
    };

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

    public String getDescription() {
        return DESCRIPTION;
    }

    public void setDescription(String description) {
        this.DESCRIPTION = description;
    }

    public Uri getImage(){ return IMAGE; }

    public void setImage(Uri image) { this.IMAGE = image; }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(NAME);
        parcel.writeString(DESCRIPTION);
        parcel.writeParcelable(IMAGE, i);
        parcel.writeInt(ID);
    }
}
