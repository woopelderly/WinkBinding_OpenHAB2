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

import static org.openhab.binding.wink.WinkBindingConstants.*;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.wink.client.IWinkDevice;
import org.openhab.binding.wink.client.WinkSupportedDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Shawn Crosby - Initial contribution
 *
 */
//@NonNullByDefault
public class DoorBellHandler extends WinkBaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(DoorBellHandler.class);

    public DoorBellHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected void handleWinkCommand(ChannelUID channelUID, Command command) {
        // no op
    }

    @Override
    protected WinkSupportedDevice getDeviceType() {
        return WinkSupportedDevice.DOORBELL;
    }

    @Override
    protected void updateDeviceState(IWinkDevice device) {
        logger.debug("Updating Doorbell device");
        if (device.getCurrentState().get("motion").equals("true")) {
            this.triggerChannel(CHANNEL_MOTION, "MOTION");
        }
        if (device.getCurrentState().get("button_pressed").equals("true")) {
            this.triggerChannel(CHANNEL_BUTTON, "BUTTON PRESS");
        }
    }
}
