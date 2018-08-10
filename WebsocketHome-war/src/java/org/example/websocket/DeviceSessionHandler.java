package org.example.websocket;

// Each client connected to the application has its own session
import javax.enterprise.context.ApplicationScoped;
import java.util.HashSet;
import java.util.Set;
import javax.websocket.Session;
import org.example.model.Device;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.JsonObject;
import javax.json.spi.JsonProvider;

import java.util.List;
import java.io.IOException;
import java.util.ArrayList;

@ApplicationScoped
public class DeviceSessionHandler {
    private int deviceId = 0;
    private final Set<Session> sessions = new HashSet<>();
    private final Set<Device> devices = new HashSet<>();
    /* Define the following methods for adding and removing sessions to the server */
  
    // Add a new session
    // Add the for loop to the addSession method to send the list of devices to the connected client.
    public void addSession(Session session) {
        sessions.add(session);
        for (Device device : devices) {
            JsonObject addMessage = createAddMessage(device);
            sendToSession(session, addMessage);
        }
    }
    // Remove and existing session
    public void removeSession(Session session) {
        sessions.remove(session);
    }
    // Send an event message to all connected clients.
    private void sendToAllConnectedSessions(JsonObject message) {
        for (Session session : sessions) {
            sendToSession(session, message);
        }
    }
    // Send an event message to a client.
    private void sendToSession(Session session, JsonObject message){
         try {
            session.getBasicRemote().sendText(message.toString());
        } catch (IOException ex) {
            sessions.remove(session);
            Logger.getLogger(DeviceSessionHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /* Define the methods that operate on the Device object. */
    
    // Retrieve the list of devices and their attributes.
    public List<Device> getDevices() {
        return new ArrayList<>(devices);
    }
    // Add a device to the application.
    public void addDevice(Device device) {
        device.setId(deviceId);
        devices.add(device);
        deviceId++;
        // Send a message, in JSON, to all sessions or active clients in the WebSocket server.
        JsonObject addMessage = createAddMessage(device);
        sendToAllConnectedSessions(addMessage);
    }
    // Remove a device from the application
    public void removeDevice(int id) {
         Device device = getDeviceById(id);
        if (device != null) {
            devices.remove(device);
            JsonProvider provider = JsonProvider.provider();
            JsonObject removeMessage = provider.createObjectBuilder()
                    .add("action", "remove")
                    .add("id", id)
                    .build();
            sendToAllConnectedSessions(removeMessage);
        }
    }
    // Toggle the device status
    public void toggleDevice(int id) {
        JsonProvider provider = JsonProvider.provider();
        Device device = getDeviceById(id);
        if (device != null) {
            if ("On".equals(device.getStatus())) {
                device.setStatus("Off");
            } else {
                device.setStatus("On");
            }
            JsonObject updateDevMessage = provider.createObjectBuilder()
                    .add("action", "toggle")
                    .add("id", device.getId())
                    .add("status", device.getStatus())
                    .build();
            sendToAllConnectedSessions(updateDevMessage);
        }
    }
    // Retrieve a device with a specific identifier.
    private Device getDeviceById(int id) {
        for (Device device : devices) {
            if (device.getId() == id) {
                return device;
            }
        }
        return null;
    }
    // Build a JSON message for adding a device to the application
    private JsonObject createAddMessage(Device device) {
         JsonProvider provider = JsonProvider.provider();
        JsonObject addMessage = provider.createObjectBuilder()
                .add("action", "add")
                .add("id", device.getId())
                .add("name", device.getName())
                .add("type", device.getType())
                .add("status", device.getStatus())
                .add("description", device.getDescription())
                .build();
        return addMessage;
    }
}

// This class handles the sessions that are connected to the websocket server.