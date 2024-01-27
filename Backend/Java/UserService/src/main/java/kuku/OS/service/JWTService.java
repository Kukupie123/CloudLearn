package kuku.OS.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import kuku.OS.util.EnvironmentVariablesUtil;

import java.util.Calendar;

public class JWTService {
    private JWTService() {
        helperFunctions = new JwtHelperFunctions();
    }

    private static JWTService getInstance;
    private final JwtHelperFunctions helperFunctions;

    public static JWTService getInstance() {
        if (getInstance == null) {
            getInstance = new JWTService();
        }
        return getInstance;
    }


    private static final Algorithm algorithm = Algorithm.HMAC256(EnvironmentVariablesUtil.JWT_SECRET_ENV);

    public String createToken(String userId) throws Exception {
        Calendar calendar = Calendar.getInstance();
        int expirationType = helperFunctions.parseExpirationType(EnvironmentVariablesUtil.JWT_EXPIRATION_TYPE_ENV);
        calendar.add(expirationType, EnvironmentVariablesUtil.JWT_EXPIRATION_TIME_ENV); // Token expires in specified Time
        return JWT.create().withClaim("userId", userId).withExpiresAt(calendar.getTime()).sign(algorithm);
    }

    public DecodedJWT verifyToken(String token) {
        JWTVerifier verifier = JWT.require(algorithm).build();
        return verifier.verify(token);
    }

}

class JwtHelperFunctions {
    public int parseExpirationType(String expirationType) throws Exception {
        switch (expirationType) {
            case "minute" -> {
                return Calendar.MINUTE;
            }
            case "hour" -> {
                return Calendar.HOUR;
            }
            case "month" -> {
                return Calendar.MONTH;
            }
        }
        throw new Exception("Invalid Expiration Type String. Passed " + expirationType + ", minute,hour,month are allowed");
    }
}