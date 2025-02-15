using Blazing.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.ComponentModel;
using P_3FA132_Gruppe_3_Frontend.Data.Models;
using P_3FA132_Gruppe_3_Frontend.Data.Models.Classes;
using P_3FA132_Gruppe_3_Frontend.Data.Models.Classes.Analytics;
using P_3FA132_Gruppe_3_Frontend.Data.Services;

namespace P_3FA132_Gruppe_3_Frontend.Data.ViewModels;

public partial class AnalyticsViewModel(
    CustomerService customerService,
    ReadingService readingService
    ) : ViewModelBase
{
    [ObservableProperty] private int _customerCount;
    [ObservableProperty] private List<Reading>? _readings;
    [ObservableProperty] private List<ReadingTypeData> _readingData = new();

    [ObservableProperty] private bool _loading = false;
    
    public override async void OnInitialized()
    {
        Loading = true;
        
        var customers = await customerService.GetAll();
        CustomerCount = customers!.Count();

        var readings = await readingService.QueryReading(new ReadingQuery());
        Readings = readings.ToList();
        
        foreach (var type in Enum.GetValues<KindOfMeter>())
        {
            var readingTypeData = new ReadingTypeData
            {
                Type = type,
                Value = Readings.Count(item => item.KindOfMeter == type)
            };
            ReadingData.Add(readingTypeData);
        }

        Loading = false;
    }
}