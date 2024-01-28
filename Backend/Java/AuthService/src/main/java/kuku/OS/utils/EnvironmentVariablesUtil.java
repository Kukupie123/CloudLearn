package kuku.OS.utils;

import kuku.OS.model.exception.EnvironmentVariableNotFoundException;

public class EnvironmentVariablesUtil {
    private EnvironmentVariablesUtil() {

    }

    private final static String JWT_SECRET = "JWT_SECRET";
    private final static String JWT_EXPIRATION_TYPE = "JWT_EXPIRATION_TYPE"; //minute, hour, month
    private final static String JWT_EXPIRATION_TIME = "JWT_EXPIRATION_TIME"; //integer

    public final static String JWT_SECRET_ENV = System.getenv(JWT_SECRET);
    public final static String JWT_EXPIRATION_TYPE_ENV = System.getenv(JWT_EXPIRATION_TYPE);
    public final static int JWT_EXPIRATION_TIME_ENV = Integer.parseInt(System.getenv(JWT_EXPIRATION_TIME));


    public static void VALIDATE_ENV_VARIABLES() throws EnvironmentVariableNotFoundException {
        VALIDATE_ENV_VARIABLES(JWT_SECRET);
        VALIDATE_ENV_VARIABLES(JWT_EXPIRATION_TYPE);
        VALIDATE_ENV_VARIABLES(JWT_EXPIRATION_TIME);
    }

    private static void VALIDATE_ENV_VARIABLES(String variableName) throws EnvironmentVariableNotFoundException {
        if (System.getenv(variableName) == null) {
            throw new EnvironmentVariableNotFoundException(variableName + " not found in environment variable");
        }
    }
}
