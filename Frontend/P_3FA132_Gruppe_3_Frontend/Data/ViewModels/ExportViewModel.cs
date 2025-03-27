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
                        values = await exportService.CreateAllReadingsXml(KindOfMeter.HEIZUNG);
                        fileName = "heizung.xml";
                        break;
                    case 'W':
                        values = await exportService.CreateAllReadingsXml(KindOfMeter.WASSER);
                        fileName = "wasser.xml";
                        break;
                    case 'S':
                        values = await exportService.CreateAllReadingsXml(KindOfMeter.STROM);
                        fileName = "strom.xml";
                        break;
                    case 'U':
                        values = await exportService.CreateAllReadingsXml(KindOfMeter.UNBEKANNT);
                        fileName = "unbekannt.xml";
                        break;
                }
            }
            else if (FormatSelected.Equals('J'))
            {
                switch (KindOfMeterSelected)
                {
                    case 'H':
                        values = await exportService.CreateAllReadingsJson(KindOfMeter.HEIZUNG);
                        fileName = "heizung.json";
                        break;
                    case 'W':
                        values = await exportService.CreateAllReadingsJson(KindOfMeter.WASSER);
                        fileName = "wasser.json";
                        break;
                    case 'S':
                        values = await exportService.CreateAllReadingsJson(KindOfMeter.STROM);
                        fileName = "strom.json";
                        break;
                    case 'U':
                        values = await exportService.CreateAllReadingsJson(KindOfMeter.UNBEKANNT);
                        fileName = "unbekannt.json";
                        break;
                }
            }
            else if (FormatSelected.Equals('C'))
            {
                switch (KindOfMeterSelected)
                {
                    case 'H':
                        values = await exportService.CreateAllReadingsCsv(KindOfMeter.HEIZUNG);
                        fileName = "heizung.csv";
                        break;
                    case 'W':
                        values = await exportService.CreateAllReadingsCsv(KindOfMeter.WASSER);
                        fileName = "wasser.csv";
                        break;
                    case 'S':
                        values = await exportService.CreateAllReadingsCsv(KindOfMeter.STROM);
                        fileName = "strom.csv";
                        break;
                    case 'U':
                        values = await exportService.CreateAllReadingsCsv(KindOfMeter.UNBEKANNT);
                        fileName = "unbekannt.csv";
                        break;
                }
            }
        }
        else if (ButtonSelected.Equals('C'))
        {
            if (FormatSelected.Equals('X'))
            {
                values = await exportService.CreateAllCustomersXml();
                fileName = "customers.xml";
            }
            else if (FormatSelected.Equals('J'))
            {
                values = await exportService.CreateAllCustomersJson();
                fileName = "customers.json";
            }
            else if (FormatSelected.Equals('C'))
            {
                values = await exportService.CreateAllCustomersCsv();
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