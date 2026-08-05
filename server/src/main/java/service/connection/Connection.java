package service.connection;

import chess.ChessGame;
import org.eclipse.jetty.websocket.api.Session;

public record Connection(
        String username,
        String playerStatus,
        ChessGame.TeamColor playerColor,
        Session session
) { }
