package service;

import dataaccess.AlreadyTakenException;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.UserData;
import request.RegisterRequest;
import result.RegisterResult;

import java.util.UUID;

public class UserService {
    private DataAccess dataAccess;

    public UserService(DataAccess dataAccess){
        this.dataAccess = dataAccess;
    }

    public static String generateAuthToken() {
        return UUID.randomUUID().toString();
    }

    public RegisterResult register(RegisterRequest registerRequest) throws DataAccessException {
        String username = registerRequest.username(), password = registerRequest.password(), email = registerRequest.email();

        if (dataAccess.getUser(username) != null){
            throw new AlreadyTakenException("already taken");
        }
        UserData userData = new UserData(username, password, email);
        dataAccess.createUser(userData);

        String authToken = generateAuthToken();
        if (dataAccess.getAuth(authToken) != null){
            throw new AlreadyTakenException("already taken");
        }
        AuthData authData = new AuthData(authToken, username);
        dataAccess.createAuth(authData);

        RegisterResult registerResult = new RegisterResult(username, authToken);
        return registerResult;
    }

//    public LoginResult login(LoginRequest loginRequest) {}
//    public void logout(LogoutRequest logoutRequest) {}

}
