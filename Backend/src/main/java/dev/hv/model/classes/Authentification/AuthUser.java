package dev.hv.model.classes.Authentification;

import dev.hv.database.services.UserPermissionService;
import dev.hv.model.enums.UserPermissions;
import dev.hv.model.enums.UserRoles;
import dev.hv.model.interfaces.IAuthUser;
import dev.hv.model.interfaces.IFieldInfo;
import dev.hv.model.interfaces.IDbItem;
import dev.provider.ServiceProvider;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class AuthUser implements IAuthUser
{
    @IFieldInfo(fieldName = "id", fieldType = String.class)
    private UUID _id;
    @IFieldInfo(fieldName = "username", fieldType = String.class)
    private String _username;
    @IFieldInfo(fieldName = "password", fieldType = String.class)
    private String _password;
    @IFieldInfo(fieldName = "role", fieldType = int.class)
    private UserRoles _role;

    private List<UserPermissions> _permissions;

    public AuthUser() { }

    public AuthUser(UUID id) {
        this._id = id;
    }

    public AuthUser(UUID id, String username, String password)
    {
        this._id = id;
        this._username = username;
        this._password = password;
    }

    public AuthUser(AuthUserDto user)
    {
        this._id = user.getId();
        this._username = user.getUsername();
        this._password = user.getPassword();
        this._role = user.getRole();
        this._permissions = user.getPermissions();
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
        return _id;
    }

    public void setId(UUID _id) {
        this._id = _id;
    }

    public UserRoles getRole() {
        return _role;
    }

    public void setRole(UserRoles _role) {
        this._role = _role;
    }

    public List<UserPermissions> getPermissions() {
        return _permissions;
    }

    public void setPermissions(List<UserPermissions> _permissionsList) {
        this._permissions = _permissionsList;
    }

    @Override
    public IDbItem dbObjectFactory(Object... args) throws SQLException, IOException, ReflectiveOperationException
    {
        this._id = UUID.fromString((String) args[0]);
        this._username = (String) args[1];
        this._password = (String) args[2];
        this._role = UserRoles.values()[(int) args[3]];

        try(UserPermissionService ups = ServiceProvider.getUserPermissionService()){
            this._permissions = ups.getAllById(this._id).stream().map(AuthUserPermissions::getPermission).filter(Objects::nonNull).toList();
        }

        return this;
    }

    @Override
    public String getSerializedStructure()
    {
        String strBuilder = "id UUID PRIMARY KEY NOT NULL," +
                "username VARCHAR(120) NOT NULL," +
                "password VARCHAR(120)," +
                "role INT NOT NULL";
        return strBuilder;
    }

    @Override
    public String getSerializedTableName()
    {
        return "authuser";
    }
}
