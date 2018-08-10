package org.example.websocket;

import javax.enterprise.context.ApplicationScoped;
import javax.websocket.server.ServerEndpoint;
import javax.websocket.Session;
import javax.websocket.OnOpen;
import javax.websocket.OnMessage;
import javax.websocket.OnError;
import javax.websocket.OnClose; 
import javax.inject.Inject;

import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import org.example.model.Device;

@ApplicationScoped
@ServerEndpoint("/actions")
public class DeviceWebSocketServer {
    
    // Inject a DeviceSessionHandler object to process the WebSocket lifecycle events in each session
    @Inject
    private DeviceSessionHandler sessionHandler;
    
    //  Read the attributes sent from the client in JSON and creates a new Device object with the specified parameters.
    @OnOpen
    public void open(Session session) {
         sessionHandler.addSession(session);
    }
    @OnClose
    public void close(Session session) {
        sessionHandler.removeSession(session);
    }
    @OnError
    public void onError(Throwable error){
        Logger.getLogger(DeviceWebSocketServer.class.getName()).log(Level.SEVERE, null, error);
    }
    // Process the OnMessage WebSocket lifecycle event
    // 1.Reads device actions and attributes sent from the client.
    // 2.Invokes the session handler to perform the proper operations
    @OnMessage
    public void handleMessage(String message, Session session) {
         try (JsonReader reader = Json.createReader(new StringReader(message))) {
            JsonObject jsonMessage = reader.readObject();

            if ("add".equals(jsonMessage.getString("action"))) {
                Device device = new Device();
                device.setName(jsonMessage.getString("name"));
                device.setDescription(jsonMessage.getString("description"));
                device.setType(jsonMessage.getString("type"));
                device.setStatus("Off");
                sessionHandler.addDevice(device);
            }

            if ("remove".equals(jsonMessage.getString("action"))) {
                int id = (int) jsonMessage.getInt("id");
                sessionHandler.removeDevice(id);
            }

            if ("toggle".equals(jsonMessage.getString("action"))) {
                int id = (int) jsonMessage.getInt("id");
                sessionHandler.toggleDevice(id);
            }
        }
    }
    
}

// WebSocket server endpoint - gets triggered when clients (WebSocket Client endpoints) connect to it
// HTTP Session: it encapsulates the interaction between two endpoints (peers).