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
            // Clean the URL: remove http/https and ensure it starts with ws
            String cleanUrl = url.replace("http://", "").replace("https://", "");
            URI socketURI = new URI("ws://" + cleanUrl + "/ws");

            this.observer = observer;
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURI);

            this.session.addMessageHandler((MessageHandler.Whole<String>) message -> {
                ServerMessage serverMessage = deserialize(message);
                observer.notify(serverMessage);
            });
        } catch (Exception e) {
            throw new Exception("WebSocket Connection Error: " + e.getMessage());
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