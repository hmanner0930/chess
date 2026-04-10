package server;

import com.google.gson.Gson;
import io.javalin.http.Context;
import service.*;
import dataaccess.DataAccessException;
import java.util.ArrayList;

public class GameHandler {
    private final GameService service;
    private final Gson gson = new Gson();

    public GameHandler(GameService service) {
        this.service = service;
    }

    public void listGames(Context ctx) throws DataAccessException {
        String authToken = ctx.header("authorization");
        var games = service.listGames(authToken);
        ctx.status(200);
        ctx.json(new ListGamesResult(new ArrayList<>(games)));
    }

    public void createGame(Context ctx) throws DataAccessException {
        String authToken = ctx.header("authorization");
        CreateGameRequest request = gson.fromJson(ctx.body(), CreateGameRequest.class);
        int gameID = service.createGame(authToken, request.gameName());
        ctx.status(200);
        ctx.json(new CreateGameResult(gameID));
    }

    public void joinGame(Context ctx) throws DataAccessException {
        String authToken = ctx.header("authorization");
        JoinGameRequest request = gson.fromJson(ctx.body(), JoinGameRequest.class);
        service.joinGame(authToken, request.playerColor(), request.gameID());

        ctx.status(200);
        ctx.json(new Object());
    }
}