package websocket;

import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.util.List;

public class SessionManager {
    // Maps GameID -> List of Connections
    public final ConcurrentHashMap<Integer, List<Connection>> connections = new ConcurrentHashMap<>();

    public void add(int gameID, String authToken, Session session) {
        var connection = new Connection(authToken, session);
        connections.computeIfAbsent(gameID, k -> new ArrayList<>()).add(connection);
    }

    public void remove(int gameID, String authToken) {
        if (connections.containsKey(gameID)) {
            connections.get(gameID).removeIf(c -> c.authToken.equals(authToken));
        }
    }

    public void broadcast(int gameID, String excludeAuthToken, ServerMessage serverMessage) throws IOException {
        var occupants = connections.get(gameID);
        if (occupants != null) {
            var cleanUpList = new ArrayList<Connection>();
            for (var c : occupants) {
                if (c.session.isOpen()) {
                    if (!c.authToken.equals(excludeAuthToken)) {
                        c.session.getRemote().sendString(new Gson().toJson(serverMessage));
                    }
                } else {
                    cleanUpList.add(c);
                }
            }
            occupants.removeAll(cleanUpList);
        }
    }

    // New helper: Broadcast to EVERYONE (including the root client)
    public void broadcastToAll(int gameID, ServerMessage serverMessage) throws IOException {
        broadcast(gameID, null, serverMessage);
    }
}

class Connection {
    public String authToken;
    public Session session;

    public Connection(String authToken, Session session) {
        this.authToken = authToken;
        this.session = session;
    }
}