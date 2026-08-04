package service.connection;

import websocket.messages.ServerMessage;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class ConnectionManager {
    private final Map<Integer, List<Connection>> connections;

    public ConnectionManager(){
        this.connections = new HashMap<>();
    }

    public void addConnection(int gameID, Connection connection){

    }

    public void removeConnection(int gameID, Connection connection){

    }

    public void notify(int gameID, Connection connection, ServerMessage message) {

    }
}
