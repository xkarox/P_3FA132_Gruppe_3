using P_3FA132_Gruppe_3_Frontend.Data.Models.Classes;
using P_3FA132_Gruppe_3_Frontend.Data.Services.Base;

namespace P_3FA132_Gruppe_3_Frontend.Data.Services
{
    public class AuthUserService : AbstractCrudService<AuthUser>
    {
        public AuthUserService(HttpClient httpClient, string endpointUrl) : base(httpClient, "auth")
        {
        }
    }
}
