using System.Collections.ObjectModel;
using Blazing.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using Microsoft.AspNetCore.Components.Forms;
using Microsoft.AspNetCore.Components.QuickGrid;
using P_3FA132_Gruppe_3_Frontend.Data.Models.Classes;
using P_3FA132_Gruppe_3_Frontend.Data.Services;

namespace P_3FA132_Gruppe_3_Frontend.Data.ViewModels;

public partial class ImportViewModel(CsvService csvService) : ViewModelBase
{
    [ObservableProperty] private ObservableCollection<Customer> _customers = [];

    [ObservableProperty] private ObservableCollection<Reading> _readings = [];
    
    [ObservableProperty]   
    private PaginationState? _paginationState;
    

    public override void OnInitialized()
    {
        base.OnInitialized();
        
        PaginationState = new PaginationState() { ItemsPerPage = 10 };
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
            
            string csvContent = await reader.ReadToEndAsync();
            
            IEnumerable<List<string>> result = await csvService.FormatValues(csvContent);

            try
            {
                for (int i = 0; i < result.Count(); i++)
                {
                    Reading reading = new Reading();

                    List<string> currentRow = result.ElementAt(i);
                    reading.DateOfReading = DateOnly.Parse(currentRow[0]);
                    reading.MeterCount = int.Parse(currentRow[1]);
                    if (currentRow.Count > 2)
                    {
                        reading.Comment = currentRow[2];
                    }
                    _readings.Add(reading);

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