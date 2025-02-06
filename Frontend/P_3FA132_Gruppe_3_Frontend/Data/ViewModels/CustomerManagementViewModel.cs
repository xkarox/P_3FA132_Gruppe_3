using Blazing.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using Microsoft.AspNetCore.Components.QuickGrid;
using Microsoft.AspNetCore.Components.Web;
using P_3FA132_Gruppe_3_Frontend.Data.Models;
using P_3FA132_Gruppe_3_Frontend.Data.Models.Classes;
using P_3FA132_Gruppe_3_Frontend.Data.Services;
using System.Collections.ObjectModel;

namespace P_3FA132_Gruppe_3_Frontend.Data.ViewModels;

public partial class CustomerManagementViewModel(
    CustomerService customerService,
    UtilityService utilityService)
    : ViewModelBase
{
    [ObservableProperty]
    private ObservableCollection<Customer> _customers = [];

    [ObservableProperty]
    private ShowCustomerColumns
        _showCustomerColumns = new ShowCustomerColumns();

    [ObservableProperty] private Customer? _selectedCustomer;

    [ObservableProperty] private Customer? _newCustomer;

    [ObservableProperty]
    private PaginationState? _paginationState;

    [ObservableProperty]
    private IEnumerable<string> _excludedProperties =
    [

    ];

    [ObservableProperty]
    private IEnumerable<string> _readOnlyProperties =
    [
        "Id"
    ];

    [ObservableProperty] private int _maxStringLength = 12;

    public override async void OnInitialized()
    {
        await base.OnInitializedAsync();

        PaginationState = new PaginationState() { ItemsPerPage = 15 };
        Console.WriteLine(PaginationState);
        LoadCustomers();
    }

    private async void LoadCustomers()
    {
        var tmpCustomers = await customerService.GetAll();
        Customers = new ObservableCollection<Customer>(
            (tmpCustomers ?? tmpCustomers) ?? Array.Empty<Customer>());
    }

    [RelayCommand]
    private void SelectCustomer(Customer customer)
    {
        SelectedCustomer = customer.Copy();
    }

    [RelayCommand]
    private async Task ConfirmCustomerUpdateCallback()
    {
        if (SelectedCustomer != null)
        {
            await customerService.Update(SelectedCustomer);
            var index = Customers.IndexOf(Customers.First(c => c.Id == SelectedCustomer.Id));
            if (index >= 0)
            {
                Customers[index] = SelectedCustomer;
            }
            SelectedCustomer = null;
        }
    }

    [RelayCommand]
    private void AbortCustomerUpdateCallback()
    {
        SelectedCustomer = null;
    }

    [RelayCommand]
    private void CloseSelectedCustomerComponentCallback()
    {
        SelectedCustomer = null;
    }

    [RelayCommand]
    private async Task DeleteCustomerCallback()
    {
        if (SelectedCustomer != null)
        {
            await customerService.Delete(SelectedCustomer.Id);
            var index = Customers.IndexOf(Customers.First(c => c.Id == SelectedCustomer.Id));
            Customers.RemoveAt(index);
            SelectedCustomer = null;
        }
    }

    [RelayCommand]
    private async Task ConfirmNewCustomerCallback()
    {
        if (NewCustomer != null)
        {
            var returnedCustomer = await customerService.Add(NewCustomer);
            if (returnedCustomer != null)
            {
                Customers.Insert(0, returnedCustomer);
            }
            NewCustomer = null;
        }
    }

    [RelayCommand]
    private void AbortNewCustomerCallback()
    {
        NewCustomer = null;
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