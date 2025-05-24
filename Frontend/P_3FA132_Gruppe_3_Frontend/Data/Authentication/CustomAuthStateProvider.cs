using System.Security.Claims;
using Microsoft.AspNetCore.Components.Authorization;
using P_3FA132_Gruppe_3_Frontend.Data.Models.Classes;
using P_3FA132_Gruppe_3_Frontend.Data.Models.Enums;

namespace P_3FA132_Gruppe_3_Frontend.Data.Authentication;

public class CustomAuthStateProvider : AuthenticationStateProvider
{
    private AuthUser? _currentUser;
    
    public override Task<AuthenticationState> GetAuthenticationStateAsync()
    {
        if (_currentUser == null)
        {
            return Task.FromResult(new AuthenticationState(new ClaimsPrincipal(new ClaimsIdentity())));
        }
        
        var claims = new List<Claim>
        {
            new Claim(ClaimTypes.Name, _currentUser.Username),
            new Claim(ClaimTypes.NameIdentifier, _currentUser.Id.ToString()),
            new Claim(ClaimTypes.Role, _currentUser.Role.ToString())
        };
        
        foreach (var permission in _currentUser.Permissions)
        {
            claims.Add(new Claim("Permission", permission.ToString()));
        }
        
        var identity = new ClaimsIdentity(claims, "Server authentication");
        return Task.FromResult(new AuthenticationState(new ClaimsPrincipal(identity)));
    }
    
    public void NotifyUserAuthentication(AuthUser user)
    {
        _currentUser = user;
        NotifyAuthenticationStateChanged(GetAuthenticationStateAsync());
    }
    
    public void NotifyUserLogout()
    {
        _currentUser = null;
        var state = GetAuthenticationStateAsync();
        NotifyAuthenticationStateChanged(state);
    }
}
