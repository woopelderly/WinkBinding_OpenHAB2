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
package org.openhab.binding.wink.handler;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.wink.client.IWinkDevice;
import org.openhab.binding.wink.client.WinkSupportedDevice;

/**
 * Its a remote
 *
 * @author Sebastian Marchand - Initial contribution
 *
 */
//@NonNullByDefault
public class RemoteHandler extends WinkBaseThingHandler {
    public RemoteHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleWinkCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    protected WinkSupportedDevice getDeviceType() {
        return WinkSupportedDevice.REMOTE;
    }

    @Override
    protected void updateDeviceState(IWinkDevice device) {
        // noop
    }
}
