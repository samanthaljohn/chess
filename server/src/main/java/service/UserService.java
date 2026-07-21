package service;

import dataaccess.AlreadyTakenException;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.UnauthorizedException;

import model.AuthData;
import model.UserData;

import org.mindrot.jbcrypt.BCrypt;
import request.LoginRequest;
import request.RegisterRequest;
import result.LoginResult;
import result.RegisterResult;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

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
        AuthData authData = new AuthData(authToken, username);
        dataAccess.createAuth(authData);

        RegisterResult registerResult = new RegisterResult(username, authToken);
        return registerResult;
    }

    public LoginResult login(LoginRequest loginRequest) throws DataAccessException{
        String username = loginRequest.username(), password = loginRequest.password();
        UserData user = dataAccess.getUser(username);
        if (user == null || !BCrypt.checkpw(password, user.password())){
            throw new UnauthorizedException("unauthorized");
        }

        String authToken = generateAuthToken();
        AuthData authData = new AuthData(authToken, username);
        dataAccess.createAuth(authData);

        LoginResult loginResult = new LoginResult(username, authToken);
        return loginResult;
    }

    public void logout(String authToken) throws DataAccessException {
        AuthData auth = dataAccess.getAuth(authToken);

        if (auth == null) {
            throw new UnauthorizedException("unauthorized");
        }

        dataAccess.deleteAuth(authToken);
    }

}
