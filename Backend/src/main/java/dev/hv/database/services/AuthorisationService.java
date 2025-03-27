package dev.hv.database.services;

import dev.hv.model.enums.UserRoles;
import dev.provider.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.sql.SQLException;
import java.util.Objects;
import java.util.UUID;

public class AuthorisationService
{
    private static final Logger logger = LoggerFactory.getLogger(AuthorisationService.class);

    public static boolean DoesAuthDbExistsWrapper()
    {
        try(AuthUserService authUserService = ServiceProvider.getAuthUserService()){
            if (!authUserService.checkIfAuthDatabaseExists())
                return false;
        } catch (SQLException e)
        {
            if(e.getMessage().contains("'homeautomation.authenticationinformation' doesn't exist"))
                return false;
            throw new RuntimeException(e);
        }
        return true;
    }

    private static boolean AuthDbFlag()
    {
        return (MDC.get("authDbExists") == null || MDC.get("authDbExists").equals("false"));
    }

    public static boolean IsUserAdmin()
    {
        if (AuthDbFlag() || Objects.equals(MDC.get("role"), UserRoles.ADMIN.toString()))
            return true;

        logger.info("User is not a admin");
        return false;
    }

    public static boolean CanUserAccessResource(UUID effectedUserId)
    {
        if (AuthDbFlag() || Objects.equals(MDC.get("id"), effectedUserId.toString()))
            return true;
        return IsUserAdmin();
    }

    public static boolean CanResourceBeAccessed()
    {
        if (AuthDbFlag())
            return true;
        return IsUserAdmin();
    }
}
