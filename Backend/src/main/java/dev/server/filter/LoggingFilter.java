package dev.server.filter;

import jakarta.annotation.Priority;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.IOException;
import java.util.UUID;

@Provider
@Priority(2)
public class LoggingFilter  implements ContainerRequestFilter, ContainerResponseFilter
{
    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException
    {
        logger.info("New incoming request with ID: {}", MDC.get("requestId"));
        logger.info("Request URI: {}", containerRequestContext.getUriInfo().getRequestUri());
        logger.info("Request Method: {}", containerRequestContext.getMethod());
    }

    @Override
    public void filter(ContainerRequestContext containerRequestContext, ContainerResponseContext containerResponseContext) throws IOException
    {
        logger.info("Request finished. Sending response with status: {}", containerResponseContext.getStatus());
    }
}
