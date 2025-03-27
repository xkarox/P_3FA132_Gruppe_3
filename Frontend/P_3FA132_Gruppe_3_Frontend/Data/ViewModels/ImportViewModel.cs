using System.Collections.ObjectModel;
using System.Globalization;
using System.Text.Json;
using System.Text.RegularExpressions;
using Blazing.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using Microsoft.AspNetCore.Components.Forms;
using Microsoft.AspNetCore.Components.QuickGrid;
using P_3FA132_Gruppe_3_Frontend.Data.Models;
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
            await using var stream = browserFile.OpenReadStream();
            using var reader = new StreamReader(stream);

            var csvContent = await reader.ReadToEndAsync();

            var response = await exportService.ValidateCsv(csvContent);
            using var jsonDoc = JsonDocument.Parse(response);
            var root = jsonDoc.RootElement;

            if (root.TryGetProperty("success", out var success) &&
                success.TryGetProperty("type", out var type) &&
                success.TryGetProperty("meter", out var meter) &&
                success.TryGetProperty("class", out var classType))
            {
                IEnumerable<List<string>> result;
                IEnumerable<Dictionary<string, string>> metaData;
                    
                var cus = await customerService.getAllCustomers();
                var typeValue = type.GetString();
                var meterValue = meter.GetString();
                var classTypeValue = classType.GetString();

                if (classTypeValue == "reading")
                {
                    result = await exportService.FormatReadingValues(csvContent);
                    metaData = await exportService.FormatMetaData(csvContent);

                    if (typeValue == "default")
                    {
                        isReadingCsv = true;
                        isCustomerCsv = false;
                        for (var i = 0; i < result.Count(); i++)
                        {
                            var reading = new Reading();

                            var currentRow = result.ElementAt(i);
                            reading.DateOfReading = DateOnly.Parse(currentRow[0]);
                            reading.MeterCount = double.Parse(currentRow[1], CultureInfo.CurrentCulture);

                            if (meterValue == "electricity")
                                reading.KindOfMeter = KindOfMeter.STROM;
                            else if (meterValue == "water")
                                reading.KindOfMeter = KindOfMeter.WASSER;
                            else if (meterValue == "heat") reading.KindOfMeter = KindOfMeter.HEIZUNG;

                            var customer = cus.FirstOrDefault(c => c.Id == Guid.Parse(metaData.ElementAt(0)["Kunde"]));
                            reading.Customer = customer;
                            reading.MeterId = metaData.ElementAt(1)["Zählernummer"];

                            if (currentRow.Count > 2)
                            {
                                var match = Regex.Match(currentRow[2], @"Nummer\s+([A-Za-z0-9\-]+)");
                                if (match.Success)
                                {
                                    var extractedNumber = match.Groups[1].Value;
                                    reading.MeterId = extractedNumber;
                                    metaData.ElementAt(1)["Zählernummer"] = extractedNumber;
                                }

                                reading.Comment = currentRow[2];
                            }

                            _readings.Add(reading);
                        }
                    }
                    else if (typeValue == "custom")
                    {
                        isReadingCsv = true;
                        isCustomerCsv = false;
                        for (var i = 0; i < result.Count(); i++)
                        {
                            var reading = new Reading();

                            var currentRow = result.ElementAt(i);
                            reading.DateOfReading = DateOnly.Parse(currentRow[0]);
                            reading.MeterCount = double.Parse(currentRow[1], CultureInfo.InvariantCulture);

                            if (meterValue == "electricity")
                                reading.KindOfMeter = KindOfMeter.STROM;
                            else if (meterValue == "water")
                                reading.KindOfMeter = KindOfMeter.WASSER;
                            else if (meterValue == "heat") reading.KindOfMeter = KindOfMeter.HEIZUNG;
                            reading.Comment = currentRow[2];
                            reading.CustomerId = Guid.Parse(currentRow[3]);
                            reading.KindOfMeter = Enum.Parse<KindOfMeter>(currentRow[4]);
                            reading.MeterId = currentRow[5];
                            reading.Substitute = Convert.ToBoolean(currentRow[6]);
                            _readings.Add(reading);
                        }
                    }
                }
                else if (classTypeValue == "customer")
                {
                    result = await exportService.FormatCustomerValues(csvContent);
                    
                    if (typeValue == "default")
                    {
                        isReadingCsv = false;
                        isCustomerCsv = true;
                        for (var i = 0; i < result.Count(); i++)
                        {
                            var customer = new Customer();
                            var currentRow = result.ElementAt(i);
                            customer.Id = Guid.Parse(currentRow[0]);
                            customer.Gender = currentRow[1].ToGender();
                            customer.FirstName = currentRow[2];
                            customer.LastName = currentRow[3];
                            if (currentRow.Count > 4) customer.DateOfBirth = DateOnly.Parse(currentRow[4]);
                            _customers.Add(customer);
                        } 
                    }
                }
            }
            else
            {
                Console.WriteLine("Fehler: JSON-Format ungültig oder 'metaData' fehlt.");
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