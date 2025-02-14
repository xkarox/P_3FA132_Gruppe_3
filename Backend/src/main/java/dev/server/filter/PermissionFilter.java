package dev.server.filter;

import dev.hv.database.services.AuthorisationService;
import dev.hv.model.enums.UserPermissions;
import dev.server.Annotations.Secured;
import jakarta.annotation.Priority;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.MDC;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Provider
@Secured
@Priority(5)
public class PermissionFilter implements ContainerRequestFilter
{
    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException
    {
        if (!AuthorisationService.DoesAuthDbExistsWrapper()){ // ToDo: redundant check -> JwtFilter
            MDC.put("authDbExists", "false");
            return;
        }

        if (MDC.get("permissions").isBlank()){
            containerRequestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
            return;
        }

        // Get the request type and check if the user has the permission to execute it
        UserPermissions requestType = UserPermissions.translateHttpToUserPermission(containerRequestContext.getMethod());
        List<String> availablePermissions = List.of(MDC.get("permissions").split(","));
        if (!availablePermissions.contains(requestType.toString()))
            containerRequestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
    }
}