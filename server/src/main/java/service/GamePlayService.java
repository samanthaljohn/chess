package service;

import chess.ChessMove;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.MySqlDataAccess;
import dataaccess.UnauthorizedException;
import jakarta.websocket.Session;
import model.AuthData;

public class GamePlayService {
    private final DataAccess dataAccess;
    private final GameService gameService;

    private void isAuthorized(String authToken) throws DataAccessException {
        AuthData auth = dataAccess.getAuth(authToken);

        if (auth == null){
            throw new UnauthorizedException("Unauthorized request.");
        }
    }

    public GamePlayService(DataAccess dataAccess, GameService gameService){
        this.dataAccess = dataAccess;
        this.gameService = gameService;
    }

    public void connect(String authToken, Integer gameID, Session session) throws DataAccessException{
        isAuthorized(authToken);
    }

    public void makeMove(String authToken, Integer gameID, ChessMove move, Session session) throws DataAccessException{
        isAuthorized(authToken);
    }

    public void leave(String authToken, Integer gameID, Session session) throws DataAccessException{
        isAuthorized(authToken);
    }

    public void resign(String authToken, Integer gameID, Session session) throws DataAccessException{
        isAuthorized(authToken);
    }
}
