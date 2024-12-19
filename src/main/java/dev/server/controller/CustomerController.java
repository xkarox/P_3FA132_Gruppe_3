package dev.server.controller;

import dev.hv.ResponseMessages;
import dev.hv.database.services.ReadingService;
import dev.hv.model.classes.Reading;
import dev.provider.ServiceProvider;
import dev.hv.Utils;
import dev.hv.database.services.CustomerService;
import dev.hv.model.classes.Customer;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import dev.server.validator.CustomerJsonSchemaValidatorService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.UUID;


@CrossOrigin
@RestController
@RequestMapping(value = "/customers")
public class CustomerController {
    private void validateRequestData(String jsonString)
    {
        boolean invalidCustomer = CustomerJsonSchemaValidatorService.getInstance().validate(jsonString);
        if ( invalidCustomer )
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ResponseMessages.ControllerBadRequest.toString());
        }
    }

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(method = RequestMethod.POST)
    public String addCustomer(@RequestBody String customerJson)
    {
        this.validateRequestData(customerJson);
        try (CustomerService cs = ServiceProvider.Services.getCustomerService())
        {
            customerJson = Utils.unpackFromJsonString(customerJson, Customer.class);
            Customer customer = Utils.getObjectMapper().readValue(customerJson, Customer.class);

            if (customer.getId() == null)
            {
                customer.setId(UUID.randomUUID());
            }
            customer = cs.add(customer);
            return Utils.packIntoJsonString(customer, Customer.class);

        }
        catch (JsonProcessingException | SQLException | RuntimeException e)
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
    public ResponseEntity<String> updateCustomer(@RequestBody String customerJson)
    {
        this.validateRequestData(customerJson);
        try (CustomerService cs = ServiceProvider.Services.getCustomerService())
        {
            customerJson = Utils.unpackFromJsonString(customerJson, Customer.class);
            Customer customer = Utils.getObjectMapper().readValue(customerJson, Customer.class);
            Customer dbCustomer = cs.getById(customer.getId());
            if (dbCustomer == null)
            {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, ResponseMessages.ControllerNotFound.toString());
            }
            cs.update(customer);
            return new ResponseEntity<String>(ResponseMessages.ControllerUpdateSuccess.toString(), HttpStatus.OK);
        }
        catch (JsonProcessingException | ReflectiveOperationException | SQLException e)
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ResponseMessages.ControllerBadRequest.toString());
        }
        catch (IOException e)
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ResponseMessages.ControllerInternalError.toString());

        }
    }

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(method = RequestMethod.GET)
    public String getCustomers()
    {
        try (CustomerService cs = ServiceProvider.Services.getCustomerService())
        {
            Collection<Customer> customer = cs.getAll();
            return Utils.packIntoJsonString(customer, Customer.class);

        } catch (IOException | ReflectiveOperationException | SQLException e)
        {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error");
        }
    }

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public String getCustomer(@PathVariable("id") UUID id)
    {
        try (CustomerService cs = ServiceProvider.Services.getCustomerService())
        {
            Customer customer = cs.getById(id);

            if (customer == null)
            {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found in database");
            }

            return Utils.packIntoJsonString(customer, Customer.class);

        } catch (IOException | ReflectiveOperationException | SQLException e)
        {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error");
        }
    }

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public String deleteCustomer(@PathVariable("id") UUID id)
    {
        try (CustomerService cs = ServiceProvider.Services.getCustomerService(); ReadingService rs = ServiceProvider.Services.getReadingService())
        {
            Customer customer = cs.getById(id);

            if (customer == null)
            {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found in database");
            }

            cs.remove(customer);

            Collection<Reading> readings = rs.getReadingsByCustomerId(customer.getId());

            String customerJsonString = Utils.packIntoJsonString(customer, Customer.class);
            String readingJsonString = Utils.packIntoJsonString(readings, Reading.class);

            return Utils.mergeJsonString(customerJsonString, readingJsonString);


        } catch (IOException | ReflectiveOperationException | SQLException e)
        {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error");
        }
    }
}
