package server;

import dataaccess.MemoryDataAccess;

import handler.ClearHandler;
import handler.GameHandler;
import handler.UserHandler;

import io.javalin.*;

import service.ClearService;
import service.GameService;
import service.UserService;

public class Server {

    private final Javalin javalin;

    public Server() {
        MemoryDataAccess dataAccess = new MemoryDataAccess();

        ClearService clearService = new ClearService(dataAccess);
        ClearHandler clearHandler = new ClearHandler(clearService);

        UserService userService = new UserService(dataAccess);
        UserHandler userHandler = new UserHandler(userService);

        GameService gameService = new GameService(dataAccess);
        GameHandler gameHandler = new GameHandler(gameService);

        javalin = Javalin.create(config -> config.staticFiles.add("web"))
        .delete("/db", clearHandler::clear)
        .post("/user", userHandler::register)
        .post("/session", userHandler::login)
        .delete("/session", userHandler::logout)
        .get("/game", gameHandler::listGames)
        .post("/game", gameHandler::createGame)
        .put("/game", gameHandler::joinGame);
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
