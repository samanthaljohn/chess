package client;

import com.google.gson.Gson;
import request.CreateGameRequest;
import request.JoinGameRequest;
import request.LoginRequest;
import request.RegisterRequest;
import result.CreateGameResult;
import result.ListGamesResult;
import result.LoginResult;
import result.RegisterResult;

import java.util.Map;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ServerFacade {
    private String url;
    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    public ServerFacade(int port){
        this.url = "http://localhost:" + port;
    }

    private HttpResponse<String> checkStatusCode(HttpRequest request) throws Exception {
        // check status code and throw error, if not return response body
        HttpResponse<String> httpResponse = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        int statusCode = httpResponse.statusCode();

        if (statusCode != 200){
            String message = (String) new Gson().fromJson(httpResponse.body(), Map.class).get("message");
            throw new ResponseException(message, statusCode);
        }

        return httpResponse;
    }

    public void clear() throws Exception {
        String clearUrl = url + "/db";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(clearUrl))
                .DELETE()
                .build();

        checkStatusCode(request);
    }

    public RegisterResult register(String username, String password, String email) throws Exception{
        String registerUrl = url + "/user";

        RegisterRequest registerRequest = new RegisterRequest(username, password, email);
        String json = new Gson().toJson(registerRequest);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(registerUrl))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> httpResponse = checkStatusCode(request);

        RegisterResult registerResult = new Gson().fromJson(httpResponse.body(), RegisterResult.class);
        return registerResult;
    }

    public LoginResult login(String username, String password) throws Exception{
        String loginUrl = url + "/session";

        LoginRequest loginRequest = new LoginRequest(username, password);
        String json = new Gson().toJson(loginRequest);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(loginUrl))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> httpResponse = checkStatusCode(request);

        LoginResult loginResult = new Gson().fromJson(httpResponse.body(), LoginResult.class);
        return loginResult;
    }

    public void logout(String authToken) throws Exception{
        String logoutUrl = url + "/session";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(logoutUrl))
                .DELETE()
                .header("authorization", authToken)
                .build();

        checkStatusCode(request);
    }

    public CreateGameResult createGame(String authToken, String gameName) throws Exception{
        String createGameUrl = url + "/game";

        CreateGameRequest createGameRequest = new CreateGameRequest(gameName);
        String json = new Gson().toJson(createGameRequest);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(createGameUrl))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .header("authorization", authToken)
                .build();

        HttpResponse<String> httpResponse = checkStatusCode(request);

        CreateGameResult createGameResult = new Gson().fromJson(httpResponse.body(), CreateGameResult.class);
        return createGameResult;
    }

    public ListGamesResult listGames(String authToken) throws Exception{
        String listGamesUrl = url + "/game";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(listGamesUrl))
                .GET()
                .header("authorization", authToken)
                .build();

        HttpResponse<String> httpResponse = checkStatusCode(request);

        ListGamesResult listGamesResult = new Gson().fromJson(httpResponse.body(), ListGamesResult.class);
        return listGamesResult;
    }

    public void joinGame(String authToken, String playerColor, int gameID) throws Exception{
        String joinGameUrl = url + "/game";

        JoinGameRequest joinGameRequest = new JoinGameRequest(playerColor, gameID);
        String json = new Gson().toJson(joinGameRequest);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(joinGameUrl))
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .header("authorization", authToken)
                .build();

        checkStatusCode(request);
    }

}
