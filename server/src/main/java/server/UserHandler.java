package server;

import com.google.gson.Gson;
import io.javalin.http.Context;
import service.*;
import dataaccess.DataAccessException;

public class UserHandler {
    private final UserService service;
    private final Gson gson = new Gson();

    public UserHandler(UserService service){
        this.service = service;
    }

    public void register(Context ctx) {
        try {
            RegisterRequest req = gson.fromJson(ctx.body(), RegisterRequest.class);
            // If JSON was totally empty, GSON might return null or an empty record
            if (req == null) {throw new DataAccessException("Error: bad request");}

            RegisterResult res = service.register(req);
            ctx.status(200);
            ctx.json(res);
        } catch (DataAccessException except) {
            mapError(except, ctx);
        }
    }

    public void login(Context ctx) {
        try {
            LoginRequest req = gson.fromJson(ctx.body(), LoginRequest.class);
            if (req == null) {throw new DataAccessException("Error: bad request");}

            RegisterResult res = service.login(req);
            ctx.status(200);
            ctx.json(res);
        } catch (DataAccessException except) {
            mapError(except, ctx);
        }
    }

    public void logout(Context ctx) {
        try {
            String authToken = ctx.header("authorization");
            service.logout(authToken);
            ctx.status(200);
            ctx.json(new Object()); // Return empty JSON object {}
        } catch (DataAccessException e) {
            mapError(e, ctx);
        }
    }

    private void mapError(DataAccessException e, Context ctx) {
        String message = e.getMessage();
        if (message.contains("bad request")) {ctx.status(400);}
        else if (message.contains("unauthorized")) {ctx.status(401);}
        else if (message.contains("already taken")) {ctx.status(403);}
        else {ctx.status(500);}

        ctx.json(new ErrorResponse(message));
    }
}