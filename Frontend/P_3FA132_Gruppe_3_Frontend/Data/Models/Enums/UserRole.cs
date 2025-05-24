namespace P_3FA132_Gruppe_3_Frontend.Data.Models.Enums
{
    public enum UserRole
    {
        ADMIN,
        USER
    }
    public static class RoleExtensions
    {
        public static UserRole ToRole(this string kindOfMeterString)
        {
            return kindOfMeterString.ToUpper() switch
            {
                "ADMIN" => UserRole.ADMIN,
                "USER" => UserRole.USER,
                _ => throw new Exception("Role not found")
            };
        }
    }
}
