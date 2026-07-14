package handler;

import com.google.gson.Gson;
import dataaccess.BadRequestException;
import io.javalin.http.Context;

import dataaccess.AlreadyTakenException;
import dataaccess.UnauthorizedException;
import dataaccess.DataAccessException;

import request.CreateGameRequest;
import request.JoinGameRequest;
import request.LoginRequest;
import request.RegisterRequest;
import result.CreateGameResult;
import result.ListGamesResult;
import result.LoginResult;
import result.RegisterResult;

import service.GameService;

import javax.xml.crypto.Data;
import java.util.Map;

public class GameHandler extends ErrorHandler{
    private final GameService gameService;

    public GameHandler(GameService gameService){
        this.gameService = gameService;
    }

    public void listGames(Context context){
        String authToken = context.header("authorization");

        try {
            ListGamesResult listGamesResult = gameService.listGames(authToken);
            context.status(200);
            context.json(listGamesResult);
        } catch (UnauthorizedException e){
            handleError(context, 401, "Error: " + e.getMessage());
        } catch (DataAccessException e){
            handleError(context, 500, "Error: " + e.getMessage());
        }
    }

    public void createGame(Context context){
        String authToken = context.header("authorization");
        CreateGameRequest createGameRequest = new Gson().fromJson(context.body(), CreateGameRequest.class);

        if (createGameRequest.gameName() == null){
            handleError(context, 400, "Error: bad request");
            return;
        }

        try {
            CreateGameResult createGameResult = gameService.createGame(authToken, createGameRequest);
            context.status(200);
            context.json(createGameResult);
        } catch (UnauthorizedException e){
            handleError(context, 401, "Error: " + e.getMessage());
        } catch (DataAccessException e){
            handleError(context, 500, "Error: " + e.getMessage());
        }
    }

    public void joinGame(Context context){
        String authToken = context.header("authorization");
        JoinGameRequest joinGameRequest = new Gson().fromJson(context.body(), JoinGameRequest.class);

        String color = joinGameRequest.playerColor();

        if(color == null || (!color.equals("WHITE") && !color.equals("BLACK")) || joinGameRequest.gameID() == 0) {
            handleError(context, 400, "Error: bad request");
            return;
        }

        try {
            gameService.joinGame(authToken, joinGameRequest);
            context.status(200);
            context.json(Map.of());
        } catch (BadRequestException e){
            handleError(context, 400, "Error: " + e.getMessage());
        } catch (UnauthorizedException e){
            handleError(context, 401, "Error: " + e.getMessage());
        } catch (AlreadyTakenException e){
            handleError(context, 403, "Error: " + e.getMessage());
        } catch (DataAccessException e){
            handleError(context, 500, "Error: " + e.getMessage());
        }
    }
}
