package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.Collection;

public class MySqlDataAccess implements DataAccess{
    public void clear() throws DataAccessException{};

    public void createUser(UserData userData) throws DataAccessException{};
    public UserData getUser(String username) throws DataAccessException{
        return null;
    };

    public int createGame(String gameName) throws DataAccessException{
        return 0;
    };
    public GameData getGame(int gameId) throws DataAccessException{
        return null;
    };
    public Collection<GameData> listGames() throws DataAccessException{
        return null;
    };
    public void updateGame(GameData gameData) throws DataAccessException{};

    public void createAuth(AuthData authData) throws DataAccessException{};
    public AuthData getAuth(String authToken) throws DataAccessException{
        return null;
    };
    public void deleteAuth(String authToken) throws DataAccessException{};
}
