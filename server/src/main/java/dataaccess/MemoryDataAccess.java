package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MemoryDataAccess implements DataAccess {
    private Map<String, UserData> users = new HashMap<>();
    private Map<Integer, GameData> games = new HashMap<>();
    private Map<String, AuthData> auths = new HashMap<>();

    @Override
    public void clear() throws DataAccessException{}

    @Override
    public void createUser(UserData userData) throws AlreadyTakenException{}

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
    public int createGame(String gameName) throws DataAccessException{}

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
    public Collection<GameData> listGames() throws DataAccessException{}

    @Override
    public void updateGame(GameData gameData) throws DataAccessException{}

    @Override
    public void createAuth(AuthData authData) throws DataAccessException{}

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
    public void deleteAuth(String authToken) throws DataAccessException{}
}
