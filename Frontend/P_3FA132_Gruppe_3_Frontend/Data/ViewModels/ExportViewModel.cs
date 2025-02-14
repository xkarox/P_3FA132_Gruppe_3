using Blazing.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using Microsoft.JSInterop;
using P_3FA132_Gruppe_3_Frontend.Data.Models.Classes;
using P_3FA132_Gruppe_3_Frontend.Data.Services;

namespace P_3FA132_Gruppe_3_Frontend.Data.ViewModels;

public partial class ExportViewModel(CustomerService customerService,
    CsvService csvService,
    IJSRuntime jsRuntime) : ViewModelBase
{
    private IEnumerable<Customer>? _customers;
    public override async Task OnInitializedAsync()
        
    {
        await base.OnInitializedAsync();
        _customers = await customerService.GetAll();
        
        
    }
    
    [RelayCommand]
    private async Task Export()
    {
        string test = await csvService.CreateReadingCsvFromCustomer(_customers.First());

        string fileName = "output.csv";
        
        await jsRuntime.InvokeVoidAsync("downloadCsv", test, fileName);
    }
}