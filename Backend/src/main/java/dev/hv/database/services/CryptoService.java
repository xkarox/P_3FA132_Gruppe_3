package dev.hv.database.services;

import dev.hv.database.DbHelperService;
import dev.hv.model.classes.Customer;
import dev.provider.ServiceProvider;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.io.IOException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Date;
import java.util.Random;

public class CryptoService
{
    private static final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private static final long EXPIRATION_TIME = 3600000; // 1 Hour
    private static final Random random = new Random();

    public static String CreateNewUsername(Customer customer) throws SQLException, IOException, ReflectiveOperationException
    {
        try(AuthInformationService authService = new AuthInformationService(ServiceProvider.Services.getDatabaseConnection()))
        {
            for (int i = 0; i < 5; i++)
            {
                StringBuilder sb = new StringBuilder();
                sb.append(customer.getFirstName());
                sb.append("_");
                sb.append(random.nextInt(1000));
                sb.append("_");
                sb.append(customer.getLastName());
                sb.append("_");
                sb.append(random.nextInt(1000));

                var userName = sb.toString().toLowerCase();
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

    public static String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key)
                .compact();
    }

    public static String validateToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

}
