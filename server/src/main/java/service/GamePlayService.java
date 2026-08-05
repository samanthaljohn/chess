package service;

import chess.ChessGame;
import chess.ChessMove;
import chess.InvalidMoveException;
import dataaccess.BadRequestException;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.UnauthorizedException;
import org.eclipse.jetty.websocket.api.Session;
import model.AuthData;
import model.GameData;
import service.connection.Connection;
import service.connection.ConnectionManager;
import websocket.commands.MakeMoveCommand;
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

    private ChessGame.TeamColor getOppositeTeamColor(ChessGame.TeamColor color) {
        switch (color){
            case WHITE: return ChessGame.TeamColor.BLACK;
            case BLACK: return ChessGame.TeamColor.WHITE;
        }
        return null;
    }

    private String isAtRiskMessage(ChessGame.TeamColor color, ChessGame game){
        if (game.isInCheckmate(color)){
            return color + " is in checkmate";
        } else if (game.isInCheck(color)){
            return color + " is in check";
        } else if (game.isInStalemate(color)){
            return "Game is in stalemate";
        } else {
            return "";
        }
    }

    private GameData createResignedGameData(AuthData auth, GameData gameData) {
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

    private GameData createNewGameData(GameData data){
        return new GameData(data.gameID(), data.whiteUsername(), data.blackUsername(), data.gameName(), data.game());
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

    private String generateMoveMessage(Connection connection, ChessMove move){
        String username = connection.username();
        return username + " has made move: " + move.toString();
    }

    public void reportErrorToRoot(ServerMessage message, Session session) {
        connectionManager.notifyRoot(message, session);
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
        connectionManager.notifyRoot(loadGameMessage, session);

        String notification = generateConnectMessage(connection);
        NotificationMessage notificationMessage = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, notification);
        connectionManager.notifyAllButRoot(gameID, notificationMessage, connection);
    }

    public void makeMove(MakeMoveCommand command, Session session) throws DataAccessException, InvalidMoveException {
        String authToken = command.getAuthToken();
        AuthData authData = getAuthData(authToken);

        int gameID = command.getGameID();
        GameData gameData = getGameData(gameID);
        ChessGame game = gameData.game();

        Connection connection = createConnection(authData, gameData, session);
        if(connection.playerStatus().equals("OBSERVER")){
            throw new BadRequestException("Error: Observers may not make moves.");
        }
        if (connection.playerColor() != game.getTeamTurn()) {
            throw new BadRequestException("Error: It is not your turn");
        }

        ChessMove move = command.getMove();
        game.makeMove(move);
        GameData newGameData = createNewGameData(gameData);
        dataAccess.updateGame(newGameData);

        LoadGameMessage loadGameMessage = new LoadGameMessage(ServerMessage.ServerMessageType.LOAD_GAME, newGameData.game());
        connectionManager.notifyAll(gameID, loadGameMessage);

        String moveMessage = generateMoveMessage(connection, move);
        NotificationMessage moveNotificationMessage = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, moveMessage);
        connectionManager.notifyAllButRoot(gameID, moveNotificationMessage, connection);

        ChessGame.TeamColor atRiskColor = getOppositeTeamColor(connection.playerColor());
        String riskMessage = isAtRiskMessage(atRiskColor, newGameData.game());
        if (!riskMessage.isEmpty()){
            NotificationMessage riskNotificationMessage = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, riskMessage);
            connectionManager.notifyAll(gameID, riskNotificationMessage);
        }
    }

    public void leave(UserGameCommand command, Session session) throws DataAccessException {
        String authToken = command.getAuthToken();
        AuthData authData = getAuthData(authToken);

        int gameID = command.getGameID();
        GameData gameData = getGameData(gameID);

        GameData newGameData = createResignedGameData(authData, gameData);
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
            throw new BadRequestException("Error: Game is already over.");
        }

        Connection connection = createConnection(authData, gameData, session);

        if (connection.playerStatus().equals("OBSERVER")){
            throw new BadRequestException("Error: Observers may not resign.");
        }

        game.setGameOver(true);
        GameData newGameData = createNewGameData(gameData);
        dataAccess.updateGame(newGameData);

        NotificationMessage notificationMessage = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, authData.username() + " has resigned");
        connectionManager.notifyAll(gameID, notificationMessage);
    }
}
