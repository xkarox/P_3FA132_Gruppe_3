package ace.model.classes;

import ace.model.interfaces.ICustomer;
import ace.model.interfaces.IReading;

import java.time.LocalDate;
import java.util.UUID;

public class Reading implements IReading
{
    private String comment;
    private Customer customer;
    private LocalDate dateOfReading;
    private KindOfMeter kindOfMeter;
    private double meterCount;
    private String meterId;
    private boolean substitute;

    @Override
    public String getComment()
    {
        return "";
    }

    @Override
    public ICustomer getCustomer()
    {
        return null;
    }

    @Override
    public LocalDate getDateOfReading()
    {
        return null;
    }

    @Override
    public KindOfMeter getKindOfMeter()
    {
        return null;
    }

    @Override
    public Double getMeterCount()
    {
        return 0.0;
    }

    @Override
    public String getMeterId()
    {
        return "";
    }

    @Override
    public Boolean getSubstitute()
    {
        return null;
    }

    @Override
    public String printDateOfReading()
    {
        return "";
    }

    @Override
    public void setComment(String comment)
    {

    }

    @Override
    public void setCustomer(ICustomer customer)
    {

    }

    @Override
    public void setDateOfReading(LocalDate dateOfReading)
    {

    }

    @Override
    public void setKindOfMeter(KindOfMeter kindOfMeter)
    {

    }

    @Override
    public void setMeterCount(Double meterCount)
    {

    }

    @Override
    public void setMeterId(String meterId)
    {

    }

    @Override
    public void setSubstitute(Boolean substitute)
    {

    }

    @Override
    public UUID getId()
    {
        return null;
    }

    @Override
    public void setId(UUID id)
    {

    }
}
