using Blazing.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using Microsoft.JSInterop;
using P_3FA132_Gruppe_3_Frontend.Data.Models;
using P_3FA132_Gruppe_3_Frontend.Data.Models.Enums;
using P_3FA132_Gruppe_3_Frontend.Data.Services;

namespace P_3FA132_Gruppe_3_Frontend.Data.ViewModels;

public partial class ExportViewModel(
    ExportImportService exportImportService,
    IJSRuntime jsRuntime) : ViewModelBase
{

    public ClassEnum ButtonSelected { get; private set; } = ClassEnum.Null;
    public KindOfMeterEnum KindOfMeterSelected { get; private set; } = KindOfMeterEnum.Null;
    public FileFormatEnum FileFormatSelected { get; private set; } = FileFormatEnum.Null;


    public enum ClassEnum
    {
        Reading,
        Customer,
        Null
    }

    public enum KindOfMeterEnum
    {
        Strom,
        Wasser,
        Heizung,
        Null
    }

    public enum FileFormatEnum
    {
        Xml,
        Json,
        Csv,
        Null
    }
    
    
    
    public override async Task OnInitializedAsync()
    {
        await base.OnInitializedAsync();
    }

    [RelayCommand]
    private async Task Export()
    {
        var values = "";
        var fileName = "";
        if (ButtonSelected.Equals(ClassEnum.Reading))
        {
            if (FileFormatSelected.Equals(FileFormatEnum.Xml))
            {
                switch (KindOfMeterSelected)
                {
                    case KindOfMeterEnum.Heizung:
                        values = await exportImportService.CreateAllReadings(KindOfMeter.HEIZUNG, "xml");
                        fileName = "heizung.xml";
                        break;
                    case KindOfMeterEnum.Wasser:
                        values = await exportImportService.CreateAllReadings(KindOfMeter.WASSER, "xml");
                        fileName = "wasser.xml";
                        break;
                    case KindOfMeterEnum.Strom:
                        values = await exportImportService.CreateAllReadings(KindOfMeter.STROM, "xml");
                        fileName = "strom.xml";
                        break;
                }
            }
            else if (FileFormatSelected.Equals(FileFormatEnum.Json))
            {
                switch (KindOfMeterSelected)
                {
                    case KindOfMeterEnum.Heizung:
                        values = await exportImportService.CreateAllReadings(KindOfMeter.HEIZUNG, "json");
                        fileName = "heizung.json";
                        break;
                    case KindOfMeterEnum.Wasser:
                        values = await exportImportService.CreateAllReadings(KindOfMeter.WASSER, "json");
                        fileName = "wasser.json";
                        break;
                    case KindOfMeterEnum.Strom:
                        values = await exportImportService.CreateAllReadings(KindOfMeter.STROM, "json");
                        fileName = "strom.json";
                        break;
                }
            }
            else if (FileFormatSelected.Equals(FileFormatEnum.Csv))
            {
                switch (KindOfMeterSelected)
                {
                    case KindOfMeterEnum.Heizung:
                        values = await exportImportService.CreateAllReadings(KindOfMeter.HEIZUNG, "csv");
                        fileName = "heizung.csv";
                        break;
                    case KindOfMeterEnum.Wasser:
                        values = await exportImportService.CreateAllReadings(KindOfMeter.WASSER, "csv");
                        fileName = "wasser.csv";
                        break;
                    case KindOfMeterEnum.Strom:
                        values = await exportImportService.CreateAllReadings(KindOfMeter.STROM, "csv");
                        fileName = "strom.csv";
                        break;
                }
            }
        }
        else if (ButtonSelected.Equals(ClassEnum.Customer))
        {
            if (FileFormatSelected.Equals(FileFormatEnum.Xml))
            {
                values = await exportImportService.CreateAllCustomers("xml");
                fileName = "customers.xml";
            }
            else if (FileFormatSelected.Equals(FileFormatEnum.Json))
            {
                values = await exportImportService.CreateAllCustomers("json");
                fileName = "customers.json";
            }
            else if (FileFormatSelected.Equals(FileFormatEnum.Csv))
            {
                values = await exportImportService.CreateAllCustomers("csv");
                fileName = "customers.csv";
            }
        }

        await jsRuntime.InvokeVoidAsync("DownloadFile", values, fileName);
    }

    [RelayCommand]
    private async Task SelectedButton(ClassEnum classEnum)
    {
        ButtonSelected = classEnum;
        KindOfMeterSelected = KindOfMeterEnum.Null;
        FileFormatSelected = FileFormatEnum.Null;
    }

    [RelayCommand]
    private async Task SelectKindOfMeter(KindOfMeterEnum kindOfMeter)
    {
        KindOfMeterSelected = kindOfMeter;
    }

    [RelayCommand]
    private async Task SelectFormat(FileFormatEnum format)
    {
        FileFormatSelected = format;
    }
}