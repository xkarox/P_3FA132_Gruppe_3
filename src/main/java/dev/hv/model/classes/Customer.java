package dev.hv.model.classes;

import dev.hv.model.ICustomer;
import dev.hv.model.decorator.IFieldInfo;
import dev.hv.model.interfaces.IDbItem;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;

public class Customer implements ICustomer
{
    @IFieldInfo(fieldName = "id", fieldType = String.class)
    private UUID _id;
    @IFieldInfo(fieldName = "firstName", fieldType = String.class)
    private String _firstName;
    @IFieldInfo(fieldName = "lastName", fieldType = String.class)
    private String _lastName;
    @IFieldInfo(fieldName = "birthDate", fieldType = LocalDate.class)
    private LocalDate _birthDate;
    @IFieldInfo(fieldName = "gender", fieldType = int.class)
    private Gender _gender;

    public Customer()
    {
        this._id = UUID.randomUUID();
        this._gender = Gender.U;
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

    @Override
    public IDbItem dbObjectFactory(Object... args)
    {

        LocalDate date = null;
        if (args[3] != null)
        {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            date = LocalDate.parse((String) args[3], formatter);
        }

        this._id = UUID.fromString((String) args[0]);
        this._firstName = (String) args[1];
        this._lastName = (String) args[2];
        this._birthDate = date;
        this._gender = Gender.values()[(int) args[4]];
        return this;
    }

    @Override
    public String getSerializedStructure()
    {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("id UUID PRIMARY KEY NOT NULL,");
        strBuilder.append("firstName VARCHAR(120) NOT NULL,");
        strBuilder.append("lastName VARCHAR(120) NOT NULL,");
        strBuilder.append("birthDate DATE,");
        strBuilder.append("gender int NOT NULL");
        return strBuilder.toString();
    }

    @Override
    public String getSerializedTableName()
    {
        return "customer";
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Customer item = (Customer) obj;

        return Objects.equals(this.getId(), item.getId())
                && Objects.equals(this.getBirthDate(), item.getBirthDate())
                && Objects.equals(this.getFirstName(), item.getFirstName())
                && Objects.equals(this.getLastName(), item.getLastName())
                && this.getGender() == item.getGender();
    }
}
