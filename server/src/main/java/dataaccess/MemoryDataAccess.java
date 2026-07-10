package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MemoryDataAccess implements DataAccess {
    @Override
    public void clear() throws DataAccessException{}

    @Override
    public void createUser(UserData userData) throws DataAccessException{}

    @Override
    public UserData getUser(String username) throws DataAccessException{}

    @Override
    public int createGame(String gameName) throws DataAccessException{}

    @Override
    public GameData getGame(int gameId) throws DataAccessException{}

    @Override
    public Collection<GameData> listGames() throws DataAccessException{}

    @Override
    public void updateGame(GameData gameData) throws DataAccessException{}

    @Override
    public void createAuth(AuthData authData) throws DataAccessException{}

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException{}

    @Override
    public void deleteAuth(String authToken) throws DataAccessException{}
}
