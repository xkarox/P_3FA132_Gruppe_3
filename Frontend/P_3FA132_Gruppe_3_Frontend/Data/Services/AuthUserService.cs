using P_3FA132_Gruppe_3_Frontend.Data.Models.Classes;
using P_3FA132_Gruppe_3_Frontend.Data.Services.Base;
using System.Net;
using System.Text;
using System.Text.Json;

namespace P_3FA132_Gruppe_3_Frontend.Data.Services
{
    public class AuthUserService : AbstractCrudService<AuthUser>
    {
        public AuthUserService(HttpClient httpClient, string endpointUrl) : base(httpClient, "auth")
        {
        }

        public async Task<HttpStatusCode> Login(string username, string password)
        {
            var response = await _httpClient.PostAsync($"{_endpointUrl}/login", new StringContent(JsonSerializer.Serialize(new { username, password }), Encoding.UTF8, "application/json"));
            return response.StatusCode;
        }
    }
}
