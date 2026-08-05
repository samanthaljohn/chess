package client;

public class ResponseException extends Exception {
    private int statusCode;

    public ResponseException(String message){
        super(message);
    }

    public ResponseException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
