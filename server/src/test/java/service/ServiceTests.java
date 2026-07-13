package service;

import chess.ChessGame;
import dataaccess.*;

import model.AuthData;
import model.GameData;
import model.PublicGameData;
import model.UserData;

import request.CreateGameRequest;
import request.JoinGameRequest;
import request.LoginRequest;
import request.RegisterRequest;
import result.CreateGameResult;
import result.ListGamesResult;
import result.LoginResult;
import result.RegisterResult;

import org.junit.jupiter.api.Test;
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

    @Test
    void listGamesPositive() throws DataAccessException{
        MemoryDataAccess dataAccess = new MemoryDataAccess();
        UserService userService = new UserService(dataAccess);
        GameService gameService = new GameService(dataAccess);

        RegisterRequest registerRequest = new RegisterRequest("username", "password", "email");
        userService.register(registerRequest);
        LoginRequest loginRequest = new LoginRequest("username", "password");
        LoginResult loginResult = userService.login(loginRequest);
        String authToken = loginResult.authToken();

        int gameID1 = dataAccess.createGame("game1");
        int gameID2 = dataAccess.createGame("game2");

        PublicGameData game1 = new PublicGameData(gameID1, null, null, "game1");
        PublicGameData game2 = new PublicGameData(gameID2, null, null, "game2");

        ListGamesResult listGamesResult = gameService.listGames(authToken);

        assertEquals(2, listGamesResult.games().size());
        assertTrue(listGamesResult.games().contains(game1));
        assertTrue(listGamesResult.games().contains(game2));

    }

    @Test
    void listGamesNegative() throws DataAccessException {
        MemoryDataAccess dataAccess = new MemoryDataAccess();
        GameService gameService = new GameService(dataAccess);

        String fakeAuthToken = "fakeAuth";

        assertThrows(UnauthorizedException.class, () -> gameService.listGames(fakeAuthToken));
    }

    @Test
    void createGamePositive() throws DataAccessException {
        MemoryDataAccess dataAccess = new MemoryDataAccess();
        UserService userService = new UserService(dataAccess);
        GameService gameService = new GameService(dataAccess);

        RegisterRequest registerRequest = new RegisterRequest("username", "password", "email");
        userService.register(registerRequest);
        LoginRequest loginRequest = new LoginRequest("username", "password");
        LoginResult loginResult = userService.login(loginRequest);
        String authToken = loginResult.authToken();

        CreateGameRequest createGameRequest = new CreateGameRequest("myGame");
        CreateGameResult createGameResult = gameService.createGame(authToken, createGameRequest);
        int gameID = createGameResult.gameID();
        GameData game = new GameData(gameID, null, null, "myGame", new ChessGame());

        assertEquals(1, gameID);
        assertEquals(game, dataAccess.getGame(gameID));
    }

    @Test
    void createGameNegative() throws DataAccessException{
        MemoryDataAccess dataAccess = new MemoryDataAccess();
        GameService gameService = new GameService(dataAccess);

        String fakeAuthToken = "fakeAuth";
        CreateGameRequest createGameRequest = new CreateGameRequest("myGame");

        assertThrows(UnauthorizedException.class, () -> gameService.createGame(fakeAuthToken, createGameRequest));
    }

    @Test
    void joinGamePositive() throws DataAccessException {
        MemoryDataAccess dataAccess = new MemoryDataAccess();
        UserService userService = new UserService(dataAccess);
        GameService gameService = new GameService(dataAccess);

        RegisterRequest registerRequest = new RegisterRequest("username", "password", "email");
        userService.register(registerRequest);
        LoginRequest loginRequest = new LoginRequest("username", "password");
        LoginResult loginResult = userService.login(loginRequest);
        String authToken = loginResult.authToken();

        CreateGameRequest createGameRequest = new CreateGameRequest("myGame");
        CreateGameResult createGameResult = gameService.createGame(authToken, createGameRequest);
        int gameID = createGameResult.gameID();

        JoinGameRequest joinGameRequest = new JoinGameRequest("WHITE", gameID);
        gameService.joinGame(authToken, joinGameRequest);

        GameData game = dataAccess.getGame(gameID);

        assertEquals("username", game.whiteUsername());
        assertEquals(null, game.blackUsername());
    }

    @Test
    void joinGameNegative() throws DataAccessException{
        MemoryDataAccess dataAccess = new MemoryDataAccess();
        UserService userService = new UserService(dataAccess);
        GameService gameService = new GameService(dataAccess);

        RegisterRequest registerRequest = new RegisterRequest("username", "password", "email");
        userService.register(registerRequest);
        LoginRequest loginRequest = new LoginRequest("username", "password");
        LoginResult loginResult = userService.login(loginRequest);
        String authToken = loginResult.authToken();

        JoinGameRequest joinGameRequest = new JoinGameRequest("WHITE", 1);

        assertThrows(BadRequestException.class, () -> gameService.joinGame(authToken, joinGameRequest));
    }
}