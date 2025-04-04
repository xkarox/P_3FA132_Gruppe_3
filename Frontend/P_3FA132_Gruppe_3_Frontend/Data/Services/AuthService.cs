using System.Net.Http.Json;
using BitzArt.Blazor.Cookies;
using Blazored.LocalStorage;
using Microsoft.AspNetCore.Components.Authorization;
using P_3FA132_Gruppe_3_Frontend.Data.Authentication;
using P_3FA132_Gruppe_3_Frontend.Data.Models.Classes;

namespace P_3FA132_Gruppe_3_Frontend.Data.Services;

public class AuthService
{
    private readonly HttpClient _httpClient;
    private readonly AuthenticationStateProvider _authStateProvider;
    private bool _authenticationEnabled;
    public string AuthenticatedUsername { get; private set; } = string.Empty;
    public bool IsAuthenticationEnabled => _authenticationEnabled; 
    public AuthService(IHttpClientFactory httpClientFactory, 
        AuthenticationStateProvider authStateProvider)
    {
        _httpClient = httpClientFactory.CreateClient("AuthApi");
        _authStateProvider = authStateProvider;
        _ = CheckAuthEnabled();
    }
    
    public async Task<bool> Login(string username, string password)
    {
        if (!_authenticationEnabled)
        {
            var dummy = AuthUser.GetDummyAuthUserForDisabledAuthentication();
            ((CustomAuthStateProvider)_authStateProvider).NotifyUserAuthentication(dummy);
            AuthenticatedUsername = dummy.Username;
            return true;
        }
        var loginModel = new { Username = username, Password = password };
        
        try {
            var response = await _httpClient.PostAsJsonAsync("/auth/login", loginModel);
        
            if (response.IsSuccessStatusCode)
            {
                var userDto = await response.Content.ReadFromJsonAsync<AuthUser>();
                ((CustomAuthStateProvider)_authStateProvider).NotifyUserAuthentication(userDto);
                AuthenticatedUsername = userDto.Username;
                return true;
            }
        }
        catch (Exception ex) {
            Console.WriteLine($"AuthService: Login error: {ex.Message}");
        }
        return false;
    }
    
    public void Logout()
    {
        ((CustomAuthStateProvider)_authStateProvider).NotifyUserLogout();
    }

    private async Task CheckAuthEnabled()
    {
        _authenticationEnabled =  await _httpClient.GetFromJsonAsync<bool>("/auth/isEnabled");
        Console.WriteLine($"AuthService: IsEnabled: {_authenticationEnabled}");
    }
}
