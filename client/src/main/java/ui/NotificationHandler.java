package ui;

import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;

public interface NotificationHandler {
    void loadGame(LoadGameMessage message);
    void notificationMessage(NotificationMessage message);
    void error(ErrorMessage message);
}
