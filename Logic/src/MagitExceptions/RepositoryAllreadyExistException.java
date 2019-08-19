package MagitExceptions;

public class RepositoryAllreadyExistException extends Exception {
    public RepositoryAllreadyExistException(String message) {
        super(message);
    }
}
