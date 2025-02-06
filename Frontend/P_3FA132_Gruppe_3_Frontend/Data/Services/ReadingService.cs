using P_3FA132_Gruppe_3_Frontend.Data.Models.Classes;
using P_3FA132_Gruppe_3_Frontend.Data.Services.Base;
using System.Text.Json;
using System.Text.Json.Serialization;

namespace P_3FA132_Gruppe_3_Frontend.Data.Services
{
    public class ReadingService : AbstractCrudService<Reading>
    {
        public ReadingService(HttpClient httpClient) : base(httpClient, "readings")
        {
        }

        public async Task<IEnumerable<Reading>> QueryReading(ReadingQuery query)
        {

            var queryParams = query.QueryParameters;
            var uri = BuildUri("readings", queryParams);
            try
            {
                var response = await _httpClient.GetAsync(uri);
                if (response.IsSuccessStatusCode)
                {
                    var jsonData = await response.Content.ReadAsStringAsync(); ;
                    var readings = DeserializeReadings(jsonData!);
                    return readings ?? Enumerable.Empty<Reading>();
                }
            }
            catch (Exception e)
            {
                return Enumerable.Empty<Reading>();
            }
            return Enumerable.Empty<Reading>();
        }

        public static Reading? DeserializeReading(string jsonData)
        {
            using var document = JsonDocument.Parse(jsonData);
            var root = document.RootElement.GetProperty("reading").ToString();
            var options = new JsonSerializerOptions
            {
                PropertyNameCaseInsensitive = true,
                Converters = { new JsonStringEnumConverter() }
            };
            return JsonSerializer.Deserialize<Reading>(root, options);
        }

        public static IEnumerable<Reading>? DeserializeReadings(string jsonData)
        {
            using var document = JsonDocument.Parse(jsonData);
            var root = document.RootElement.GetProperty("readings").ToString();
            var options = new JsonSerializerOptions
            {
                PropertyNameCaseInsensitive = true,
                Converters = { new JsonStringEnumConverter() }
            };
            return JsonSerializer.Deserialize<List<Reading>>(root, options);
        }
    }
}
