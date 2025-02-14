package dev.server.filter;

import dev.hv.database.services.AuthUserService;
import dev.hv.database.services.AuthorisationService;
import dev.hv.database.services.CryptoService;
import dev.provider.ServiceProvider;
import dev.server.Annotations.Secured;
import jakarta.annotation.Priority;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.MDC;

import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;

@Secured
@Provider
@Priority(4)
public class JwtFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String authorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

        if (!AuthorisationService.DoesAuthDbExistsWrapper()){
            MDC.put("authDbExists", "false");
            return;
        }

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
            return;
        }

        String token = authorizationHeader.substring(7);
        try {
            String userId = CryptoService.validateToken(token);
            try(AuthUserService authUserService = ServiceProvider.getAuthUserService()){
                var user = authUserService.getById(UUID.fromString(userId));
                if(user == null)
                    requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());

                MDC.put("id", userId);
                MDC.put("username", user.getUsername());
                MDC.put("role", user.getRole().toString());
                MDC.put("permissions", String.join(", ", user.getPermissions().stream()
                        .map(Object::toString)
                        .toArray(String[]::new)));
            }
        } catch (Exception e) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
        }
    }
}