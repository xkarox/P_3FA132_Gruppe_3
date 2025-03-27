using P_3FA132_Gruppe_3_Frontend.Data.Models.Classes;
using P_3FA132_Gruppe_3_Frontend.Data.Services.Base;
using System.Net;
using System.Text;
using System.Text.Json;

namespace P_3FA132_Gruppe_3_Frontend.Data.Services
{
    public class AuthUserService : AbstractCrudService<AuthUser>
    {
        public AuthUserService(HttpClient httpClient) : base(httpClient, "auth")
        {
        }

        public async Task<bool> Login(string username, string password)
        {
            var response = await _httpClient.PostAsync($"{_endpointUrl}/login", new StringContent(JsonSerializer.Serialize(new { username, password }), Encoding.UTF8, "application/json"));
            
            if (response.StatusCode == HttpStatusCode.OK)
            {
                // Check if the "Set-Cookie" header exists
                if (response.Headers.TryGetValues("Set-Cookie", out var cookieHeaders))
                {
                    foreach (var cookie in cookieHeaders)
                    {
                        Console.WriteLine($"Set-Cookie: {cookie}");
                    }
                }
                else
                {
                    Console.WriteLine("No Set-Cookie header found.");
                }

                return true;
            }

            return false;
        }
    }
}
