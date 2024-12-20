package dev.server.Services;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.security.Key;
import java.util.Date;

public class AuthenticationService
{
    static final Key SecretKey = Keys.hmacShaKeyFor("DeinSichererSchlüsselMitMindestens32Zeichen!".getBytes());
    static final String Issuer = "DeineMudda";
    static final long ExpirationTime = 600000; // 10 min


    public static String requestToken(UserRoles role) {
        return Jwts.builder()
                .setSubject(role.toString())
                .setIssuedAt(new Date())
                .setIssuer(Issuer)
                .setExpiration(new Date(System.currentTimeMillis() + ExpirationTime)) // Ablaufzeit (10 Minuten)
                .signWith(SecretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public static Boolean validateToken(String token)
    {
        if (token == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid token");
        }
        try {
            Jwts.parserBuilder()
                    .setSigningKey(SecretKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid token");
        }
    }

    public static UserRoles getRoleFromToken(String token) {
        return UserRoles.valueOf(Jwts.parserBuilder()
                .setSigningKey(SecretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject());
    }

    public enum UserRoles
    {
        ADMIN,
        USER
    }
}
