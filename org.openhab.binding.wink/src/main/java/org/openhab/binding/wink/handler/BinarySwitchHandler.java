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

import static org.openhab.binding.wink.WinkBindingConstants.CHANNEL_SWITCHSTATE;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.wink.client.IWinkDevice;
import org.openhab.binding.wink.client.WinkSupportedDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BinarySwitchHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Scott Hanson - Initial contribution
 */
//@NonNullByDefault
public class BinarySwitchHandler extends WinkBaseThingHandler {
    public BinarySwitchHandler(Thing thing) {
        super(thing);
    }

    private final Logger logger = LoggerFactory.getLogger(BinarySwitchHandler.class);

    @Override
    public void handleWinkCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(CHANNEL_SWITCHSTATE)) {
            if (command.equals(OnOffType.ON)) {
                setSwitchState(true);
            } else if (command.equals(OnOffType.OFF)) {
                setSwitchState(false);
            } else if (command instanceof RefreshType) {
                logger.debug("Refreshing state");
                updateDeviceState(getDevice());
            }
        }
    }

    private void setSwitchState(boolean state) {
        if (state) {
            this.bridgeHandler.switchOnDevice(getDevice());
        } else {
            this.bridgeHandler.switchOffDevice(getDevice());
        }
    }

    @Override
    protected WinkSupportedDevice getDeviceType() {
        return WinkSupportedDevice.BINARY_SWITCH;
    }

    @Override
    protected void updateDeviceState(IWinkDevice device) {
        boolean switchedState = Boolean.valueOf(device.getCurrentState().get("powered"));
        updateState(CHANNEL_SWITCHSTATE, (switchedState ? OnOffType.ON : OnOffType.OFF));
    }

}
