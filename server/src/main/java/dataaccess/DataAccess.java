package dataaccess;

import model.UserData;
import model.GameData;
import model.AuthData;

import java.util.Collection;

public interface DataAccess {
    void clear() throws DataAccessException;

    void createUser(UserData userData) throws DataAccessException;
    UserData getUser(String username) throws DataAccessException;

    int createGame(String gameName) throws DataAccessException;
    GameData getGame(int gameId) throws DataAccessException;
    Collection<GameData> listGames() throws DataAccessException;
    void updateGame(GameData gameData) throws DataAccessException;

    void createAuth(AuthData authData) throws DataAccessException;
    AuthData getAuth(String authToken) throws DataAccessException;
    void deleteAuth(String authToken) throws DataAccessException;
}
