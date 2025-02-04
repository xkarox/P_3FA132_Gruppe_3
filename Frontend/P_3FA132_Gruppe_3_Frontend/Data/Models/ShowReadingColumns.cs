namespace P_3FA132_Gruppe_3_Frontend.Data.Models;

public class ShowReadingColumns
{
    public bool ShowId { get; set; } = true;
    public bool ShowComment { get; set; } = true;
    public bool ShowCustomerId { get; set; } = true;
    public bool ShowCustomer { get; set; } = false;
    public bool ShowDateOfReading { get; set; } = true;
    public bool ShowKindOfMeter { get; set; } = true;
    public bool ShowMeterCount { get; set; } = true;
    public bool ShowMeterId { get; set; } = true;
    public bool ShowSubstitute { get; set; } = true;
}