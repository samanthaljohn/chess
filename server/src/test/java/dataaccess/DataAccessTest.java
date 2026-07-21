package dataaccess;

import chess.ChessGame;
import model.UserData;
import model.GameData;
import model.AuthData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

public class DataAccessTest {
    @BeforeEach
    void clearTables() throws DataAccessException{
        DataAccess dataAccess = new MySqlDataAccess();
        dataAccess.clear();
    }

//    @Test
//    void clear() throws DataAccessException {
//        DataAccess dataAccess = new MySqlDataAccess();
//
//
//    }

    @Test
    void createUserPositive() throws DataAccessException {
        DataAccess dataAccess = new MySqlDataAccess();

        UserData user = new UserData("newUsername", "password", "myemail@gmail.com");
        dataAccess.createUser(user);

        UserData storedUser = dataAccess.getUser("newUsername");

        assertEquals(user.username(), storedUser.username());
        assertTrue(BCrypt.checkpw(user.password(), storedUser.password()));
        assertEquals(user.email(), storedUser.email());
    }

    @Test
    void createUserNegative() throws DataAccessException{
        DataAccess dataAccess = new MySqlDataAccess();

        UserData user = new UserData("newUsername", "password", "myemail@gmail.com");
        dataAccess.createUser(user);

        assertThrows(DataAccessException.class, () -> dataAccess.createUser(user));
    }

    @Test
    void getUserPositive() throws DataAccessException {
        DataAccess dataAccess = new MySqlDataAccess();

        UserData user = new UserData("newUsername", "password", "myemail@gmail.com");
        dataAccess.createUser(user);

        UserData storedUser = dataAccess.getUser("newUsername");

        assertEquals(user.username(), storedUser.username());
        assertTrue(BCrypt.checkpw(user.password(), storedUser.password()));
        assertEquals(user.email(), storedUser.email());
    }

    @Test
    void getUserNegative() throws DataAccessException {
        DataAccess dataAccess = new MySqlDataAccess();

        assertNull(dataAccess.getUser("nonexistentUsername"));
    }

    @Test
    void createGamePositive() throws DataAccessException {
        DataAccess dataAccess = new MySqlDataAccess();

        int gameID = dataAccess.createGame("newGame");

        assertEquals("newGame", dataAccess.getGame(gameID).gameName());
    }

     @Test
    void createGameNegative() throws DataAccessException {
        DataAccess dataAccess = new MySqlDataAccess();

        assertThrows(DataAccessException.class, () -> dataAccess.createGame(null));
    }

    @Test
    void getGamePositive() throws DataAccessException {
        DataAccess dataAccess = new MySqlDataAccess();
        dataAccess.createGame("newGame");
        GameData game = new GameData(1, null, null, "newGame", new ChessGame());

        assertEquals(game, dataAccess.getGame(1));
    }

    @Test
    void getGameNegative() throws DataAccessException {
        DataAccess dataAccess = new MySqlDataAccess();

        assertNull(dataAccess.getGame(2039484));
    }

    @Test
    void listGamesPositive() throws DataAccessException {
        DataAccess dataAccess = new MySqlDataAccess();

        int gameID1 = dataAccess.createGame("gameOne");
        int gameID2 = dataAccess.createGame("gameTwo");

        Collection<GameData> games = dataAccess.listGames();

        assertEquals(2, games.size());
        assertTrue(games.contains(new GameData(gameID1, null, null, "gameOne", new ChessGame())));
        assertTrue(games.contains(new GameData(gameID2, null, null, "gameTwo", new ChessGame())));
    }

    @Test
    void listGamesNegative() throws DataAccessException {
        DataAccess dataAccess = new MySqlDataAccess();

        Collection<GameData> games = dataAccess.listGames();

        assertEquals(0, games.size());
    }

//
//    @Test
//    void updateGamePositive() throws DataAccessException {
//        MemoryDataAccess dataAccess = new MemoryDataAccess();
//        int gameID = dataAccess.createGame("newGame");
//
//        GameData updatedGame = new GameData(gameID, "whitePlayer", null, "newGame", new ChessGame());
//        dataAccess.updateGame(updatedGame);
//
//        assertEquals(updatedGame, dataAccess.getGame(gameID));
//    }
//
//    @Test
//    void updateGameNegative() throws DataAccessException{
//        MemoryDataAccess dataAccess = new MemoryDataAccess();
//
//        GameData fakeGame = new GameData(999, null, null, "fakeGame", new ChessGame());
//
//        assertThrows(DataAccessException.class, () -> dataAccess.updateGame(fakeGame));
//    }
//
    @Test
    void getAuthPositive() throws DataAccessException {
        DataAccess dataAccess = new MySqlDataAccess();
        AuthData auth = new AuthData("token123", "someUser");
        dataAccess.createAuth(auth);

        assertEquals(auth, dataAccess.getAuth("token123"));
    }


    @Test
    void getAuthNegative() throws DataAccessException{
        DataAccess dataAccess = new MySqlDataAccess();

        assertNull(dataAccess.getAuth("nonexistentToken"));
    }

    @Test
    void createAuthPositive() throws DataAccessException {
        DataAccess dataAccess = new MySqlDataAccess();

        AuthData auth = new AuthData("token123", "someUser");
        dataAccess.createAuth(auth);

        assertEquals(auth, dataAccess.getAuth("token123"));
    }

    @Test
    void createAuthNegative() throws DataAccessException {
        DataAccess dataAccess = new MySqlDataAccess();

        AuthData auth = new AuthData("token123", "someUser");
        dataAccess.createAuth(auth);

        assertThrows(DataAccessException.class, () -> dataAccess.createAuth(auth));
    }


    @Test
    void deleteAuthPositive() throws DataAccessException {
        DataAccess dataAccess = new MySqlDataAccess();

        AuthData auth = new AuthData("token123", "someUser");
        dataAccess.createAuth(auth);

        dataAccess.deleteAuth("token123");

        assertNull(dataAccess.getAuth("token123"));
    }

    @Test
    void deleteAuthNegative() throws DataAccessException {
        DataAccess dataAccess = new MySqlDataAccess();

        assertDoesNotThrow(() -> dataAccess.deleteAuth("nonExistentToken"));
    }
}
