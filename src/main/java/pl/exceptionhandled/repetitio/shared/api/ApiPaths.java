package pl.exceptionhandled.repetitio.shared.api;

public final class ApiPaths {

    private ApiPaths() {
    }

    public static final String API = "/api/v1";

    public static final String AUTH = API + "/auth";

    public static final String REGISTER = "/register";
    public static final String LOGIN = "/login";

    public static final String AUTH_REGISTER = AUTH + REGISTER;
    public static final String AUTH_LOGIN = AUTH + LOGIN;
}