using System.Net.Http.Headers;
using System.Net.Http.Json;
using Microsoft.AspNetCore.Components.Forms;

namespace P_3FA132_Gruppe_3_Frontend.Data.Services;

public class CsvService
{
    private readonly HttpClient _httpClient;
    private readonly string _baseUrl = "http://localhost:8080/api/csv/upload";

    public CsvService(HttpClient httpClient)
    {
        _httpClient = httpClient;
    }

    public async Task<IEnumerable<List<string>>> UploadCsvFileAsync(string csvContent)
    {
        var response = await _httpClient.PostAsJsonAsync(_baseUrl, csvContent);
        response.EnsureSuccessStatusCode();

        return await response.Content.ReadFromJsonAsync<IEnumerable<List<string>>>() ?? new List<List<string>>();
    }
}