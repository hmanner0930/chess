package websocket;

import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.*;

public class SessionManager {
    private static class Connection {
        String auth;
        Session session;

        Connection(String auth, Session session) {
            this.auth = auth;
            this.session = session;
        }
    }
    private final Map<Integer, List<Connection>> gameSessions = new HashMap<>();

    public void add(int gameID, String auth, Session session) {
        Connection conn = new Connection(auth, session);
        if (!gameSessions.containsKey(gameID)) {
            gameSessions.put(gameID, new ArrayList<>());
        }
        gameSessions.get(gameID).add(conn);
    }

    public void remove(int gameID, String auth) {
        List<Connection> list = gameSessions.get(gameID);
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).auth.equals(auth)) {
                    list.remove(i);
                    break;
                }
            }
        }
    }

    public void toOne(int gameID, String skipToken, ServerMessage msg) throws IOException {
        List<Connection> conns = gameSessions.get(gameID);
        if (conns == null) return;

        List<Connection> closed = new ArrayList<>();
        String json = new Gson().toJson(msg);

        for (Connection connection : conns) {
            if (connection.session.isOpen()) {
                if (skipToken == null || !connection.auth.equals(skipToken)) {
                    connection.session.getRemote().sendString(json);
                }
            } else {
                closed.add(connection);
            }
        }
        conns.removeAll(closed);
    }

    public void toAll(int gameID, ServerMessage msg) throws IOException {
        toOne(gameID, null, msg);
    }
}