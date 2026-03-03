package server;

import com.google.gson.Gson;
import io.javalin.http.Context;
import service.ClearService;
import dataaccess.DataAccessException;

public class ClearHandler {
    private final ClearService service;
    public ClearHandler(ClearService service){
        this.service = service;
    }
    public void handle(Context ctx){
        try{
            service.clear();
            ctx.status(200);
            ctx.json(new Object());
        } catch (DataAccessException except){
            ctx.status(500);
            ctx.json(new ErrorResponse(except.getMessage()));
        }
    }
}
