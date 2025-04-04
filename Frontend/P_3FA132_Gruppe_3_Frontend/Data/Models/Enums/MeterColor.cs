using P_3FA132_Gruppe_3_Frontend.Data.Models;
using P_3FA132_Gruppe_3_Frontend.Data.Models.Enums;

namespace P_3FA132_Gruppe_3_Frontend.Data.Enums;

public static class MeterColor
{
    public const string Strom = "rgba(255, 218, 0, 1)";
    public const string Wasser = "rgba(0, 149, 255, 1)";
    public const string Heizung = "rgba(255, 21, 0, 1)";
    public const string Unbekannt = "rgba(255, 0, 234, 1)";

    public static string GetByEnumValue(KindOfMeter kind)
    {
        switch (kind)
        {
            case KindOfMeter.STROM:
                return Strom;
            case KindOfMeter.WASSER:
                return Wasser;
            case KindOfMeter.HEIZUNG:
                return Heizung;
            case KindOfMeter.UNBEKANNT:
                return Unbekannt;
        }
        return Unbekannt;
    }
}