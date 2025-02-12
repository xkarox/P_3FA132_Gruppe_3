package dev.server.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.hv.ResponseMessages;
import dev.hv.database.services.AuthInformationService;
import dev.hv.database.services.CryptoService;
import dev.hv.model.classes.AuthUserDto;
import dev.hv.model.classes.AuthenticationUser;
import dev.provider.ServiceProvider;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import static dev.hv.Utils.createErrorResponse;

@Path("/auth")
public class AuthenticationController
{
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);

    // ToDo: Secure other endpoints with JWT
    // ToDo: Admin pw -> config
    // ToDo: User roles & enum
    // ToDo: Add user id in MDR ?
    // ToDo: Add static master key for crypto service
    // ToDo: Add schema validation for user
    // ToDo: Add check on change / update for user account

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(String userBody) throws JsonProcessingException
    {
        logger.info("Received request to login user: {}", userBody);
        try (AuthInformationService as = new AuthInformationService(ServiceProvider.Services.getDatabaseConnection()))
        {
            AuthUserDto user = mapper.readValue(userBody, AuthUserDto.class);
            AuthenticationUser authInfo = as.getByUserName(user.getUsername());

            if (authInfo == null || authInfo.getPassword() == null || user.getPassword() == null)
            {
                logger.info("User not found or password is null");
                return createErrorResponse(Response.Status.NOT_FOUND, ResponseMessages.ControllerNotFound.toString());
            }

            boolean hasCorrectPassword = CryptoService.compareStringWithHash(user.getPassword(), authInfo.getPassword());

            if (!hasCorrectPassword)
            {
                logger.info("User provided incorrect password");
                return createErrorResponse(Response.Status.UNAUTHORIZED, ResponseMessages.ControllerUnauthorized.toString());
            }

            String token = CryptoService.generateToken(user.getUsername());
            return Response.ok(token).build();
        } catch (SQLException | ReflectiveOperationException e)
        {
            logger.info("Error while logging in user: {}", e.getMessage(), e);
            return createErrorResponse(Response.Status.BAD_REQUEST, ResponseMessages.ControllerBadRequest.toString());
        } catch (IOException e){
            logger.info("Internal server error: {}", e.getMessage(), e);
            return createErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, ResponseMessages.ControllerInternalError.toString());

        }
    }

    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response register(String userBody) throws JsonProcessingException
    {
        logger.info("Received request to register user: {}", userBody);
        try (AuthInformationService as = new AuthInformationService(ServiceProvider.Services.getDatabaseConnection()))
        {
            AuthUserDto user = mapper.readValue(userBody, AuthUserDto.class);
            if (as.getByUserName(user.getUsername()) != null)
            {
                logger.info("User already exists");
                return createErrorResponse(Response.Status.BAD_REQUEST, ResponseMessages.ControllerBadRequest.toString());
            }

            AuthenticationUser newAuthInfo = new AuthenticationUser(user);
            as.add(newAuthInfo);

            return Response.status(Response.Status.CREATED).build();
        } catch (SQLException | ReflectiveOperationException e)
        {
            logger.info("Error while registering user: {}", e.getMessage(), e);
            return createErrorResponse(Response.Status.BAD_REQUEST, ResponseMessages.ControllerBadRequest.toString());
        } catch (IOException e){
            logger.info("Internal server error: {}", e.getMessage(), e);
            return createErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, ResponseMessages.ControllerInternalError.toString());
        }
    }

    @DELETE
    @Path("/{userName}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(@PathParam("userName") String userName) throws JsonProcessingException
    {
        logger.info("Received request to delete user: {}", userName);
        try (AuthInformationService as = new AuthInformationService(ServiceProvider.Services.getDatabaseConnection())){
            var user = as.getByUserName(userName);
            if (user == null){
                logger.info("User not found");
                return createErrorResponse(Response.Status.NOT_FOUND, ResponseMessages.ControllerNotFound.toString());
            }

            as.remove(user);
            logger.info("User deleted successfully: {}", userName + " " + user.getId());
            return Response.status(Response.Status.OK).build();
        } catch (SQLException | ReflectiveOperationException e)
        {
            logger.info("Error while deleting user: {}", e.getMessage(), e);
            return createErrorResponse(Response.Status.BAD_REQUEST, ResponseMessages.ControllerBadRequest.toString());
        } catch (IOException e)
        {
            logger.info("Internal server error: {}", e.getMessage(), e);
            return createErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, ResponseMessages.ControllerInternalError.toString());
        }
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateUser(String userBody) throws JsonProcessingException
    {
        logger.info("Received request to update user: {}", userBody);
        try (AuthInformationService as = new AuthInformationService(ServiceProvider.Services.getDatabaseConnection())){
            AuthUserDto user = mapper.readValue(userBody, AuthUserDto.class);
            AuthenticationUser authInfo = as.getByUserName(user.getUsername());
            if (authInfo == null){
                logger.info("User not found");
                return createErrorResponse(Response.Status.NOT_FOUND, ResponseMessages.ControllerNotFound.toString());
            }
            as.update(authInfo);
            logger.info("User updated successfully: {}", user);
            return Response.status(Response.Status.OK).build();
        } catch (SQLException | ReflectiveOperationException e){
            logger.error("Error updating user: {}", e.getMessage(), e);
            return createErrorResponse(Response.Status.BAD_REQUEST, ResponseMessages.ControllerBadRequest.toString());
        } catch (IOException e)
        {
            logger.info("Internal server error: {}", e.getMessage(), e);
            return createErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, ResponseMessages.ControllerInternalError.toString());
        }
    }
}
