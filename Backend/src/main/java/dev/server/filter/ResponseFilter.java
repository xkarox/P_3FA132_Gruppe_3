package dev.server.filter;

import dev.hv.database.services.AuthUserService;
import dev.hv.database.services.AuthorisationService;
import dev.hv.database.services.CryptoService;
import dev.provider.ServiceProvider;
import dev.server.Annotations.Secured;
import io.jsonwebtoken.io.IOException;
import jakarta.annotation.Priority;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import java.util.UUID;

@Secured
@Provider
@Priority(6)
public class ResponseFilter implements ContainerResponseFilter
{
    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException
    {
        if (!AuthorisationService.DoesAuthDbExistsWrapper())
            return;

        Cookie tokenCookie = requestContext.getCookies().get("jwt-token");
        if (tokenCookie == null)
            return;

        String token = tokenCookie.getValue();
        try {
            String userId = CryptoService.validateToken(token);
            try(AuthUserService authUserService = ServiceProvider.getAuthUserService()){
                var user = authUserService.getById(UUID.fromString(userId));
                if(user == null || user.getId() == null){
                    requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
                    return;
                }

                responseContext.getHeaders().add(HttpHeaders.SET_COOKIE, CryptoService.createTokenCookie(user.getId()));
            }
        } catch (Exception e) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
        }
    }
}
