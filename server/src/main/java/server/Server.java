package server;

import dataaccess.MemoryDataAccess;
import handler.ClearHandler;
import io.javalin.*;
import service.ClearService;

public class Server {

    private final Javalin javalin;

    public Server() {
        MemoryDataAccess dataAccess = new MemoryDataAccess();
        ClearService clearService = new ClearService(dataAccess);
        ClearHandler clearHandler = new ClearHandler(clearService);

        javalin = Javalin.create(config -> config.staticFiles.add("web"))
        .delete("/db", clearHandler::clear);
        // Register your endpoints and exception handlers here.

    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
