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
 * Exception that is thrown when unable to authenticate to wink api
 *
 * @author Shawn Crosby - Initial contribution
 *
 */
//@NonNullByDefault
public class AuthenticationException extends RuntimeException {

    public AuthenticationException(String string) {
        super(string);
    }

    /**
     * serialization version ID
     */
    private static final long serialVersionUID = 1L;

}
