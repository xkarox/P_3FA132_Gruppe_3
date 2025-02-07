package dev.server.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.hv.database.services.AuthInformationService;
import dev.hv.database.services.CryptoService;
import dev.hv.database.services.CustomerService;
import dev.hv.model.classes.AuthUserDto;
import dev.hv.model.classes.AuthenticationInformation;
import dev.provider.ServiceProvider;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.IOException;
import java.sql.SQLException;

@Path("/auth")
public class AuthenticationController
{
    private static final ObjectMapper mapper = new ObjectMapper();

    // ToDo: Implement user pw update
    // ToDo: Secure other endpoints with JWT
    // ToDo: Admin pw -> config
    // ToDo: User roles & enum
    // ToDo: Add user id in MDR ?

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(String userBody)
    {
        try
        {
            AuthUserDto user = mapper.readValue(userBody, AuthUserDto.class);

            AuthInformationService as = new AuthInformationService(ServiceProvider.Services.getDatabaseConnection());
            CustomerService cs = ServiceProvider.Services.getCustomerService();

            AuthenticationInformation authInfo = as.getByUserName(user.getUsername());

            if (authInfo == null || authInfo.getPassword() == null)
            {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }

            boolean hasCorrectPassword = CryptoService.compareStringWithHash(user.getPassword(), authInfo.getPassword());

            if (!hasCorrectPassword)
            {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }

            String token = CryptoService.generateToken(user.getUsername());
            return Response.ok(token).build();
        } catch (SQLException | ReflectiveOperationException | IOException e)
        {
            throw new RuntimeException(e);
        }
    }
}
