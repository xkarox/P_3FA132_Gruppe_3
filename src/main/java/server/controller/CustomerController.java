package server.controller;

import ace.Utils;
import ace.database.ServiceProvider;
import ace.database.services.CustomerService;
import ace.model.classes.Customer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.sql.SQLException;
import java.util.UUID;

@RestController
@RequestMapping(value = "/customers")
public class CustomerController {
    static ObjectMapper _objMapper = Utils.getObjectMapper();

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(method = RequestMethod.POST)
    public Customer postCustomer(@RequestBody Customer customer)
    public Customer addCustomer(@RequestBody String customerJson)
    {
        try
        {
            Customer customer = _objMapper.readValue(customerJson, Customer.class);
            CustomerService cs = ServiceProvider.GetCustomerService();
            if (customer.getId() == null)
            {
                customer.setId(UUID.randomUUID());
            }
            return cs.add(customer);
        }
        catch (JsonProcessingException | SQLException | RuntimeException e)
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid customer data provided");
        }
    }
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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error adding customer", e);
        }
    }
}
