package client;

public class ServerFacade {
    private String url;

    public ServerFacade(int port){
        this.url = "http://localhost:" + port;
    }
}
