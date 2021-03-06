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

package ryey.easer.plugins.event.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import ryey.easer.commons.plugindef.eventplugin.AbstractSlot;
import ryey.easer.commons.plugindef.eventplugin.EventData;
import ryey.easer.commons.plugindef.eventplugin.EventType;

public class WifiConnSlot extends AbstractSlot {
    String target_ssid = null;
    EventType type = null;

    String ssid = null;

    BroadcastReceiver connReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                    WifiInfo wifiInfo = intent.getParcelableExtra(WifiManager.EXTRA_WIFI_INFO);
                    if (type == EventType.is)
                        changeSatisfiedState(compare(wifiInfo));
                    else
                        changeSatisfiedState(false);
                } else
                    changeSatisfiedState(false);
            }
        }
    };

    IntentFilter filter;

    {
        filter = new IntentFilter();
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
    }

    public WifiConnSlot(Context context) {
        super(context);
    }

    @Override
    public void set(EventData data) {
        if (data instanceof WifiEventData) {
            setWifiConn((String) data.get());
            type = data.type();
        } else {
            throw new RuntimeException("illegal data");
        }
    }

    public void setWifiConn(String name) {
        if (name == null || name.isEmpty())
            return;
        target_ssid = name;
    }

    @Override
    public boolean isValid() {
        if (target_ssid == null || target_ssid.isEmpty()) {
            return false;
        }
        return true;
    }

    @Override
    public void listen() {
        context.registerReceiver(connReceiver, filter);
    }

    @Override
    public void cancel() {
        context.unregisterReceiver(connReceiver);
    }

    @Override
    public void check() {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (type == EventType.is) {
            changeSatisfiedState(compare(wifiInfo));
        } else if (type == EventType.is_not) {
            changeSatisfiedState(!compare(wifiInfo));
        }
    }

    private boolean compare(WifiInfo wifiInfo) {
        ssid = wifiInfo.getSSID();
        if (ssid.startsWith("\"")) {
            ssid = ssid.substring(1, ssid.length() - 1);
        }
        return ssid.equals(target_ssid);
    }
}
