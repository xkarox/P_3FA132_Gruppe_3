package server.controller;

import ace.database.ServiceProvider;
import ace.database.services.CustomerService;
import ace.model.classes.Customer;
import ace.services.logService.LogLevel;
import ace.services.logService.LogService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping(value = "/customers")
@ResponseStatus(HttpStatus.CREATED)
public class CustomerController {

    @RequestMapping(method = RequestMethod.POST)
    public Customer postCustomer(@RequestBody Customer customer)
    {
        CustomerService cs = ServiceProvider.GetCustomerService();
        if (customer.getId() == null)
        {
            customer.setId(UUID.randomUUID());
        }
        try
        {
            return cs.add(customer);
        }
        catch (Exception e)
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }
}
