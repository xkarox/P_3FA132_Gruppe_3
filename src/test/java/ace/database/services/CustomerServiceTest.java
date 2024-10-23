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

        assertNotNull(customerFromDb, "Customer should not be null after being added to the database.");

        assertEquals(_testCustomer.getId(), customerFromDb.getId(),
                new StringBuilder()
                        .append("Customer ID mismatch. ")
                        .append("Expected: ").append(_testCustomer.getId())
                        .append(", but got: ").append(customerFromDb.getId())
                        .toString()
        );

        assertEquals(_testCustomer.getFirstName(), customerFromDb.getFirstName(),
                new StringBuilder()
                        .append("First name mismatch. ")
                        .append("Expected: ").append(_testCustomer.getFirstName())
                        .append(", but got: ").append(customerFromDb.getFirstName())
                        .toString()
        );

        assertEquals(_testCustomer.getLastName(), customerFromDb.getLastName(),
                new StringBuilder()
                        .append("Last name mismatch. ")
                        .append("Expected: ").append(_testCustomer.getLastName())
                        .append(", but got: ").append(customerFromDb.getLastName())
                        .toString()
        );

        assertEquals(_testCustomer.getBirthDate(), customerFromDb.getBirthDate(),
                new StringBuilder()
                        .append("Birth date mismatch. ")
                        .append("Expected: ").append(_testCustomer.getBirthDate())
                        .append(", but got: ").append(customerFromDb.getBirthDate())
                        .toString()
        );

        assertEquals(_testCustomer.getGender(), customerFromDb.getGender(),
                new StringBuilder()
                        .append("Gender mismatch. ")
                        .append("Expected: ").append(_testCustomer.getGender())
                        .append(", but got: ").append(customerFromDb.getGender())
                        .toString()
        );
    }

}
