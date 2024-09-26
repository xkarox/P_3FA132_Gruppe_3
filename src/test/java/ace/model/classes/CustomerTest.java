package ace.model.classes;

import ace.model.interfaces.ICustomer;
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
    private final LocalDate _birthDate = LocalDate.of(2000, 1, 1);
    private final String _firstName = "John";
    private final String _lastName = "Doe";
    private final Gender _gender = Gender.M;

    @BeforeEach
    void setUp()
    {
        _customer = new Customer(this._firstName, this._lastName, this._birthDate, this._gender);
    }

    @BeforeAll
    static void beforeAll()
    {
        _nullCustomer = new Customer(null, null, null, null);
    }

    @Test
    void testGetBirthDate()
    {
        LocalDate birthDate = _customer.getBirthDate();
        assertEquals(this._birthDate, birthDate, "Birth date does not match the expected birth date");

        assertNull(_nullCustomer.getBirthDate(), "Birth date does not match the expected null value");
    }

    @Test
    void testGetFirstName()
    {
        String firstName = _customer.getFirstName();
        assertEquals(this._firstName, firstName, "First name does not match the expected first name");

        assertNull(_nullCustomer.getFirstName(), "First name does not match the expected null value");
    }

    @Test
    void testGetGender()
    {
        ICustomer.Gender gender = _customer.getGender();
        assertEquals(this._gender, gender, "Gender does not match the expected gender");

        assertNull(_nullCustomer.getGender(), "Gender does not match the expected null value");
    }

    @Test
    void testGetLastName()
    {
        String lastName = _customer.getLastName();
        assertEquals(this._lastName, lastName, "Last name does not match the expected last name");

        assertNull(_nullCustomer.getLastName(), "Last name does not match the expected null value");
    }

    @Test
    void testSetBirthDate()
    {
        LocalDate newBirthDate = LocalDate.of(2001, 1, 1);
        _customer.setBirthDate(newBirthDate);
        LocalDate birthDate = _customer.getBirthDate();
        assertEquals(newBirthDate, birthDate, "Birth date does not match the new birth date");

        _customer.setBirthDate(null);
        assertNull(_customer.getBirthDate(), "Birth date should be null after setting to null");
    }

    @Test
    void testSetFirstName()
    {
        String newFirstName = "Jane";
        _customer.setFirstName(newFirstName);
        String firstName = _customer.getFirstName();
        assertEquals(newFirstName, firstName, "First name does not match the new first name");
    }

    @Test
    void testSetGender()
    {
        ICustomer.Gender newGender = ICustomer.Gender.W;
        _customer.setGender(newGender);
        ICustomer.Gender gender = _customer.getGender();
        assertEquals(newGender, gender, "Gender does not match the new Gender");
    }

    @Test
    void testSetLastName()
    {
        String newLastName = "Smith";
        _customer.setLastName(newLastName);
        String lastName = _customer.getLastName();
        assertEquals(newLastName, lastName, "Last name does not match the new last name");
    }

    @Test
    void testGetId()
    {
        assertNotNull(_customer.getId(), "ID should not be null");
    }

    @Test
    void testSetId()
    {
        UUID newId = UUID.fromString("169e4567-e89b-69d3-a456-426914174001");
        _customer.setId(newId);
        assertNotNull(_customer.getId(), "ID should not be null after setting a new ID");

        assertThrows(IllegalArgumentException.class, () -> _customer.setId(null), "ID should not be null after setting to null");
    }
}
