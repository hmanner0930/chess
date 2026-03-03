package server;

import io.javalin.http.Context;
import service.ClearService;
import dataaccess.DataAccessException;

public class ClearHandler {

    private final ClearService service;

    public ClearHandler(ClearService service){
        this.service = service;
    }

    public void handle(Context ctx) throws DataAccessException {
        service.clear();
        ctx.status(200);
        ctx.json(new Object());
    }
}
