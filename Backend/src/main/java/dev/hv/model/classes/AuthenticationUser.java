package dev.hv.model.classes;

import dev.hv.model.interfaces.IAuthInformation;
import dev.hv.model.interfaces.IFieldInfo;
import dev.hv.model.interfaces.IDbItem;

import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;

public class AuthenticationUser implements IAuthInformation
{
    @IFieldInfo(fieldName = "userId", fieldType = String.class)
    private UUID _userId;
    @IFieldInfo(fieldName = "username", fieldType = String.class)
    private String _username;
    @IFieldInfo(fieldName = "password", fieldType = String.class)
    private String _password;


    public AuthenticationUser() { }

    public AuthenticationUser(UUID id) {
        this._userId = id;
    }

    public AuthenticationUser(UUID userId, String username, String password)
    {
        this._userId = userId;
        this._username = username;
        this._password = password;
    }

    public AuthenticationUser(AuthUserDto user)
    {
        this._userId = user.getUserId();
        this._username = user.getUsername();
        this._password = user.getPassword();
    }

    public String getUsername() {
        return _username;
    }

    public void setUsername(String _username) {
        this._username = _username;
    }

    public String getPassword() {
        return _password;
    }

    public void setPassword(String _password) {
        this._password = _password;
    }

    public UUID getId() {
        return _userId;
    }

    public void setId(UUID _userId) {
        this._userId = _userId;
    }

    @Override
    public IDbItem dbObjectFactory(Object... args) throws SQLException, IOException, ReflectiveOperationException
    {
        this._userId = UUID.fromString((String) args[0]);
        this._username = (String) args[1];
        this._password = (String) args[2];
        return this;
    }

    @Override
    public String getSerializedStructure()
    {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("id UUID PRIMARY KEY NOT NULL,");
        strBuilder.append("username VARCHAR(120) NOT NULL,");
        strBuilder.append("password VARCHAR(120)");
        return strBuilder.toString();
    }

    @Override
    public String getSerializedTableName()
    {
        return "AuthenticationInformation";
    }
}
