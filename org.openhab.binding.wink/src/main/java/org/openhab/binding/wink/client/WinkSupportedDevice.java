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

/**
 * WinkBaseThingHandler uses this enum to describe devices that it handles.
 *
 * @author Shawn Crosby - Initial contribution
 *
 */
public enum WinkSupportedDevice {
    DIMMABLE_LIGHT("light_bulb", "/light_bulbs"),
    BINARY_SWITCH("binary_switch", "/binary_switches"),
    REMOTE("remote", "/remotes"),
    LOCK("lock", "/locks"),
    HUB("hub", "/hubs"),
    DOORBELL("door_bell", "/door_bells"),
    THERMOSTAT("thermostat", "/thermostats");

    private String device_type;
    private String path;

    WinkSupportedDevice(String device_type, String path) {
        this.device_type = device_type;
        this.path = path;
    }

    /**
     * Retrieves the path used in the wink api to access devices of this type
     *
     * @return
     */
    public String getPath() {
        return this.path;
    }

    /**
     * Retrieves the object_type defined by the wink api
     *
     * @return
     */
    public String getDeviceType() {
        return this.device_type;
    }

    /**
     * Looks up the appropriate enum value based on the object_type defined by the wink api
     *
     * @param device_type
     * @return
     */
    public static WinkSupportedDevice lookup(String device_type) {
        for (WinkSupportedDevice dType : values()) {
            if (dType.getDeviceType().equals(device_type)) {
                return dType;
            }
        }
        return null;
    }
}
