package dev.server.controller;

import dev.server.Annotations.Secured;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Secured
@Path("/secure")
@Produces(MediaType.APPLICATION_JSON)
public class SecureResource {

    @GET
    @Path("/data")
    public Response getSecureData(@HeaderParam("username") String username) {
        return Response.ok("{\"message\": \"Hallo " + username + ", dies ist eine geschützte Ressource!\"}").build();
    }
}
