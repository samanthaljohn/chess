package service.connection;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class ConnectionManager {
    private final Map<Integer, List<Connection>> gameConnections;

    private boolean gameHasConnections(int gameID) {
        if (gameConnections.get(gameID) == null) {
            return false;
        }
        return true;
    }

    public boolean connectionInConnections(int gameID, Connection connection) {
        if (gameConnections.get(gameID).contains(connection)) {
            return true;
        }
        return false;
    }

    public ConnectionManager() {
        this.gameConnections = new HashMap<>();
    }

    public void addConnection(int gameID, Connection connection) {
        if (gameHasConnections(gameID)) {
            gameConnections.get(gameID).add(connection);

        } else {
            List<Connection> connections = new ArrayList<>();
            connections.add(connection);

            gameConnections.put(gameID, connections);
        }
    }

    public void removeConnection(int gameID, Connection connection) {
        if (gameHasConnections(gameID) && connectionInConnections(gameID, connection)) {
            gameConnections.get(gameID).remove(connection);
        }
    }

    public void notifyRoot(ServerMessage message, Session session) {
        String json = new Gson().toJson(message);

        try {
            session.getRemote().sendString(json);
        } catch (Exception e){}
    }

    public void notifyAll(int gameID, ServerMessage message) {
        String json = new Gson().toJson(message);

        List<Connection> connections = gameConnections.get(gameID);
        if (connections == null) {
            return;
        }

        for (Connection existingConnection : connections){
            Session session = existingConnection.session();
            try {
                session.getRemote().sendString(json);
            } catch (Exception e){}
        }
    }


    public void notifyAllButRoot(int gameID, ServerMessage message, Connection connectionToExclude) {
        String json = new Gson().toJson(message);

        List<Connection> connections = gameConnections.get(gameID);
        if (connections == null) {
            return;
        }

        for (Connection existingConnection : connections) {
            Session session = existingConnection.session();
            if (!existingConnection.equals(connectionToExclude)) {
                try {
                    session.getRemote().sendString(json);
                } catch (Exception e) {}
            }
        }
    }
}
