package service;

import chess.ChessGame;
import dataaccess.BadRequestException;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.UnauthorizedException;
import jakarta.websocket.Session;
import model.AuthData;
import model.GameData;
import service.connection.Connection;
import service.connection.ConnectionManager;
import websocket.commands.UserGameCommand;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

public class GamePlayService {
    private final DataAccess dataAccess;
    private final ConnectionManager connectionManager;

    private AuthData getAuthData(String authToken) throws DataAccessException {
        AuthData authData = dataAccess.getAuth(authToken);

        if (authData == null){
            throw new UnauthorizedException("Unauthorized request.");
        }

        return authData;
    }

    private GameData getGameData(int gameID) throws DataAccessException {
        GameData game = dataAccess.getGame(gameID);

        if (game == null) {
            throw new BadRequestException("Game does not exist.");
        }

        return game;
    }

    private GameData createUpdatedGameData(AuthData auth, GameData gameData) {
        String username = auth.username();

        String white = gameData.whiteUsername();
        String black = gameData.blackUsername();

        if (white.equals(username)){
            white = null;
        } else if (black.equals(username)){
            black = null;
        } else {
            return null;
        }

        return new GameData(gameData.gameID(), white, black, gameData.gameName(), gameData.game());
    }

    private Connection createConnection(AuthData auth, GameData gameData, Session session){
        String username = auth.username();

        String playerStatus;
        ChessGame.TeamColor color;

        if (gameData.whiteUsername().equals(username)){
            playerStatus = "PLAYER";
            color = ChessGame.TeamColor.WHITE;
        } else if (gameData.blackUsername().equals(username)){
            playerStatus = "PLAYER";
            color = ChessGame.TeamColor.BLACK;
        } else {
            playerStatus = "OBSERVER";
            color = null;
        }

        return new Connection(username, playerStatus, color, session);
    }

    private String generateConnectMessage(Connection connection) {
        String username = connection.username();
        String outputMessage = username + " joined the game as ";

        String playerStatus = connection.playerStatus();
        ChessGame.TeamColor playerColor = connection.playerColor();
        if (playerStatus.equals("PLAYER")){
            outputMessage = outputMessage + playerColor + ".";
        } else {
            outputMessage = outputMessage + "an observer.";
        }

        return outputMessage;
    }

    public GamePlayService(DataAccess dataAccess){
        this.dataAccess = dataAccess;
        this.connectionManager = new ConnectionManager();
    }

    public void connect(UserGameCommand command, Session session) throws DataAccessException {
        String authToken = command.getAuthToken();
        AuthData authData = getAuthData(authToken);

        int gameID = command.getGameID();
        GameData gameData = getGameData(gameID);

        Connection connection = createConnection(authData, gameData, session);
        connectionManager.addConnection(gameID, connection);

        LoadGameMessage loadGameMessage = new LoadGameMessage(ServerMessage.ServerMessageType.LOAD_GAME, gameData.game());
        connectionManager.notifyRoot(loadGameMessage, connection);

        String notification = generateConnectMessage(connection);
        NotificationMessage notificationMessage = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, notification);
        connectionManager.notifyAllButRoot(gameID, notificationMessage, connection);
    }

    public void makeMove(UserGameCommand command, Session session) throws DataAccessException {
        String authToken = command.getAuthToken();
        getAuthData(authToken);
    }

    public void leave(UserGameCommand command, Session session) throws DataAccessException {
        String authToken = command.getAuthToken();
        AuthData authData = getAuthData(authToken);

        int gameID = command.getGameID();
        GameData gameData = getGameData(gameID);

        GameData newGameData = createUpdatedGameData(authData, gameData);
        if (newGameData != null){
            dataAccess.updateGame(newGameData);
        }

        Connection connection = createConnection(authData, gameData, session);

        NotificationMessage notificationMessage = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, authData.username() + " left the game");
        connectionManager.notifyAllButRoot(gameID, notificationMessage, connection);

        connectionManager.removeConnection(gameID, connection);
    }

    public void resign(UserGameCommand command, Session session) throws DataAccessException {
        String authToken = command.getAuthToken();
        AuthData authData = getAuthData(authToken);

        int gameID = command.getGameID();
        GameData gameData = getGameData(gameID);

        ChessGame game = gameData.game();

        if (game.getGameOver()) {
            throw new BadRequestException("Game is already over.");
        }

        Connection connection = createConnection(authData, gameData, session);

        if (connection.playerStatus().equals("OBSERVER")){
            throw new BadRequestException("Observers may not resign.");
        }

        game.setGameOver(true);
        GameData newGameData = new GameData(gameID, gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName(), game);
        dataAccess.updateGame(newGameData);

        NotificationMessage notificationMessage = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, authData.username() + " has resigned");
        connectionManager.notifyAll(gameID, notificationMessage);
    }
}
