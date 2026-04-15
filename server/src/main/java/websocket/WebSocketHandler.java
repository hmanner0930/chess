package websocket;

import com.google.gson.Gson;
import io.javalin.websocket.*;
import websocket.commands.*;
import websocket.messages.*;
import dataaccess.*;
import model.*;
import chess.*;

public class WebSocketHandler {

    private final SessionManager sessions = new SessionManager();
    private final GameDAO gameDAO;
    private final AuthDAO authDAO;

    public WebSocketHandler(GameDAO gameDAO, AuthDAO authDAO) {
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    public void configure(WsConfig ws) {
        ws.onConnect(WsContext::enableAutomaticPings);

        ws.onMessage(ctx -> {
            var message = ctx.message();
            var command = new Gson().fromJson(message, UserGameCommand.class);

            switch (command.getCommandType()) {
                case CONNECT -> connect(ctx, command);
                case MAKE_MOVE -> makeMove(ctx);
                case LEAVE -> leave(command);
                case RESIGN -> resign(ctx, command);
            }
        });

        ws.onError(ctx -> System.out.println("WS Error: " + ctx.error()));
    }

    private void connect(WsMessageContext ctx, UserGameCommand command) {
        try {
            var auth = authDAO.getAuth(command.getAuthToken());
            var gameData = gameDAO.getGame(command.getGameID());

            if (auth == null || gameData == null) {
                sendError(ctx, "Error: bad auth or game ID");
                return;
            }

            sessions.add(command.getGameID(), command.getAuthToken(), ctx.session);
            ctx.send(new Gson().toJson(new LoadGameMessage(gameData.game())));

            String name = auth.username();
            String message;
            if (name.equals(gameData.whiteUsername())) {
                message = name + " joined as white";
            } else if (name.equals(gameData.blackUsername())) {
                message = name + " joined as black";
            } else {
                message = name + " is observing";
            }

            sessions.toOne(command.getGameID(), command.getAuthToken(), new NotificationMessage(message));

        } catch (Exception exception) {
            sendError(ctx, "Error: " + exception.getMessage());
        }
    }

    private void makeMove(WsMessageContext ctx) {
        try {
            var moveCommand = new Gson().fromJson(ctx.message(), MakeMoveCommand.class);
            var auth = authDAO.getAuth(moveCommand.getAuthToken());
            var gData = gameDAO.getGame(moveCommand.getGameID());
            var game = gData.game();
            var move = moveCommand.getMove();

            if (game.isGameOver()) {
                sendError(ctx, "Error: game over");
                return;
            }
            var user = auth.username();
            boolean isWhite = user.equals(gData.whiteUsername());
            boolean isBlack = user.equals(gData.blackUsername());

            if ((game.getTeamTurn() == ChessGame.TeamColor.WHITE && !isWhite) ||
                    (game.getTeamTurn() == ChessGame.TeamColor.BLACK && !isBlack)) {
                sendError(ctx, "Error: not your turn");
                return;
            }

            game.makeMove(move);
            gameDAO.updateGame(gData.gameID(), game);
            sessions.toAll(gData.gameID(), new LoadGameMessage(game));
            var moveMessage = user + " moved " + move.getStartPosition() + " to " + move.getEndPosition();
            sessions.toOne(gData.gameID(), auth.authToken(), new NotificationMessage(moveMessage));

            checkStatus(gData, game);

        } catch (Exception exception) {
            sendError(ctx, "Error: " + exception.getMessage());
        }
    }

    private void leave(UserGameCommand command) throws Exception {
        var auth = authDAO.getAuth(command.getAuthToken());
        var gData = gameDAO.getGame(command.getGameID());

        String white = gData.whiteUsername();
        String black = gData.blackUsername();

        if (auth.username().equals(white)) {
            white = null;
        }
        else if (auth.username().equals(black)) {
            black = null;
        }

        gameDAO.updateGame(command.getGameID(), white, black);
        sessions.remove(command.getGameID(), command.getAuthToken());

        sessions.toOne(command.getGameID(), command.getAuthToken(),
                new NotificationMessage(auth.username() + " left"));
    }

    private void resign(WsMessageContext ctx, UserGameCommand command) throws Exception {
        var gData = gameDAO.getGame(command.getGameID());
        var auth = authDAO.getAuth(command.getAuthToken());
        var game = gData.game();

        if (!auth.username().equals(gData.whiteUsername()) && !auth.username().equals(gData.blackUsername())) {
            sendError(ctx, "Error: observers can't resign");
            return;
        }

        if (game.isGameOver()) {
            sendError(ctx, "Error: game already over");
            return;
        }

        game.setGameOver(true);
        gameDAO.updateGame(gData.gameID(), game);
        sessions.toAll(command.getGameID(), new NotificationMessage(auth.username() + " resigned"));
    }

    private void checkStatus(GameData gData, ChessGame game) throws Exception {
        String message = null;
        if (game.isInCheckmate(ChessGame.TeamColor.WHITE)) {
            message = gData.whiteUsername() + " is in checkmate";
        }
        else if (game.isInCheckmate(ChessGame.TeamColor.BLACK)) {
            message = gData.blackUsername() + " is in checkmate";
        }
        else if (game.isInCheck(ChessGame.TeamColor.WHITE)) {
            message = gData.whiteUsername() + " is in check";
        }
        else if (game.isInCheck(ChessGame.TeamColor.BLACK)) {
            message = gData.blackUsername() + " is in check";
        }

        if (message != null) {
            sessions.toAll(gData.gameID(), new NotificationMessage(message));
        }
    }

    private void sendError(WsMessageContext ctx, String message) {
        ctx.send(new Gson().toJson(new ErrorMessage(message)));
    }
}