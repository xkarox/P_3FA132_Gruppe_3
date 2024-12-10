package dev.server.controller;

import dev.hv.Utils;
import dev.hv.database.services.CustomerService;
import dev.hv.model.classes.Customer;
import dev.provider.ServiceProvider;
import dev.hv.database.provider.InternalServiceProvider;
import dev.hv.database.services.ReadingService;
import dev.hv.model.classes.Reading;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.server.validator.CustomerJsonSchemaValidatorService;
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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid reading data provided");
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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid reading data provided");
        }
        catch (IOException e)
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Internal Server IOError");
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
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found in database");
            }
            rs.update(reading);
            return new ResponseEntity<String>("Customer successfully updated", HttpStatus.OK);
        }
        catch (JsonProcessingException | ReflectiveOperationException | SQLException e)
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid customer data provided");
        }
        catch (IOException e)
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Internal Server IOError");

        }
    }
}
