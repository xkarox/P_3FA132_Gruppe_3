package dev.server.filter;

import dev.hv.database.DatabaseConnection;
import dev.hv.database.services.CryptoService;
import dev.hv.model.classes.AuthenticationUser;
import dev.server.Annotations.Secured;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.MDC;

import java.io.IOException;
import java.sql.SQLException;

@Secured
@Provider
public class JwtFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String authorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

        if (!checkIfDatabaseExists())
            return;

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
            return;
        }

        String token = authorizationHeader.substring(7);
        try {
            String username = CryptoService.validateToken(token);
            MDC.put("username", username); // ToDo: this
            requestContext.setProperty("username", username); // ToDo: or that
        } catch (Exception e) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
        }
    }

    private boolean checkIfDatabaseExists(){
        try (var dbCon = new DatabaseConnection())
        {
            dbCon.openConnection();
            return dbCon.getAllTableNames().contains(new AuthenticationUser().getSerializedTableName());
        } catch (SQLException | IOException e)
        {
            return false;
        }
    }
}



