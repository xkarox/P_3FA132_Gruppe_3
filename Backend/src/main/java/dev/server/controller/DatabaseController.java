package dev.server.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.hv.ResponseMessages;
import dev.hv.database.DatabaseConnection;
import dev.hv.database.DbHelperService;
import dev.hv.database.services.AuthUserService;
import dev.hv.model.classes.Authentification.AuthUserDto;
import dev.hv.model.classes.Authentification.AuthUser;
import dev.provider.ServiceProvider;
import dev.server.Annotations.Secured;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;

import static dev.hv.Utils.createErrorResponse;

@Secured
@Path("/setupDB")
public class DatabaseController {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(DatabaseController.class);

    @DELETE
    public Response setupDatabase() throws JsonProcessingException {
        logger.info("Received request to setup database");
        try {
            DatabaseConnection dbCon = ServiceProvider.Services.getDatabaseConnection();
            dbCon.removeAllTables();
            dbCon.createAllTables();
            logger.info("Database setup successfully");
            return Response.status(Response.Status.OK).build();
        } catch (SQLException e) {
            logger.error("Error setting up database: {}", e.getMessage(), e);
            return createErrorResponse(Response.Status.BAD_REQUEST, ResponseMessages.ControllerBadRequest.toString());
        } catch (IOException e){
            logger.info("Internal server error: {}", e.getMessage(), e);
            return createErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, ResponseMessages.ControllerInternalError.toString());
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response setupDatabaseMod(String userBody,
                                  @DefaultValue("false") @QueryParam("security") boolean hasSecurity) throws JsonProcessingException
    {
        if (userBody != null)
            logger.info("Received request to add admin user: {}", userBody);

        try(DatabaseConnection dbCon = ServiceProvider.Services.getDatabaseConnection())
        {
            dbCon.removeAllTables();
            if (hasSecurity)
            {
                dbCon.createAllTablesWithAuth();
                logger.info("Database auth setup successfully");
            }
            else
            {
                dbCon.createAllTables();
                logger.info("Database setup successfully");
            }
            if (hasSecurity)
            {
                try (AuthUserService as = ServiceProvider.getAuthUserService())
                {
                    AuthUserDto user;
                    if (userBody != null)
                        user = mapper.readValue(userBody, AuthUserDto.class);
                    else {
                        var properties = DbHelperService.loadProperties();
                        user = new AuthUserDto();
                        user.setUsername(properties.getProperty("default.admin.username"));
                        user.setPassword(properties.getProperty("default.admin.password"));
                    }

                    user.setId(UUID.randomUUID()); // Admin user id

                    if (as.getByUserName(user.getUsername()) != null)
                    {
                        logger.info("User already exists");
                        return createErrorResponse(Response.Status.BAD_REQUEST, ResponseMessages.ControllerBadRequest.toString());
                    }

                    AuthUser newAuthInfo = new AuthUser(user);
                    as.add(newAuthInfo);
                    logger.info("Successfully created a new admin user: " + user.getUsername());
                }
            }
            return Response.status(Response.Status.OK).build();
        } catch (SQLException | ReflectiveOperationException e)
        {
            logger.info("Error setting up database: {}", e.getMessage(), e);
            return createErrorResponse(Response.Status.BAD_REQUEST, ResponseMessages.ControllerBadRequest.toString());
        } catch (IOException e){
            logger.info("Internal server error: {}", e.getMessage(), e);
            return createErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, ResponseMessages.ControllerInternalError.toString());
        }
    }
}