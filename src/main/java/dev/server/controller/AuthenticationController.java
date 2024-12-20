package dev.server.controller;

import dev.server.Services.AuthenticationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "+")
@RestController
@RequestMapping("/auth")
public class AuthenticationController
{
    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<String> requestToken()
    {
        return ResponseEntity.ok(AuthenticationService.requestToken(AuthenticationService.UserRoles.USER));
    }

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<String> accessProtectedEndpoint(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        if (authorizationHeader == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid token");
        }
        if (AuthenticationService.validateToken(authorizationHeader))
            return ResponseEntity.ok("You have access to this protected endpoint");

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired token");
    }
}
