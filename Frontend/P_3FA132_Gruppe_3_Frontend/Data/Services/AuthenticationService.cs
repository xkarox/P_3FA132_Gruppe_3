using System.Net.Http.Json;
using P_3FA132_Gruppe_3_Frontend.Data.Services.Base;

namespace P_3FA132_Gruppe_3_Frontend.Data.Services;

public record Login(string username, string password);
public class AuthenticationService : AbstractBaseService
{
    private string? _token;
    
    public AuthenticationService(HttpClient httpClient) : base(httpClient, "auth/login")
    {
    }
    
    public async Task<bool> Login(string username, string password)
    {
        var loginCredentials = new
        {
            username = username,
            password = password
        };
        var response = await _httpClient.PostAsJsonAsync(_endpointUrl, loginCredentials);
        if (!response.IsSuccessStatusCode)
            return false;
        _token = await response.Content.ReadAsStringAsync();
        return true;
    }
}