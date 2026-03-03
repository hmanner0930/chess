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

    public void register(Context ctx) throws DataAccessException{
        RegisterRequest request = gson.fromJson(ctx.body(), RegisterRequest.class);
        RegisterResult result = service.register(request);
        ctx.status(200);
        ctx.json(result);
    }

    public void login(Context ctx) throws DataAccessException{
        LoginRequest request = gson.fromJson(ctx.body(), LoginRequest.class);
        RegisterResult result = service.login(request);
        ctx.status(200);
        ctx.json(result);
    }

    public void logout(Context ctx) throws DataAccessException{
        String authToken = ctx.header("authorization");
        service.logout(authToken);
        ctx.status(200);
        ctx.json(new Object());
    }

}