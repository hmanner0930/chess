package dataaccess;

public class DataAccessException extends Exception{
    public DataAccessException(String message) {
        super(message);
    }
    public DataAccessException(String message, Throwable except) {
        super(message, except);
    }
}
