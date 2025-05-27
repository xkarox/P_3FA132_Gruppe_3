namespace P_3FA132_Gruppe_3_Frontend.Data.Models
{
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

        public static Gender ToGender(this string gender)
        {
            return gender switch
            {
                "Herr" => Gender.M,
                "Frau" => Gender.W,
                "k.A." => Gender.U,
                "M" => Gender.M,
                "U" => Gender.U,
                "W" => Gender.W,
                _ => Gender.U
            };
        }
    }
}
