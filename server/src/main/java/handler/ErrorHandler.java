package handler;

import io.javalin.http.Context;

import java.util.Map;

public class ErrorHandler {
    public void handleError(Context context, int statusCode, String message){
        context.status(statusCode);
        context.json(Map.of("message", message));
    }
}
