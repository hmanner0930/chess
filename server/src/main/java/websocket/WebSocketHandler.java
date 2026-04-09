package websocket;

import com.google.gson.Gson;
import jakarta.websocket.OnMessage;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;

@WebSocket
public class WebSocketHandler {

    private final SessionManager sessions = new SessionManager();

    @OnMessage
    public void onMessage(Session session, String message) throws Exception {
        UserGameCommand command = new Gson().fromJson(message, UserGameCommand.class);
        switch (command.getCommandType()) {
            case CONNECT -> connect(session, command);
            case MAKE_MOVE -> makeMove(session, command);
            case LEAVE -> leave(session, command);
            case RESIGN -> resign(session, command);
        }
    }

    private void connect(Session session, UserGameCommand command) throws Exception {
        // We will fill this in next once it compiles
        System.out.println("Connect received");
    }

    private void makeMove(Session session, UserGameCommand command) throws Exception {
        System.out.println("Make Move received");
    }

    private void leave(Session session, UserGameCommand command) throws Exception {
        System.out.println("Leave received");
    }

    private void resign(Session session, UserGameCommand command) throws Exception {
        System.out.println("Resign received");
    }
}