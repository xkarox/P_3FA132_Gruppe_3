package ace.database.services;

import ace.database.DatabaseConnection;
import ace.model.classes.Reading;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ReadingService extends AbstractBaseService<Reading>
{
    protected ReadingService(DatabaseConnection dbConnection)
    {
        super(dbConnection);
    }


    @Override
    public Reading add(Reading item)
    {
        return null;
    }

    @Override
    public Reading getById(UUID id)
    {
        return null;
    }

    @Override
    public List<Reading> getAll()
    {
        return List.of();
    }

    @Override
    public Reading update(Reading item)
    {
        if (item.getId() == null)
        {
            throw new RuntimeException("Cannot update reading without id");
        }
        StringBuilder sb = new StringBuilder("UPDATE ");
        sb.append(item.getSerializedTableName()).append("SET ");
        sb.append("customer=").append(Objects.requireNonNull(item.getCustomer()).getId());
        sb.append(",dateOfReading=").append(item.getDateOfReading());
        sb.append(",kindOfMeter=").append(item.getKindOfMeter());
        sb.append(",meterCount=").append(item.getMeterCount());
        sb.append(",meterId=").append(item.getMeterId());
        sb.append(",substitute=").append(item.getSubstitute());
        sb.append(" WHERE Id=").append(item.getId());
        try
        {
            _dbConnection.executeSqlUpdateCommand(sb.toString(), 1);
            return item;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void remove(Reading item) {}
}
