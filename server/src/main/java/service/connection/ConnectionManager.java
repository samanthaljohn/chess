package service.connection;

import com.google.gson.Gson;
import jakarta.websocket.Session;
import websocket.messages.ServerMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class ConnectionManager {
    private final Map<Integer, List<Connection>> gameConnections;

    private boolean gameHasConnections(int gameID){
        if (gameConnections.get(gameID) == null){
            return false;
        }
        return true;
    }

    private boolean connectionInConnections(int gameID, Connection connection){
       if (gameConnections.get(gameID).contains(connection)){
           return true;
       }
       return false;
    }

    public ConnectionManager(){
        this.gameConnections = new HashMap<>();
    }

    public void addConnection(int gameID, Connection connection){
        if (gameHasConnections(gameID)) {
            gameConnections.get(gameID).add(connection);

        } else {
            List<Connection> connections = new ArrayList<>();
            connections.add(connection);

            gameConnections.put(gameID, connections);
        }
    }

    public void removeConnection(int gameID, Connection connection) {
        if (gameHasConnections(gameID) && connectionInConnections(gameID, connection)){
            gameConnections.get(gameID).remove(connection);
        }
    }

    public void notifyAllButOne(int gameID, ServerMessage message, Connection connectionToExclude) throws Exception {
        String json = new Gson().toJson(message);

        List<Connection> connections = gameConnections.get(gameID);
        if (connections == null){
            return;
        }

        for (Connection existingConnection : connections){
            Session session = existingConnection.session();
            if (!existingConnection.equals(connectionToExclude)) {
                try {
                    session.getBasicRemote().sendText(json);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }

    public void notifyOne(ServerMessage message, Connection connectionToSend) throws Exception {
        String json = new Gson().toJson(message);

        Session session = connectionToSend.session();
        try {
            session.getBasicRemote().sendText(json);
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
}
