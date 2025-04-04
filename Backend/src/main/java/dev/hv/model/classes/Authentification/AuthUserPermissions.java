package dev.hv.model.classes.Authentification;


import dev.hv.model.classes.Customer;
import dev.hv.model.enums.UserPermissions;
import dev.hv.model.interfaces.IAuthPermissions;
import dev.hv.model.interfaces.IDbItem;
import dev.hv.model.interfaces.IFieldInfo;

import java.util.Objects;
import java.util.UUID;

public class AuthUserPermissions implements IAuthPermissions
{
    @IFieldInfo(fieldName = "id", fieldType = String.class)
    private UUID _id;
    @IFieldInfo(fieldName = "permission", fieldType = int.class)
    private UserPermissions _permission;

    public AuthUserPermissions() { }
    public AuthUserPermissions(UUID id, UserPermissions permission)
    {
        _id = id;
        _permission = permission;
    }

    public UserPermissions getPermission()
    {
        return _permission;
    }

    public void setPermission(UserPermissions permission)
    {
        _permission = permission;
    }


    @Override
    public IDbItem dbObjectFactory(Object... args)
    {
        this._id = UUID.fromString(args[0].toString());
        this._permission = UserPermissions.values()[(int) args[1]];
        return this;
    }

    @Override
    public String getSerializedStructure()
    {
        String strBuilder = "id UUID NOT NULL," +
                "permission INT NOT NULL";
        return strBuilder;
    }

    @Override
    public String getSerializedTableName()
    {
        return "userroles";
    }

    @Override
    public UUID getId()
    {
        return _id;
    }

    @Override
    public void setId(UUID id)
    {
        _id = id;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        AuthUserPermissions item =(AuthUserPermissions) obj;

        return Objects.equals(this.getId(), item.getId())
                && Objects.equals(this.getPermission(), item.getPermission());
    }
}
