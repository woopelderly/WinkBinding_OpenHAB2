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

import java.util.Arrays;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.wink.client.AuthenticationException;
import org.openhab.binding.wink.client.IWinkDevice;
import org.openhab.binding.wink.client.JsonWinkDevice;
import org.openhab.binding.wink.client.WinkSupportedDevice;
import org.openhab.binding.wink.client.WinkAuthenticationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.enums.PNLogVerbosity;
import com.pubnub.api.enums.PNStatusCategory;
import com.pubnub.api.enums.PNOperationType;
import com.pubnub.api.enums.PNReconnectionPolicy;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;

/**
 * This is the base class for devices connected to the wink hub. Implements pubnub registration
 * and initialization for all wink devices.
 *
 * @author Shawn Crosby - Initial contribution
 *
 */
//@NonNullByDefault
public abstract class WinkBaseThingHandler extends BaseThingHandler {
    public WinkBaseThingHandler(Thing thing) {
        super(thing);
    }

    private final Logger logger = LoggerFactory.getLogger(WinkBaseThingHandler.class);

    protected WinkHub2BridgeHandler bridgeHandler;
    protected PubNub pubnub;
    private ScheduledFuture<?> pollingJob;
    Boolean pubnubReady;

    @Override
    public void initialize() {
        pubnubReady = false;
        logger.debug("Initializing Device {}", getThing());
        Bridge bridge = getBridge();
        if(null == bridge){
            logger.error("Wink initialize failed becuase bridge is null.  Restart OpenHAB to try again.");
            return;
        }
        bridgeHandler = (WinkHub2BridgeHandler) bridge.getHandler();
        
        // Wait for the WinkAuthenticationService to complete. Otherwise the code below will throw and not be attempted again.
        long start = System.nanoTime();
        logger.debug("start time: {}", start);
        long timeout = 2000000000; // 2 seconds
        while (null == WinkAuthenticationService.getInstance().getAuthToken() &&
                (System.nanoTime() - start) < timeout){
            logger.debug("Waiting for WinkAuthenticationService to get setup.");
            try{
                TimeUnit.MILLISECONDS.sleep(250);
            } 
            catch(InterruptedException ex) 
            {
                Thread.currentThread().interrupt();
            }
        }
    
        if (getThing().getConfiguration().get("uuid") == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "UUID must be specified in Config");
        } else {
            try {
                logger.debug("WinkBaseThingHandler::initialize() calling GetDevice on line 87");
                IWinkDevice device = getDevice();
                if (device.getCurrentState().get("connection").equals("true")) {
                    updateStatus(ThingStatus.ONLINE);
                    logger.debug("Thing is online, calling updateDeviceState()");
                    updateDeviceState(device);
                    logger.debug("updateDeviceState() returned.  Calling registerToPubNub");
                    //registerToPubNub();
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Device Not Connected");
                }
            } catch (AuthenticationException e) {
                logger.error("Auth Exception, Unable to initialize device {}: {}", getThing(), e.getMessage());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            } catch (RuntimeException e) {
                logger.error("RuntimeException, Unable to initialize device {}: {}", getThing(), e.getMessage());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        }

        logger.debug("BaseThingHandler Polling Interval: {}", WinkAuthenticationService.getInstance().getPollingInterval());
        pollingJob = this.scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                logger.debug("WinkBaseThingHandler::pollingJob() calling GetDevice");
                //if(pubnubReady){
                    IWinkDevice device = getDevice();
                    logger.debug("Polling device: {}", device.toString());
                    updateDeviceState(device);
                //}
            }
        }, 0, WinkAuthenticationService.getInstance().getPollingInterval(), TimeUnit.SECONDS);

        super.initialize();
    }

    @Override
    public void dispose() {
        logger.debug("Shutting down thing {}", getThing());
        if (pubnub != null) {
            this.pubnub.unsubscribeAll();
            this.pubnub.destroy();
        }

        this.pollingJob.cancel(true);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        try {
            handleWinkCommand(channelUID, command);
        } catch (RuntimeException e) {
            logger.error("Unable to process command: {}", e.getMessage());
        }
    }

    /**
     * Sub-implementation of ThingHandler handleCommand to deal with exception handling more cleanly
     *
     * @param channelUID
     * @param command
     */
    protected abstract void handleWinkCommand(ChannelUID channelUID, Command command);

    @Override
    public void channelLinked(ChannelUID channelUID) {
        logger.debug("WinkBaseThingHandler::channelLinked() calling GetDevice");
        //IWinkDevice device = getDevice();
        try {
            for (Channel channel : getThing().getChannels()) {
                if (channelUID.equals(channel.getUID())) {
                    //updateDeviceState(device);
                    //ogger.debug("Channel: {} Linked for device: {}", channelUID.getId(), device.toString());
                    logger.debug("Channel: {} Linked", channelUID.getId());
                    break;
                }
            }
        } catch (AuthenticationException e) {
            //logger.error("Unable to process channel link: {}, for device: {}", e.getMessage(), device.toString());
        }
    }

    /**
     * Subclasses must define the correct wink supported device type
     *
     * @return Enum from WinkSupportedDevice for this device
     */
    protected abstract WinkSupportedDevice getDeviceType();

    /**
     * Retrieves the device configuration and state from the API
     *
     * @return
     */
    protected IWinkDevice getDevice() {
        return bridgeHandler.getDevice(getDeviceType(), getThing().getConfiguration().get("uuid").toString());
    }

    /**
     * Subclasses must implement this method to perform the mapping between the properties and state
     * retrieved from the API and how that state is represented in OpenHab.
     *
     * @param device
     */
    protected abstract void updateDeviceState(IWinkDevice device);

    /**
     * Handles state change events from the api
     */
    protected void registerToPubNub() {
        logger.debug("Doing the PubNub registration for :\n{}", thing.getLabel());

        try {
            logger.debug("WinkBaseThingHandler::registerToPubNub() calling GetDevice");
            IWinkDevice deviceForPubNubSetup = getDevice();

            PNConfiguration pnConfiguration = new PNConfiguration();
            pnConfiguration.setSubscribeKey(deviceForPubNubSetup.getPubNubSubscriberKey());
            pnConfiguration.setLogVerbosity(PNLogVerbosity.BODY);
            pnConfiguration.setNonSubscribeRequestTimeout(30);
            pnConfiguration.setReconnectionPolicy(PNReconnectionPolicy.EXPONENTIAL);
            pnConfiguration.setOrigin(deviceForPubNubSetup.getPubNubOrigin());
            //pnConfiguration.setOrigin(MessageFormat.format("https://{1}",deviceForPubNubSetup.getPubNubOrigin()));
            logger.debug("PubNub configuring origin with: {}", deviceForPubNubSetup.getPubNubOrigin());
            logger.debug("PubNub configuring sub_key with: {}", deviceForPubNubSetup.getPubNubSubscriberKey());

            this.pubnub = new PubNub(pnConfiguration);
            this.pubnub.addListener(new SubscribeCallback() {
                @Override
                public void message(PubNub pubnub, PNMessageResult message) {
                    if(message.getChannel() != null){
                        logger.debug("message.getChannel not null. message.getChannel: {}", message.getChannel());
                        logger.debug("message.getChannel not null. message.getSubscription: {}", message.getSubscription());
                        logger.debug("message.getChannel not null. message.getMessage: {}", message.getMessage());
                        logger.debug("message.getChannel not null. message.getTimetoken: {}", message.getTimetoken());
                        logger.debug("message.getChannel not null. message.getUserMetadata: {}", message.getUserMetadata());
                    } else {
                        logger.debug("message.getChannel is null. message.getSubscription: {}", message.getSubscription());
                        logger.debug("message.getChannel is null. message.getMessage: {}", message.getMessage());
                        logger.debug("message.getChannel is null. message.getTimetoken: {}", message.getTimetoken());
                        logger.debug("message.getChannel is null. message.getUserMetadata: {}", message.getUserMetadata());
                    }
                    logger.debug("PubNub.message string to be parsed by json: {}", message.getMessage().getAsString());
                    // If 'pull_url' is in the message it means pubnub was unable to get the current state.
                    if(message.getMessage().getAsString().contains("pull_url")){
                        logger.debug("PubNub.message, PubNub unable to get current state, calling GetDevice, polling...");
                        updateDeviceState(getDevice());
                        return;
                    }
                    JsonParser parser = new JsonParser();
                    JsonObject jsonMessage = parser.parse(message.getMessage().getAsString()).getAsJsonObject();
                    IWinkDevice device = new JsonWinkDevice(jsonMessage);
                    logger.debug("PubNub.addListener: Received update from device: {}", device);
                    updateDeviceState(device);
                }

                @Override
                public void presence(PubNub pubnub, PNPresenceEventResult presence) {
                    // No PubNub presence support.
                    logger.debug("PubNub.presence received");
                }

                @Override
                public void status(PubNub arg0, PNStatus status) {
                    if (status.getOperation() == PNOperationType.PNSubscribeOperation ||
                        status.getOperation() == PNOperationType.PNUnsubscribeOperation){
                        if (status.getCategory() == PNStatusCategory.PNConnectedCategory) {
                            // subscription connection success.
                            logger.debug("PubNub connected, PNConnectedCategory");
                            pubnubReady = true;
                        } else if (status.getCategory() == PNStatusCategory.PNUnexpectedDisconnectCategory) {
                            // internet got lost, will attempt to reconnect automatically.
                            logger.debug("Unexpected Disconnect from PubNub, reconnecting automatically, PNUnexpectedDisconnectCategory");
                        } else if (status.getCategory() == PNStatusCategory.PNTimeoutCategory) {
                            // will automatically attempt to reconnect.
                            logger.debug("PubNub timeout, reconnecting automatically, PNTimeoutCategory");
                        } else if (status.getCategory() == PNStatusCategory.PNReconnectedCategory) {
                            // Something went wrong, but successfully reconnected.
                            logger.debug("PubNub reconnected, PNReconnectedCategory");
                        } else if (status.getCategory() == PNStatusCategory.PNDisconnectedCategory) {
                            // Sucessfully unsubscribed
                            logger.debug("PubNub unsubscribed without error, PNDisconnectedCategory");
                        } else if (status.getCategory() == PNStatusCategory.PNBadRequestCategory) {
                            // Request can't be completed because not all required values have been
                            // passed or passed values have unexpected data type. PubNub Java SDK 
                            // will send PNBadRequestCategory when some parameter is missing like 
                            // channel, subscribe key
                            logger.debug("PubNub request is incorrect, PNBadRequestCategory");
                        } else if (status.isError()) {
                            logger.error("PubNub Error {}", status.toString());
                        } else {
                            logger.debug("PubNub Status Category: {}", status.getCategory());
                        }
                        logger.debug("PubNub Status: {}", status.toString());
                        if( status.getErrorData() == null){
                            logger.debug("PubNub Status ErrorData is null");
                        } else {
                            logger.debug("PubNub Status ErrorData: {}", status.getErrorData().toString());
                            logger.debug("PubNub Status ErrorDataInfo: {}", status.getErrorData().getInformation());
                            if(status.getErrorData().getThrowable() == null){
                                logger.debug("PubNub Status ErrorDataThrowableMessage is null");
                            } else {
                                //logger.debug("PubNub Status ErrorDataThrowable: {}", status.getErrorData().getThrowable());
                                logger.debug("PubNub Status ErrorDataThrowableToString: {}", status.getErrorData().getThrowable().toString());
                                logger.debug("PubNub Status ErrorDataThrowableMessage: {}", status.getErrorData().getThrowable().getMessage());
                            }
                            
                        }
                    } else if (status.getOperation() == PNOperationType.PNHeartbeatOperation){
                        if (status.isError()) {
                            logger.debug("PubNub failed heartbeat.");
                        } else {
                            logger.debug("PubNub heartbeat success.");
                        }
                    } else {
                        logger.debug("PubNub status operation: {}", status.getOperation());
                    }
                }
            });

            String fullChannelInfo = deviceForPubNubSetup.getPubNubChannel();
            String channelInfoToUse = fullChannelInfo;//.split("\\|")[1];
            this.pubnub.subscribe()
                //.withPresence()
                .channels(Arrays.asList(channelInfoToUse))
                .execute();

            logger.debug("PubNub.subscribe called with channel info: {}", channelInfoToUse);
        } catch (AuthenticationException e) {
            logger.error("Unable to subscribe to pubnub: {}", e.getMessage());
        }
    }

}
