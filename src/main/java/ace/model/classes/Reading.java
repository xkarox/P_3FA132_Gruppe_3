package ace.model.classes;

import ace.model.interfaces.ICustomer;
import ace.model.interfaces.IReading;

import java.time.LocalDate;
import java.util.UUID;

public class Reading implements IReading
{
    private UUID _id;
    private String _comment;
    private Customer _customer;
    private LocalDate _dateOfReading;
    private KindOfMeter _kindOfMeter;
    private double _meterCount;
    private String _meterId;
    private boolean _substitute;

    public Reading()
    {
        this._id = UUID.randomUUID();
    }

    @Override
    public String getComment()
    {
        return this._comment;
    }

    @Override
    public ICustomer getCustomer()
    {
        return this._customer;
    }

    @Override
    public LocalDate getDateOfReading()
    {
        return this._dateOfReading;
    }

    @Override
    public KindOfMeter getKindOfMeter()
    {
        return this._kindOfMeter;
    }

    @Override
    public double getMeterCount()
    {
        return this._meterCount;
    }

    @Override
    public String getMeterId()
    {
        return this._meterId;
    }

    @Override
    public boolean getSubstitute()
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
}
