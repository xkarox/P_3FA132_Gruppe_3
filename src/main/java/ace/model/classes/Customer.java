package ace.model.classes;

import ace.model.interfaces.ICustomer;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public class Customer implements ICustomer
{
    private UUID id;
    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private Gender gender;

    public Customer(String firstName, String lastName, LocalDate birthDate, Gender gender)
    {
        this.id = UUID.randomUUID();
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
        this.gender = Objects.requireNonNullElse(gender, Gender.U);
    }

    // Constructor for initializing a customer from the database
    public Customer(UUID id, String firstName, String lastName, LocalDate birthDate, Gender gender)
    {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
        if (gender == null)
        {
            throw new IllegalArgumentException("Gender cannot be null");
        }
        this.gender = gender;
    }

    @Nullable
    @Override
    public LocalDate getBirthDate()
    {
        return this.birthDate;
    }

    @Nullable
    @Override
    public String getFirstName()
    {
        return this.firstName;
    }

    @Override
    public Gender getGender()
    {
        return this.gender;
    }

    @Nullable
    @Override
    public String getLastName()
    {
        return this.lastName;
    }

    @Override
    public void setBirthDate(LocalDate birtDate)
    {
        this.birthDate = birtDate;
    }

    @Override
    public void setFirstName(String firstName)
    {
        this.firstName = firstName;
    }

    @Override
    public void setGender(Gender gender)
    {
        this.gender = gender;
    }

    @Override
    public void setLastName(String lastName)
    {
        this.lastName = lastName;
    }

    @Override
    public UUID getId()
    {
        return this.id;
    }

    @Override
    public void setId(UUID id)
    {
        if (id == null)
        {
            throw new IllegalArgumentException("ID cannot be null");
        }
        this.id = id;
    }
}