package dev.server.controller;

import dev.hv.ResponseMessages;
import dev.hv.database.DatabaseConnection;
import dev.provider.ServiceProvider;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.sql.SQLException;

@CrossOrigin
@RestController
@RequestMapping(value = "/setupDB")
public class DatabaseController
{
    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(method = RequestMethod.DELETE)
    public void setupDatabase()
    {
        try
        {
            DatabaseConnection dbCon = ServiceProvider.Services.getDatabaseConnection();
            dbCon.removeAllTables();
            dbCon.createAllTables();

        } catch (SQLException | IOException e)
        {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ResponseMessages.ControllerInternalError.toString());
        }
    }
}