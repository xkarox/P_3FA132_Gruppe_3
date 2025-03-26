using Blazing.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using Microsoft.JSInterop;
using P_3FA132_Gruppe_3_Frontend.Data.Models.Classes;
using P_3FA132_Gruppe_3_Frontend.Data.Services;

namespace P_3FA132_Gruppe_3_Frontend.Data.ViewModels;

public partial class ExportViewModel(CustomerService customerService,
    ExportService exportService,
    ReadingService readingService,
    IJSRuntime jsRuntime) : ViewModelBase
{
    
    [ObservableProperty] private char _buttonSelected;
    [ObservableProperty] private char _kindOfMeterSelected;
    [ObservableProperty] private char _formatSelected;
    
    private IEnumerable<Customer>? _customers;
    public override async Task OnInitializedAsync()
    {
        await base.OnInitializedAsync();
        _customers = await customerService.GetAll();
    }
    
    [RelayCommand]
    private async Task Export()
    {
        var values = "";
        var fileName = "";
        if (ButtonSelected.Equals('R'))
        {
            // add type download
            if (FormatSelected.Equals('X'))
            {
                values = await exportService.CreateAllReadingsXml();
                fileName = "readings.xml";
            }
            else if (FormatSelected.Equals('J'))
            {
                
            }
            else if (FormatSelected.Equals('C'))
            {
                
            }
        }
        else if (ButtonSelected.Equals('C'))
        {
            
        }
        string test = await exportService.CreateReadingCsvFromCustomer(_customers.First());
        
        await jsRuntime.InvokeVoidAsync("downloadCsv", test, fileName);
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