package service;

import chess.ChessGame;
import chess.ChessMove;
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

public class GamePlayService {
    private final DataAccess dataAccess;
    private final ConnectionManager connectionManager;

    private AuthData isAuthorized(String authToken) throws DataAccessException {
        AuthData auth = dataAccess.getAuth(authToken);

        if (auth == null){
            throw new UnauthorizedException("Unauthorized request.");
        }
        return auth;
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

    public GamePlayService(DataAccess dataAccess){
        this.dataAccess = dataAccess;
        this.connectionManager = new ConnectionManager();
    }

    public void connect(UserGameCommand command, Session session) throws DataAccessException {
        String authToken = command.getAuthToken();
        AuthData authData = isAuthorized(authToken);

        int gameID = command.getGameID();
        GameData game = dataAccess.getGame(gameID);
        if (game == null) {
            throw new BadRequestException("Game does not exist.");
        } else {
            Connection connection = createConnection(authData, game, session);
            connectionManager.addConnection(gameID, connection);
        }
    }

    public void makeMove(UserGameCommand command, Session session) throws DataAccessException {
        String authToken = command.getAuthToken();
        isAuthorized(authToken);
    }

    public void leave(UserGameCommand command, Session session) throws DataAccessException {
        String authToken = command.getAuthToken();
        isAuthorized(authToken);
    }

    public void resign(UserGameCommand command, Session session) throws DataAccessException {
        String authToken = command.getAuthToken();
        isAuthorized(authToken);
    }
}
