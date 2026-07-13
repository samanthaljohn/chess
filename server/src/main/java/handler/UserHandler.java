package handler;

import com.google.gson.Gson;
import dataaccess.AlreadyTakenException;
import dataaccess.UnauthorizedException;
import io.javalin.http.Context;
import dataaccess.DataAccessException;
import request.LoginRequest;
import request.RegisterRequest;
import result.LoginResult;
import result.RegisterResult;
import service.UserService;

import javax.xml.crypto.Data;
import java.util.Map;

public class UserHandler {
    private final UserService userService;

    public UserHandler(UserService userService){
        this.userService = userService;
    }

    public void register(Context context){
        RegisterRequest registerRequest = new Gson().fromJson(context.body(), RegisterRequest.class);

        if (registerRequest.username() == null || registerRequest.password() == null || registerRequest.email() == null) {
            context.status(400);
            context.contentType("application/json");
            context.result(new Gson().toJson(Map.of("message", "Error: bad request")));
            return;
        }

        try{
            RegisterResult result = userService.register(registerRequest);
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

    public void login(Context context){
        LoginRequest loginRequest = new Gson().fromJson(context.body(), LoginRequest.class);

        if (loginRequest.username() == null || loginRequest.password() == null){
            context.status(400);
            context.contentType("application/json");
            context.result(new Gson().toJson(Map.of("message", "Error: bad request")));
            return;
        }

        try {
            LoginResult result = userService.login(loginRequest);
            context.status(200);
            context.contentType("application/json");
            context.result(new Gson().toJson(result));
        } catch (UnauthorizedException e){
            context.status(401);
            context.contentType("application/json");
            context.result(new Gson().toJson(Map.of("message", "Error: " + e.getMessage())));
        } catch (DataAccessException e) {
            context.status(500);
            context.contentType("application/json");
            context.result(new Gson().toJson(Map.of("message", "Error: " + e.getMessage())));
        }
    }

    public void logout(Context context){
        String authToken = context.header("authorization");

        try {
            userService.logout(authToken);
            context.contentType("application/json");
            context.result(new Gson().toJson(Map.of()));
        } catch (UnauthorizedException e){
            context.status(401);
            context.contentType("application/json");
            context.result(new Gson().toJson(Map.of("message", "Error: " + e.getMessage())));
        } catch (DataAccessException e){
            context.status(500);
            context.contentType("application/json");
            context.result(new Gson().toJson(Map.of("message", "Error: " + e.getMessage())));
        }
    }
}
