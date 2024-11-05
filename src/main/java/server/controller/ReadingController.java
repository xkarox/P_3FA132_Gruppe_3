package server.controller;

import ace.database.ServiceProvider;
import ace.database.services.ReadingService;
import ace.model.classes.Reading;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;


@RestController
@RequestMapping(value = "/readings")
public class ReadingController
{
    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(method = RequestMethod.POST)
    public Reading postReading(@RequestBody Reading reading){
        if (reading == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reading cannot be null");
        }
        ReadingService rs = ServiceProvider.GetReadingService();
        if (reading.getId() == null)
        {
            reading.setId(UUID.randomUUID());
        }
        try
        {
            return rs.add(reading);
        }
        catch (Exception e)
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error adding reading", e);
        }
    }
}
