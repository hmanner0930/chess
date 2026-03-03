package server;

import com.google.gson.Gson;
import io.javalin.http.Context;
import service.*;
import dataaccess.DataAccessException;
import java.util.Map;

public class GameHandler {
    private final GameService service;

    public GameHandler(GameService service) {
        this.service = service;
    }

    public void listGames(Context ctx) {
        try {
            String authToken = ctx.header("authorization");
            var games = service.listGames(authToken);
            ctx.status(200);
            ctx.json(Map.of("games", games)); // Wraps list in {"games": [...]}
        } catch (DataAccessException e) {
            mapError(e, ctx);
        }
    }

    public void createGame(Context ctx) {
        try {
            String authToken = ctx.header("authorization");
            CreateGameRequest req = new Gson().fromJson(ctx.body(), CreateGameRequest.class);
            int gameID = service.createGame(authToken, req.gameName());
            ctx.status(200);
            ctx.json(Map.of("gameID", gameID));
        } catch (DataAccessException e) {
            mapError(e, ctx);
        }
    }

    public void joinGame(Context ctx) {
        try {
            String authToken = ctx.header("authorization");
            JoinGameRequest req = new Gson().fromJson(ctx.body(), JoinGameRequest.class);
            service.joinGame(authToken, req.playerColor(), req.gameID());
            ctx.status(200);
            ctx.json(new Object());
        } catch (DataAccessException e) {
            mapError(e, ctx);
        }
    }

    private void mapError(DataAccessException e, Context ctx) {
        if (e.getMessage().contains("bad request")) {ctx.status(400);}
        else if (e.getMessage().contains("unauthorized")) {ctx.status(401);}
        else if (e.getMessage().contains("already taken")) {ctx.status(403);}
        else {ctx.status(500);}
        ctx.json(new ErrorResponse(e.getMessage()));
    }
}