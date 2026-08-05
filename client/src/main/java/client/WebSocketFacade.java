package client;

import chess.ChessMove;
import com.google.gson.Gson;
import jakarta.websocket.*;
import ui.NotificationHandler;

import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.net.URI;

public class WebSocketFacade extends Endpoint {
    Session session;
    private final String url;
    NotificationHandler notificationHandler;

    private void sendJson(String json) throws ResponseException {
        try {
            session.getBasicRemote().sendText(json);
        } catch (Exception e) {
            throw new ResponseException(e.getMessage());
        }
    }

    public WebSocketFacade(int port, NotificationHandler notificationHandler) throws ResponseException {
        this.url = "http://localhost:" + port;
        this.notificationHandler = notificationHandler;

        try {
            String webSocketUrl = url.replace("http", "ws");
            URI socketURI = new URI(webSocketUrl + "/ws");

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURI);

            this.session.addMessageHandler(new MessageHandler.Whole<String>() {
                public void onMessage(String message) {
                    ServerMessage.ServerMessageType type = new Gson().fromJson(message, ServerMessage.class).getServerMessageType();
                    switch (type) {
                        case LOAD_GAME:
                            LoadGameMessage loadGameMessage = new Gson().fromJson(message, LoadGameMessage.class);
                            notificationHandler.loadGame(loadGameMessage);
                            break;
                        case NOTIFICATION:
                            NotificationMessage notification = new Gson().fromJson(message, NotificationMessage.class);
                            notificationHandler.notificationMessage(notification);
                            break;
                        case ERROR:
                            ErrorMessage errorMessage = new Gson().fromJson(message, ErrorMessage.class);
                            notificationHandler.error(errorMessage);
                            break;
                    };
                }
            });
        } catch (Exception e) {
            throw new ResponseException(e.getMessage());
        }
    }

    public void connect(String authToken, int gameID) throws ResponseException {
        UserGameCommand command = new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID);
        String json = new Gson().toJson(command);

        sendJson(json);
    }

    public void makeMove(String authToken, int gameID, ChessMove move) throws ResponseException {
        MakeMoveCommand command = new MakeMoveCommand(UserGameCommand.CommandType.MAKE_MOVE, authToken, gameID, move);
        String json = new Gson().toJson(command);

        sendJson(json);
    }

    public void resign(String authToken, int gameID) throws ResponseException {
        UserGameCommand command = new UserGameCommand(UserGameCommand.CommandType.RESIGN, authToken, gameID);
        String json = new Gson().toJson(command);

        sendJson(json);
    }
    public void leave (String authToken, int gameID) throws ResponseException {
        UserGameCommand command = new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, gameID);
        String json = new Gson().toJson(command);

        sendJson(json);
    }

    @Override
    public void onOpen(Session session, EndpointConfig config) {
    }
}
