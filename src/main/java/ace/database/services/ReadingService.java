package ace.database.services;

import ace.database.DatabaseConnection;
import ace.database.provider.InternalServiceProvider;
import ace.database.provider.ServiceProvider;
import ace.model.classes.Customer;
import ace.model.classes.Reading;

import java.io.IOException;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ReadingService extends AbstractBaseService<Reading>
{
    public ReadingService(DatabaseConnection dbConnection, InternalServiceProvider provider)
    {
        super(dbConnection, provider);
    }

    public ReadingService(DatabaseConnection dbConnection)
    {
        super(dbConnection, null);
    }

    @Override
    public Reading add(Reading item) throws IOException
    {
        if (item == null)
        {
            throw new RuntimeException("Reading is null and cannot be inserted.");
        }

        String sqlStatement = "INSERT INTO " + item.getSerializedTableName() +
                " (id, comment, customerId, dateOfReading, kindOfMeter, meterCount, meterId, substitute) VALUES (?, ?, ?, ?, ?, ?, ?, ?);";

        try (PreparedStatement statement = this._dbConnection.newPrepareStatement(sqlStatement))
        {
            statement.setObject(1, item.getId());
            statement.setString(2, item.getComment());
            if (item.getCustomer() == null)
            {
                return null;
            } else
            {
                try(CustomerService customerService = ServiceProvider.GetCustomerService()){
                    Customer existingCustomer = customerService.getById(item.getCustomer().getId());

                    // customer does not exists
                    if (existingCustomer == null)
                    {
                        customerService.add((Customer) item.getCustomer());
                    }

                    statement.setObject(3, item.getCustomer().getId());
                }
            }
            statement.setDate(4, Date.valueOf(item.getDateOfReading()));
            assert item.getKindOfMeter() != null;
            statement.setString(5, String.valueOf(item.getKindOfMeter().ordinal()));
            statement.setDouble(6, item.getMeterCount());
            statement.setString(7, item.getMeterId());
            statement.setBoolean(8, item.getSubstitute());
            this._dbConnection.executePreparedStatementCommand(statement);
        } catch (SQLException e)
        {
            throw new RuntimeException(e);
        }

        return item;
    }

    @Override
    public Reading getById(UUID id)
    {
        var result = this._dbConnection.getAllObjectsFromDbTableWithFilter(new Reading(), String.format("WHERE id = '%s'", id));
        if (result.size() > 1)
        {
            throw new RuntimeException(String.format("Expected size of result be equal to 1, but found %d", result.size()));
        }
        if (result.isEmpty())
            return null;
        return (Reading) result.getFirst();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Reading> getAll()
    {
        return (List<Reading>) this._dbConnection.getAllObjectsFromDbTable(new Reading());
    }

    @Override
    public Reading update(Reading item)
    {
        if (item.getId() == null)
        {
            throw new RuntimeException("Cannot update reading without id");
        }
        StringBuilder sb = new StringBuilder("UPDATE ");
        sb.append(item.getSerializedTableName()).append(" SET");
        sb.append(" customerId='").append(Objects.requireNonNull(item.getCustomer()).getId());
        sb.append("' ,comment='").append(item.getComment());
        sb.append("' ,dateOfReading='").append(item.getDateOfReading());
        assert item.getKindOfMeter() != null;
        sb.append("' ,kindOfMeter='").append(item.getKindOfMeter().ordinal());
        sb.append("' ,meterCount='").append(item.getMeterCount());
        sb.append("' ,meterId='").append(item.getMeterId());
        sb.append("' ,substitute=").append(item.getSubstitute().toString());
        sb.append(" WHERE Id='").append(item.getId()).append("';");
        try
        {
            _dbConnection.executeSqlUpdateCommand(sb.toString(), 1);
            return item;
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void remove(Reading item)
    {
        String delStatement = new StringBuilder("DELETE FROM ").append(item.getSerializedTableName())
                .append(" WHERE id=?").toString();
        if (item.getId() == null)
        {
            throw new RuntimeException("Cannot delete a reading without id");
        }
        try
        {
            PreparedStatement preparedStatement = _dbConnection.getConnection().prepareStatement(delStatement);
            preparedStatement.setString(1, item.getId().toString());
            preparedStatement.executeUpdate();
        } catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close()
    {
        if (this._provider != null)
        {
            this._provider.releaseDbConnection(this._dbConnection);
            this._provider.releaseReadingService(this);
        }
    }
}
