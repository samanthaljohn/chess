package handler;

import com.google.gson.Gson;
import io.javalin.http.Context;

import dataaccess.AlreadyTakenException;
import dataaccess.UnauthorizedException;
import dataaccess.DataAccessException;

import request.LoginRequest;
import request.RegisterRequest;
import result.LoginResult;
import result.RegisterResult;

import service.UserService;

import java.util.Map;

public class UserHandler extends ErrorHandler {
    private final UserService userService;

    public UserHandler(UserService userService){
        this.userService = userService;
    }

    public void register(Context context){
        RegisterRequest registerRequest = new Gson().fromJson(context.body(), RegisterRequest.class);

        if (registerRequest.username() == null || registerRequest.password() == null || registerRequest.email() == null) {
            handleError(context, 400, "Error: bad request");
            return;
        }

        try{
            RegisterResult result = userService.register(registerRequest);
            context.status(200);
            context.json(result);
        } catch (AlreadyTakenException e){
            handleError(context, 403, "Error: " + e.getMessage());
        } catch (DataAccessException e) {
            handleError(context, 500, "Error: " + e.getMessage());
        }
    }

    public void login(Context context){
        LoginRequest loginRequest = new Gson().fromJson(context.body(), LoginRequest.class);

        if (loginRequest.username() == null || loginRequest.password() == null){
            handleError(context, 400, "Error: bad request");
            return;
        }

        try {
            LoginResult result = userService.login(loginRequest);
            context.status(200);
            context.json(result);
        } catch (UnauthorizedException e){
            handleError(context, 401, "Error: " + e.getMessage());
        } catch (DataAccessException e) {
            handleError(context, 500, "Error: " + e.getMessage());
        }
    }

    public void logout(Context context){
        String authToken = context.header("authorization");

        try {
            userService.logout(authToken);
            context.contentType("application/json");
            context.result(new Gson().toJson(Map.of()));
        } catch (UnauthorizedException e){
            handleError(context, 401, "Error: " + e.getMessage());
        } catch (DataAccessException e){
            handleError(context, 500, "Error: " + e.getMessage());
        }
    }
}
