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

import static org.openhab.binding.wink.WinkBindingConstants.WINK_URI;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * This implementation communicates with the wink rest api.
 *
 * @author Shawn Crosby - Initial contribution
 *
 */
//@NonNullByDefault
public class CloudRestfulWinkClient implements IWinkClient {

    private final Logger log = LoggerFactory.getLogger(CloudRestfulWinkClient.class);

    @Override
    public List<IWinkDevice> listDevices() {
        log.debug("Getting all devices for user");
        List<IWinkDevice> ret = new ArrayList<IWinkDevice>();

        Client winkClient = ClientBuilder.newClient();
        WebTarget target = winkClient.target(WINK_URI).path("/users/me/wink_devices");

        JsonArray resultJson = executeGet(target).getAsJsonArray();
        Iterator<JsonElement> iterator = resultJson.getAsJsonArray().iterator();

        while (iterator.hasNext()) {
            JsonElement element = iterator.next();
            if (!element.isJsonObject()) {
                continue;
            }
            ret.add(new JsonWinkDevice(element.getAsJsonObject()));
        }
        winkClient.close();

        return ret;
    }

    @Override
    public IWinkDevice getDevice(WinkSupportedDevice type, String Id) {
        log.debug("Getting Device: {}", Id);
        IWinkDevice ret = null;
        for(int i = 0; i < 10; i++) {
            try {
                Client winkClient = ClientBuilder.newClient();

                WebTarget target = winkClient.target(WINK_URI).path(type.getPath() + "/" + Id);
                JsonElement resultJson = executeGet(target);

                ret = new JsonWinkDevice(resultJson.getAsJsonObject());

                winkClient.close();
                break;
            } catch (Exception e) {
                // Delay and try again.
                log.error("IWinkDevice getDevice threw Exception: {}, retrying...", e.getMessage());
                try{
                    TimeUnit.MILLISECONDS.sleep(1000);
                } 
                catch(InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        log.debug("Got Device, Name: {}", ret.getName());
        return ret;
    }

    @Override
    public IWinkDevice updateDeviceState(IWinkDevice device, Map<String, String> updatedState) {
        Client winkClient = ClientBuilder.newClient();
        WebTarget target = winkClient.target(WINK_URI)
                .path(device.getDeviceType().getPath() + "/" + device.getId() + "/desired_state");
        String desiredState = new Gson().toJson(updatedState);
        String wrapper = "{\"desired_state\":" + desiredState + "}";
        JsonElement jsonResult = executePut(target, wrapper);

        IWinkDevice ret = new JsonWinkDevice(jsonResult.getAsJsonObject());
        winkClient.close();

        return ret;
    }

    private JsonElement executePut(WebTarget target, String payload) {
        String token = WinkAuthenticationService.getInstance().getAuthToken();

        Response response = doPut(target, payload, token);

        if (response.getStatus() != 200) {
            log.debug("Got status {}, retrying with new token", response.getStatus());
            token = WinkAuthenticationService.getInstance().refreshToken();
            response = doPut(target, payload, token);
        }

        return getResultAsJson(response);
    }

    private JsonElement executeGet(WebTarget target) {
        String token = WinkAuthenticationService.getInstance().getAuthToken();
        Response response = doGet(target, token);

        if(response.getStatus() != 200){
            log.debug("Got status {}, retrying with new token", response.getStatus());
            log.debug("Initial token: {}", token);
            token = WinkAuthenticationService.getInstance().refreshToken();
            log.debug("New token: {}", token);
            response = doGet(target, token);
            log.debug("Status after retry with refreshed token: {}", response.getStatus());
        }

        return getResultAsJson(response);
    }

    private Response doGet(WebTarget target, String token) {
        log.debug("Doing Get: {}", target);
        Response response = null;
        for(int i = 0; i < 10; i++) {
            try {
                response = target.request(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "Bearer " + token)
                .get();
                break;
            } catch (Exception e) {
                // Delay and try again.
                log.error("doGet threw Exception: {}, retrying...", e.getMessage());
                try{
                    TimeUnit.MILLISECONDS.sleep(500);
                } 
                catch(InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        } 
        return response;
    }

    private Response doPut(WebTarget target, String payload, String token) {
        log.debug("Doing Put: {}, Payload: {}", target, payload);
        Response response = target.request(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "Bearer " + token)
                .put(Entity.json(payload));
        return response;
    }

    private JsonElement getResultAsJson(Response response) {
        String result = response.readEntity(String.class);

        log.debug("CloudRestfulWinkClient::getResultAsJson() response passed in = {}", result);

        JsonParser parser = new JsonParser();
        JsonObject resultJson = parser.parse(result).getAsJsonObject();

        JsonElement ret = resultJson.get("data");
        log.info("Json Result: {}", ret);

        return ret;
    }

}
