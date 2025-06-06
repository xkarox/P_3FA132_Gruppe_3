package dev.hv.database.services;

import dev.hv.database.DbHelperService;
import dev.hv.model.classes.Customer;
import dev.provider.ServiceProvider;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.ws.rs.core.NewCookie;

import java.io.IOException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

public class CryptoService
{
    private static final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private static final int  EXPIRATION_TIME = 300000;
    private static final Random random = new Random();

    public static int getExpirationTime()
    {
        return EXPIRATION_TIME;
    }

    public static String CreateNewUsername(Customer customer) throws SQLException, IOException, ReflectiveOperationException
    {
        try(AuthUserService authService = ServiceProvider.getAuthUserService())
        {
            for (int i = 0; i < 5; i++)
            {
                String sb = customer.getFirstName() +
                        "_" +
                        random.nextInt(1000) +
                        "_" +
                        customer.getLastName() +
                        "_" +
                        random.nextInt(1000);

                var userName = sb.toLowerCase();
                if (authService.DisplayNameAvailable(userName))
                {
                    return userName;
                }

            }
        }
        throw new RuntimeException("Could not create a new username.");
    }

    public static String hashStringWithSalt(String input)
    {
        try
        {
            String salt = DbHelperService.loadProperties().getProperty("saltySecret");
            String combinedInput = input + salt;

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = digest.digest(combinedInput.getBytes());

            StringBuilder hexString = new StringBuilder();
            for (byte b : hashedBytes)
            {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1)
                {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException | IOException e)
        {
            throw new RuntimeException("Fehler beim Hashen: " + e.getMessage());
        }
    }

    public static boolean compareStringWithHash(String input, String hashedInput)
    {
        return hashStringWithSalt(input).equals(hashedInput);
    }

    public static String generateToken(UUID id)
    {
        return Jwts.builder()
                .setSubject(id.toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key)
                .compact();
    }

    public static String validateToken(String token)
    {
        try{
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        }
        catch (Exception e)
        {
            return null;
        }

    }

    public static NewCookie createTokenCookie(UUID id)
    {
        var updatedToken = CryptoService.generateToken(id);
        return new NewCookie.Builder("jwt-token")
                .value(updatedToken)
                .path("/")
                .maxAge(CryptoService.getExpirationTime())
                .secure(false)
                .httpOnly(true)
                .sameSite(NewCookie.SameSite.LAX)
                .build();
    }
}
