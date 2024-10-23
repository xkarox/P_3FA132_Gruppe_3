package ace.database.services;

import ace.database.DatabaseConnection;
import ace.model.classes.Reading;

import java.util.List;
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
        return null;
    }

    @Override
    public void remove(Reading item)
    {

    }
}
