package dev.server.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.hv.ResponseMessages;
import dev.hv.Utils;
import dev.hv.database.DatabaseConnection;
import dev.provider.ServiceProvider;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Path("/setupDB")
public class DatabaseController
{
    @DELETE
    public Response setupDatabase() throws JsonProcessingException
    {
        try
        {
            DatabaseConnection dbCon = ServiceProvider.Services.getDatabaseConnection();
            dbCon.removeAllTables();
            dbCon.createAllTables();
            return Response.status(Response.Status.OK)
                    .build();

        } catch (SQLException | IOException e)
        {
            Map<String, String> responseBody = new HashMap<>();
            responseBody.put("message", ResponseMessages.ControllerInternalError.toString());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Utils.packIntoJsonString(responseBody))
                    .build();
        }
    }
}