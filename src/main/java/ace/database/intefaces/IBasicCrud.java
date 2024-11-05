package ace.database.intefaces;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public interface IBasicCrud<T>
{
    T add(T item) throws IOException;          // Create

    T getById(UUID id);     // Read

    List<T> getAll();       // Get all items from db

    T update(T item);       // Update

    void remove(T item);    // Delete
}
