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
    public void clear() {
        clearUserData();
        clearGameData();
        clearAuthData();
    }

    @Override
    public void createUser(UserData userData) {
        users.put(userData.username(), userData);
    }

    @Override
    public UserData getUser(String username) {
        if (users.containsKey(username)){
            return users.get(username);
        }
        else {
            return null;
        }
    }

    @Override
    public int createGame(String gameName) {
        gameIDCounter += 1;
        GameData gameData = new GameData(gameIDCounter, null, null, gameName, new ChessGame());
        games.put(gameIDCounter, gameData);
        return gameIDCounter;
    }

    @Override
    public GameData getGame(int gameId) {
        if (games.containsKey(gameId)){
            return games.get(gameId);
        }
        else {
            return null;
        }
    }

    @Override
    public Collection<GameData> listGames() {
        return games.values();
    }

    @Override
    public void updateGame(GameData gameData){
        games.put(gameData.gameID(), gameData);
    }

    @Override
    public void createAuth(AuthData authData){
        auths.put(authData.authToken(), authData);
    }

    @Override
    public AuthData getAuth(String authToken) {
        if (auths.containsKey(authToken)){
            return auths.get(authToken);
        }
        else {
            return null;
        }
    }

    @Override
    public void deleteAuth(String authToken) {
        auths.remove(authToken);
    }
}
