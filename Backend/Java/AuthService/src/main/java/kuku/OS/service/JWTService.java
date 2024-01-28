package kuku.OS.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import kuku.OS.utils.EnvironmentVariablesUtil;

import java.util.Calendar;
import java.util.Map;


public class JWTService {
    private JWTService() {
        helperFunctions = new JwtHelperFunctions();
    }

    private static JWTService instance;
    private final JwtHelperFunctions helperFunctions;

    public static JWTService getInstance() {
        if (instance == null) {
            instance = new JWTService();
        }
        return instance;
    }


    private static final Algorithm algorithm = Algorithm.HMAC256(EnvironmentVariablesUtil.JWT_SECRET_ENV);

    public String createToken(Map<String, String> claims) throws Exception {
        Calendar calendar = Calendar.getInstance();
        int expirationType = helperFunctions.parseExpirationType(EnvironmentVariablesUtil.JWT_EXPIRATION_TYPE_ENV);
        calendar.add(expirationType, EnvironmentVariablesUtil.JWT_EXPIRATION_TIME_ENV); // Token expires in specified Time
        var token = JWT.create();
        if (claims != null)
            for (String key : claims.keySet()) {
                token.withClaim(key, claims.get(key));
            }
        return token.withExpiresAt(calendar.getTime()).sign(algorithm);
    }

    public DecodedJWT verifyToken(String token) {
        JWTVerifier verifier = JWT.require(algorithm).build();

        var decoded = verifier.verify(token);
        return decoded;
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