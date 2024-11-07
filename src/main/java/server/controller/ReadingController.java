package server.controller;

import ace.Utils;
import ace.ServiceProvider;
import ace.database.services.ReadingService;
import ace.model.classes.Reading;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import server.validator.ReadingJsonSchemaValidationService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;


@RestController
@RequestMapping(value = "/readings")
public class ReadingController
{
    static ObjectMapper _objMapper = Utils.getObjectMapper();

    private void validateRequestData(String jsonString)
    {
        ReadingJsonSchemaValidationService readingValidator = ServiceProvider.Validator.getReadingValidator();
        boolean invalidCustomer = !readingValidator.validate(jsonString);
        if ( invalidCustomer )
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid reading data provided");
        }
    }

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(method = RequestMethod.POST)
    public String addReading(@RequestBody String readingJson)
    {
        this.validateRequestData(readingJson);
        try
        {
            readingJson = Utils.unpackFromJsonString(readingJson, Reading.class);
            Reading reading = _objMapper.readValue(readingJson, Reading.class);
            ReadingService rs = ServiceProvider.Services.getReadingService();
            if (reading.getId() == null)
            {
                reading.setId(UUID.randomUUID());
            }
            reading = rs.add(reading);
            return Utils.packIntoJsonString(reading, Reading.class);
        }
        catch (JsonProcessingException | SQLException | ReflectiveOperationException e)
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid customer data provided");
        }
        catch (IOException e)
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Internal Server IOError");
        }
    }
}
