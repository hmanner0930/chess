package websocket;

import com.google.gson.Gson;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class WebSocketFacade extends Endpoint {

    private final Session session;
    private final ServerMessageObserver observer;

    public WebSocketFacade(String url, ServerMessageObserver observer) throws Exception {
        try {
            url = url.replace("http", "ws");
            // In WebSocketFacade.java
            url = url.replace("http", "ws");
            if (!url.endsWith("/ws")) {
                url = url + "/ws";
            }
            URI socketURI = new URI(url);
            this.observer = observer;

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURI);

            // Set up a listener for incoming messages
            this.session.addMessageHandler((MessageHandler.Whole<String>) message -> {
                System.out.println("\n[DEBUG] Raw Message from Server: " + message); // NEW PRINT
                try {
                    ServerMessage serverMessage = deserialize(message);
                    this.observer.notify(serverMessage);
                } catch (Exception e) {
                    System.err.println("[DEBUG] Failed to process message: " + e.getMessage());
                }
            });
        } catch (DeploymentException | URISyntaxException | IOException e) {
            throw new Exception("Failed to connect to server: " + e.getMessage());
        }
    }

    // This must stay empty but is required by the Endpoint class
    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {}

    public void sendCommand(UserGameCommand command) throws IOException {
        this.session.getBasicRemote().sendText(new Gson().toJson(command));
    }

    private ServerMessage deserialize(String json) {
        Gson gson = new Gson();
        ServerMessage generic = gson.fromJson(json, ServerMessage.class);

        if (generic == null || generic.getServerMessageType() == null) {
            System.err.println("[DEBUG] Received malformed JSON: " + json);
            return null;
        }

        return switch (generic.getServerMessageType()) {
            case LOAD_GAME -> gson.fromJson(json, LoadGameMessage.class);
            case ERROR -> gson.fromJson(json, ErrorMessage.class);
            case NOTIFICATION -> gson.fromJson(json, NotificationMessage.class);
        };
    }
}