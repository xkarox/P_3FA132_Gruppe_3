public interface IDebugModeService
{
    bool IsDebugMode { get; }
}

public class DebugModeService : IDebugModeService
{
    public bool IsDebugMode
    {
        get
        {
#if DEBUG
            return true;
#else
            return false;
#endif
        }
    }
}