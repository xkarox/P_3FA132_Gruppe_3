using System.Collections.ObjectModel;
using Blazing.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using Microsoft.AspNetCore.Components.QuickGrid;
using Microsoft.AspNetCore.Components.Web;
using P_3FA132_Gruppe_3_Frontend.Data.Models;  
using P_3FA132_Gruppe_3_Frontend.Data.Models.Classes;
using P_3FA132_Gruppe_3_Frontend.Data.Models.Enums;
using P_3FA132_Gruppe_3_Frontend.Data.Services;  
namespace P_3FA132_Gruppe_3_Frontend.Data.ViewModels;

public partial class ReadingManagementViewModel(
    ReadingService readingService,
    CustomerService customerService,
    UtilityService utilityService)
    : ViewModelBase
{
    [ObservableProperty] private ObservableCollection<Reading>? _readings;

    [ObservableProperty] private Reading? _newReading;

    [ObservableProperty] private Reading? _selectedReading;

    [ObservableProperty] private PaginationState? _paginationState;

    [ObservableProperty] private ReadingQuery? _readingQuery;

    [ObservableProperty]
    private ShowReadingColumns _showReadingColumns = new ShowReadingColumns();

    [ObservableProperty] private IEnumerable<Customer>? _customers;

    [ObservableProperty]
    private IEnumerable<string> _excludedProperties =
    [
        "Customer",
        "CustomerName",
        "FormattedDate",
        "MeterCountWithUnit"
    ];

    [ObservableProperty]
    private IEnumerable<string> _readOnlyProperties =
    [
        "CustomerId",
        "Id"
    ];

    [ObservableProperty] private int _maxStringLength = 12;


    public override async void OnInitialized()
    {
        Readings = new ObservableCollection<Reading>();
        PaginationState = new PaginationState() { ItemsPerPage = 7 };
        ReadingQuery = new ReadingQuery();
        Customers = await customerService.GetAll() ?? new List<Customer>();

        var enumerable = Customers as Customer[] ?? Customers.ToArray();
        if (enumerable?.Count() >= 1)
        {
            ReadingQuery.Customer = enumerable.ToArray()[0].Id.ToString();
        }
    }

    [RelayCommand]
    private async Task Query()
    {
        var readings = await readingService.QueryReading(ReadingQuery);
        Readings =
            new ObservableCollection<Reading>(
                readings.OrderByDescending(r => r.DateOfReading));
    }

    [RelayCommand]
    private void SelectReading(Reading reading)
    {
        SelectedReading = reading.Copy();
    }

    [RelayCommand]
    private async Task ConfirmReadingUpdateCallback()
    {
        if (SelectedReading != null)
        {
            await readingService.Update(SelectedReading);
            var index =
                Readings.IndexOf(
                    Readings.First(c => c.Id == SelectedReading.Id));
            if (index >= 0)
            {
                Readings[index] = SelectedReading;
            }

            SelectedReading = null;
        }
    }

    [RelayCommand]
    private void AbortReadingUpdateCallback()
    {
        SelectedReading = null;
    }

    [RelayCommand]
    private void CloseSelectedReadingComponentCallback()
    {
        SelectedReading = null;
    }

    [RelayCommand]
    private async Task DeleteReadingCallback()
    {
        if (SelectedReading != null)
        {
            await readingService.Delete(SelectedReading.Id);
            var index = Readings.IndexOf(Readings.First(c => c.Id == SelectedReading.Id));
            Readings.RemoveAt(index);
            SelectedReading = null;
        }
    }

    [RelayCommand]
    private async Task ConfirmNewReadingCallback()
    {
        if (NewReading is { CustomerId: not null })
        {
            var customer =
                await customerService.Get(NewReading.CustomerId ?? Guid.Empty);
            if (customer == null) return;
            NewReading.Customer = customer;
            NewReading.DateOfReading ??= new DateOnly();

            var returnedReading = await readingService.Add(NewReading);
            if (returnedReading != null)
            {
                Readings!.Insert(0, returnedReading);
            }
            NewReading = null;
        }
    }

    [RelayCommand]
    private void AbortNewReadingCallback()
    {
        NewReading = null;
    }
    [RelayCommand]
    private async Task CopyValue((string value, MouseEventArgs e) parameters)
    {
        await utilityService.CopyToClipboard(
            parameters.value, parameters.e.ClientX, parameters.e.ClientY);
    }

    [RelayCommand]
    private async Task ShowFullString((string value, MouseEventArgs e) parameters)
    {
        if (parameters.value.Length >= MaxStringLength)
            await utilityService.ShowNotificationHover(
                parameters.value, parameters.e.ClientX, parameters.e.ClientY);
    }

    [RelayCommand]
    private async Task RemoveFullStringNotification()
    {
        await utilityService.RemoveNotificationHover();
    }

    public string TruncateString(string value)
    {
        if (string.IsNullOrEmpty(value)) return value;
        return value.Length <= MaxStringLength ? value : value[..MaxStringLength] + "...";
    }
}


