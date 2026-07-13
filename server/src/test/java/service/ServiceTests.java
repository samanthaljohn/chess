package service;

import dataaccess.AlreadyTakenException;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import dataaccess.UnauthorizedException;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.Test;
import request.LoginRequest;
import request.RegisterRequest;
import result.LoginResult;
import result.RegisterResult;

import static org.junit.jupiter.api.Assertions.*;

class ServiceTests {

    @Test
    void clear() throws DataAccessException {
        MemoryDataAccess dataAccess = new MemoryDataAccess();
        ClearService clearService = new ClearService(dataAccess);

        UserData user = new UserData("newUsername", "password", "myemail@gmail.com");
        dataAccess.createUser(user);
        dataAccess.createGame("newGame");
        AuthData auth = new AuthData("token123", "someUser");
        dataAccess.createAuth(auth);

        clearService.clear();

        assertNull(dataAccess.getUser("newUsername"));
        assertTrue(dataAccess.listGames().isEmpty());
        assertNull(dataAccess.getAuth("token123"));
    }

    @Test
    void registerPositive() throws DataAccessException {
        MemoryDataAccess dataAccess = new MemoryDataAccess();
        UserService userService = new UserService(dataAccess);

        RegisterRequest request = new RegisterRequest("username", "password", "email@email.com");
        RegisterResult result = userService.register(request);
        UserData userData = new UserData("username", "password", "email@email.com");

        assertEquals(userData, dataAccess.getUser("username"));
        assertEquals("username", result.username());
        assertNotNull(result.authToken());
        assertNotNull(dataAccess.getAuth(result.authToken()));
    }

    @Test
    void registerNegative() throws DataAccessException, AlreadyTakenException {
        MemoryDataAccess dataAccess = new MemoryDataAccess();
        UserService userService = new UserService(dataAccess);

        RegisterRequest request = new RegisterRequest("username", "password", "email@email.com");
        userService.register(request);
        UserData userOneData = new UserData("username", "password", "email@email.com");

        RegisterRequest newRequest = new RegisterRequest("username", "different password", "differentemail@email.com");

        assertThrows(AlreadyTakenException.class, () -> userService.register(newRequest));
        assertEquals(userOneData, dataAccess.getUser("username"));
    }

    @Test
    void loginPositive() throws DataAccessException{
        MemoryDataAccess dataAccess = new MemoryDataAccess();
        UserService userService = new UserService(dataAccess);

        RegisterRequest request = new RegisterRequest("username", "password", "email@email.com");
        userService.register(request);

        LoginRequest loginRequest = new LoginRequest("username", "password");
        LoginResult result = userService.login(loginRequest);

        assertEquals("username", result.username());
        assertNotNull(result.authToken());
        assertNotNull(dataAccess.getAuth(result.authToken()));
    }

    @Test
    void loginNegative() throws DataAccessException{
        MemoryDataAccess dataAccess = new MemoryDataAccess();
        UserService userService = new UserService(dataAccess);

        RegisterRequest registerRequest = new RegisterRequest("username", "password", "email@email.com");
        userService.register(registerRequest);

        LoginRequest loginRequest = new LoginRequest("username", "incorrect password");

        assertThrows(UnauthorizedException.class, () -> userService.login(loginRequest));
    }

    @Test
    void logoutPositive() throws DataAccessException{
        MemoryDataAccess dataAccess = new MemoryDataAccess();
        UserService userService = new UserService(dataAccess);

        RegisterRequest registerRequest = new RegisterRequest("username", "password", "email@email.com");
        userService.register(registerRequest);

        LoginRequest loginRequest = new LoginRequest("username", "password");
        LoginResult loginResult = userService.login(loginRequest);
        String authToken = loginResult.authToken();

        userService.logout(authToken);

        assertNull(dataAccess.getAuth(authToken));
    }

    @Test
    void logoutNegative() throws DataAccessException{
        MemoryDataAccess dataAccess = new MemoryDataAccess();
        UserService userService = new UserService(dataAccess);

        String fakeAuthToken = "fakeAuth";

        assertThrows(UnauthorizedException.class, () -> userService.logout(fakeAuthToken));
    }
}