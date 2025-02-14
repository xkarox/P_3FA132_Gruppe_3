package dev.provider;
import dev.hv.database.DatabaseConnection;
import dev.hv.database.provider.InternalServiceProvider;
import dev.hv.database.services.AuthUserService;
import dev.hv.database.services.UserPermissionService;

// This class is just a wrapper for other classes to be used in a static context.
// Default values are random and represent nothing meaningful as of the moment of writing.
public class ServiceProvider
{
    public static InternalServiceProvider Services;

    public static AuthUserService getAuthUserService()
    {
        return new AuthUserService(new DatabaseConnection());
    }
    public static UserPermissionService getUserPermissionService() { return new UserPermissionService(new DatabaseConnection()); }

    static {
        Services = new InternalServiceProvider(100, 10, 10);
    }
}
