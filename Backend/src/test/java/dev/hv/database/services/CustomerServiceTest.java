package dev.hv.database.services;

import dev.hv.ResponseMessages;
import dev.hv.database.DatabaseConnection;
import dev.hv.database.DbHelperService;
import dev.hv.database.DbTestHelper;
import dev.hv.model.interfaces.ICustomer;
import dev.hv.model.interfaces.ICustomer.Gender;
import dev.hv.model.interfaces.IReading;
import dev.hv.model.classes.Customer;
import dev.hv.model.classes.Reading;
import dev.provider.ServiceProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CustomerServiceTest
{
    private Customer _testCustomer;
    private Reading _testReading;

    @BeforeAll
    static void OneTimeSetup() throws IOException
    {
        ServiceProvider.Services.dbConnectionPropertiesOverwrite(DbHelperService.loadProperties(DbTestHelper.loadTestDbProperties()));
        DbTestHelper.LoadTestServiceProvider();
    }

    @AfterAll
    static void OneTimeTearDown()
    {
        DbTestHelper.UnloadTestServiceProvider();
    }

    @BeforeEach
    void SetUp() throws IOException, SQLException
    {
        this._testCustomer = new Customer(UUID.randomUUID(), "John", "Doe", LocalDate.now(),
                ICustomer.Gender.M);
        this._testReading = new Reading(UUID.randomUUID(), "", this._testCustomer.getId(), null, LocalDate.now(),
                IReading.KindOfMeter.HEIZUNG, 1.69, "90-238-01sdf", false);

        try(DatabaseConnection dbCon = ServiceProvider.Services.getDatabaseConnection()){
            dbCon.openConnection(DbHelperService.loadProperties(DbTestHelper.loadTestDbProperties()));
            dbCon.removeAllTables();
            dbCon.createAllTables();
        }
    }

    @Test
    void testAdd() throws ReflectiveOperationException, SQLException, IOException
    {
        try (CustomerService cs = ServiceProvider.Services.getCustomerService())
        {
            try (ReadingService rs = ServiceProvider.Services.getReadingService())
            {
                cs.add(this._testCustomer);

                Customer customerFromDb = cs.getById(this._testCustomer.getId());

                assertNotNull(customerFromDb, "Customer should not be null after being added to the database.");
                assertEquals(this._testCustomer, customerFromDb, "Customer are not equal");
            }
        }
    }

    @Test
    void updateTest() throws ReflectiveOperationException, SQLException, IOException
    {
        try (CustomerService cs = ServiceProvider.Services.getCustomerService())
        {
            try (ReadingService rs = ServiceProvider.Services.getReadingService())
            {
                cs.add(this._testCustomer);

                this._testCustomer.setFirstName("Peter");
                this._testCustomer.setLastName("Griffin");
                this._testCustomer.setBirthDate(LocalDate.of(2000, 11, 2));
                this._testCustomer.setGender(ICustomer.Gender.W);
                cs.update(this._testCustomer);
                Customer updatedCustomer = cs.getById(this._testCustomer.getId());
                assertEquals(this._testCustomer, updatedCustomer, "Customer should be changed");
            }
        }
    }

    @Test
    void removeTest() throws ReflectiveOperationException, SQLException, IOException
    {
        try (CustomerService cs = ServiceProvider.Services.getCustomerService())
        {
            try (ReadingService rs = ServiceProvider.Services.getReadingService())
            {
                cs.add(this._testCustomer);
                this._testReading.setCustomer(this._testCustomer);
                rs.add(this._testReading);
                cs.remove(this._testCustomer);
                assertNull(cs.getById(this._testCustomer.getId()), "Should return null because the " +
                        "customer was deleted before");
                Reading reading = rs.getById(this._testReading.getId());
                assertNull(reading.getCustomer(), "Should return null because customer is already deleted");
            }
        }
    }

    @Test
    void getByIdTest() throws ReflectiveOperationException, SQLException, IOException
    {
        try (CustomerService cs = ServiceProvider.Services.getCustomerService())
        {
            try (ReadingService rs = ServiceProvider.Services.getReadingService())
            {
                var nullResult = cs.getById(UUID.randomUUID());
                assertNull(nullResult, "Because there are no items in the db");

                List<Customer> customers = createTestData();
                Customer customer = customers.getFirst();
                var result = cs.getById(customer.getId());
                assertEquals(customer, result, "Because the customer should exist");
            }
        }
    }

    @Test
    void getByIdSizeErrorTest() throws ReflectiveOperationException, SQLException, IOException
    {
        List<Customer> items = new ArrayList<>();
        items.add(new Customer());
        items.add(new Customer());

        Exception thrownException = new RuntimeException(ResponseMessages.ResultSizeError.toString(List.of(items.size())));

        DatabaseConnection mockConnection = mock(DatabaseConnection.class);
        when(mockConnection.getAllObjectsFromDbTableWithFilter(any(), anyString()))
                .thenAnswer(invocation -> items);

        var caughtException = assertThrows(RuntimeException.class,
                () -> new CustomerService(mockConnection).getById(new Customer().getId()));
        assertEquals(thrownException.getMessage(), caughtException.getMessage());
    }

    @Test
    void getAllTest() throws ReflectiveOperationException, SQLException, IOException
    {
        try (CustomerService cs = ServiceProvider.Services.getCustomerService())
        {
            try (ReadingService rs = ServiceProvider.Services.getReadingService())
            {
                var nullResult = cs.getAll();
                assertTrue(nullResult.isEmpty(), "Because there are no items in the db");

                List<Customer> customers = createTestData();
                customers.sort(Comparator.comparing(Customer::getId));

                var result = cs.getAll();

                result.sort(Comparator.comparing(Customer::getId));
                assertEquals(customers, result, "Because all customers should exist");
            }
        }
    }

    @Test
    void crudNullCheck() throws NoSuchFieldException, IllegalAccessException, SQLException, IOException
    {
        try (CustomerService cs = ServiceProvider.Services.getCustomerService())
        {
            try (ReadingService rs = ServiceProvider.Services.getReadingService())
            {
                Customer customer = _testCustomer;
                Field secretField = Customer.class.getDeclaredField("_id");
                secretField.setAccessible(true);
                secretField.set(customer, null);

                assertThrows(IllegalArgumentException.class, () -> cs.add(null));
                assertThrows(IllegalArgumentException.class, () -> cs.update(customer));
                assertThrows(IllegalArgumentException.class, () -> cs.remove(customer));
            }
        }
    }

    @Test
    void closeDisposeNullTest()
    {
        try(CustomerService con = new CustomerService(new DatabaseConnection()))
        {
            // Do nothing
        } catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Test
    void DateNullTest() throws ReflectiveOperationException, SQLException, IOException
    {
        try (CustomerService cs = ServiceProvider.Services.getCustomerService())
        {
            try (ReadingService rs = ServiceProvider.Services.getReadingService())
            {
                this._testCustomer.setBirthDate(null);
                cs.add(this._testCustomer);
                assertNull(cs.getById(this._testCustomer.getId()).getBirthDate(), "Because the date should be null");

                this._testCustomer.setBirthDate(LocalDate.now());
                cs.update(this._testCustomer);
                assertNotNull(cs.getById(this._testCustomer.getId()).getBirthDate(), "Because the date should not be null");

                this._testCustomer.setBirthDate(null);
                cs.update(this._testCustomer);
                assertNull(cs.getById(this._testCustomer.getId()).getBirthDate(), "Because the date should be null");
            }
        }
    }

    private List<Customer> createTestData() throws SQLException, IOException
    {
        List<Customer> items = new ArrayList<>();
        try (CustomerService cs = ServiceProvider.Services.getCustomerService())
        {
            try (ReadingService rs = ServiceProvider.Services.getReadingService())
            {
                items.add(new Customer(UUID.randomUUID(), "John", "Doe", LocalDate.now(), Gender.M));
                items.add(new Customer(UUID.randomUUID(), "Jane", "Doe", LocalDate.now().plusMonths(1), Gender.W));
                items.add(new Customer(UUID.randomUUID(), "James", "Doe", LocalDate.now().plusYears(2), Gender.M));
                items.add(new Customer(UUID.randomUUID(), "Juno", "Doe", LocalDate.now().minusWeeks(20), Gender.D));

                for (Customer item : items)
                {
                    cs.add(item);
                }
            }
        }
        return items;
    }
}
