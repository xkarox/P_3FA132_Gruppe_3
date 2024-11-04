package ace.services.logService;

public enum LogLevel
{
    INFO
            {
                @Override
                public String toString()
                {
                    return "\u001B[34mINFO\u001B[0m";
                }
            },
    DEBUG
            {
                @Override
                public String toString()
                {
                    return "\u001B[36mDEBUG\u001B[0m";
                }
            },
    WARNING
            {
                @Override
                public String toString()
                {
                    return "\u001B[33mWARNING\u001B[0m";
                }
            },
    ERROR
            {
                @Override
                public String toString()
                {
                    return "\u001B[31mERROR\u001B[0m";
                }
            }
}
