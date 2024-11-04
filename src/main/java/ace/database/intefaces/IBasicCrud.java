package ace.database.intefaces;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public interface IBasicCrud<T>
{
    T add(T item) throws ReflectiveOperationException, SQLException;          // Create

    T getById(UUID id) throws ReflectiveOperationException, SQLException;     // Read

    List<T> getAll() throws ReflectiveOperationException, SQLException;       // Get all items from db

    T update(T item);       // Update

    void remove(T item);    // Delete
}
