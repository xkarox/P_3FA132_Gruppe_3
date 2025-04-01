using System.Security.Claims;
using P_3FA132_Gruppe_3_Frontend.Data.Models.Classes;

namespace P_3FA132_Gruppe_3_Frontend.Data.Models.Authentication;

public class AuthenticatedUserStorage
{
    public AuthUser? AuthenticatedUser { get; set; }
    public ClaimsPrincipal? Claims { get; set; }

    public void Clear()
    {
        AuthenticatedUser = null;
        Claims = null;
    }
}