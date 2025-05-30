package dev.hv.database.services;

import dev.hv.database.DatabaseConnection;
import dev.hv.database.DbHelperService;
import dev.hv.database.DbTestHelper;
import dev.hv.model.interfaces.ICustomer.Gender;
import dev.hv.model.interfaces.IReading;
import dev.hv.model.interfaces.IReading.KindOfMeter;
import dev.hv.model.classes.Customer;
import dev.hv.model.classes.Reading;
import dev.provider.ServiceProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class ReadingServiceTest
{

    private Reading _testReading;
    private Reading _testReadingWithoutCustomer;
    private Customer _testCustomer;

    @BeforeAll
    static void OneTimeSetup() throws IOException
    {
        ServiceProvider.Services.dbConnectionPropertiesOverwrite(DbHelperService.loadProperties(DbTestHelper.loadTestDbProperties()));
    }

    @BeforeEach
    void SetUp() throws IOException, SQLException
    {
        this._testCustomer = new Customer(UUID.randomUUID(), "John", "Doe"
                , LocalDate.now(), Gender.M);
        this._testReading = new Reading(UUID.randomUUID()
                , "Omae wa mou shindeiru!", this._testCustomer.getId()
                , null, LocalDate.now(), KindOfMeter.STROM
                , 1234.5, "10006660001", false);
        this._testReading.setCustomer(this._testCustomer);
        this._testReadingWithoutCustomer = new Reading(UUID.randomUUID()
                , "lalalala", null, null, LocalDate.now()
                , KindOfMeter.WASSER, 1823.293, "8231891239", true);
        DatabaseConnection _databaseConnection = new DatabaseConnection();
        _databaseConnection.openConnection(DbHelperService.loadProperties(DbTestHelper.loadTestDbProperties()));
        _databaseConnection.removeAllTables();
        _databaseConnection.createAllTables();
    }

    @Test
    void testAdd() throws ReflectiveOperationException, SQLException, IOException
    {
        try (CustomerService cs = ServiceProvider.Services.getCustomerService())
        {
            try (ReadingService rs = ServiceProvider.Services.getReadingService())
            {
                rs.add(this._testReading);
                Reading readingFromDb = rs.getById(this._testReading.getId());
                assertNotNull(readingFromDb, "Reading should not be null after being added to the database.");
                assertEquals(this._testReading, readingFromDb, "Readings are not the same");

                Customer createdCustomer = cs.getById(this._testReading.getCustomer().getId());
                assertNotNull(createdCustomer, "A customer should be created");
                assertEquals(createdCustomer, this._testReading.getCustomer());

                assertThrows(IllegalArgumentException.class, () -> rs.add(this._testReadingWithoutCustomer));
            }
        }
    }

    @Test
    void addRollback() throws SQLException, IOException, ReflectiveOperationException
    {
        try (CustomerService cs = ServiceProvider.Services.getCustomerService())
        {
            try (ReadingService rs = ServiceProvider.Services.getReadingService())
            {
                Connection spyCon = spy(rs._dbConnection.getConnection());
                doThrow(new SQLException("Test exception")).when(spyCon).commit();

                Field privateConnection = DatabaseConnection.class.getDeclaredField("_connection");
                privateConnection.setAccessible(true);
                privateConnection.set(rs._dbConnection, spyCon);

                assertThrows(SQLException.class, () -> rs.add(_testReading));
                assertEquals(0, cs.getAll().size());
                assertEquals(0, rs.getAll().size());

                _testReading.setId(UUID.randomUUID());
            }
        }
    }

    @Test
    void addBatch() throws SQLException, IOException, ReflectiveOperationException
    {
        Reading testReading1 = new Reading(UUID.randomUUID()
                , "Omae wa mou shindeiru!", null
                , null, LocalDate.now(), KindOfMeter.STROM
                , 1234.5, "10006660001", false);

        List<Reading> readings = new ArrayList<>(){{
            add(testReading1);
        }};
        try (CustomerService cs = ServiceProvider.Services.getCustomerService())
        {
            try (ReadingService rs = ServiceProvider.Services.getReadingService())
            {
                assertThrows(IllegalArgumentException.class, () -> rs.addBatch(null));
                assertThrows(IllegalArgumentException.class, () -> rs.addBatch(new ArrayList<>()));
                assertThrows(IllegalArgumentException.class, () -> rs.addBatch(readings));


                readings.clear();
                readings.add(_testReading);

                assertDoesNotThrow(() -> rs.addBatch(readings));
                assertEquals(1, cs.getAll().size());
                assertEquals(1, rs.getAll().size());

                readings.clear();
                testReading1.setCustomer(_testCustomer);
                readings.add(testReading1);
                rs.addBatch(readings);
                assertEquals(1, cs.getAll().size());
                assertEquals(2, rs.getAll().size());

                assertThrows(IllegalArgumentException.class, () -> rs.addBatch(readings));

                readings.getFirst().getCustomer().setId(UUID.randomUUID());
                readings.getFirst().setId(UUID.randomUUID());
                assertDoesNotThrow(() -> rs.addBatch(readings));
                assertEquals(2, cs.getAll().size());
                assertEquals(3, rs.getAll().size());
            }
        }
    }

    @Test
    void addBatchRollback() throws SQLException, IOException, ReflectiveOperationException
    {

        List<Reading> readings = new ArrayList<>();
        try (CustomerService cs = ServiceProvider.Services.getCustomerService())
        {
            try (ReadingService rs = ServiceProvider.Services.getReadingService())
            {
                Connection spyCon = spy(rs._dbConnection.getConnection());
                doThrow(new SQLException("Test exception")).when(spyCon).commit();

                Field privateConnection = DatabaseConnection.class.getDeclaredField("_connection");
                privateConnection.setAccessible(true);
                privateConnection.set(rs._dbConnection, spyCon);

                readings.add(_testReading);
                assertThrows(SQLException.class, () -> rs.addBatch(readings));
                assertEquals(0, cs.getAll().size());
                assertEquals(0, rs.getAll().size());
            }
        }
    }

    @Test
    void addBatch2() throws SQLException, IOException, ReflectiveOperationException
    {
        List<Reading> readings = new ArrayList<>();
        try (CustomerService cs = ServiceProvider.Services.getCustomerService())
        {
            try (ReadingService rs = ServiceProvider.Services.getReadingService())
            {
                readings.add(_testReading);

                rs.addBatch(readings);

                _testReading.setCustomer(_testCustomer);
                _testReading.setId(UUID.randomUUID());

                readings.clear();
                rs.add(_testReading);
                readings.add(_testReading);

                assertThrows(IllegalArgumentException.class, () -> rs.addBatch(readings));

                Reading reading = new Reading();
                reading.setDateOfReading(LocalDate.now());
                reading.setKindOfMeter(KindOfMeter.STROM);
                reading.setMeterCount(1234.5);
                reading.setSubstitute(true);
                reading.setCustomer(_testCustomer);
                readings.clear();
                readings.add(reading);
                assertThrows(SQLException.class, () -> rs.addBatch(readings));
            }
        }
    }

    @Test
    void addBatch3() throws SQLException, IOException, ReflectiveOperationException
    {
        List<Reading> readings = new ArrayList<>();
        try (CustomerService cs = ServiceProvider.Services.getCustomerService())
        {
            try (ReadingService rs = ServiceProvider.Services.getReadingService())
            {
                this._testReading = new Reading(UUID.randomUUID()
                        , "Omae wa mou shindeiru!", null
                        , null, LocalDate.now(), KindOfMeter.STROM
                        , 1234.5, "10006660001", false);


                _testReading.setCustomer(_testCustomer);
                readings.add(_testReading);
                _testReading.setId(UUID.randomUUID());
                readings.add(_testReading);
                Customer customer = new Customer(null, "asd", "asd", LocalDate.now(), Gender.U);
                _testReading.setCustomer(customer);
                readings.add(_testReading);

                assertDoesNotThrow(() -> rs.addBatch(readings));
            }
        }
    }

    @Test
    void addSqlTest() throws SQLException
    {
        DatabaseConnection mockConnection = mock(DatabaseConnection.class);
        when(mockConnection.newPrepareStatement(anyString())).thenThrow(SQLException.class);
        assertThrows(IllegalArgumentException.class, () -> new ReadingService(mockConnection).add(new Reading()));
    }

    @Test
    void updateTest() throws ReflectiveOperationException, SQLException, IOException
    {
        try (CustomerService cs = ServiceProvider.Services.getCustomerService())
        {
            try (ReadingService rs = ServiceProvider.Services.getReadingService())
            {
                cs.add(this._testCustomer);
                //        add origin reading
                rs.add(this._testReading);
                //        modify reading
                this._testReading.setComment("NANI?!");
                this._testReading.setDateOfReading(LocalDate.of(2000, 11, 2));
                this._testReading.setKindOfMeter(KindOfMeter.HEIZUNG);
                this._testReading.setMeterCount(98765.5);
                this._testReading.setMeterId("456738901");
                this._testReading.setSubstitute(true);
                //        update reading
                rs.update(this._testReading);
                //        get reading
                Reading updatedReading = rs.getById(this._testReading.getId());
                //        check if reading updated correctly
                assertEquals(this._testReading, updatedReading, "Reading should be changed");
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
                cs.add(this._testCustomer);
                rs.add(this._testReading);

                var nullResult = rs.getById(UUID.randomUUID());
                assertNull(nullResult, "Because there are no items in the db");

                var result = rs.getById(this._testReading.getId());
                assertEquals(this._testReading, result, "Because the customer should exist");
            }
        }
    }

    @Test
    void getReadingsByCustomerIdTest() throws ReflectiveOperationException, SQLException, IOException
    {
        Reading reading1 = new Reading();
        reading1.setId(UUID.randomUUID());
        reading1.setCustomer(this._testCustomer);
        reading1.setSubstitute(true);
        reading1.setDateOfReading(LocalDate.now());
        reading1.setKindOfMeter(IReading.KindOfMeter.STROM);
        reading1.setMeterCount(100);
        reading1.setMeterId("123");

        Reading reading2 = new Reading();
        reading2.setId(UUID.randomUUID());
        reading2.setCustomer(this._testCustomer);
        reading2.setSubstitute(true);
        reading2.setDateOfReading(LocalDate.now());
        reading2.setKindOfMeter(IReading.KindOfMeter.WASSER);
        reading2.setMeterCount(9999);
        reading2.setMeterId("789");

        try (ReadingService rs = ServiceProvider.Services.getReadingService()) {
            rs.add(reading1);
            rs.add(reading2);
            List<Reading> readings = rs.getReadingsByCustomerId(this._testCustomer.getId());
            assertEquals(2, readings.size(), "Should return 2 readings for the test customer");
            assertTrue(readings.contains(reading1), "Should contain the first test reading");
            assertTrue(readings.contains(reading2), "Should contain the second test reading");

            rs.remove(reading1);
            rs.remove(reading2);

        }
    }

    @Test
    void getReadingsByCustomerIdNull() throws ReflectiveOperationException, SQLException, IOException
    {
        DatabaseConnection mockConnection = mock(DatabaseConnection.class);
        when(mockConnection.getAllObjectsFromDbTableWithFilter(any(), anyString()))
                .thenReturn(new ArrayList<>());

        var result = new ReadingService(mockConnection).getReadingsByCustomerId(null);
        assertTrue(result.isEmpty(), "Because the id is null");
    }

    @Test
    void getByIdSizeErrorTest() throws ReflectiveOperationException, SQLException, IOException
    {
        List<Reading> items = new ArrayList<>();
        items.add(new Reading());
        items.add(new Reading());

        Exception thrownException = new RuntimeException(String.format("Expected size of result be equal to 1, but found %d", items.size()));

        DatabaseConnection mockConnection = mock(DatabaseConnection.class);
        when(mockConnection.getAllObjectsFromDbTableWithFilter(any(), anyString()))
                .thenAnswer(invocation -> items);

        var caughtException = assertThrows(RuntimeException.class,
                () -> new ReadingService(mockConnection).getById(new Reading().getId()));
        assertEquals(thrownException.getMessage(), caughtException.getMessage());
    }

    @Test
    void getAllTest() throws ReflectiveOperationException, SQLException, IOException
    {
        try (CustomerService cs = ServiceProvider.Services.getCustomerService())
        {
            try (ReadingService rs = ServiceProvider.Services.getReadingService())
            {
                cs.add(this._testCustomer);
                rs.add(this._testReading);

                Reading reading2 = new Reading(UUID.randomUUID()
                        , "no comment", this._testCustomer.getId()
                        , null, LocalDate.now(), KindOfMeter.HEIZUNG
                        , 999.9, "10009960001", true);
                reading2.setCustomer(this._testCustomer);

                rs.add(reading2);

                var result = rs.getAll();
                result.sort(Comparator.comparing(Reading::getId));
                assertEquals(2, result.size(), "Because there are 2 items");

                // Because Java is shit
                Reading reading1 = this._testReading;
                List<Reading> prepeared = new ArrayList<>()
                {
                    {
                        add(reading1);
                        add(reading2);
                    }
                };
                prepeared.sort(Comparator.comparing(Reading::getId));

                assertEquals(prepeared, result);
            }
        }
    }

    @Test
    void queryReadings() throws SQLException, IOException, ReflectiveOperationException
    {
        KindOfMeter meterType = this._testReading.getKindOfMeter();
        UUID customerId = this._testReading.getCustomerId();

        try (ReadingService rs = ServiceProvider.Services.getReadingService())
        {
            rs.add(this._testReading);

            Collection<Reading> readings =  rs.queryReadings(Optional.ofNullable(customerId), Optional.empty(), Optional.empty(), Optional.ofNullable(meterType));
            assertEquals(1, readings.size());
            assertTrue(readings.contains(this._testReading));
        }
    }

    @Test
    void queryReadings2() throws SQLException, IOException, ReflectiveOperationException
    {
        KindOfMeter meterType = this._testReading.getKindOfMeter();
        try (ReadingService rs = ServiceProvider.Services.getReadingService())
        {
            rs.add(this._testReading);
            this._testReading.setId(UUID.randomUUID());
            rs.add(this._testReading);

            Collection<Reading> readings =  rs.queryReadings(Optional.ofNullable(null), Optional.empty(), Optional.empty(), Optional.ofNullable(meterType));
            assertEquals(2, readings.size());
            assertTrue(readings.contains(this._testReading));
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
                rs.add(this._testReading);
                rs.remove(this._testReading);
                    Reading nullReading = rs.getById(this._testReading.getId());
                    assertNull(nullReading, "Should return null because the " +
                            "reading was deleted before");
            }
        }
    }

    @Test
    void crudNullCheck() throws NoSuchFieldException, IllegalAccessException, SQLException, IOException
    {

        try (ReadingService rs = ServiceProvider.Services.getReadingService())
        {
            Reading reading = _testReading;
            Field secretField = Reading.class.getDeclaredField("_id");
            secretField.setAccessible(true);
            secretField.set(reading, null);

            assertThrows(IllegalArgumentException.class, () -> rs.add(null));
            assertThrows(IllegalArgumentException.class, () -> rs.update(reading));
            assertThrows(IllegalArgumentException.class, () -> rs.remove(reading));
        }
    }

    @Test
    void closeDisposeNullTest()
    {
        try(ReadingService con = new ReadingService(new DatabaseConnection()))
        {
            // Do nothing
        } catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

}
