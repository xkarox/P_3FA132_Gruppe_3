package ace.model.classes;

import ace.model.interfaces.ICustomer;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public class Customer implements ICustomer
{
    private UUID _id;
    private String _firstName;
    private String _lastName;
    private LocalDate _birthDate;
    private Gender _gender;

    public Customer()
    {
        this.id = UUID.randomUUID();
        this.gender = Gender.U;
    }

    // Constructor for initializing a customer from the database
    public Customer(UUID id, String firstName, String lastName, LocalDate birthDate, Gender gender)
    {
        this._id = id;
        this._firstName = firstName;
        this._lastName = lastName;
        this._birthDate = birthDate;
        if (gender == null)
        {
            throw new IllegalArgumentException("Gender cannot be null");
        }
        this._gender = gender;
    }

    @Nullable
    @Override
    public LocalDate getBirthDate()
    {
        return this._birthDate;
    }

    @Nullable
    @Override
    public String getFirstName()
    {
        return this._firstName;
    }

    @Override
    public Gender getGender()
    {
        return this._gender;
    }

    @Nullable
    @Override
    public String getLastName()
    {
        return this._lastName;
    }

    @Override
    public void setBirthDate(LocalDate birtDate)
    {
        this._birthDate = birtDate;
    }

    @Override
    public void setFirstName(String firstName)
    {
        this._firstName = firstName;
    }

    @Override
    public void setGender(Gender gender)
    {
        this._gender = gender;
    }

    @Override
    public void setLastName(String lastName)
    {
        this._lastName = lastName;
    }

    @Override
    public UUID getId()
    {
        return this._id;
    }

    @Override
    public void setId(UUID id)
    {
        if (id == null)
        {
            throw new IllegalArgumentException("ID cannot be null");
        }
        this._id = id;
    }
}
