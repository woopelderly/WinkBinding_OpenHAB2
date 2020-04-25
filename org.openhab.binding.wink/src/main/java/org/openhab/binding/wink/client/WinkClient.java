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
 * This is a singleton instance of a wink client for communicating with the wink rest api.
 *
 * @author Shawn Crosby - Initial contribution
 *
 */
//@NonNullByDefault
public class WinkClient {
    private static IWinkClient instance;

    /**
     * Get a singleton instance of the wink client
     *
     * @return
     */
    public static synchronized IWinkClient getInstance() {
        if (instance == null) {
            instance = new CloudRestfulWinkClient();
        }

        return instance;
    }

    /**
     * Allows for setting an instance of a new client. Mostly for unit tests
     *
     * @param testClient
     */
    public static synchronized void setInstance(IWinkClient testClient) {
        instance = testClient;
    }
}
