package handler;

import chess.ChessMove;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import io.javalin.websocket.*;
import org.eclipse.jetty.websocket.api.Session;
import service.GamePlayService;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;

public class GamePlayHandler implements WsConnectHandler, WsMessageHandler, WsCloseHandler {
    private final GamePlayService gamePlayService;

    public GamePlayHandler(GamePlayService gamePlayService) {
        this.gamePlayService = gamePlayService;
    }

    private void callCommand(UserGameCommand command, Session session) throws DataAccessException, InvalidMoveException {
        UserGameCommand.CommandType commandType = command.getCommandType();
        switch (commandType) {
            case CONNECT:
                gamePlayService.connect(command, session);
                break;
            case MAKE_MOVE:
                gamePlayService.makeMove((MakeMoveCommand) command, session);
                break;
            case LEAVE:
                gamePlayService.leave(command, session);
                break;
            case RESIGN:
                gamePlayService.resign(command, session);
                break;
        }
    }

    @Override
    public void handleConnect(WsConnectContext context) {
        context.enableAutomaticPings();
    }

    @Override
    public void handleMessage(WsMessageContext context) {
        UserGameCommand command = new Gson().fromJson(context.message(), UserGameCommand.class);
    }

    @Override
    public void handleClose(WsCloseContext context) {
        System.out.println("Websocket closed");
    }
}
