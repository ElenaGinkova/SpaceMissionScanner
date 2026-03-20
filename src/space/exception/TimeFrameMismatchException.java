package space.exception;

public class TimeFrameMismatchException extends RuntimeException {
    public TimeFrameMismatchException(String message) {
        super(message);
    }

    public TimeFrameMismatchException(String message, Throwable cause) {
        super(message, cause);
    }
}
