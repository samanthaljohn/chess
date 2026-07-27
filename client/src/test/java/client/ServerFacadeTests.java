package client;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import result.CreateGameResult;
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

}
