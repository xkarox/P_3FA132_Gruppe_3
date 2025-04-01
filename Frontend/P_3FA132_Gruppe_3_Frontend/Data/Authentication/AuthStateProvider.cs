using System.Security.Claims;
using Microsoft.AspNetCore.Components.Authorization;
using P_3FA132_Gruppe_3_Frontend.Data.Models.Authentication;
using P_3FA132_Gruppe_3_Frontend.Data.Models.Classes;

public class AuthStateProvider : AuthenticationStateProvider
{
    private readonly UserAuthService _userAuthService;
    private readonly AuthenticatedUserStorage _authUserStore;
    
    public AuthStateProvider(UserAuthService userAuthService, AuthenticatedUserStorage authUserStore)
    {
        _userAuthService = userAuthService;
        _authUserStore = authUserStore;
    }
    
    public override async Task<AuthenticationState> GetAuthenticationStateAsync()
    {
        var user = _userAuthService.FetchUserFromBrowser();
        if (user is not null)
        {
            return new AuthenticationState(_authUserStore.Claims);
        }
        
        return new AuthenticationState(new ClaimsPrincipal(new ClaimsIdentity()));
    }
    
    public async Task LoginAsync(string username, string password)
    {
        var user = await _userAuthService.SendAuthenticateRequestAsync(username, password);
        if (user != null)
        {
            NotifyAuthenticationStateChanged(
                Task.FromResult(
                    new AuthenticationState(_authUserStore.Claims)));
        }
    }
    
    public void Logout()
    {
        NotifyAuthenticationStateChanged(Task.FromResult(
            new AuthenticationState(new ClaimsPrincipal())));
    }
}