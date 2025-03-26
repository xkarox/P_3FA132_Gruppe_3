package dev.server.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.hv.ResponseMessages;
import dev.hv.database.services.AuthUserService;
import dev.hv.database.services.AuthorisationService;
import dev.hv.database.services.CryptoService;
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

import static dev.hv.Utils.createErrorResponse;

@Path("/auth")
public class AuthenticationController
{
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(String userBody) throws JsonProcessingException
    {
        logger.info("Received request to login user: {}", userBody);
        try (AuthUserService as = ServiceProvider.getAuthUserService())
        {
            AuthUserDto user = mapper.readValue(userBody, AuthUserDto.class);
            AuthUser authInfo = as.getByUserName(user.getUsername());

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

            String token = CryptoService.generateToken(authInfo.getId());
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
    @Secured
    @Path("/register")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response register(String userBody) throws JsonProcessingException
    {
        logger.info("Received request to register user: {}", userBody);

        if (!AuthorisationService.IsUserAdmin())
            return createErrorResponse(Response.Status.UNAUTHORIZED, ResponseMessages.ControllerUnauthorized.toString());

        try (AuthUserService as = ServiceProvider.getAuthUserService())
        {
            AuthUserDto user = mapper.readValue(userBody, AuthUserDto.class);
            if (user.getId() == null){
                logger.info("User id is null");
                return createErrorResponse(Response.Status.BAD_REQUEST, ResponseMessages.ControllerBadRequest.toString());
            }
            if (as.getByUserName(user.getUsername()) != null)
            {
                logger.info("User already exists");
                return createErrorResponse(Response.Status.BAD_REQUEST, ResponseMessages.ControllerBadRequest.toString());
            }

            AuthUser newAuthInfo = new AuthUser(user);
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
    @Secured
    @Path("/delete/{userName}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(@PathParam("userName") String userName) throws JsonProcessingException
    {
        logger.info("Received request to delete user: {}", userName);

        if (!AuthorisationService.IsUserAdmin())
            return createErrorResponse(Response.Status.UNAUTHORIZED, ResponseMessages.ControllerUnauthorized.toString());

        try (AuthUserService as = ServiceProvider.getAuthUserService()){
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
    @Secured
    @Path("/update")
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(String userBody) throws JsonProcessingException
    {
        logger.info("Received request to update user: {}", userBody);

        if (!AuthorisationService.IsUserAdmin())
            return createErrorResponse(Response.Status.UNAUTHORIZED, ResponseMessages.ControllerUnauthorized.toString());

        try (AuthUserService as = ServiceProvider.getAuthUserService()){
            AuthUserDto user = mapper.readValue(userBody, AuthUserDto.class);
            AuthUser authInfo = as.getByUserName(user.getUsername());
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


    @GET
    @Secured
    @Path("/all")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllUsers() throws JsonProcessingException
    {
        logger.info("Received request to get all users");

        if (!AuthorisationService.IsUserAdmin())
            return createErrorResponse(Response.Status.UNAUTHORIZED, ResponseMessages.ControllerUnauthorized.toString());

        try (AuthUserService as = ServiceProvider.getAuthUserService()){
            var users = as.getAll();
            for (AuthUser user : users){
                user.setPassword(null);
            }

            logger.info("Users retrieved successfully");
            return Response.status(Response.Status.OK)
                    .entity(mapper.writeValueAsString(users))
                    .build();
        } catch (SQLException | ReflectiveOperationException e){
            logger.error("Error retrieving users: {}", e.getMessage(), e);
            return createErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, ResponseMessages.ControllerInternalError.toString());
        } catch (IOException e){
            logger.info("Internal server error: {}", e.getMessage(), e);
            return createErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, ResponseMessages.ControllerInternalError.toString());
        }
    }
}