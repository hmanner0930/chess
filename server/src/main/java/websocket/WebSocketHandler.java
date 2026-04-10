package websocket;

import com.google.gson.Gson;
import io.javalin.websocket.WsConfig;
import io.javalin.websocket.WsMessageContext;
import websocket.commands.UserGameCommand;
import websocket.commands.MakeMoveCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import dataaccess.GameDAO;
import dataaccess.AuthDAO;
import model.AuthData;
import model.GameData;
import chess.ChessGame;
import chess.ChessMove;
import chess.InvalidMoveException;

public class WebSocketHandler {

    private final SessionManager sessions = new SessionManager();
    private final GameDAO gameDAO;
    private final AuthDAO authDAO;

    public WebSocketHandler(GameDAO gameDAO, AuthDAO authDAO) {
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    public void configure(WsConfig ws) {
        ws.onConnect(ctx -> {
            // Essential to prevent the 30-second idle timeout
            ctx.enableAutomaticPings();
            System.out.println("[SERVER DEBUG] WebSocket connected: " + ctx.sessionId());
        });

        ws.onMessage(ctx -> {
            String message = ctx.message();
            UserGameCommand command = new Gson().fromJson(message, UserGameCommand.class);

            // Standard command routing
            switch (command.getCommandType()) {
                case CONNECT -> connect(ctx, command);
                case MAKE_MOVE -> makeMove(ctx);
                case LEAVE -> leave(ctx, command);
                case RESIGN -> resign(ctx, command);
            }
        });

        ws.onClose(ctx -> {
            System.out.println("[SERVER DEBUG] WebSocket closed: " + ctx.sessionId());
            // Optional: Clean up sessions if your SessionManager doesn't handle closure
        });

        ws.onError(ctx -> System.out.println("WebSocket Error: " + ctx.error()));
    }

    private void connect(WsMessageContext ctx, UserGameCommand command) throws Exception {
        try {
            // 1. MUST fetch these first before you can use them in debug prints!
            AuthData auth = authDAO.getAuth(command.getAuthToken());
            GameData gameData = gameDAO.getGame(command.getGameID());

            if (auth == null || gameData == null) {
                sendError(ctx, "Error: Invalid authToken or gameID");
                return;
            }

            // 2. NOW the debug prints will work because 'auth' and 'gameData' exist
            System.out.println("[SERVER DEBUG] Preparing to send board to user: " + auth.username());
            String json = new Gson().toJson(new LoadGameMessage(gameData.game()));
            System.out.println("[SERVER DEBUG] JSON being sent: " + json);

            // 3. Logic
            sessions.add(command.getGameID(), command.getAuthToken(), ctx.session);

            // Send LOAD_GAME to Root
            ctx.send(json);

            // Notification message logic
            String message;
            if (auth.username().equals(gameData.whiteUsername())) {
                message = String.format("%s joined as White", auth.username());
            } else if (auth.username().equals(gameData.blackUsername())) {
                message = String.format("%s joined as Black", auth.username());
            } else {
                message = String.format("%s joined as an observer", auth.username());
            }

            sessions.broadcast(command.getGameID(), command.getAuthToken(), new NotificationMessage(message));

        } catch (Exception e) {
            System.out.println("[SERVER DEBUG] Error in connect: " + e.getMessage());
            sendError(ctx, "Error: " + e.getMessage());
        }
    }

    private void makeMove(WsMessageContext ctx) throws Exception {
        try {
            MakeMoveCommand moveCommand = new Gson().fromJson(ctx.message(), MakeMoveCommand.class);
            AuthData auth = authDAO.getAuth(moveCommand.getAuthToken());
            GameData gameData = gameDAO.getGame(moveCommand.getGameID());
            ChessGame game = gameData.game();
            ChessMove move = moveCommand.getMove();

            // 1. Validations
            if (game.isGameOver()) {
                sendError(ctx, "Error: Game is over");
                return;
            }

            // Verify it's the right player's turn
            boolean isWhite = auth.username().equals(gameData.whiteUsername());
            boolean isBlack = auth.username().equals(gameData.blackUsername());
            if ((game.getTeamTurn() == ChessGame.TeamColor.WHITE && !isWhite) ||
                    (game.getTeamTurn() == ChessGame.TeamColor.BLACK && !isBlack)) {
                sendError(ctx, "Error: Not your turn");
                return;
            }

            // 2. Try the move
            game.makeMove(move);
            gameDAO.updateGame(gameData.gameID(), game);

            // 3. Broadcast updates
            sessions.broadcastToAll(gameData.gameID(), new LoadGameMessage(game));
            String msg = String.format("%s moved %s", auth.username(), move);
            sessions.broadcast(gameData.gameID(), auth.authToken(), new NotificationMessage(msg));

            // 4. Status Checks (Check/Checkmate)
            checkStatus(gameData, game);

        } catch (InvalidMoveException e) {
            sendError(ctx, "Error: Invalid move");
        } catch (Exception e) {
            sendError(ctx, "Error: " + e.getMessage());
        }
    }

    private void leave(WsMessageContext ctx, UserGameCommand command) throws Exception {
        AuthData auth = authDAO.getAuth(command.getAuthToken());
        GameData gameData = gameDAO.getGame(command.getGameID());

        String whiteUser = gameData.whiteUsername();
        String blackUser = gameData.blackUsername();

        // Check which slot the leaving player occupies and set it to null
        if (auth.username().equals(whiteUser)) {
            whiteUser = null;
        } else if (auth.username().equals(blackUser)) {
            blackUser = null;
        }

        // Update the DAO with the new player list (one of which is now null)
        gameDAO.updateGame(command.getGameID(), whiteUser, blackUser);

        sessions.remove(command.getGameID(), command.getAuthToken());
        String msg = String.format("%s left the game", auth.username());
        sessions.broadcast(command.getGameID(), command.getAuthToken(), new NotificationMessage(msg));
    }

    private void resign(WsMessageContext ctx, UserGameCommand command) throws Exception {
        GameData gameData = gameDAO.getGame(command.getGameID());
        AuthData auth = authDAO.getAuth(command.getAuthToken());
        ChessGame game = gameData.game();

        // Validate player is actually in the game
        if (!auth.username().equals(gameData.whiteUsername()) && !auth.username().equals(gameData.blackUsername())) {
            sendError(ctx, "Error: Observers cannot resign");
            return;
        }

        if (game.isGameOver()) {
            sendError(ctx, "Error: Game already over");
            return;
        }

        game.setGameOver(true);
        gameDAO.updateGame(gameData.gameID(), game);

        String msg = String.format("%s resigned. Game over.", auth.username());
        sessions.broadcastToAll(command.getGameID(), new NotificationMessage(msg));
    }

    private void checkStatus(GameData gameData, ChessGame game) throws Exception {
        String msg = null;
        if (game.isInCheckmate(ChessGame.TeamColor.WHITE)) msg = "White is in checkmate!";
        else if (game.isInCheckmate(ChessGame.TeamColor.BLACK)) msg = "Black is in checkmate!";
        else if (game.isInCheck(ChessGame.TeamColor.WHITE)) msg = "White is in check!";
        else if (game.isInCheck(ChessGame.TeamColor.BLACK)) msg = "Black is in check!";

        if (msg != null) sessions.broadcastToAll(gameData.gameID(), new NotificationMessage(msg));
    }

    private void sendError(WsMessageContext ctx, String message) {
        ctx.send(new Gson().toJson(new ErrorMessage(message)));
    }
}