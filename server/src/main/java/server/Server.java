package server;

import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;

import dataaccess.MySqlDataAccess;
import handler.ClearHandler;
import handler.GameHandler;
import handler.GamePlayHandler;
import handler.UserHandler;

import io.javalin.*;

import io.javalin.json.JsonMapper;
import service.ClearService;
import service.GamePlayService;
import service.GameService;
import service.UserService;

import java.lang.reflect.Type;

public class Server {

    private final Javalin javalin;

    public Server() {
        JsonMapper gsonMapper = new JsonMapper() {
            @Override
            public String toJsonString(Object object, Type type) {
                var serializer = new Gson();
                var json = serializer.toJson(object);
                return json;
            }

            @Override
            public <T> T fromJsonString(String json, Type targetType) {
                var serializer = new Gson();
                var object = serializer.fromJson(json, targetType);
                return (T) object;
            }
        };

        DataAccess dataAccess;

        try{
            dataAccess = new MySqlDataAccess();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }

        ClearService clearService = new ClearService(dataAccess);
        ClearHandler clearHandler = new ClearHandler(clearService);

        UserService userService = new UserService(dataAccess);
        UserHandler userHandler = new UserHandler(userService);

        GameService gameService = new GameService(dataAccess);
        GameHandler gameHandler = new GameHandler(gameService);

        GamePlayService gamePlayService = new GamePlayService(dataAccess);
        GamePlayHandler gamePlayHandler = new GamePlayHandler(gamePlayService);

        javalin = Javalin.create(config -> {
            config.staticFiles.add("web");
            config.jsonMapper(gsonMapper);})
        .delete("/db", clearHandler::clear)
        .post("/user", userHandler::register)
        .post("/session", userHandler::login)
        .delete("/session", userHandler::logout)
        .get("/game", gameHandler::listGames)
        .post("/game", gameHandler::createGame)
        .put("/game", gameHandler::joinGame)
        .ws("/ws", ws -> {
            ws.onConnect(gamePlayHandler::handleConnect);
            ws.onMessage(gamePlayHandler::handleMessage);
            ws.onClose(gamePlayHandler::handleClose);
        });
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
