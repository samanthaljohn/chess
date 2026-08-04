package service.connection;

import websocket.messages.ServerMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class ConnectionManager {
    private final Map<Integer, List<Connection>> connections;

    private boolean gameHasConnections(int gameID){
        if (connections.get(gameID) == null){
            return false;
        }
        return true;
    }

    private boolean connectionInConnections(int gameID, Connection connection){
       if (connections.get(gameID).contains(connection)){
           return true;
       }
       return false;
    }

    public ConnectionManager(){
        this.connections = new HashMap<>();
    }

    public void addConnection(int gameID, Connection connection){
        if (gameHasConnections(gameID)) {
            connections.get(gameID).add(connection);

        } else {
            List<Connection> newConnectionList = new ArrayList<>();
            newConnectionList.add(connection);

            connections.put(gameID, newConnectionList);
        }
    }

    public void removeConnection(int gameID, Connection connection) {
        if (gameHasConnections(gameID) && connectionInConnections(gameID, connection)){
            connections.get(gameID).remove(connection);
        }
    }

    public void notify(int gameID, Connection connection, ServerMessage message) {

    }
}
