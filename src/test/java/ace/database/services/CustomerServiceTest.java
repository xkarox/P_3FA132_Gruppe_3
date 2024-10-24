package ace.database.services;

import ace.database.DatabaseConnection;
import ace.database.DbHelperService;
import ace.model.classes.Customer;
import ace.model.interfaces.ICustomer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CustomerServiceTest
{
    private Customer _testCustomer;
    private CustomerService _customerService;
    private DatabaseConnection _databaseConnection;

    @BeforeEach
    void SetUp() {
        this._testCustomer = new Customer(UUID.randomUUID(), "John", "Doe", LocalDate.now(), ICustomer.Gender.M);
        this._databaseConnection = new DatabaseConnection();
        this._databaseConnection.openConnection(DbHelperService.loadProperties());
        this._customerService = new CustomerService(this._databaseConnection);
    }

    @Test
    void updateTest() {
//        add origin customer
        this._customerService.add(this._testCustomer);

//        modify customer
        this._testCustomer.setFirstName("Peter");
        this._testCustomer.setLastName("Griffin");
        this._testCustomer.setBirthDate(LocalDate.of(2000, 11, 2));
        this._testCustomer.setGender(ICustomer.Gender.W);
//        update customer
        this._customerService.update(this._testCustomer);
//        get customer
        Customer updatedCustomer = this._customerService.getById(this._testCustomer.getId());
//        check if customer is updated correctly
        assertEquals(this._testCustomer, updatedCustomer, "Customer should be changed");
    }

    @AfterEach
    void tearDown() {

    }

}
