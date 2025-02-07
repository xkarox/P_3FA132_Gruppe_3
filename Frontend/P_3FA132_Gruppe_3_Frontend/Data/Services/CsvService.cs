using System.Net.Http.Headers;
using System.Net.Http.Json;
using System.Text;
using Microsoft.AspNetCore.Components.Forms;

namespace P_3FA132_Gruppe_3_Frontend.Data.Services;

public class CsvService
{
    private readonly HttpClient _httpClient;
    private const string BaseUrl = "http://localhost:8080/csv";

    public CsvService(HttpClient httpClient)
    {
        _httpClient = httpClient;
    }

    public async Task<IEnumerable<List<string>>> FormatValues(string csvContent)
    {
        const string path = "/values";
        var content = new StringContent(csvContent, Encoding.UTF8, "text/plain");

        var response = await _httpClient.PostAsync(BaseUrl + path, content);
        response.EnsureSuccessStatusCode();

        return await response.Content.ReadFromJsonAsync<IEnumerable<List<string>>>() ?? new List<List<string>>();
    }

    public async Task<IEnumerable<string>> FormatHeader(string csvContent)
    {
        const string path = "/header";
        var response = await _httpClient.PostAsJsonAsync(BaseUrl + path, csvContent);
        response.EnsureSuccessStatusCode();
        
        return await response.Content.ReadFromJsonAsync<IEnumerable<string>>() ?? new List<string>();
    }

    public async Task<IEnumerable<Dictionary<string, string>>> FormatMetaData(string csContent)
    {
        const string path = "/metaData";
        var response = await _httpClient.PostAsJsonAsync(BaseUrl + path, csContent);
        response.EnsureSuccessStatusCode();
        
        return await response.Content.ReadFromJsonAsync<IEnumerable<Dictionary<string, string>>>() ?? new List<Dictionary<string, string>>();
        
        
    }
    
    
}