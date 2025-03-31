package dev.server.config;

import dev.hv.model.classes.Reading;
import dev.server.controller.*;
import dev.server.filter.CORSFilter;
import dev.server.filter.LoggingFilter;
import dev.server.filter.RequestIdFilter;
import jakarta.ws.rs.ApplicationPath;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;

@ApplicationPath("")
public class JerseyConfig extends ResourceConfig
{
    public JerseyConfig() {
        // Register controller
        packages("dev.hv.server.controller");
        register(CustomerController.class);
        register(ReadingController.class);
        register(DatabaseController.class);
        register(ExportController.class);

        // Register filter
        packages("dev.hv.server.filter");
        register(CORSFilter.class);
        register(RequestIdFilter.class);
        register(LoggingFilter.class);

        // Enable logging to debug registration
        property(ServerProperties.MONITORING_ENABLED, false);
    }
}
