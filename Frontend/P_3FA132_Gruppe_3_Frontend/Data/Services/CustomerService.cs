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

        public async Task<IEnumerable<Customer>> QueryCustomer(CustomerQuery query)
        {
            var queryParams = query.QueryParameters;
            var uri = BuildUri("customers" + "/getCustomersQuery", queryParams);
            try
            {
                var response = await _httpClient.GetAsync(uri);
                if (response.IsSuccessStatusCode)
                {
                    var jsonData = await response.Content.ReadAsStringAsync();;
                    var customers =  DeserializeCustomers(jsonData!);
                    return customers ?? Enumerable.Empty<Customer>();
                }
            }
            catch (Exception e)
            {
                return Enumerable.Empty<Customer>();
            }
            return Enumerable.Empty<Customer>();
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

        public async Task<IEnumerable<Customer>> getAllCustomers()
        {
            var uri = BuildUri("customers" + "/getCustomers");
            try
            {
                var response = await _httpClient.GetAsync(uri);
                if (response.IsSuccessStatusCode)
                {
                    var jsonData = await response.Content.ReadAsStringAsync();;
                    var customers =  DeserializeCustomers(jsonData!);
                    return customers ?? Enumerable.Empty<Customer>();
                }
            }
            catch (Exception e)
            {
                return Enumerable.Empty<Customer>();
            }
            return Enumerable.Empty<Customer>();
        }
        
        
    }
}