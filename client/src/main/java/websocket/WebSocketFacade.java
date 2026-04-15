package websocket;

import com.google.gson.Gson;
import websocket.commands.UserGameCommand;
import websocket.messages.*;

import jakarta.websocket.*;
import java.io.IOException;
import java.net.URI;

public class WebSocketFacade extends Endpoint {

    private final Session session;

    public WebSocketFacade(String url, ServerMessageObserver observer) throws Exception {
        try {
            //I looked this cleanUrl up

            String wsURL = url.replace("http://", "").replace("https://", "");
            URI uri = new URI("ws://" + wsURL + "/ws");

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, uri);
            this.session.addMessageHandler(new MessageHandler.Whole<String>() {
                @Override
                public void onMessage(String message) {
                    try {

                        ServerMessage serverMessage = readMessage(message);
                        if (serverMessage != null) {
                            observer.notify(serverMessage);
                        }
                    } catch (Exception exception) {
                        System.err.printf(exception.getMessage());
                    }
                }
            });
        } catch (Exception exception) {
            throw new Exception("WebSocket Error: " + exception.getMessage());
        }
    }

    @Override
    public void onOpen(Session session, EndpointConfig config) {}

    public void sendCommand(UserGameCommand command) throws IOException {
        this.session.getBasicRemote().sendText(new Gson().toJson(command));
    }

    private ServerMessage readMessage(String json) {
        Gson gson = new Gson();
        ServerMessage something = gson.fromJson(json, ServerMessage.class);

        if (something == null || something.getServerMessageType() == null) {
            return null;
        }
        return switch (something.getServerMessageType()) {
            case LOAD_GAME -> gson.fromJson(json, LoadGameMessage.class);
            case ERROR -> gson.fromJson(json, ErrorMessage.class);
            case NOTIFICATION -> gson.fromJson(json, NotificationMessage.class);
        };
    }
}