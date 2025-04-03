namespace P_3FA132_Gruppe_3_Frontend.Data.Models
{
    public enum KindOfMeter
    {
        HEIZUNG,
        STROM,
        UNBEKANNT,
        WASSER
    }

    public static class KindOfMeterExtensions
    {
        public static KindOfMeter ToKindOfMeter(this string kindOfMeterString)
        {
            return kindOfMeterString.ToUpper() switch
            {
                "HEIZUNG" => KindOfMeter.HEIZUNG,
                "STROM" => KindOfMeter.STROM,
                "WASSER" => KindOfMeter.WASSER,
                "UNBEKANNT" => KindOfMeter.UNBEKANNT,
                _ => KindOfMeter.UNBEKANNT
            };
        }

        public static string ToEmoji(this KindOfMeter kindOfMeter)
        {
            return kindOfMeter switch
            {
                KindOfMeter.HEIZUNG => "🔥",
                KindOfMeter.STROM => "⚡",
                KindOfMeter.UNBEKANNT => "❓",
                KindOfMeter.WASSER => "💧",
                _ => "❓"
            };
        }

        public static string GetUnit(this KindOfMeter kindOfMeter)
        {
            return kindOfMeter switch
            {
                KindOfMeter.HEIZUNG => "m\u00b3",
                KindOfMeter.STROM => "kWh",
                KindOfMeter.UNBEKANNT => "?",
                KindOfMeter.WASSER => "m\u00b3",
                _ => "❓"
            };
        }
    }
}
