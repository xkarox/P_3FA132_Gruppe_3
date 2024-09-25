package ace.model.classes;

import ace.model.interfaces.ICustomer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CustomerTest
{
    private static Customer customer;

    @BeforeAll
    static void setUp()
    {
        UUID id = UUID.randomUUID();
        String firstName = "John";
        String lastName = "Doe";
        LocalDate birthDate = LocalDate.of(2000, 1, 1);
        ICustomer.Gender gender = ICustomer.Gender.M;

        customer = new Customer(id, firstName, lastName, birthDate, gender);
    }

    @Test
    void testGetBirthDate()
    {
        LocalDate birthDate = customer.getBirthDate();
        LocalDate expectedBirthDate = LocalDate.of(2000, 1, 1);
        assertEquals(expectedBirthDate, birthDate, "Birth date does not match the expected birth date");
    }

    @Test
    void testGetFirstName()
    {
        String firstName = customer.getFirstName();
        String expectedFirstName = "John";
        assertEquals(expectedFirstName, firstName, "First name does not match the expected first name");
    }

    @Test
    void testGetGender()
    {
        ICustomer.Gender gender = customer.getGender();
        ICustomer.Gender expectedGender = ICustomer.Gender.M;
        assertEquals(expectedGender, gender, "Gender does not match the expected gender");
    }

    @Test
    void testGetLastName()
    {
        String lastName = customer.getLastName();
        String expectedLastName = "Doe";
        assertEquals(expectedLastName, lastName, "Last name does not match the expected last name");
    }

    @Test
    void testSetBirthDate()
    {
        LocalDate newBirthDate = LocalDate.of(2001, 1, 1);
        customer.setBirthDate(newBirthDate);
        LocalDate birthDate = customer.getBirthDate();
        assertEquals(newBirthDate, birthDate, "Birth date does not match the new birth date");
    }

    @Test
    void testSetFirstName()
    {
        String newFirstName = "Jane";
        customer.setFirstName(newFirstName);
        String firstName = customer.getFirstName();
        assertEquals(newFirstName, firstName, "First name does not match the new first name");
    }

    @Test
    void testSetGender()
    {
        ICustomer.Gender newGender = ICustomer.Gender.W;
        customer.setGender(newGender);
        ICustomer.Gender gender = customer.getGender();
        assertEquals(newGender, gender, "Gender does not match the new Gender");
    }

    @Test
    void testSetLastName()
    {
        String newLastName = "Smith";
        customer.setLastName(newLastName);
        String lastName = customer.getLastName();
        assertEquals(newLastName, lastName, "Last name does not match the new last name");
    }

    @Test
    void testGetId()
    {
        UUID id = customer.getId();
        assertEquals(id, customer.getId(), "ID does not match the expected ID");
    }

    @Test
    void testSetId()
    {
        UUID newId = UUID.randomUUID();
        customer.setId(newId);
        UUID id = customer.getId();
        assertEquals(newId, id, "ID does not match the new ID");
    }
}
