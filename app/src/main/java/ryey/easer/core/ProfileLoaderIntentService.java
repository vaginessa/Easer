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

package ryey.easer.core;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import java.io.IOException;
import java.util.Calendar;

import ryey.easer.commons.plugindef.operationplugin.OperationData;
import ryey.easer.commons.plugindef.operationplugin.OperationLoader;
import ryey.easer.commons.plugindef.operationplugin.OperationPlugin;
import ryey.easer.core.data.ProfileStructure;
import ryey.easer.core.data.storage.ProfileDataStorage;
import ryey.easer.core.data.storage.xml.profile.XmlProfileDataStorage;
import ryey.easer.plugins.PluginRegistry;

public class ProfileLoaderIntentService extends IntentService {
    public static final String ACTION_LOAD_PROFILE = "ryey.easer.action.LOAD_PROFILE";
    public static final String ACTION_PROFILE_LOADED = "ryey.easer.action.PROFILE_LOADED";

    public static final String EXTRA_PROFILE_NAME = "ryey.easer.extra.PROFILE_NAME";
    public static final String EXTRA_EVENT_NAME = "ryey.easer.extra.EVENT_NAME";
    public static final String EXTRA_LOAD_TIME = "ryey.easer.extra.LOAD_TIME";

    public ProfileLoaderIntentService() {
        super("ProfileLoaderIntentService");
    }

    @Override
    public void onCreate() {
        Log.d(getClass().getSimpleName(), "onCreate");
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(getClass().getSimpleName(), "onHandleIntent");
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_LOAD_PROFILE.equals(action)) {
                final String name = intent.getStringExtra(EXTRA_PROFILE_NAME);
                final String event = intent.getStringExtra(EXTRA_EVENT_NAME);
                handleActionLoadProfile(name, event);
            }
        }
    }

    private void handleActionLoadProfile(String name, String event) {
        Log.i(getClass().getSimpleName(), String.format("onHandleActionLoadProfile: %s by %s", name, event));
        try {
            ProfileDataStorage storage = XmlProfileDataStorage.getInstance(this);
            ProfileStructure profile = storage.get(name);

            for (OperationPlugin plugin : PluginRegistry.getInstance().getOperationPlugins()) {
                OperationLoader loader = plugin.loader(getApplicationContext());
                OperationData data = profile.get(plugin.name());
                if (data != null) {
                    if (loader.load(data)) {
                        Intent intent = new Intent(ACTION_PROFILE_LOADED);
                        intent.putExtra(EXTRA_EVENT_NAME, event);
                        intent.putExtra(EXTRA_PROFILE_NAME, name);
                        Calendar calendar = Calendar.getInstance();
                        intent.putExtra(EXTRA_LOAD_TIME, calendar.getTimeInMillis());
                        sendBroadcast(intent);
                    }
                }
            }

            Log.i(getClass().getSimpleName(), "loaded profile: " + name);
        } catch (IOException e) {
            e.printStackTrace();
            Log.i(getClass().getSimpleName(), "failed to load profile: " + name);
        }
    }
}
