package dev.server.controller;

import dev.hv.Utils;
import dev.hv.model.IReading;
import dev.provider.ServiceProvider;
import dev.hv.database.services.ReadingService;
import dev.hv.model.classes.Reading;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import dev.server.validator.ReadingJsonSchemaValidationService;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;
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
    public String updateReading(@RequestBody String readingJson)
    {
        this.validateRequestData(readingJson);
        try (ReadingService rs = ServiceProvider.Services.getReadingService())
        {
            readingJson = Utils.unpackFromJsonString(readingJson, Reading.class);
            Reading reading = Utils.getObjectMapper().readValue(readingJson, Reading.class);
            if (rs.getById(reading.getId()) == null)
            {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found in database");
            }
            rs.update(reading);
            return Utils.packIntoJsonString(reading, Reading.class);
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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid reading data provided");
        }
        catch (IOException e)
        {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server IOError");
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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid reading data provided");
        }
        catch (IOException e)
        {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server IOError");
        }
    }

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(method = RequestMethod.GET)
    public String getReadings(@RequestParam(name = "customer", required = false) String customerId,
                              @RequestParam(name = "start", required = false) String startDate,
                              @RequestParam(name = "end", required = false) String endDate,
                              @RequestParam(name = "kindOfMeter", required = false)Integer kindOfMeter)
    {
        try {
            UUID id = customerId != null ? UUID.fromString(customerId) : null;

            LocalDate start = null;
            if (startDate != null)
            {
                if (!startDate.matches("\\d{4}-\\d{2}-\\d{2}"))
                {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid start date format, expected yyyy-mm-dd");
                }
                start = LocalDate.parse(startDate);
            }
            LocalDate end = null;
            if (endDate != null)
            {
                if (!endDate.matches("\\d{4}-\\d{2}-\\d{2}"))
                {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid end date format, expected yyyy-mm-dd");
                }
                end = LocalDate.parse(endDate);
            }
            IReading.KindOfMeter meterType = null;
            if (kindOfMeter != null)
            {
                if (kindOfMeter >= 0 && kindOfMeter < IReading.KindOfMeter.values().length)
                {
                    meterType = IReading.KindOfMeter.values()[kindOfMeter];
                } else
                {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid kindOfMeter value provided");
                }
            }
            String returnString = null;
            try (ReadingService rs = ServiceProvider.Services.getReadingService())
            {
                Collection<Reading> queryResults = rs.queryReadings(Optional.ofNullable(id), Optional.ofNullable(start),
                        Optional.ofNullable(end), Optional.ofNullable(meterType));
                returnString = Utils.packIntoJsonString(queryResults, Reading.class);
            }

            return returnString;
        } catch (SQLException | IOException | ReflectiveOperationException e )
        {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error");
        }
    }
}
