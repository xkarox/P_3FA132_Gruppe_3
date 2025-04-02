using System.Text.Json;
using System.Text.Json.Serialization;
using P_3FA132_Gruppe_3_Frontend.Data.Models.Classes;
using P_3FA132_Gruppe_3_Frontend.Data.Services.Base;

namespace P_3FA132_Gruppe_3_Frontend.Data.Services
{
    public class CustomerService : AbstractCrudService<Customer>
    {
        public CustomerService(HttpClient httpClient) : base(httpClient, "customers")
        {
        }
        
        
        public static IEnumerable<Customer>? DeserializeCustomers(string jsonData)
        {
            using var document = JsonDocument.Parse(jsonData);
            var root = document.RootElement.GetProperty("customers").ToString();
            var options = new JsonSerializerOptions
            {
                PropertyNameCaseInsensitive = true,
                Converters = { new JsonStringEnumConverter() }
            };
            return JsonSerializer.Deserialize<List<Customer>>(root, options);
        }
    }
}