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

    private AuthData getAuth(String authToken) throws DataAccessException {
        AuthData authData = dataAccess.getAuth(authToken);

        if (authData == null){
            throw new UnauthorizedException("Unauthorized request.");
        }

        return authData;
    }

    private GameData getGame(int gameID) throws DataAccessException {
        GameData game = dataAccess.getGame(gameID);

        if (game == null) {
            throw new BadRequestException("Game does not exist.");
        }

        return game;
    }

    private Connection createConnection(AuthData auth, GameData game, Session session){
        String username = auth.username();

        String playerStatus;
        ChessGame.TeamColor color;

        if (game.whiteUsername().equals(username)){
            playerStatus = "PLAYER";
            color = ChessGame.TeamColor.WHITE;
        } else if (game.blackUsername().equals(username)){
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
        AuthData authData = getAuth(authToken);

        int gameID = command.getGameID();
        GameData game = getGame(gameID);

        Connection connection = createConnection(authData, game, session);
        connectionManager.addConnection(gameID, connection);

        LoadGameMessage loadGameMessage = new LoadGameMessage(ServerMessage.ServerMessageType.LOAD_GAME, game.game());
        connectionManager.notifyRoot(loadGameMessage, connection);

        String notification = generateConnectMessage(connection);
        NotificationMessage notificationMessage = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, notification);
        connectionManager.notifyAllButRoot(gameID, notificationMessage, connection);
    }

    public void makeMove(UserGameCommand command, Session session) throws DataAccessException {
        String authToken = command.getAuthToken();
        getAuth(authToken);
    }

    public void leave(UserGameCommand command, Session session) throws DataAccessException {
        String authToken = command.getAuthToken();
        getAuth(authToken);

        int gameID = command.getGameID();

    }

    public void resign(UserGameCommand command, Session session) throws DataAccessException {
        String authToken = command.getAuthToken();
        getAuth(authToken);
    }
}
