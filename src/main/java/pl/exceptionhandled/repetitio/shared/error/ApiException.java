package pl.exceptionhandled.repetitio.shared.error;

public class ApiException extends RuntimeException {
    private final int status;
    public ApiException(int status, String message) { super(message); this.status = status; }
    public int status() { return status; }
    public static ApiException notFound() { return new ApiException(404, "Resource not found"); }
    public static ApiException conflict() { return new ApiException(409, "Resource changed; reload before retrying"); }
}

