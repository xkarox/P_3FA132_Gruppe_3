package ace.model.classes;

import ace.model.decorator.IFieldInfo;
import ace.model.interfaces.ICustomer;
import ace.model.interfaces.IDbItem;
import ace.model.interfaces.IReading;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.time.LocalDate;
import java.util.UUID;

public class Reading implements IReading
{
    @IFieldInfo(fieldName = "id", fieldType = String.class)
    private UUID _id;
    @IFieldInfo(fieldName = "comment", fieldType = String.class)
    private String _comment;
    @IFieldInfo(fieldName = "customerId", fieldType = String.class)
    private Customer _customer;
    @IFieldInfo(fieldName = "dateOfReading", fieldType = LocalDate.class)
    private LocalDate _dateOfReading;
    @IFieldInfo(fieldName = "kindOfMeter", fieldType = String.class)
    private KindOfMeter _kindOfMeter;
    @IFieldInfo(fieldName = "meterCount", fieldType = Double.class)
    private Double _meterCount;
    @IFieldInfo(fieldName = "meterId", fieldType = String.class)
    private String _meterId;
    @IFieldInfo(fieldName = "substitute", fieldType = Boolean.class)
    private Boolean _substitute;

    public Reading()
    {
        this._id = UUID.randomUUID();
    }

    @Nullable
    @Override
    public String getComment()
    {
        return this._comment;
    }

    @Nullable
    @Override
    public ICustomer getCustomer()
    {
        return this._customer;
    }

    @Nullable
    @Override
    public LocalDate getDateOfReading()
    {
        return this._dateOfReading;
    }

    @Nullable
    @Override
    public KindOfMeter getKindOfMeter()
    {
        return this._kindOfMeter;
    }

    @Override
    public Double getMeterCount()
    {
        return this._meterCount;
    }

    @Nullable
    @Override
    public String getMeterId()
    {
        return this._meterId;
    }

    @Override
    public Boolean getSubstitute()
    {
        return this._substitute;
    }

    @Override
    public String printDateOfReading()
    {
        return this._dateOfReading.toString();
    }

    @Override
    public void setComment(String comment)
    {
        this._comment = comment;
    }

    @Override
    public void setCustomer(ICustomer customer)
    {
        this._customer = (Customer) customer;
    }

    @Override
    public void setDateOfReading(LocalDate dateOfReading)
    {
        this._dateOfReading = dateOfReading;
    }

    @Override
    public void setKindOfMeter(KindOfMeter kindOfMeter)
    {
        this._kindOfMeter = kindOfMeter;
    }

    @Override
    public void setMeterCount(double meterCount)
    {
        this._meterCount = meterCount;
    }

    @Override
    public void setMeterId(String meterId)
    {
        this._meterId = meterId;
    }

    @Override
    public void setSubstitute(boolean substitute)
    {
        this._substitute = substitute;
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
        return null;
    }

    @Override
    public String getSerializedStructure()
    {
        String structure = "";
        structure += "id UUID PRIMARY KEY NOT NULL,";
        structure += "comment VARCHAR(120),";
        structure += "customerId UUID NOT NULL,";
        structure += "dateOfReading DATE NOT NULL,";
        structure += "kindOfMeter VARCHAR(10) NOT NULL,"; // Longest element in enum is 9 chars long
        structure += "meterCount REAL NOT NULL,";
        structure += "meterId VARCHAR(60) NOT NULL,"; // Check length
        structure += "substitute BOOLEAN NOT NULL";
//        structure += "FOREIGN KEY(customerId) REFERENCES customer(id)";
        return structure;
    }

    @Override
    public String getSerializedTableName()
    {
        return "reading";
    }
}
