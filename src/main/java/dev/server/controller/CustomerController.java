package dev.server.controller;

import dev.provider.ServiceProvider;
import dev.hv.Utils;
import dev.hv.database.provider.InternalServiceProvider;
import dev.hv.database.services.CustomerService;
import dev.hv.model.classes.Customer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import dev.server.validator.CustomerJsonSchemaValidatorService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;


@RestController
@RequestMapping(value = "/customers")
public class CustomerController {
    private static ObjectMapper _objMapper = Utils.getObjectMapper();
    private static InternalServiceProvider _serviceProvider = ServiceProvider.Services;

    public static void setObjectMapper(ObjectMapper objectMapper)
    {
        _objMapper = objectMapper;
    }

    public static void setServiceProvider(InternalServiceProvider serviceProvider)
    {
        _serviceProvider = serviceProvider;
    }

    private void validateRequestData(String jsonString)
    {
        CustomerJsonSchemaValidatorService customerValidator = ServiceProvider.Validator.getCustomerValidator();
        boolean invalidCustomer = !customerValidator.validate(jsonString);
        if ( invalidCustomer )
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid customer data provided");
        }
    }

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(method = RequestMethod.POST)
    public String addCustomer(@RequestBody String customerJson)
    {
        this.validateRequestData(customerJson);
        try
        {
            customerJson = Utils.unpackFromJsonString(customerJson, Customer.class);
            Customer customer = _objMapper.readValue(customerJson, Customer.class);
            CustomerService cs = _serviceProvider.getCustomerService();
            if (customer.getId() == null)
            {
                customer.setId(UUID.randomUUID());
            }
            customer = cs.add(customer);
            return Utils.packIntoJsonString(customer, Customer.class);

        }
        catch (JsonProcessingException | SQLException | RuntimeException e)
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid customer data provided");
        }
        catch (IOException e)
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Internal Server IOError");
        }
    }

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(method = RequestMethod.PUT)
    public ResponseEntity<String> updateCustomer(@RequestBody String customerJson)
    {
        this.validateRequestData(customerJson);
        try
        {
            customerJson = Utils.unpackFromJsonString(customerJson, Customer.class);
            Customer customer = _objMapper.readValue(customerJson, Customer.class);
            CustomerService cs = _serviceProvider.getCustomerService();
            Customer dbCustomer = cs.getById(customer.getId());
            if (dbCustomer == null)
            {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found in database");
            }
            cs.update(customer);
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
