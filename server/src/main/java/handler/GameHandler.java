package handler;

import com.google.gson.Gson;
import io.javalin.http.Context;

import dataaccess.AlreadyTakenException;
import dataaccess.UnauthorizedException;
import dataaccess.DataAccessException;

import request.CreateGameRequest;
import request.LoginRequest;
import request.RegisterRequest;
import result.CreateGameResult;
import result.ListGamesResult;
import result.LoginResult;
import result.RegisterResult;

import service.GameService;

import javax.xml.crypto.Data;
import java.util.Map;

public class GameHandler {
    private final GameService gameService;

    public GameHandler(GameService gameService){
        this.gameService = gameService;
    }

    public void listGames(Context context){
        String authToken = context.header("authorization");

        try {
            ListGamesResult listGamesResult = gameService.listGames(authToken);
            context.status(200);
            context.contentType("application/json");
            context.result(new Gson().toJson(listGamesResult));
        } catch (UnauthorizedException e){
            context.status(401);
            context.contentType("application/json");
            context.result(new Gson().toJson(Map.of("message", "Error: " + e.getMessage())));
        } catch (DataAccessException e){
            context.status(500);
            context.contentType("application/json");
            context.result(new Gson().toJson(Map.of("message", "Error: " + e.getMessage())));
        }
    }

    public void createGame(Context context){
        String authToken = context.header("authorization");
        CreateGameRequest createGameRequest = new Gson().fromJson(context.body(), CreateGameRequest.class);

        if (createGameRequest.gameName() == null){
            context.status(400);
            context.contentType("application/json");
            context.result(new Gson().toJson(Map.of("message", "Error: bad request")));
            return;
        }

        try {
            CreateGameResult createGameResult = gameService.createGame(authToken, createGameRequest);
            context.status(200);
            context.contentType("application/json");
            context.result(new Gson().toJson(createGameResult));
        } catch (UnauthorizedException e){
            context.status(401);
            context.contentType("application/json");
            context.result(new Gson().toJson(Map.of("message", "Error: " + e.getMessage())));
        } catch (DataAccessException e){
            context.status(500);
            context.contentType("application/json");
            context.result(new Gson().toJson(Map.of("message", "Error: " + e.getMessage())));
        }
    }
}
