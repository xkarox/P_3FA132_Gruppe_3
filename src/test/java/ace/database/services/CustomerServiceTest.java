package ace.database.services;

import ace.model.classes.Customer;
import ace.model.interfaces.ICustomer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CustomerServiceTest
{
    private Customer _testCustomer;
    private CustomerService _customerService;
    @BeforeEach
    void SetUp() {
        this._testCustomer = new Customer(UUID.randomUUID(), "John", "Doe", LocalDate.now(), ICustomer.Gender.M);

    }

    @Test
    void testAdd() {
        this._customerService.add(this._testCustomer);

        Customer customerFromDb = this._customerService.getById(this._testCustomer.getId());
        assertNotNull(customerFromDb);
        assertEquals(_testCustomer.getId(), customerFromDb.getId());
        assertEquals(_testCustomer.getFirstName(), customerFromDb.getFirstName());
        assertEquals(_testCustomer.getLastName(), customerFromDb.getLastName());
        assertEquals(_testCustomer.getBirthDate(), customerFromDb.getBirthDate());
        assertEquals(_testCustomer.getGender(), customerFromDb.getGender());
    }

}
