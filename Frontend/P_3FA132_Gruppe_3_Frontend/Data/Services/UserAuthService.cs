using System.IdentityModel.Tokens.Jwt;
using System.Net.Http.Json;
using System.Security.Claims;
using BitzArt.Blazor.Cookies;
using Microsoft.AspNetCore.Components;
using Microsoft.AspNetCore.Components.Authorization;
using Microsoft.AspNetCore.Components.WebAssembly.Http;
using Newtonsoft.Json;
using P_3FA132_Gruppe_3_Frontend.Data.Models.Authentication;
using P_3FA132_Gruppe_3_Frontend.Data.Models.Classes;

public class UserAuthService
{
    private readonly HttpClient _httpClient;
    private readonly ICookieService _cookieService;
    private readonly AuthenticatedUserStorage _authUserStore;
    private readonly NavigationManager _navigationManager;
    
    public UserAuthService(IHttpClientFactory httpClientFactory, 
        ICookieService cookieService,
        AuthenticatedUserStorage authUserStore,
        NavigationManager navigationManager)
    {
        _httpClient = httpClientFactory.CreateClient("AuthApi");
        _cookieService = cookieService;
        _authUserStore = authUserStore;
        _navigationManager = navigationManager;
    }
    
    public async Task<AuthUser?> SendAuthenticateRequestAsync(string username, string password)
    {
        // Create login request with credentials
        var loginRequest = new { username = username, password = password };
        
        // Send request to API
        var response = await _httpClient.PostAsJsonAsync("/auth/login", loginRequest);
        
        if (response.IsSuccessStatusCode)
        {
            var userString = await response.Content.ReadAsStringAsync();
            var user = AuthUser.LoadJson(userString);
            _authUserStore.AuthenticatedUser = user;
            
            Cookie? tokenCookie = await GetJwtToken();
            string token = tokenCookie?.Value!;
            _authUserStore.Claims = CreateClaimsPrincipalFromToken(token);
            return _authUserStore.AuthenticatedUser;
        }
        
        return null;
    }
    
    public ClaimsPrincipal CreateClaimsPrincipalFromToken(string token)
    {
        var tokenHandler = new JwtSecurityTokenHandler();
        var identity = new ClaimsIdentity();
        
        if (tokenHandler.CanReadToken(token))
        {
            var jwtSecurityToken = tokenHandler.ReadJwtToken(token);
            identity = new ClaimsIdentity(jwtSecurityToken.Claims, "JWT");
        }
        
        return new ClaimsPrincipal(identity);
    }
    
    public AuthUser? FetchUserFromBrowser()
    {
        return _authUserStore.AuthenticatedUser;
    }
    
    private async Task<Cookie?> GetJwtToken()
    {
        return await _cookieService.GetAsync("jwt-token");
    }
    
    public async Task Logout()
    {
        await _cookieService.RemoveAsync("jwt-token");
        _authUserStore.Clear();
        _navigationManager.NavigateTo("/");
    }
}