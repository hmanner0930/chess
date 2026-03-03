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

    private void registerHandlers(){
        javalin.exception(DataAccessException.class, (except, ctx) -> {
            ctx.status(status(except));
            ctx.json(new ErrorResponse(except.getMessage()));
        });
    }

    private void registerRoutes(){
        UserDAO userDAO = new MemoryUserDAO();
        AuthDAO authDAO = new MemoryAuthDAO();
        GameDAO gameDAO = new MemoryGameDAO();

        ClearService clearService = new ClearService(userDAO, authDAO, gameDAO);
        UserService userService = new UserService(userDAO, authDAO);
        GameService gameService = new GameService(gameDAO, authDAO);

        javalin.delete("/db", new ClearHandler(clearService)::handle);
        javalin.post("/user", new UserHandler(userService)::register);
        javalin.post("/session", new UserHandler(userService)::login);
        javalin.delete("/session", new UserHandler(userService)::logout);
        javalin.get("/game", new GameHandler(gameService)::listGames);
        javalin.post("/game", new GameHandler(gameService)::createGame);
        javalin.put("/game", new GameHandler(gameService)::joinGame);
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