package dev.hv.model.enums;

import java.util.List;

public enum UserPermissions
{
    READ,
    WRITE,
    Update,
    DELETE;

    public static UserPermissions translateHttpToUserPermission(String methodeType){
        return switch (methodeType)
        {
            case "POST" -> UserPermissions.WRITE;
            case "GET" -> UserPermissions.READ;
            case "PUT" -> UserPermissions.Update;
            case "DELETE" -> UserPermissions.DELETE;
            case null, default -> null;
        };
    }
}
