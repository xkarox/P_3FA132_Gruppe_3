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
    private IBrowserFile? browserFile;

    [ObservableProperty] private bool fileIsSelected;

    [ObservableProperty] private bool isCustomerCsv;

    [ObservableProperty] private bool isReadingCsv;


    public override void OnInitialized()
    {
        Customers = new ObservableCollection<Customer>();
        PaginationState = new PaginationState { ItemsPerPage = 10 };
    }

    [RelayCommand]
    private async Task SelectFile(InputFileChangeEventArgs e)
    {
        resetValues();
        browserFile = e.File;


        if (browserFile != null)
        {
            var fileName = browserFile.Name;
            var extension = Path.GetExtension(fileName).ToLower();

            await using var stream = browserFile.OpenReadStream();
            using var reader = new StreamReader(stream);

            var stringContent = await reader.ReadToEndAsync();

            var responses = await exportService.ExportFile(stringContent, extension);

            using (var document = JsonDocument.Parse(responses))
            {
                var root = document.RootElement;

                if (root.TryGetProperty("customers", out var Customers))
                {
                    IEnumerable<Customer> customers = Customer.LoadJsonList(responses);
                    for (var i = 0; i < customers.Count(); i++)
                    {
                        _customers.Add(customers.ElementAt(i));
                    }
                    isCustomerCsv = true;
                }
                else if (root.TryGetProperty("readings", out var Readings))
                {
                    IEnumerable<Reading> readings = Reading.LoadJsonList(responses);
                    for (var i = 0; i < readings.Count(); i++)
                    {
                        _readings.Add(readings.ElementAt(i));
                    }
                    isReadingCsv = true;
                }
            }
            fileIsSelected = true;
        }
    }


    [RelayCommand]
    private async Task Upload()
    {
        if (isCustomerCsv)
            for (var i = 0; i < _customers.Count; i++)
                await customerService.Add(_customers[i]);
        else if (isReadingCsv)
            for (var i = 0; i < _readings.Count; i++)
                await readingService.Add(_readings[i]);
        resetValues();
    }

    [RelayCommand]
    private void SelectCustomer(Customer customer)
    {
        SelectedCustomer = customer.Copy();
    }

    private void resetValues()
    {
        fileIsSelected = false;
        isCustomerCsv = false;
        isReadingCsv = false;
        _customers.Clear();
        _readings.Clear();
        browserFile = null;
    }
}