using System.Collections.ObjectModel;
using Blazing.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using Microsoft.AspNetCore.Components.Forms;
using P_3FA132_Gruppe_3_Frontend.Data.Models.Classes;
using P_3FA132_Gruppe_3_Frontend.Data.Services;

namespace P_3FA132_Gruppe_3_Frontend.Data.ViewModels;

public partial class ImportViewModel(CustomerService customerService, ReadingService readingService) : ViewModelBase
{
    [ObservableProperty] private ObservableCollection<Customer> _customers = [];

    [ObservableProperty] private ObservableCollection<Reading> _readings = [];

    /*
    public override async void OnInitialized()
    {
        await base.OnInitializedAsync();

    }

    private async void LoadCustomers()
    {
        var tmpCustomers = await customerService.GetAll();

        Customers = new ObservableCollection<Customer>(
            (tmpCustomers ?? tmpCustomers) ?? Array.Empty<Customer>());
    }

    private async void LoadReadings()
    {
        var tmpReadings = await readingService.GetAll();
    }
    */

    [RelayCommand]
    private async Task SelectFile(InputFileChangeEventArgs e)
    {
        var browserFile = e.File;

        if (browserFile != null)
        {
            
        }
    }
    

    [RelayCommand]
    private async Task UploadFile()
    {
    }

    [RelayCommand]
    private async Task DeleteFile()
    {
    }
}