package dev.hv.model.enums;

public enum UserPermissions
{
    READ,
    WRITE,
    UPDATE,
    DELETE;

    public static UserPermissions translateHttpToUserPermission(String methodeType){
        return switch (methodeType)
        {
            case "POST" -> UserPermissions.WRITE;
            case "GET" -> UserPermissions.READ;
            case "PUT" -> UserPermissions.UPDATE;
            case "DELETE" -> UserPermissions.DELETE;
            case null, default -> null;
        };
    }
}
