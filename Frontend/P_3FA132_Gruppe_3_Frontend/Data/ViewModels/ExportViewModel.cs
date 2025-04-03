using Blazing.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using Microsoft.JSInterop;
using P_3FA132_Gruppe_3_Frontend.Data.Models;
using P_3FA132_Gruppe_3_Frontend.Data.Services;

namespace P_3FA132_Gruppe_3_Frontend.Data.ViewModels;

public partial class ExportViewModel(
    ExportService exportService,
    IJSRuntime jsRuntime) : ViewModelBase
{
    [ObservableProperty] private char _buttonSelected;
    [ObservableProperty] private char _formatSelected;
    [ObservableProperty] private char _kindOfMeterSelected;

    public override async Task OnInitializedAsync()
    {
        await base.OnInitializedAsync();
    }

    [RelayCommand]
    private async Task Export()
    {
        var values = "";
        var fileName = "";
        if (ButtonSelected.Equals('R'))
        {
            if (FormatSelected.Equals('X'))
            {
                switch (KindOfMeterSelected)
                {
                    case 'H':
                        values = await exportService.CreateAllReadings(KindOfMeter.HEIZUNG, "xml");
                        fileName = "heizung.xml";
                        break;
                    case 'W':
                        values = await exportService.CreateAllReadings(KindOfMeter.WASSER, "xml");
                        fileName = "wasser.xml";
                        break;
                    case 'S':
                        values = await exportService.CreateAllReadings(KindOfMeter.STROM, "xml");
                        fileName = "strom.xml";
                        break;
                    case 'U':
                        values = await exportService.CreateAllReadings(KindOfMeter.UNBEKANNT, "xml");
                        fileName = "unbekannt.xml";
                        break;
                }
            }
            else if (FormatSelected.Equals('J'))
            {
                switch (KindOfMeterSelected)
                {
                    case 'H':
                        values = await exportService.CreateAllReadings(KindOfMeter.HEIZUNG, "json");
                        fileName = "heizung.json";
                        break;
                    case 'W':
                        values = await exportService.CreateAllReadings(KindOfMeter.WASSER, "json");
                        fileName = "wasser.json";
                        break;
                    case 'S':
                        values = await exportService.CreateAllReadings(KindOfMeter.STROM, "json");
                        fileName = "strom.json";
                        break;
                    case 'U':
                        values = await exportService.CreateAllReadings(KindOfMeter.UNBEKANNT, "json");
                        fileName = "unbekannt.json";
                        break;
                }
            }
            else if (FormatSelected.Equals('C'))
            {
                switch (KindOfMeterSelected)
                {
                    case 'H':
                        values = await exportService.CreateAllReadings(KindOfMeter.HEIZUNG, "csv");
                        fileName = "heizung.csv";
                        break;
                    case 'W':
                        values = await exportService.CreateAllReadings(KindOfMeter.WASSER, "csv");
                        fileName = "wasser.csv";
                        break;
                    case 'S':
                        values = await exportService.CreateAllReadings(KindOfMeter.STROM, "csv");
                        fileName = "strom.csv";
                        break;
                    case 'U':
                        values = await exportService.CreateAllReadings(KindOfMeter.UNBEKANNT, "csv");
                        fileName = "unbekannt.csv";
                        break;
                }
            }
        }
        else if (ButtonSelected.Equals('C'))
        {
            if (FormatSelected.Equals('X'))
            {
                values = await exportService.CreateAllCustomers("xml");
                fileName = "customers.xml";
            }
            else if (FormatSelected.Equals('J'))
            {
                values = await exportService.CreateAllCustomers("json");
                fileName = "customers.json";
            }
            else if (FormatSelected.Equals('C'))
            {
                values = await exportService.CreateAllCustomers("csv");
                fileName = "customers.csv";
            }
        }

        await jsRuntime.InvokeVoidAsync("DownloadFile", values, fileName);
    }

    [RelayCommand]
    private async Task SelectedButton(char button)
    {
        ButtonSelected = button;
        KindOfMeterSelected = '\0';
        FormatSelected = '\0';
    }

    [RelayCommand]
    private async Task SelectKindOfMeter(char kindOfMeter)
    {
        KindOfMeterSelected = kindOfMeter;
    }

    [RelayCommand]
    private async Task SelectFormat(char format)
    {
        FormatSelected = format;
    }
}