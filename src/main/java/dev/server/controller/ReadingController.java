package dev.server.controller;

import dev.hv.ResponseMessages;
import dev.hv.Utils;
import dev.provider.ServiceProvider;
import dev.hv.database.services.ReadingService;
import dev.hv.model.classes.Reading;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import dev.server.validator.ReadingJsonSchemaValidationService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;


@RestController
@RequestMapping(value = "/readings")
public class ReadingController
{
    private void validateRequestData(String jsonString)
    {
        boolean invalidCustomer = ReadingJsonSchemaValidationService.getInstance().validate(jsonString);
        if ( invalidCustomer )
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ResponseMessages.ControllerBadRequest.toString());
        }
    }

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(method = RequestMethod.POST)
    public String addReading(@RequestBody String readingJson)
    {
        this.validateRequestData(readingJson);
        try (ReadingService rs = ServiceProvider.Services.getReadingService())
        {
            readingJson = Utils.unpackFromJsonString(readingJson, Reading.class);
            Reading reading = Utils.getObjectMapper().readValue(readingJson, Reading.class);
            if (reading.getId() == null)
            {
                reading.setId(UUID.randomUUID());
            }
            reading = rs.add(reading);
            return Utils.packIntoJsonString(reading, Reading.class);
        }
        catch (JsonProcessingException | SQLException | ReflectiveOperationException e)
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ResponseMessages.ControllerBadRequest.toString());
        }
        catch (IOException e)
        {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ResponseMessages.ControllerInternalError.toString());
        }
    }

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(method = RequestMethod.PUT)
    public ResponseEntity<String> updateReading(@RequestBody String readingJson)
    {
        this.validateRequestData(readingJson);
        try (ReadingService rs = ServiceProvider.Services.getReadingService())
        {
            readingJson = Utils.unpackFromJsonString(readingJson, Reading.class);
            Reading reading = Utils.getObjectMapper().readValue(readingJson, Reading.class);
            Reading dbCustomer = rs.getById(reading.getId());
            if (dbCustomer == null)
            {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, ResponseMessages.ControllerNotFound.toString());
            }
            rs.update(reading);
            return new ResponseEntity<String>(ResponseMessages.ControllerUpdateSuccess.toString(), HttpStatus.OK);
        }
        catch (JsonProcessingException | ReflectiveOperationException | SQLException e)
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ResponseMessages.ControllerBadRequest.toString());
        }
        catch (IOException e)
        {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ResponseMessages.ControllerInternalError.toString());

        }
    }

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public String getReading(@PathVariable("id") UUID id)
    {
        try
        {
            try (ReadingService rs = ServiceProvider.Services.getReadingService())
            {
                Reading reading = rs.getById(id);
                return Utils.packIntoJsonString(reading, Reading.class);
            }
        }
        catch (SQLException | ReflectiveOperationException e)
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ResponseMessages.ControllerBadRequest.toString());
        }
        catch (IOException e)
        {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ResponseMessages.ControllerInternalError.toString());
        }
    }

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public String deleteReading(@PathVariable("id") UUID id){
        try
        {
            try (ReadingService rs = ServiceProvider.Services.getReadingService())
            {
                Reading reading = rs.getById(id);
                ServiceProvider.Services.getReadingService().remove(reading);
                return Utils.packIntoJsonString(reading, Reading.class);
            }
        }
        catch (SQLException | ReflectiveOperationException e)
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ResponseMessages.ControllerBadRequest.toString());
        }
        catch (IOException e)
        {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ResponseMessages.ControllerInternalError.toString());
        }
    }
}
