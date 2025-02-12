using System.Collections.ObjectModel;
using Blazing.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using Microsoft.AspNetCore.Components.Forms;
using Microsoft.AspNetCore.Components.QuickGrid;
using P_3FA132_Gruppe_3_Frontend.Data.Models;
using P_3FA132_Gruppe_3_Frontend.Data.Models.Classes;
using P_3FA132_Gruppe_3_Frontend.Data.Services;

namespace P_3FA132_Gruppe_3_Frontend.Data.ViewModels;

public partial class ImportViewModel(CsvService csvService) : ViewModelBase
{
    [ObservableProperty] private ObservableCollection<Customer> _customers = [];

    [ObservableProperty] private PaginationState? _paginationState;

    [ObservableProperty] private ObservableCollection<Reading> _readings = [];

    [ObservableProperty] private bool isCustomerCsv;

    [ObservableProperty] private bool isReadingCsv;

    private readonly int numberOfCustomerHeaderValues = 5;
    private readonly int numberOfReadingHeaderValues = 3;


    public override void OnInitialized()
    {
        base.OnInitialized();

        PaginationState = new PaginationState { ItemsPerPage = 10 };
        Console.WriteLine(PaginationState);
    }

    [RelayCommand]
    private async Task SelectFile(InputFileChangeEventArgs e)
    {
        var browserFile = e.File;

        if (browserFile != null)
        {
            await using var stream = browserFile.OpenReadStream();
            using var reader = new StreamReader(stream);

            var csvContent = await reader.ReadToEndAsync();

            IEnumerable<string> header = await csvService.FormatHeader(csvContent);
            IEnumerable<Dictionary<string, string>> metaData = await csvService.FormatMetaData(csvContent);
            IEnumerable<List<string>> result = await csvService.FormatValues(csvContent);

            try
            {
                if (header.Count() == numberOfCustomerHeaderValues)
                {
                    isCustomerCsv = true;
                    isReadingCsv = false;

                    for (var i = 0; i < result.Count(); i++)
                    {
                        var customer = new Customer();
                        List<string> currentRow = result.ElementAt(i);
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

                    for (var i = 0; i < result.Count(); i++)
                    {
                        var reading = new Reading();

                        List<string> currentRow = result.ElementAt(i);
                        reading.DateOfReading = DateOnly.Parse(currentRow[0]);
                        reading.MeterCount = int.Parse(currentRow[1]);
                        if (currentRow.Count > 2) reading.Comment = currentRow[2];
                        _readings.Add(reading);
                    }
                }
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex.Message);
            }

            Console.WriteLine(result);
            Console.WriteLine($"Received {result.Count()} rows from backend.");
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