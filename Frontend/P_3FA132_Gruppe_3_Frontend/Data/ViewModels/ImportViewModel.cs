using System.Collections.ObjectModel;
using System.Text.Json;
using Blazing.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using Microsoft.AspNetCore.Components.Forms;
using Microsoft.AspNetCore.Components.QuickGrid;
using P_3FA132_Gruppe_3_Frontend.Data.Models.Classes;
using P_3FA132_Gruppe_3_Frontend.Data.Services;

namespace P_3FA132_Gruppe_3_Frontend.Data.ViewModels;

public partial class ImportViewModel(
    ExportService exportService,
    ReadingService readingService,
    CustomerService customerService) : ViewModelBase
{
    private readonly int numberOfCustomerHeaderValues = 5;
    private readonly int numberOfReadingHeaderValues = 3;

    [ObservableProperty] private ObservableCollection<Customer> _customers = [];

    [ObservableProperty] private PaginationState? _paginationState;

    [ObservableProperty] private ObservableCollection<Reading> _readings = [];

    [ObservableProperty] private Customer? _selectedCustomer;

    [ObservableProperty] private Guid? _selectedCustomerId;
    private IBrowserFile? _browserFile;

    [ObservableProperty] private bool _fileIsSelected;

    [ObservableProperty] private bool _isCustomerCsv;

    [ObservableProperty] private bool _isReadingCsv;


    public override void OnInitialized()
    {
        Customers = new ObservableCollection<Customer>();
        PaginationState = new PaginationState { ItemsPerPage = 10 };
    }

    [RelayCommand]
    private async Task SelectFile(InputFileChangeEventArgs e)
    {
        ResetValues();
        _browserFile = e.File;


        if (_browserFile != null)
        {
            var fileName = _browserFile.Name;
            var extension = Path.GetExtension(fileName).ToLower();

            await using var stream = _browserFile.OpenReadStream();
            using var reader = new StreamReader(stream);

            var stringContent = await reader.ReadToEndAsync();

            var responses = await exportService.ExportFile(stringContent, extension);

            using (var document = JsonDocument.Parse(responses))
            {
                var root = document.RootElement;

                if (root.TryGetProperty("customers", out _))
                {
                    IEnumerable<Customer> customers = Customer.LoadJsonList(responses);
                    for (var i = 0; i < customers.Count(); i++)
                    {
                        Customers.Add(customers.ElementAt(i));
                    }
                    IsCustomerCsv = true;
                }
                else if (root.TryGetProperty("readings", out _))
                {
                    IEnumerable<Reading> readings = Reading.LoadJsonList(responses);
                    for (var i = 0; i < readings.Count(); i++)
                    {
                        Readings.Add(readings.ElementAt(i));
                    }
                    IsReadingCsv = true;
                }
            }
            FileIsSelected = true;
        }
    }


    [RelayCommand]
    private async Task Upload()
    {
        if (IsCustomerCsv)
            for (var i = 0; i < Customers.Count; i++)
                await customerService.Add(Customers[i]);
        else if (IsReadingCsv)
            for (var i = 0; i < Readings.Count; i++)
                await readingService.Add(Readings[i]);
        ResetValues();
    }

    [RelayCommand]
    private void SelectCustomer(Customer customer)
    {
        SelectedCustomer = customer.Copy();
    }

    private void ResetValues()
    {
        FileIsSelected = false;
        IsCustomerCsv = false;
        IsReadingCsv = false;
        Customers.Clear();
        Readings.Clear();
        _browserFile = null;
    }
}