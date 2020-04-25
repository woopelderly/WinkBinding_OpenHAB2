/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.wink.client;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

/**
 * This class parses json from the wink api and from pubnub and produces an IWinkDevice
 *
 * @author Shawn Crosby - Initial contribution
 *
 */
//@NonNullByDefault
public class JsonWinkDevice implements IWinkDevice {
    private JsonObject json;

    public JsonWinkDevice(JsonObject element) {
        this.json = element;
    }

    @Override
    public String getId() {
        if(null == json.get("uuid")){
            return null;
        }
        return json.get("uuid").getAsString();
    }

    @Override
    public String getName() {
        if(null == json.get("name")){
            return null;
        }
        return json.get("name").getAsString();
    }

    @Override
    public WinkSupportedDevice getDeviceType() {
        if (json.get("lock_id") != null) {
            return WinkSupportedDevice.LOCK;
        } else if (json.get("light_bulb_id") != null) {
            return WinkSupportedDevice.DIMMABLE_LIGHT;
        } else if (json.get("binary_switch_id") != null) {
            return WinkSupportedDevice.BINARY_SWITCH;
        } else if (json.get("remote_id") != null) {
            return WinkSupportedDevice.REMOTE;
        } else if (json.get("door_bell_id") != null) {
            return WinkSupportedDevice.DOORBELL;
        } else if (json.get("thermostat_id") != null) {
            return WinkSupportedDevice.THERMOSTAT;
        } else {
            return WinkSupportedDevice.HUB;
        }
    }

    @Override
    public String getPubNubSubscriberKey() {
        return json.get("subscription").getAsJsonObject().get("pubnub").getAsJsonObject().get("subscribe_key")
                .getAsString();
    }

    @Override
    public String getPubNubChannel() {
        return json.get("subscription").getAsJsonObject().get("pubnub").getAsJsonObject().get("channel").getAsString();
    }

    @Override
    public String getPubNubOrigin() {
        return json.get("subscription").getAsJsonObject().get("pubnub").getAsJsonObject().get("origin").getAsString();
    }

    @Override
    public String getProperty(String property) {
        return json.get(property).getAsString();
    }

    @Override
    public Map<String, String> getCurrentState() {
        JsonObject data = json.get("last_reading").getAsJsonObject();
        return toMap(data);
    }

    @Override
    public Map<String, String> getDesiredState() {
        JsonObject data = json.get("desired_state").getAsJsonObject();
        return toMap(data);
    }

    private Map<String, String> toMap(JsonObject json) {
        Map<String, Object> theMap = new Gson().fromJson(json, new TypeToken<HashMap<String, Object>>() {
        }.getType());

        Map<String, String> mapString = new HashMap<String,String>();
        for(Map.Entry entry : theMap.entrySet()){
            mapString.put(entry.getKey().toString(), entry.getValue() == null ? null : entry.getValue().toString());
        }

        return mapString;
    }

    @Override
    public String toString() {
        StringBuffer ret = new StringBuffer();
        ret.append(this.getDeviceType() + " ");
        ret.append("Device: (" + this.getId() + ") ");
        ret.append(this.getName());

        return ret.toString();
    }

}
