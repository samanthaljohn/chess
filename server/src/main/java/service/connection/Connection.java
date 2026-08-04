package service.connection;

import chess.ChessGame;
import jakarta.websocket.Session;

public record Connection(
        String username,
        String playerStatus,
        ChessGame.TeamColor playerColor,
        Session session
) { }
