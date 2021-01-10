package it.polimi.se2.clup.CLupEJB.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import it.polimi.se2.clup.CLupEJB.exceptions.TokenException;

public final class TokenManager {
    private static final String KEY = "secret";

    private TokenManager() {
    }

    public static String generateCustomerToken(String customerId) throws TokenException {
        try {
            Algorithm algorithm = Algorithm.HMAC256(KEY);
            return JWT.create()
                    .withClaim("type", "customer")
                    .withClaim("customerId", customerId)
                    .sign(algorithm);
        } catch (JWTCreationException exception) {
            throw new TokenException("Could not generate token");
        }
    }

    public static String getCustomerId(String token) throws TokenException {
        try {
            Algorithm algorithm = Algorithm.HMAC256(KEY);
            JWTVerifier verifier = JWT.require(algorithm)
                    .build();
            DecodedJWT jwt = verifier.verify(token);

            String type = jwt.getClaim("type").asString();
            String customerId = jwt.getClaim("customerId").asString();

            if (type == null || customerId == null || !type.equals("customer")) {
                throw new TokenException("Invalid token");
            }

            return customerId;
        } catch (JWTVerificationException exception) {
            throw new TokenException("Invalid token");
        }
    }

    public static String generateEmployeeToken(int storeId) throws TokenException {
        try {
            Algorithm algorithm = Algorithm.HMAC256(KEY);
            return JWT.create()
                    .withClaim("type", "employee")
                    .withClaim("storeId", storeId)
                    .sign(algorithm);
        } catch (JWTCreationException exception) {
            throw new TokenException("Could not generate token");
        }
    }

    public static Integer getEmployeeStoreId(String token) throws TokenException {
        try {
            Algorithm algorithm = Algorithm.HMAC256(KEY);
            JWTVerifier verifier = JWT.require(algorithm)
                    .build();
            DecodedJWT jwt = verifier.verify(token);

            String type = jwt.getClaim("type").asString();
            Integer storeId = jwt.getClaim("storeId").asInt();

            if (type == null || storeId == null || !type.equals("employee")) {
                throw new TokenException("Invalid token");
            }

            return storeId;
        } catch (JWTVerificationException exception) {
            throw new TokenException("Invalid token");
        }
    }
}
