package dev.hv.model.interfaces;

public interface IDbItem
{
    // Used for automatic creation of db items
    IDbItem dbObjectFactory(Object... args);

    String getSerializedStructure();

    String getSerializedTableName();
}
