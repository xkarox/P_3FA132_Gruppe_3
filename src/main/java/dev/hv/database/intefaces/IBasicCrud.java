package dev.hv.database.intefaces;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public interface IBasicCrud<T>
{
    T add(T item) throws ReflectiveOperationException, SQLException, IOException;          // Create

    T getById(UUID id) throws ReflectiveOperationException, SQLException, IOException;     // Read

    List<T> getAll() throws ReflectiveOperationException, SQLException, IOException;       // Get all items from db

    T update(T item) throws SQLException;       // Update

    void remove(T item) throws IllegalArgumentException, SQLException;    // Delete
}
