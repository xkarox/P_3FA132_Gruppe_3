package ace.model.classes;

import ace.model.decorator.IFieldInfo;
import ace.model.interfaces.ICustomer;
import ace.model.interfaces.IDbItem;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.parse((String) args[3], formatter);

        this._id = UUID.fromString((String) args[0]);
        this._firstName = (String) args[1];
        this._lastName = (String) args[2];
        this._birthDate = date;
        this._gender = Gender.values() [(int) args[4]];
        return this;
    }

    @Override
    public String getSerializedStructure()
    {
        String structure = "";
        structure += "id UUID PRIMARY KEY NOT NULL,";
        structure += "firstName VARCHAR(120) NOT NULL,";
        structure += "lastName VARCHAR(120) NOT NULL,";
        structure += "birthDate DATE NOT NULL,";
        structure += "gender VARCHAR(1) NOT NULL";
        return structure;
    }

    @Override
    public String getSerializedTableName()
    {
        return "customer";
    }
}
