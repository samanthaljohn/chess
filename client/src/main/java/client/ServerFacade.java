package client;

import com.google.gson.Gson;
import request.LoginRequest;
import request.RegisterRequest;
import result.LoginResult;
import result.RegisterResult;

import java.util.Map;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Locale;

public class ServerFacade {
    private String url;
    private static final HttpClient client = HttpClient.newHttpClient();

    public ServerFacade(int port){
        this.url = "http://localhost:" + port;
    }

    public void clear() throws Exception {
        String clearUrl = url + "/db";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(clearUrl))
                .DELETE()
                .build();

        HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
        int statusCode = httpResponse.statusCode();

        if (statusCode != 200){
            String message = (String) new Gson().fromJson(httpResponse.body(), Map.class).get("message");
            throw new ResponseException(message, statusCode);
        }
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

        HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
        int statusCode = httpResponse.statusCode();

        if (statusCode != 200){
            String message = (String) new Gson().fromJson(httpResponse.body(), Map.class).get("message");
            throw new ResponseException(message, statusCode);
        }

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

        HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
        int statusCode = httpResponse.statusCode();

        if (statusCode != 200){
            String message = (String) new Gson().fromJson(httpResponse.body(), Map.class).get("message");
            throw new ResponseException(message, statusCode);
        }

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

        HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
        int statusCode = httpResponse.statusCode();

        if (statusCode != 200){
            String message = (String) new Gson().fromJson(httpResponse.body(), Map.class).get("message");
            throw new ResponseException(message, statusCode);
        }
    }

}
