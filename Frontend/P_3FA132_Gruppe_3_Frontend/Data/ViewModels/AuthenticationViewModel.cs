using Blazing.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;

namespace P_3FA132_Gruppe_3_Frontend.Data.ViewModels;

public partial class AuthenticationViewModel : ViewModelBase
{
    [ObservableProperty] public bool _authenticated = false;
    [ObservableProperty] public string _username;
    [ObservableProperty] public bool _showLoginModal = false;


    [RelayCommand]
    private void ShowLoginComponent() {}

}