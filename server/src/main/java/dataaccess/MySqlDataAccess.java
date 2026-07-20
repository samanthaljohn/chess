package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;

import java.sql.SQLException;
import java.util.Collection;

public class MySqlDataAccess implements DataAccess{
    public MySqlDataAccess() throws DataAccessException{
        DatabaseManager.createDatabase();
        configureDatabase();
    }

    private void configureDatabase() throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var createUserDataTable = """
            CREATE TABLE IF NOT EXISTS userData (
                username VARCHAR(255) NOT NULL,
                password VARCHAR(255) NOT NULL,
                email VARCHAR(255) NOT NULL,
                PRIMARY KEY (username)
            )""";
            try (var createTableStatement = conn.prepareStatement(createUserDataTable)) {
                createTableStatement.executeUpdate();
            }

            var createAuthDataTable = """
            CREATE TABLE IF NOT EXISTS authData (
                authToken VARCHAR(255) NOT NULL,
                username VARCHAR(255) NOT NULL,
                PRIMARY KEY (authToken)
            )""";
            try (var createTableStatement = conn.prepareStatement(createAuthDataTable)){
                createTableStatement.executeUpdate();
            }

            var createGameDataTable = """
            CREATE TABLE IF NOT EXISTS gameData (
                gameID INT NOT NULL AUTO_INCREMENT,
                whiteUsername VARCHAR(255),
                blackUsername VARCHAR(255),
                gameName VARCHAR(255) NOT NULL,
                game longtext NOT NULL,
                PRIMARY KEY (gameID)        
            )""";
            try (var createTableStatement = conn.prepareStatement(createGameDataTable)){
                createTableStatement.executeUpdate();
            }

        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    public void clear() throws DataAccessException{}

    public void createUser(UserData userData) throws DataAccessException{}
    public UserData getUser(String username) throws DataAccessException{
        return null;
    }

    public int createGame(String gameName) throws DataAccessException{
        return 0;
    }
    public GameData getGame(int gameId) throws DataAccessException{
        return null;
    }
    public Collection<GameData> listGames() throws DataAccessException{
        return null;
    }
    public void updateGame(GameData gameData) throws DataAccessException{}

    public void createAuth(AuthData authData) throws DataAccessException{}
    public AuthData getAuth(String authToken) throws DataAccessException{
        return null;
    }
    public void deleteAuth(String authToken) throws DataAccessException{}
}
