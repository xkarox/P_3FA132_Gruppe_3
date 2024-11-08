package dev.provider;

import dev.server.provider.ValidatorServiceProvider;

import dev.hv.database.provider.InternalServiceProvider;

// This class is just a wrapper for other classes to be used in a static context.
// Default values are random and represent nothing meaningful as of the moment of writing.
public class ServiceProvider
{
    public static InternalServiceProvider Services;
    public static ValidatorServiceProvider Validator;

    static {
        Services = new InternalServiceProvider(100, 10, 10);
        Validator = new ValidatorServiceProvider();
    }
}
