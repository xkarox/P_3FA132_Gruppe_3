package dev.hv.database.services;

import dev.hv.database.DatabaseConnection;
import dev.hv.database.provider.InternalServiceProvider;
import dev.hv.model.classes.User;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class UserService extends AbstractBaseService<User>
{
    public UserService(DatabaseConnection dbConnection, InternalServiceProvider provider)
    {
        super(dbConnection, provider);
    }

    @Override
    public User add(User item) throws ReflectiveOperationException, SQLException, IOException
    {
        if (item == null)
            throw new IllegalArgumentException("User is null and cannot be inserted.");

        String sqlStatement = new StringBuilder("INSERT INTO ")
                .append(item.getSerializedTableName())
                .append(" (id, username, passwordHash, createdAt, updatedAt) VALUES (?, ?, ?, ?, ?)")
                .toString();

        try(PreparedStatement stmt = this._dbConnection.newPrepareStatement(sqlStatement))
        {
            stmt.setString(1, item.getId().toString());
            stmt.setString(2, item.getUsername());
            stmt.setString(3, item.getPasswordHash());
            stmt.setDate(4, item.getCreatedAt() != null ? Date.valueOf(item.getCreatedAt()) : Date.valueOf(LocalDate.now()));
            stmt.setDate(5, item.getUpdatedAt() != null ? Date.valueOf(item.getUpdatedAt()) : null);
            this._dbConnection.executePreparedStatementCommand(stmt, 1);
        }

        return item;
    }

    @Bean
    @Override
    public User getById(UUID id) throws ReflectiveOperationException, SQLException
    {
        var result = this._dbConnection.getAllObjectsFromDbTableWithFilter(User.class, String.format("WHERE id = '%s'", id));
        if (result.size() > 1)
        {
            throw new RuntimeException(String.format("Expected size of result be equal to 1, but found %d", result.size()));
        }
        if (result.isEmpty())
            return null;
        return (User) result.getFirst();
    }

    public User getByUsername(String username) throws ReflectiveOperationException, SQLException
    {
        var result = this._dbConnection.getAllObjectsFromDbTableWithFilter(User.class, String.format("WHERE username = '%s'", username));
        if (result.size() > 1)
        {
            throw new RuntimeException(String.format("Expected size of result be equal to 1, but found %d", result.size()));
        }
        if (result.isEmpty())
            return null;
        return (User) result.getFirst();
    }

    @Override
    public List<User> getAll() throws ReflectiveOperationException, SQLException
    {
        return (List<User>) this._dbConnection.getAllObjectsFromDbTable(User.class);
    }

    @Override
    public User update(User item) throws SQLException
    {
        if (item.getId() == null)
            throw new IllegalArgumentException("Cannot update user without id");

        String sqlStatement = new StringBuilder("UPDATE ")
                .append(item.getSerializedTableName())
                .append(" SET username = ?, passwordHash = ?, createdAt = ?, updatedAt = ?")
                .toString();

        try (PreparedStatement stmt = this._dbConnection.newPrepareStatement(sqlStatement)) {
            stmt.setString(1, item.getUsername());
            stmt.setString(2, item.getPasswordHash());
            stmt.setDate(3, Date.valueOf(item.getCreatedAt()));
            stmt.setDate(4, Date.valueOf(LocalDate.now()));

            this._dbConnection.executePreparedStatementCommand(stmt, 1);
        }
        return item;
    }

    @Override
    public void remove(User item) throws IllegalArgumentException, SQLException
    {
        removeDbItem(item);
    }

    @Override
    public void close() throws Exception
    {
        if (this._provider != null){
            this._provider.releaseDbConnection(this._dbConnection);
            this._provider.releaseUserService(this);
        }
    }
}
