package MagitExceptions;

public class RepositoryDoesnotExistException extends Exception {
    public RepositoryDoesnotExistException(String message) {
        super(message);
    }
}
