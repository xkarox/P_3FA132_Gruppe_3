namespace P_3FA132_Gruppe_3_Frontend.Data.Models.Enums
{
    public enum UserPermissions
    {
        READ, 
        WRITE, 
        UPDATE, 
        DELETE
    }

    public static class UserPermissionsExtensions
    {
        public static UserPermissions ToUserPermissions(this string userPermissionsString)
        {
            return userPermissionsString.ToUpper() switch
            {
                "READ" => UserPermissions.READ,
                "WRITE" => UserPermissions.WRITE,
                "UPDATE" => UserPermissions.UPDATE,
                "DELETE" => UserPermissions.DELETE,
                _ => throw new Exception("UserPermissions not found")
            };
        }
    }
}
