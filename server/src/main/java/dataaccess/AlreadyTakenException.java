package dataaccess;

/**
 * Indicates some information has already been taken
 */
public class AlreadyTakenException extends DataAccessException{
    public AlreadyTakenException(String message) {
        super(message);
    }
    public AlreadyTakenException(String message, Throwable ex) {
        super(message, ex);
    }
}
