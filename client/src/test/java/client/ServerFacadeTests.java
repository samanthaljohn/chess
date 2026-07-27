package client;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

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
    public void loginNegative() throws Exception{
        facade.register("username", "password", "email@email.com");

        assertThrows(ResponseException.class, () -> facade.login("nonexistentUser", "password"));
        assertThrows(ResponseException.class, () -> facade.login("username", "wrongPassword"));
    }

}
