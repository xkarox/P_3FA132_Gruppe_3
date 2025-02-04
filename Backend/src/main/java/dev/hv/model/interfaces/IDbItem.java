package dev.hv.model.interfaces;

import java.io.IOException;
import java.sql.SQLException;

public interface IDbItem
{
    // Used for automatic creation of db items
    IDbItem dbObjectFactory(Object... args) throws SQLException, IOException, ReflectiveOperationException;

    String getSerializedStructure();

    String getSerializedTableName();
}
