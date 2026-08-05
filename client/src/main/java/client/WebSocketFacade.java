package client;

import jakarta.websocket.Session;

public class WebSocketFacade {
    Session session;

    public WebSocketFacade(String url) throws ResponseException {

    }

    public void connect() throws ResponseException {}
    public void makeMove() throws ResponseException {}
    public void resign() throws ResponseException {}
    public void leave () throws ResponseException {}
}
