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

import java.util.List;
import java.util.Map;

/**
 * Provides an interface to the wink api. Currently cloud based, but future implementations may
 * be local.
 *
 * @author scrosby - Initial contribution
 *
 */
//@NonNullByDefault
public interface IWinkClient {
    /**
     * Get a list of all devices connected to the wink hub
     *
     * @return List<IWinkDevice> unordered list of devices connected to this hub
     */
    public List<IWinkDevice> listDevices();

    /**
     * Retrieves a specific device identified by the device uid
     *
     * @param type Supported Wink Device
     * @param Id UID of the device to retrieve
     * @return IWinkDevice object representing the device specified
     */
    public IWinkDevice getDevice(WinkSupportedDevice type, String Id);

    /**
     * Updates the state of a specified device.
     *
     * @param device Current device object
     * @param updatedState A Map of states as strings to be updated and their new values
     * @return IWinkDevice the updated result of the change.
     */
    public IWinkDevice updateDeviceState(IWinkDevice device, Map<String, String> updatedState);
}
