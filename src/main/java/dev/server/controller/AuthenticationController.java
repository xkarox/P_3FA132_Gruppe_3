package dev.server.controller;

import dev.hv.Utils;
import dev.hv.database.services.UserService;
import dev.hv.model.classes.User;
import dev.provider.ServiceProvider;
import dev.server.services.JwtService;
import dev.server.validator.UserJsonSchemaValidatorService;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping(value = "/auth")
public class AuthenticationController
{

    private final PasswordEncoder passwordEncoder = new PasswordEncoder()
    {
        @Override
        public String encode(CharSequence rawPassword)
        {
            return DigestUtils.md5Hex(rawPassword.toString());
        }

        @Override
        public boolean matches(CharSequence rawPassword, String encodedPassword)
        {
            return Objects.equals(encode(rawPassword), encodedPassword);
        }
    };

    private void validateRequestData(String jsonString)
    {
        boolean invalidUser = UserJsonSchemaValidatorService.getInstance().validate(jsonString);
        if ( invalidUser )
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid user data provided");
        }
    }

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(method = RequestMethod.POST, value="/register")
    public String register(@RequestBody String userJson)
    {
        this.validateRequestData(userJson);
        try(UserService us = ServiceProvider.Services.getUserService())
        {
            userJson = Utils.unpackFromJsonString(userJson, User.class);
            User user = Utils.getObjectMapper().readValue(userJson, User.class);
            if(us.getByUsername(user.getUsername()) != null)
            {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username already taken");
            }
            user.setId(UUID.randomUUID());
            user.setPasswordHash(passwordEncoder.encode(user.getPassword()));
            user.setCreatedAt(LocalDate.now());
            user = us.add(user);
            return Utils.packIntoJsonString(user, User.class);
        }
        catch (SQLException e)
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid user data provided");
        }
        catch (Exception e)
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Internal Server IOError");
        }
    }

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(method = RequestMethod.POST, value="/login")
    public String authenticate(@RequestBody String userJson) {
        this.validateRequestData(userJson);
        try(UserService us = ServiceProvider.Services.getUserService(); JwtService jwtS = ServiceProvider.Services.getJwtService())
        {
            userJson = Utils.unpackFromJsonString(userJson, User.class);
            User user = Utils.getObjectMapper().readValue(userJson, User.class);
            User databaseUser = us.getByUsername(user.getUsername());
            if(databaseUser == null)
            {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found");
            }
            if(passwordEncoder.matches(user.getPassword(), databaseUser.getPassword()))
            {
                var token = jwtS.generateToken(databaseUser);
                String response = new StringBuilder("{\"token\": \"").append(token).append("\"}").toString();
                return response;
            }
            return Utils.packIntoJsonString(user, User.class);
        }
        catch (SQLException e)
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid user data provided");
        }
        catch (Exception e)
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Internal Server IOError");
        }
    }
}
