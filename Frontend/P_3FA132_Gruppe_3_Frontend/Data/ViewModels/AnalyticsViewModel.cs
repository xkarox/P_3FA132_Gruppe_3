using Blazing.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.ComponentModel;
using P_3FA132_Gruppe_3_Frontend.Data.Models;
using P_3FA132_Gruppe_3_Frontend.Data.Models.Classes;
using P_3FA132_Gruppe_3_Frontend.Data.Models.Classes.Analytics;
using P_3FA132_Gruppe_3_Frontend.Data.Models.Enums;
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
    [ObservableProperty] private Reading? _latestWaterReading;
    [ObservableProperty] private Reading? _latestGasReading;
    [ObservableProperty] private Reading? _latestElectricityReading;

    [ObservableProperty] private List<Reading>? _usagePerMonthReadings;

    [ObservableProperty] private bool _loading = false;

    [ObservableProperty] public static string _readingCountSvgPath =
        "M7.375 21.025q-.9-.025-1.713-.462t-1.537-1.288q-1-1.2-1.562-2.862T2 " +
        "13q0-2.075.788-3.9t2.137-3.175T8.1 3.788T12 3t3.9.8t3.175 2.175T21.213 " +
        "9.2T22 13.175q0 1.925-.625 3.6T19.6 19.6q-.7.7-1.475 1.063t-1.575.362q-.45 " +
        "0-.9-.112t-.9-.338l-1.4-.7q-.3-.15-.638-.225T12 19.575t-.712.075t-." +
        "638.225l-1.4.7q-.475.25-.937.363t-.938.087m.05-2q.225 0 .463-.05t.462-.175l1.4-.7q." +
        "525-.275 1.088-.4t1.137-.125t1.15.125t1.1.4l1.425.7q.225.125.45.175t.45.05q.475 0 .9-.25t" +
        ".85-.75q.8-.95 1.25-2.275t.45-2.725q0-3.35-2.325-5.687T12 5T6.325 7.35T4 13.05q0 1.425.462 " +
        "2.775T5.75 18.1q.425.5.825.713t.85.212M12 15q.825 0 1.413-.588T14 13q0-.2-.038-.4t-.112-.4l1.25" +
        "-1.675q.275.35.45.712t.3.763t.375.7t.65.3q.5 0 .788-.438t.162-.962q-.5-2.025-2.125-3.312T12" +
        " 7Q9.9 7 8.287 8.288T6.176 11.6q-.125.525.163.963t.787.437q.4 0 .65-.3t.375-.7q.35-1.325 1.413-2.162T12" +
        " 9q.4 0 .788.075t.737.225l-1.275 1.725q-.05 0-.125-.013T12 11q-.825 0-1.412.588T10 13t.588 1.413T12 15";
    
    public override async void OnInitialized()
    {
        Loading = true;
        
        var customers = await customerService.GetAll();
        CustomerCount = customers!.Count();

        var readings = await readingService.GetAll();
        Readings = readings != null ? readings.ToList() : new ();
        
        foreach (var type in Enum.GetValues<KindOfMeter>())
        {
            var readingTypeData = new ReadingTypeData
            {
                Type = type,
                Value = Readings.Count(item => item.KindOfMeter == type)
            };
            ReadingData.Add(readingTypeData);
        }

        LatestElectricityReading = FindLatestReading(KindOfMeter.STROM);
        LatestGasReading = FindLatestReading(KindOfMeter.HEIZUNG);
        LatestWaterReading = FindLatestReading(KindOfMeter.WASSER);

        UsagePerMonthReadings = GenerateDummyData(100);
        Loading = false;
    }

    private Reading? FindLatestReading(KindOfMeter kind)
    {
        if (Readings == null)
        {
            return null;
        }

        Reading? latestReading = null;
        foreach (Reading reading in Readings.Where(reading => reading.KindOfMeter == kind))
        {
            if (latestReading == null)
            {
                latestReading = reading;
            }

            if (latestReading.DateOfReading < reading.DateOfReading)
            {
                latestReading = reading;
            }
        }
        return latestReading;
    }


    public List<Reading> GenerateDummyData(int entryCount)
    {
        var list = new List<Reading>();
        for (int i = 0; i < entryCount; i++)
        {
            var reading = new Reading();
            Random random = new Random();
            int year = random.Next(2020, 2025 + 1);
            int month = random.Next(1, 13);
            int day = random.Next(1, DateTime.DaysInMonth(year, month) + 1);
            reading.DateOfReading = new DateOnly(year, month, day);

            int value = random.Next(100, 100000);
            reading.MeterCount = value;

            KindOfMeter[] values = Enum.GetValues<KindOfMeter>();
            reading.KindOfMeter = (KindOfMeter)random.Next(values.Length);
            
            list.Add(reading);
        }

        return list;
    }
}