using P_3FA132_Gruppe_3_Frontend.Data.Models.Classes;
using P_3FA132_Gruppe_3_Frontend.Data.Services.Base;
using System.Net;

namespace P_3FA132_Gruppe_3_Frontend.Data.Services
{
    public class DatabaseService : AbstractBaseService
    {
        public DatabaseService(IHttpClientFactory httpClientFactory) 
            : base(httpClientFactory, "setupDB")
        {
        }

        public async Task<HttpStatusCode> SetupDatabase()
        {
            var response = await _httpClient.DeleteAsync(_endpointUrl);
            return response.StatusCode;
        }
    }
}
