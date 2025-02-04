using P_3FA132_Gruppe_3_Frontend.Data.Models.Classes;
using P_3FA132_Gruppe_3_Frontend.Data.Services.Base;
using System.Net;

namespace P_3FA132_Gruppe_3_Frontend.Data.Services
{
    public class DatabaseService : AbstractBaseService<Customer>
    {
        public DatabaseService(HttpClient httpClient) : base(httpClient, "setupDB")
        {
        }

        public async Task<HttpStatusCode> SetupDatabase()
        {
            var response = await _httpClient.DeleteAsync(_endpointUrl);
            return response.StatusCode;
        }
    }
}
