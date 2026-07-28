package client;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import result.CreateGameResult;
import result.ListGamesResult;
import result.LoginResult;
import result.RegisterResult;
import server.Server;

public class ServerFacadeTests {

    private static Server server;
    static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);

        facade = new ServerFacade(port);
    }

    @BeforeEach
    public void clear() throws Exception{
        facade.clear();
    }

    @AfterAll
    static void stopServer() { server.stop(); }


    @Test
    public void registerPositive() throws Exception{
        RegisterResult registerResult = facade.register("username", "password", "email@email.com");

        assertEquals("username", registerResult.username());
        assertNotNull(registerResult.authToken());
        assertTrue(registerResult.authToken().length() > 10);
    }

    @Test
    public void registerNegative() throws Exception{
        RegisterResult registerResult = facade.register("username", "password", "email@email.com");

        assertThrows(ResponseException.class, () -> facade.register("username", "differentPassword", "differentEmail@email.com"));
    }

    @Test
    public void loginPositive() throws Exception{
        facade.register("username", "password", "email@email.com");

        LoginResult loginResult = facade.login("username", "password");

        assertEquals("username", loginResult.username());
        assertNotNull(loginResult.authToken());
        assertTrue(loginResult.authToken().length() > 10);
    }

    @Test
    public void loginNonexistentUser() throws Exception {
        assertThrows(ResponseException.class, () -> facade.login("nonexistentUser", "password"));
    }

    @Test
    public void loginBadPassword() throws Exception{
        facade.register("username", "password", "email@email.com");

        assertThrows(ResponseException.class, () -> facade.login("username", "wrongPassword"));
    }

    @Test
    public void logoutPositive() throws Exception{
        facade.register("username", "password", "email@email.com");
        LoginResult loginResult = facade.login("username", "password");
        String authToken = loginResult.authToken();

        assertDoesNotThrow(() -> facade.logout(authToken));
    }

    @Test
    public void logoutNegative() throws Exception{
        facade.register("username", "password", "email@email.com");
        LoginResult loginResult = facade.login("username", "password");
        String authToken = loginResult.authToken();
        facade.logout(authToken);

        assertThrows(ResponseException.class, () -> facade.logout(authToken));
    }

    @Test
    public void createGamePositive() throws Exception{
        facade.register("username", "password", "email@email.com");
        LoginResult loginResult = facade.login("username", "password");
        String authToken = loginResult.authToken();

        CreateGameResult createGameResult = facade.createGame(authToken,"gameName");

        assertTrue(createGameResult.gameID() > 0);
    }

    @Test
    public void createGameNegative() throws Exception{
        assertThrows(ResponseException.class, () -> facade.createGame("badAuthToken", "gameName"));
    }

    @Test
    public void listGamesPositive() throws Exception {
        facade.register("username", "password", "email@email.com");
        LoginResult loginResult = facade.login("username", "password");
        String authToken = loginResult.authToken();

        facade.createGame(authToken, "gameOne");
        facade.createGame(authToken, "gameTwo");
        facade.createGame(authToken, "gameThree");

        ListGamesResult listGamesResult = facade.listGames(authToken);
        assertEquals(3, listGamesResult.games().size());
    }

    @Test
    public void listGamesNegative() throws Exception {
        assertThrows(ResponseException.class, () -> facade.listGames("badAuthToken"));
    }

    @Test
    public void joinGamePositive() throws Exception{
        facade.register("username", "password", "email@email.com");
        LoginResult loginResult = facade.login("username", "password");
        String authToken = loginResult.authToken();

        CreateGameResult createGameResult = facade.createGame(authToken, "gameOne");
        int gameID = createGameResult.gameID();

        assertDoesNotThrow(() -> facade.joinGame(authToken, "WHITE", gameID));
    }

    @Test
    public void joinGameBadAuth() throws Exception{
        facade.register("username", "password", "email@email.com");
        LoginResult loginResult = facade.login("username", "password");
        String authToken = loginResult.authToken();

        CreateGameResult createGameResult = facade.createGame(authToken, "gameName");
        int gameID = createGameResult.gameID();

        assertThrows(ResponseException.class, () -> facade.joinGame("badAuthToken", "WHITE", gameID));
    }

    @Test
    public void joinGameBadGameID() throws Exception{
        facade.register("username", "password", "email@email.com");
        LoginResult loginResult = facade.login("username", "password");
        String authToken = loginResult.authToken();

        assertThrows(ResponseException.class, () -> facade.joinGame(authToken, "WHITE", 999));
    }

    @Test
    public void joinGameBadPlayerColor() throws Exception{
        facade.register("username", "password", "email@email.com");
        LoginResult loginResultOne = facade.login("username", "password");
        String authTokenOne = loginResultOne.authToken();

        facade.register("usernameTwo", "passwordTwo", "emailTwo@email.com");
        LoginResult loginResultTwo = facade.login("usernameTwo","passwordTwo");
        String authTokenTwo = loginResultTwo.authToken();

        CreateGameResult createGameResult = facade.createGame(authTokenOne,"gameName");
        int gameID = createGameResult.gameID();

        facade.joinGame(authTokenOne, "WHITE", gameID);

        assertThrows(ResponseException.class, () -> facade.joinGame(authTokenTwo, "WHITE", gameID));
    }
}
