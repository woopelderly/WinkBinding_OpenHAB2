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
 * This is a singleton instance of the authentication service to get tokens from the wink rest api
 *
 * @author Shawn Crosby - Initial contribution
 *
 */
//@NonNullByDefault
public class WinkAuthenticationService {
    private static IWinkAuthenticationService instance;

    /**
     * Returns a singleton instance of the current authentication service
     *
     * @return
     */
    public static synchronized IWinkAuthenticationService getInstance() {
        if (instance == null) {
            // The actual service used will call setInstance once it is configured.
            instance = new DummyService();
        }
        return instance;
    }

    /**
     * Creates a new singleton authentication service
     *
     * @param service
     */
    public static synchronized void setInstance(IWinkAuthenticationService service) {
        instance = service;
    }

    private static class DummyService implements IWinkAuthenticationService {

        @Override
        public String getAuthToken() {
            return null;
        }

        @Override
        public String refreshToken() throws AuthenticationException {
            return null;
        }

        @Override
        public Integer getPollingInterval() {
            return 300;
        }

    }
}
