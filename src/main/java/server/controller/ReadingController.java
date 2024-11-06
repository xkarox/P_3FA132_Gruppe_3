package server.controller;

import ace.Utils;
import ace.database.ServiceProvider;
import ace.database.services.ReadingService;
import ace.model.classes.Reading;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.sql.SQLException;
import java.util.UUID;


@RestController
@RequestMapping(value = "/readings")
public class ReadingController
{
    static ObjectMapper _objMapper = Utils.getObjectMapper();

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(method = RequestMethod.POST)
    public Reading addReading(@RequestBody String readingJson)
    {
        try
        {
            Reading reading = _objMapper.readValue(readingJson, Reading.class);
            ReadingService rs = ServiceProvider.GetReadingService();
            if (reading.getId() == null)
            {
                reading.setId(UUID.randomUUID());
            }
            reading = rs.add(reading);
            if (reading == null)
            {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid reading data provided");
            }
            return reading;
        }
        catch (Exception e)
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid reading data provided");
        }
    }
}
