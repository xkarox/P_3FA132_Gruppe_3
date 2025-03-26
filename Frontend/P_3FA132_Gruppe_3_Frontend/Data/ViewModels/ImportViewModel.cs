using System.Collections.ObjectModel;
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
    private IBrowserFile? browserFile;

    [ObservableProperty] private ObservableCollection<Customer> _customers = [];

    [ObservableProperty] private PaginationState? _paginationState;

    [ObservableProperty] private ObservableCollection<Reading> _readings = [];

    [ObservableProperty] private Customer? _selectedCustomer;

    [ObservableProperty] private Guid? _selectedCustomerId;

    [ObservableProperty] private bool isCustomerCsv;

    [ObservableProperty] private bool isReadingCsv;
    
    [ObservableProperty] private bool fileIsSelected = false;


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

            var header = await exportService.FormatHeader(csvContent);
            IEnumerable<Dictionary<string, string>> metaData = await exportService.FormatMetaData(csvContent);
            IEnumerable<List<string>> result = await exportService.FormatValues(csvContent);

            try
            {
                if (header.Count() == numberOfCustomerHeaderValues)
                {
                    isCustomerCsv = true;
                    isReadingCsv = false;
                    

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
                else if (header.Count() == numberOfReadingHeaderValues)
                {
                    isReadingCsv = true;
                    isCustomerCsv = false;
                    
                    bool isElectric = false;
                    bool isWater = false;
                    bool isUnknown = false;
                    bool isHeat = false;

                    if (header.ElementAt(1).Contains("kWh"))
                    {
                        isElectric = true;
                    }
                    else if (header.ElementAt(1).Contains("m\u00b3"))
                    {
                        isWater = true;
                    }
                    else if (header.ElementAt(1).Contains("MWh"))
                    {
                        isHeat = true;
                    }

                    else
                    {
                        isUnknown = true;
                    }
                    

                    
                    var cus = await customerService.getAllCustomers();
                    for (var i = 0; i < result.Count(); i++)
                    {
                        var reading = new Reading();

                        var currentRow = result.ElementAt(i);
                        reading.DateOfReading = DateOnly.Parse(currentRow[0]);
                        reading.MeterCount = double.Parse(currentRow[1]);

                        if (isElectric)
                        {
                            reading.KindOfMeter = KindOfMeter.STROM;
                        }
                        else if (isWater)
                        {
                            reading.KindOfMeter = KindOfMeter.WASSER;
                        }
                        else if (isHeat)
                        {
                            reading.KindOfMeter = KindOfMeter.HEIZUNG;
                        }
                        
                        Customer customer = cus.FirstOrDefault(c => c.Id == Guid.Parse(metaData.ElementAt(0)["Kunde"]));
                        reading.Customer = customer;
                        reading.MeterId = metaData.ElementAt(1)["Zählernummer"];
                        
                        if (currentRow.Count > 2)
                        {
                            
                            Match match = Regex.Match(currentRow[2], @"Nummer\s+([A-Za-z0-9\-]+)");
                            if (match.Success)
                            {
                                string extractedNumber = match.Groups[1].Value;
                                reading.MeterId = extractedNumber;
                                metaData.ElementAt(1)["Zählernummer"] = extractedNumber;
                            }
                            
                            reading.Comment = currentRow[2];
                        }
                        _readings.Add(reading);
                    }

                    isHeat = false;
                    isElectric = false;
                    isWater = false;
                    isUnknown = false;
                }
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex.Message);
            }
            Console.WriteLine($"Received {result.Count()} rows from backend.");
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