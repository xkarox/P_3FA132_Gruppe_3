package dev.hv.database.mocks;

import dev.hv.model.interfaces.IId;
import dev.hv.model.interfaces.IFieldInfo;
import dev.hv.model.interfaces.IDbItem;

import java.util.ArrayList;
import java.util.UUID;

public class MockTableObject2 implements IDbItem, IId
{
    @IFieldInfo(fieldName = "name", fieldType = ArrayList.class)
    public String name;

    @IFieldInfo(fieldName = "age", fieldType = int.class)
    public int age;

    private String _schema = """
            id INT PRIMARY KEY,
            name VARCHAR (50),
            age INT
            """;
    private String _tableName = "testing";

    public MockTableObject2()
    {
        this.name = "";
        this.age = 0;
    }

    public MockTableObject2(String name, int age)
    {
        this.name = name;
        this.age = age;
    }

    public void testSetSchema(String schema)
    {
        this._schema = schema;
    }

    public void testSetTableName(String tableName)
    {
        this._tableName = tableName;
    }

    @Override
    public IDbItem dbObjectFactory(Object... args)
    {
        this.name = (String) args[0];
        this.age = (int) args[1];
        return this;
    }

    @Override
    public String getSerializedStructure()
    {
        return this._schema;
    }

    @Override
    public String getSerializedTableName()
    {
        return this._tableName;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj instanceof MockTableObject other)
        {
            return this.age == other.age && this.name.equals(other.name);
        }
        return false;
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

