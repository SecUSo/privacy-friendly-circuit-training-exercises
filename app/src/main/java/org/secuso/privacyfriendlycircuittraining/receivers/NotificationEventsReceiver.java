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

package org.secuso.privacyfriendlycircuittraining.receivers;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import static org.secuso.privacyfriendlycircuittraining.helpers.NotificationHelper.isMotivationAlertEnabled;
import static org.secuso.privacyfriendlycircuittraining.helpers.NotificationHelper.setMotivationAlert;

/**
 * Receives a broadcast when boot is completed and restarts the motivation notification if
 * it is enabled.
 *
 * @author Alexander Karakuz
 * @version 20170812
 */
public class NotificationEventsReceiver extends BroadcastReceiver {

    private static final String TAG = "NotificationEventsReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()) && AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED.equals(intent.getAction())) return;
        if(isMotivationAlertEnabled(context)){
            setMotivationAlert(context);
        }
    }
}
