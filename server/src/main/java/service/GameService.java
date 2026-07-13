package service;

import dataaccess.*;
import model.AuthData;
import model.GameData;
import model.PublicGameData;
import request.CreateGameRequest;
import request.JoinGameRequest;
import result.CreateGameResult;
import result.ListGamesResult;

import javax.xml.crypto.Data;
import java.util.ArrayList;
import java.util.Collection;

public class GameService {
    private final DataAccess dataAccess;

    public GameService(DataAccess dataAccess){
        this.dataAccess = dataAccess;
    }

    public Collection<PublicGameData> toPublicGameData(Collection<GameData> games){
        Collection<PublicGameData> publicGames = new ArrayList<>();

        for (GameData game : games){
            int gameID = game.gameID();
            String whiteUsername = game.whiteUsername();
            String blackUsername = game.blackUsername();
            String gameName = game.gameName();

            PublicGameData publicGame = new PublicGameData(gameID, whiteUsername, blackUsername, gameName);

            publicGames.add(publicGame);
        }

        return publicGames;
    }

    public ListGamesResult listGames(String authToken) throws DataAccessException {
        AuthData auth = dataAccess.getAuth(authToken);

        if (auth == null){
            throw new UnauthorizedException("unauthorized");
        }

        Collection<GameData> games = dataAccess.listGames();
        Collection<PublicGameData> publicGames = toPublicGameData(games);

        ListGamesResult listGamesResult = new ListGamesResult(publicGames);

        return listGamesResult;
    }

    public CreateGameResult createGame(String authToken, CreateGameRequest createGameRequest) throws DataAccessException{
        AuthData auth = dataAccess.getAuth(authToken);

        if (auth == null){
            throw new UnauthorizedException("unauthorized");
        }

        String gameName = createGameRequest.gameName();
        int gameID = dataAccess.createGame(gameName);
        CreateGameResult createGameResult = new CreateGameResult(gameID);

        return createGameResult;
    }

    public void joinGame(String authToken, JoinGameRequest joinGameRequest) throws DataAccessException {
        AuthData auth = dataAccess.getAuth(authToken);

        if (auth == null){
            throw new UnauthorizedException("unauthorized");
        }

        int gameID = joinGameRequest.gameID();
        GameData game = dataAccess.getGame(gameID);
        if (game == null){
            throw new BadRequestException("bad request");
        }

        String playerColor = joinGameRequest.playerColor();
        String whiteUsername = game.whiteUsername();
        String blackUsername = game.blackUsername();

        if ((playerColor.equals("WHITE") && whiteUsername != null) || (playerColor.equals("BLACK") && blackUsername != null)){
            throw new AlreadyTakenException("already taken");
        }

        String username = auth.username();
        if (playerColor.equals("WHITE")){
            GameData newGame = new GameData(gameID, username, game.blackUsername(), game.gameName(), game.game());
            dataAccess.updateGame(newGame);
        } else {
            GameData newGame = new GameData(gameID, game.whiteUsername(), username, game.gameName(), game.game());
            dataAccess.updateGame(newGame);
        }
    }
}
