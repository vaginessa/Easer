/*
 * Copyright (c) 2016 - 2017 Rui Zhao <renyuneyun@gmail.com>
 *
 * This file is part of Easer.
 *
 * Easer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Easer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Easer.  If not, see <http://www.gnu.org/licenses/>.
 */

package ryey.easer.plugins.event.dayofweek;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import ryey.easer.R;
import ryey.easer.commons.plugindef.StorageData;
import ryey.easer.plugins.event.TypedContentLayout;

public class DayOfWeekContentLayout extends TypedContentLayout {
    CompoundButton []day_buttons = new CompoundButton[7];

    public DayOfWeekContentLayout(Context context) {
        super(context);
        setAvailableTypes(new DayOfWeekEventData().availableTypes());
        setType(new DayOfWeekEventData().type());
        setDesc(context.getString(R.string.event_day_of_week));
        inflate(context, R.layout.plugin_event__day_of_week, this);
        ViewGroup vg = (ViewGroup) findViewById(R.id.plugin__day_of_week_container);
        SimpleDateFormat sdf = new SimpleDateFormat("E", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        for (int i = 0; i < 7; i++) {
            ToggleButton toggleButton = (ToggleButton) vg.getChildAt(i);
            day_buttons[i] = toggleButton;
            cal.set(Calendar.DAY_OF_WEEK, i + 1);
            String text = sdf.format(cal.getTime());
            toggleButton.setText(text);
            toggleButton.setTextOn(text);
            toggleButton.setTextOff(text);
        }
    }

    @Override
    public void fill(StorageData data) {
        if (data instanceof DayOfWeekEventData) {
            super.fill(data);
            Set<Integer> days = (Set<Integer>) data.get();
            for (int day : days) {
                day_buttons[day].setChecked(true);
            }
        } else {
            throw new RuntimeException("illegal data");
        }
    }

    @Override
    public StorageData getData() {
        Set<Integer> days = new HashSet<>();
        for (int i = 0; i < 7; i++) {
            if (day_buttons[i].isChecked())
                days.add(i);
        }
        return new DayOfWeekEventData(days, selectedType());
    }
}
