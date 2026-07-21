package dataaccess;


import chess.ChessGame;
import model.AuthData;
import model.GameData;
import model.UserData;

import com.google.gson.Gson;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import static java.sql.Statement.RETURN_GENERATED_KEYS;

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

    @Override
    public void clear() throws DataAccessException{
        try (var conn = DatabaseManager.getConnection()){
            try (var clearUserData = conn.prepareStatement("TRUNCATE TABLE userData")){
                clearUserData.executeUpdate();
            }
            try (var clearAuthData = conn.prepareStatement("TRUNCATE TABLE authData")){
                clearAuthData.executeUpdate();
            }
            try (var clearGameData = conn.prepareStatement("TRUNCATE TABLE gameData")){
                clearGameData.executeUpdate();
            }
        } catch (SQLException e){
            throw new DataAccessException(e.getMessage());
        }
    }

    @Override
    public void createUser(UserData userData) throws DataAccessException{
        try (var conn = DatabaseManager.getConnection()){
            try (var createUserStatement = conn.prepareStatement("INSERT INTO userData (username, password, email) VALUES(?, ?, ?)")) {
                String username = userData.username();
                String password = userData.password();
                String email = userData.email();

                createUserStatement.setString(1, username);
                String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
                createUserStatement.setString(2, hashedPassword);
                createUserStatement.setString(3, email);

                createUserStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException{
        try (var conn = DatabaseManager.getConnection()){
            try (var getUserStatement = conn.prepareStatement("SELECT username, password, email FROM userData WHERE username = ?")){
                getUserStatement.setString(1, username);
                try (var result = getUserStatement.executeQuery()){
                    if (result.next()){
                        return new UserData(username, result.getString("password"), result.getString("email"));
                    }
                    else {
                        return null;
                    }
                }
            }
        } catch (SQLException e){
            throw new DataAccessException(e.getMessage());
        }
    }

    @Override
    public int createGame(String gameName) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()){
            try (var createGameStatement = conn.prepareStatement("INSERT INTO gameData (whiteUsername, blackUsername, gameName, game) VALUES (?, ?, ?, ?)", RETURN_GENERATED_KEYS)){
                createGameStatement.setString(1, null);
                createGameStatement.setString(2, null);
                createGameStatement.setString(3, gameName);

                String json = new Gson().toJson(new ChessGame());
                createGameStatement.setString(4, json);

                createGameStatement.executeUpdate();

                var result = createGameStatement.getGeneratedKeys();
                result.next();
                return result.getInt(1);
            }
        } catch (SQLException e){
            throw new DataAccessException(e.getMessage());
        }
    }

    @Override
    public GameData getGame(int gameId) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()){
            try (var getGameStatement = conn.prepareStatement("SELECT gameID, whiteUsername, blackUsername, gameName, game FROM gameData WHERE gameID = ?")){
                getGameStatement.setInt(1, gameId);
                try (var result = getGameStatement.executeQuery()){
                    if (result.next()){
                        String whiteUsername = result.getString("whiteUsername");
                        String blackUsername = result.getString("blackUsername");

                        String json = result.getString("game");
                        ChessGame game = new Gson().fromJson(json, ChessGame.class);

                        String gameName = result.getString("gameName");

                        return new GameData(gameId, whiteUsername, blackUsername, gameName, game);
                    }
                    else {
                        return null;
                    }
                }
            }
        } catch (SQLException e){
            throw new DataAccessException(e.getMessage());
        }
    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()){
            try (var listGamesStatement = conn.prepareStatement("SELECT gameID, whiteUsername, blackUsername, gameName, game FROM gameData")){
                try (var result = listGamesStatement.executeQuery()){
                    Collection<GameData> games = new ArrayList<>();
                    while (result.next()){
                        int gameID = result.getInt("gameID");
                        String whiteUsername = result.getString("whiteUsername");
                        String blackUsername = result.getString("blackUsername");
                        String gameName = result.getString("gameName");

                        String json = result.getString("game");
                        ChessGame game = new Gson().fromJson(json, ChessGame.class);

                        games.add(new GameData(gameID, whiteUsername, blackUsername, gameName, game));
                    }
                    return games;
                }
            }
        } catch (SQLException e){
            throw new DataAccessException(e.getMessage());
        }
    }

    @Override
    public void updateGame(GameData gameData) throws DataAccessException {}

    @Override
    public void createAuth(AuthData authData) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()){
            try (var createAuthStatement = conn.prepareStatement("INSERT INTO authData (authToken, username) VALUES(?, ?)")){
                String authToken = authData.authToken();
                String username = authData.username();

                createAuthStatement.setString(1, authToken);
                createAuthStatement.setString(2, username);

                createAuthStatement.executeUpdate();
            }
        } catch (SQLException e){
            throw new DataAccessException(e.getMessage());
        }
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException{
        try (var conn = DatabaseManager.getConnection()){
            try (var getAuthStatement = conn.prepareStatement("SELECT authToken, username FROM authData WHERE authToken = ?")){
                getAuthStatement.setString(1, authToken);
                try (var result = getAuthStatement.executeQuery()) {
                    if (result.next()){
                        return new AuthData(authToken, result.getString("username"));
                    }
                    else {
                        return null;
                    }
                }
            }
        } catch (SQLException e){
            throw new DataAccessException(e.getMessage());
        }
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException{
        try (var conn = DatabaseManager.getConnection()){
            try (var deleteAuthStatement = conn.prepareStatement("DELETE FROM authData WHERE authToken = ?")){
                deleteAuthStatement.setString(1, authToken);
                deleteAuthStatement.executeUpdate();
            }
        } catch (SQLException e){
            throw new DataAccessException(e.getMessage());
        }
    }
}
