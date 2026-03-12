package server;

import io.javalin.Javalin;
import io.javalin.json.JavalinGson;
import dataaccess.*;
import service.*;

public class Server {
    private final Javalin javalin;

    public Server() {
        this.javalin = createServ();
        registerHandlers();
        registerRoutes();
    }

    private Javalin createServ(){
        return Javalin.create(config -> {
            config.staticFiles.add("web");
            config.jsonMapper(new JavalinGson());
        });
    }

    private void registerHandlers() {
        javalin.exception(DataAccessException.class, (except, ctx) -> {
            int statusCode = status(except);
            ctx.status(statusCode);

            String message = except.getMessage();
            // If the message doesn't already start with "Error: ", add it.
            // This makes your code robust for Test 3!
            if (!message.startsWith("Error:")) {
                message = "Error: " + message;
            }

            ctx.json(new ErrorResponse(message));
        });

        javalin.exception(Exception.class, (except, ctx) -> {
            ctx.status(500);
            ctx.json(new ErrorResponse("Error: " + except.getMessage()));
        });
    }

    private void registerRoutes(){
        UserDAO userDAO = new SqlUserDAO();
        AuthDAO authDAO = new SqlAuthDAO();
        GameDAO gameDAO = new SqlGameDAO();

        ClearService clearService = new ClearService(userDAO, authDAO, gameDAO);
        UserService userService = new UserService(userDAO, authDAO);
        GameService gameService = new GameService(gameDAO, authDAO);

        javalin.delete("/db", ctx -> new ClearHandler(clearService).handle(ctx));
        javalin.post("/user", ctx -> new UserHandler(userService).register(ctx));
        javalin.post("/session", ctx -> new UserHandler(userService).login(ctx));
        javalin.delete("/session", ctx -> new UserHandler(userService).logout(ctx));
        javalin.get("/game", ctx -> new GameHandler(gameService).listGames(ctx));
        javalin.post("/game", ctx -> new GameHandler(gameService).createGame(ctx));
        javalin.put("/game", ctx -> new GameHandler(gameService).joinGame(ctx));
    }

    private int status(DataAccessException except){
        String message =except.getMessage().toLowerCase();
        if (message.contains("bad request")) {
            return 400;
        }
        if (message.contains("unauthorized")) {
            return 401;
        }
        if (message.contains("already taken")) {
            return 403;
        }

        return 500;
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}