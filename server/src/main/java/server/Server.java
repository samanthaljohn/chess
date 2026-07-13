package server;

import dataaccess.MemoryDataAccess;
import handler.ClearHandler;
import handler.UserHandler;
import io.javalin.*;
import service.ClearService;
import service.UserService;

public class Server {

    private final Javalin javalin;

    public Server() {
        MemoryDataAccess dataAccess = new MemoryDataAccess();

        ClearService clearService = new ClearService(dataAccess);
        ClearHandler clearHandler = new ClearHandler(clearService);

        UserService userService = new UserService(dataAccess);
        UserHandler userHandler = new UserHandler(userService);

        javalin = Javalin.create(config -> config.staticFiles.add("web"))
        .delete("/db", clearHandler::clear)
        .post("/user", userHandler::register)
        .post("/session", userHandler::login)
        .delete("/session", userHandler::logout);
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
