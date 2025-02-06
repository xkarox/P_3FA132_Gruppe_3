using System.Net.Http.Headers;
using System.Net.Http.Json;
using Microsoft.AspNetCore.Components.Forms;

namespace P_3FA132_Gruppe_3_Frontend.Data.Services;

public class CsvService
{
    private readonly HttpClient _httpClient;
    private readonly string _baseUrl = "http://localhost:8080/api/csv";

    public CsvService(HttpClient httpClient)
    {
        _httpClient = httpClient;
    }

    public async Task<IEnumerable<List<string>>> FormatValues(string csvContent)
    {
        const string path = "/values";
        var response = await _httpClient.PostAsJsonAsync(_baseUrl + path, csvContent);
        response.EnsureSuccessStatusCode();

        return await response.Content.ReadFromJsonAsync<IEnumerable<List<string>>>() ?? new List<List<string>>();
    }

    public async Task<IEnumerable<String>> FormatHeader(string csvContent)
    {
        const string path = "/header";
        var response = await _httpClient.PostAsJsonAsync(_baseUrl + path, csvContent);
        response.EnsureSuccessStatusCode();
        
        return await response.Content.ReadFromJsonAsync<IEnumerable<String>>() ?? new List<string>();
    }

    public async Task<IEnumerable<Dictionary<String, String>>> FormatMetaData(string csContent)
    {
        const string path = "/metaData";
        var response = await _httpClient.PostAsJsonAsync(_baseUrl + path, csContent);
        response.EnsureSuccessStatusCode();
        
        return await response.Content.ReadFromJsonAsync<IEnumerable<Dictionary<String, String>>>() ?? new List<Dictionary<String, String>>();
        
        
    }
    
    
}