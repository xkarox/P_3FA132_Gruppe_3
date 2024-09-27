package ace.database.intefaces;

import java.util.UUID;

public interface IBasicCrud<T>
{
    T add(T item);          // Create
    T getById(UUID id);     // Read
    T update(T item);       // Update
    void remove(T item);    // Delete
}
