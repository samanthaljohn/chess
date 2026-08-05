package client;

import handler.GamePlayHandler;
import org.eclipse.jetty.websocket.api.Session;
import service.GamePlayService;

public class WebSocketFacade {
    Session session;
    GamePlayHandler gamePlayHandler;

    public WebSocketFacade(String url) throws ResponseException {

    }

    public void connect() throws ResponseException { ... }
    public void makeMove() throws ResponseException { ... }
    public void resign() throws ResponseException { ... }
    public void leave () throws ResponseException { ... }
}
