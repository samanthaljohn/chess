package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;

import chess.ChessGame;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MemoryDataAccess implements DataAccess {
    private Map<String, UserData> users = new HashMap<>();
    private Map<Integer, GameData> games = new HashMap<>();
    private Map<String, AuthData> auths = new HashMap<>();

    private int gameIDCounter = 0;

    public void clearUserData(){
        users = new HashMap<>();
    }

    public void clearGameData(){
        games = new HashMap<>();
    }

    public void clearAuthData(){
        auths = new HashMap<>();
    }

    @Override
    public void clear() throws DataAccessException{
        clearUserData();
        clearGameData();
        clearAuthData();
    }

    @Override
    public void createUser(UserData userData) throws DataAccessException {
        if (!users.containsKey(userData.username())){
            users.put(userData.username(), userData);
        }
        else {
            throw new AlreadyTakenException("Error: Username Already Taken");
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException{
        if (users.containsKey(username)){
            return users.get(username);
        }
        else {
            return null;
        }
    }

    @Override
    public int createGame(String gameName) throws DataAccessException{
        gameIDCounter += 1;
        GameData gameData = new GameData(gameIDCounter, null, null, gameName, new ChessGame());
        games.put(gameIDCounter, gameData);
        return gameIDCounter;
    }

    @Override
    public GameData getGame(int gameId) throws DataAccessException{
        if (games.containsKey(gameId)){
            return games.get(gameId);
        }
        else {
            return null;
        }
    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException{
        return games.values();
    }

    @Override
    public void updateGame(GameData gameData) throws DataAccessException{
        if (games.containsKey(gameData.gameID())){
            games.put(gameData.gameID(), gameData);
        }
        else {
            throw new DataAccessException("Error: not an existing game to update");
        }
    }

    @Override
    public void createAuth(AuthData authData) throws DataAccessException{
        if(!auths.containsKey(authData.authToken())){
            auths.put(authData.authToken(), authData);
        }
        else {
            throw new AlreadyTakenException("Error: Authorization already exists");
        }
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException{
        if (auths.containsKey(authToken)){
            return auths.get(authToken);
        }
        else {
            return null;
        }
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException{
        auths.remove(authToken);
    }
}
