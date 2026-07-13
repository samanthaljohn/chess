package handler;

import com.google.gson.Gson;
import dataaccess.AlreadyTakenException;
import io.javalin.http.Context;
import dataaccess.DataAccessException;
import request.RegisterRequest;
import result.RegisterResult;
import service.UserService;

import java.util.Map;

public class UserHandler {
    private final UserService userService;

    public UserHandler(UserService userService){
        this.userService = userService;
    }

    public void register(Context context){
        RegisterRequest request = new Gson().fromJson(context.body(), RegisterRequest.class);

        if (request.username() == null || request.password() == null || request.email() == null) {
            context.status(400);
            context.contentType("application/json");
            context.result(new Gson().toJson(Map.of("message", "Error: bad request")));
            return;
        }

        try{
            RegisterResult result = userService.register(request);
            context.status(200);
            context.contentType("application/json");
            context.result(new Gson().toJson(result));
        } catch (AlreadyTakenException e){
            context.status(403);
            context.contentType("application/json");
            context.result(new Gson().toJson(Map.of("message", "Error: " + e.getMessage())));
        } catch (DataAccessException e) {
            context.status(500);
            context.contentType("application/json");
            context.result(new Gson().toJson(Map.of("message", "Error: " + e.getMessage())));
        }
    }
}
