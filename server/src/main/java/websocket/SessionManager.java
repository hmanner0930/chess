package websocket;

import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.util.List;

public class SessionManager {
    public static class Connection {
        public String authToken;
        public Session session;
        public Connection(String authToken, Session session) {
            this.authToken = authToken;
            this.session = session;
        }
    }

    public final ConcurrentHashMap<Integer, List<Connection>> connections = new ConcurrentHashMap<>();

    public void add(int gameID, String authToken, Session session) {
        var connection = new Connection(authToken, session);
        connections.computeIfAbsent(gameID, number -> new ArrayList<>()).add(connection);
    }

    public void remove(int gameID, String authToken) {
        if (connections.containsKey(gameID)) {
            connections.get(gameID).removeIf(number -> number.authToken.equals(authToken));
        }
    }

    public void toOne(int gameID, String excludeAuthToken, ServerMessage serverMessage) throws IOException {
        var occupants = connections.get(gameID);
        if (occupants != null) {
            var cleanUpList = new ArrayList<Connection>();
            for (var something : occupants) {
                if (something.session.isOpen()) {
                    if (!something.authToken.equals(excludeAuthToken)) {
                        something.session.getRemote().sendString(new Gson().toJson(serverMessage));
                    }
                } else {
                    cleanUpList.add(something);
                }
            }
            occupants.removeAll(cleanUpList);
        }
    }

    public void toAll(int gameID, ServerMessage serverMessage) throws IOException {
        toOne(gameID, null, serverMessage);
    }
}