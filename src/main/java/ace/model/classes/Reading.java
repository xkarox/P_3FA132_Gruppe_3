package ace.model.classes;

import ace.model.interfaces.ICustomer;
import ace.model.interfaces.IReading;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.time.LocalDate;
import java.util.UUID;

public class Reading implements IReading
{
    private UUID _id;
    private String _comment;
    private Customer _customer;
    private LocalDate _dateOfReading;
    private KindOfMeter _kindOfMeter;
    private Double _meterCount;
    private String _meterId;
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
    public String getSerializedStructure()
    {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("id UUID PRIMARY KEY NOT NULL,");
        strBuilder.append("comment VARCHAR(120),");
        strBuilder.append("customerId UUID NOT NULL,");
        strBuilder.append("dateOfReading DATE NOT NULL,");
        strBuilder.append("kindOfMeter VARCHAR(10) NOT NULL,"); // Longest element in enum is 9 chars long
        strBuilder.append("meterCount REAL NOT NULL,");
        strBuilder.append("meterId VARCHAR(60) NOT NULL,"); // Check length
        strBuilder.append("substitute BOOLEAN NOT NULL");
//    strBuilder.append("FOREIGN KEY(customerId) REFERENCES customer(id)");
        return strBuilder.toString();
    }

    @Override
    public String getSerializedTableName()
    {
        return "reading";
    }
}
