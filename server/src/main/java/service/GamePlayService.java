package service;

import chess.ChessMove;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.UnauthorizedException;
import jakarta.websocket.Session;
import model.AuthData;
import websocket.commands.UserGameCommand;

public class GamePlayService {
    private final DataAccess dataAccess;

    private void isAuthorized(String authToken) throws DataAccessException {
        AuthData auth = dataAccess.getAuth(authToken);

        if (auth == null){
            throw new UnauthorizedException("Unauthorized request.");
        }
    }

    public GamePlayService(DataAccess dataAccess){
        this.dataAccess = dataAccess;
    }

    public void connect(UserGameCommand command, Session session) throws DataAccessException {
        String authToken = command.getAuthToken();
        isAuthorized(authToken);
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
