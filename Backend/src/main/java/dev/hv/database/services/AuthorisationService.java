package dev.hv.database.services;

import dev.provider.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.MDC;

import java.sql.SQLException;
import java.util.Objects;
import java.util.UUID;

public class AuthorisationService
{
    public static boolean DoesAuthDbExistsWrapper(){
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

    private static boolean AuthDbFlag(){
        return (MDC.get("authDbExists") == null || MDC.get("authDbExists").equals("false"));
    }

    public static boolean IsUserAdmin(Logger logger)
    {
        if (AuthDbFlag() || Objects.equals(MDC.get("role"), "admin"))
            return true;

        logger.info("User is not admin");
        return false;
    }

    public static boolean CanUserAccessResource(UUID effectedUserId, Logger logger){
        if (AuthDbFlag() || Objects.equals(MDC.get("id"), effectedUserId.toString()))
            return true;
        return IsUserAdmin(logger);
    }
}
