
public enum Gender
{
    D, // divers
    M, // männlich
    U, // unbekannt
    W // weiblich
}
public static class GenderExtensions
{
    public static string ToEmoji(this Gender gender)
    {
        return gender switch
        {
            Gender.D => "🌈",
            Gender.M => "👨",
            Gender.U => "❓",
            Gender.W => "👩",
            _ => "❓"
        };
    }

    public static string ToDescriptionString(this Gender gender)
    {
        return gender switch
        {
            Gender.D => "Diverse",
            Gender.M => "Male",
            Gender.U => "Unknown",
            Gender.W => "Female",
            _ => "Unknown"
        };
    }
}
