package websocket;

import com.google.gson.Gson;
import io.javalin.websocket.WsConfig;
import io.javalin.websocket.WsContext;
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

    public void configure(WsConfig webSocket) {
        webSocket.onConnect(WsContext::enableAutomaticPings);

        webSocket.onMessage(ctx -> {
            String message = ctx.message();
            UserGameCommand command = new Gson().fromJson(message, UserGameCommand.class);
            switch (command.getCommandType()) {
                case CONNECT -> connect(ctx, command);
                case MAKE_MOVE -> makeMove(ctx);
                case LEAVE -> leave(command);
                case RESIGN -> resign(ctx, command);
            }
        });

        webSocket.onError(ctx -> System.out.println("WebSocket Error: " + ctx.error()));
    }

    private void connect(WsMessageContext ctx, UserGameCommand command) {
        try {
            AuthData authenticate = authDAO.getAuth(command.getAuthToken());
            GameData gameData = gameDAO.getGame(command.getGameID());

            if (authenticate == null || gameData == null) {
                sendError(ctx, "Error: Invalid authToken or gameID");
                return;
            }
            String json = new Gson().toJson(new LoadGameMessage(gameData.game()));

            sessions.add(command.getGameID(), command.getAuthToken(), ctx.session);
            ctx.send(json);

            String message;
            if (authenticate.username().equals(gameData.whiteUsername())) {
                message = String.format("%s joined as White", authenticate.username());
            } else if (authenticate.username().equals(gameData.blackUsername())) {
                message = String.format("%s joined as Black", authenticate.username());
            } else {
                message = String.format("%s joined as observer", authenticate.username());
            }

            sessions.toOne(command.getGameID(), command.getAuthToken(), new NotificationMessage(message));

        } catch (Exception exception) {
            sendError(ctx, "Error: " + exception.getMessage());
        }
    }

    private void makeMove(WsMessageContext ctx) {
        try {
            MakeMoveCommand moveCommand = new Gson().fromJson(ctx.message(), MakeMoveCommand.class);
            AuthData authenticate = authDAO.getAuth(moveCommand.getAuthToken());
            GameData gameData = gameDAO.getGame(moveCommand.getGameID());
            ChessGame game = gameData.game();
            ChessMove move = moveCommand.getMove();

            if (game.isGameOver()) {
                sendError(ctx, "Error: game is over");
                return;
            }
            boolean isWhite = authenticate.username().equals(gameData.whiteUsername());
            boolean isBlack = authenticate.username().equals(gameData.blackUsername());
            if ((game.getTeamTurn() == ChessGame.TeamColor.WHITE && !isWhite) ||
                    (game.getTeamTurn() == ChessGame.TeamColor.BLACK && !isBlack)) {
                sendError(ctx, "Error: not your turn");
                return;
            }
            game.makeMove(move);
            gameDAO.updateGame(gameData.gameID(), game);
            sessions.toAll(gameData.gameID(), new LoadGameMessage(game));
            String message = String.format("%s moved %s", authenticate.username(), move);
            sessions.toOne(gameData.gameID(), authenticate.authToken(), new NotificationMessage(message));

            checkStatus(gameData, game);

        } catch (InvalidMoveException exception) {
            sendError(ctx, "Error: Invalid move");
        } catch (Exception exception) {
            sendError(ctx, "Error: " + exception.getMessage());
        }
    }

    private void leave(UserGameCommand command) throws Exception {
        AuthData auth = authDAO.getAuth(command.getAuthToken());
        GameData gameData = gameDAO.getGame(command.getGameID());

        String whiteUser = gameData.whiteUsername();
        String blackUser = gameData.blackUsername();

        if (auth.username().equals(whiteUser)) {
            whiteUser = null;
        } else if (auth.username().equals(blackUser)) {
            blackUser = null;
        }
        gameDAO.updateGame(command.getGameID(), whiteUser, blackUser);

        sessions.remove(command.getGameID(), command.getAuthToken());
        String message = String.format("%s left the game", auth.username());
        sessions.toOne(command.getGameID(), command.getAuthToken(), new NotificationMessage(message));
    }

    private void resign(WsMessageContext ctx, UserGameCommand command) throws Exception {
        GameData gameData = gameDAO.getGame(command.getGameID());
        AuthData authenticate = authDAO.getAuth(command.getAuthToken());
        ChessGame game = gameData.game();

        if (!authenticate.username().equals(gameData.whiteUsername()) && !authenticate.username().equals(gameData.blackUsername())) {
            sendError(ctx, "Error: observers don't resign");
            return;
        }

        if (game.isGameOver()) {
            sendError(ctx, "Error: game is over");
            return;
        }

        game.setGameOver(true);
        gameDAO.updateGame(gameData.gameID(), game);

        String message = String.format("%s resigned. game over.", authenticate.username());
        sessions.toAll(command.getGameID(), new NotificationMessage(message));
    }

    private void checkStatus(GameData gameData, ChessGame game) throws Exception {
        String message = null;
        if (game.isInCheckmate(ChessGame.TeamColor.WHITE)) {
            message = "White is in checkmate!";
        }
        else if (game.isInCheckmate(ChessGame.TeamColor.BLACK)) {
            message = "Black is in checkmate!";
        }
        else if (game.isInCheck(ChessGame.TeamColor.WHITE)) {
            message = "White is in check!";
        }
        else if (game.isInCheck(ChessGame.TeamColor.BLACK)) {
            message = "Black is in check!";
        }

        if (message != null) {sessions.toAll(gameData.gameID(), new NotificationMessage(message));}
    }

    private void sendError(WsMessageContext ctx, String message) {
        ctx.send(new Gson().toJson(new ErrorMessage(message)));
    }
}