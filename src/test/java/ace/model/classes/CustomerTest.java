package ace.model.classes;

import ace.model.interfaces.ICustomer.Gender;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class CustomerTest
{
    private Customer _customer;
    private static Customer _nullCustomer;
    private final UUID _id = UUID.fromString("169e4567-e89b-69d3-a456-426614174000");
    private final LocalDate _birthDate = LocalDate.of(2000, 1, 1);
    private final String _firstName = "John";
    private final String _lastName = "Doe";
    private final Gender _gender = Gender.M;

    @BeforeEach
    void setUp()
    {
        _customer = new Customer(this._id, this._firstName, this._lastName, this._birthDate, this._gender);
    }

    @BeforeAll
    static void beforeAll()
    {
        _nullCustomer = new Customer();
    }

    @Test
    void testGetBirthDate()
    {
        LocalDate birthDate = this._customer.getBirthDate();
        assertEquals(this._birthDate, birthDate, "Birth date does not match the expected birth date");

        assertNull(_nullCustomer.getBirthDate(), "Birth date does not match the expected null value");
    }

    @Test
    void testGetFirstName()
    {
        String firstName = this._customer.getFirstName();
        assertEquals(this._firstName, firstName, "First name does not match the expected first name");

        assertNull(_nullCustomer.getFirstName(), "First name does not match the expected null value");
    }

    @Test
    void testGetGender()
    {
        Gender gender = this._customer.getGender();
        assertEquals(this._gender, gender, "Gender does not match the expected gender");

        assertEquals(Gender.U, _nullCustomer.getGender(), "Gender does not match the expected Undefined Gender value");
    }

    @Test
    void testGetLastName()
    {
        String lastName = this._customer.getLastName();
        assertEquals(this._lastName, lastName, "Last name does not match the expected last name");

        assertNull(_nullCustomer.getLastName(), "Last name does not match the expected null value");
    }

    @Test
    void testSetBirthDate()
    {
        LocalDate newBirthDate = LocalDate.of(2001, 1, 1);
        this._customer.setBirthDate(newBirthDate);
        LocalDate birthDate = this._customer.getBirthDate();
        assertEquals(newBirthDate, birthDate, "Birth date does not match the new birth date");

        _customer.setBirthDate(null);
        assertNull(this._customer.getBirthDate(), "Birth date should be null after setting to null");
    }

    @Test
    void testSetFirstName()
    {
        String newFirstName = "Jane";
        this._customer.setFirstName(newFirstName);
        String firstName = this._customer.getFirstName();
        assertEquals(newFirstName, firstName, "First name does not match the new first name");
    }

    @Test
    void testSetGender()
    {
        Gender newGender = Gender.W;
        this._customer.setGender(newGender);
        Gender gender = this._customer.getGender();
        assertEquals(newGender, gender, "Gender does not match the new Gender");
    }

    @Test
    void testSetLastName()
    {
        String newLastName = "Smith";
        this._customer.setLastName(newLastName);
        String lastName = this._customer.getLastName();
        assertEquals(newLastName, lastName, "Last name does not match the new last name");
    }

    @Test
    void testGetId()
    {
        assertNotNull(this._customer.getId(), "ID should not be null");
    }

    @Test
    void testSetId()
    {
        UUID newId = UUID.fromString("169e4567-e89b-69d3-a456-426914174001");
        this._customer.setId(newId);
        assertNotNull(this._customer.getId(), "ID should not be null after setting a new ID");

        assertThrows(IllegalArgumentException.class, () -> this._customer.setId(null), "setID should throw an IllegalArgumentException when setting a null ID");
    }

    @Test
    void testGetSerializedStructure()
    {
        String expectedStructure = "";
        expectedStructure += "id UUID PRIMARY KEY NOT NULL,";
        expectedStructure += "firstName VARCHAR(120) NOT NULL,";
        expectedStructure += "lastName VARCHAR(120) NOT NULL,";
        expectedStructure += "birthDate DATE NOT NULL,";
        expectedStructure += "gender int NOT NULL";
        assertEquals(expectedStructure, this._customer.getSerializedStructure());
    }

    @Test
    void testGetSerializedTableName()
    {
        assertEquals("customer", this._customer.getSerializedTableName());
    }

    @Test
    void equalsTest()
    {
        Customer customer1 = new Customer(this._id, this._firstName, this._lastName, this._birthDate, this._gender);
        Customer customer2 = new Customer(this._id, this._firstName, this._lastName, this._birthDate, this._gender);

        Customer customer3 = new Customer(UUID.randomUUID(), this._firstName, this._lastName, this._birthDate, this._gender);
        Customer customer4 = new Customer(this._id, "Hans", this._lastName, this._birthDate, this._gender);
        Customer customer5 = new Customer(this._id, this._firstName, "Peter", this._birthDate, this._gender);
        Customer customer6 = new Customer(this._id, this._firstName, this._lastName, LocalDate.now().minusWeeks(2), this._gender);
        Customer customer7 = new Customer(this._id, this._firstName, this._lastName, this._birthDate, Gender.D);

        Reading reading1 = new Reading();

        assertEquals(customer1, customer2, "Because they should be the same");
        assertEquals(customer1, customer1, "Because they are the same");
        assertNotEquals(customer1, reading1, "Because equals should fail");
        assertNotEquals(customer1, null, "Because it is null");

        assertNotEquals(customer1, customer3, "Diff id");
        assertNotEquals(customer1, customer4, "Diff bday");
        assertNotEquals(customer1, customer5, "Diff firstName");
        assertNotEquals(customer1, customer6, "Diff lastName");
        assertNotEquals(customer1, customer7, "Diff gender");
    }

    @Test
    void constructorNullTest()
    {
        boolean exceptionThrown = false;
        try
        {
            new Customer(this._id, this._firstName, this._lastName, this._birthDate, null);
        } catch (IllegalArgumentException e)
        {
            exceptionThrown = true;
            assertEquals(e.getMessage(), "Gender cannot be null");
        }
        assertTrue(exceptionThrown, "Because the exception should have been triggert");
    }
}
