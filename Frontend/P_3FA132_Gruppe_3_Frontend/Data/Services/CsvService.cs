using System.Net.Http.Headers;
using System.Net.Http.Json;
using System.Text;
using System.Text.Json;
using Microsoft.AspNetCore.Components.Forms;
using P_3FA132_Gruppe_3_Frontend.Data.Models.Classes;

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
        var content = new StringContent(csvContent, Encoding.UTF8, "text/plain");
        var response = await _httpClient.PostAsync(BaseUrl + path, content);
        response.EnsureSuccessStatusCode();
        
        return await response.Content.ReadFromJsonAsync<IEnumerable<string>>() ?? new List<string>();
    }

    public async Task<IEnumerable<Dictionary<string, string>>> FormatMetaData(string csvContent)
    {
        const string path = "/metaData";
        var content = new StringContent(csvContent, Encoding.UTF8, "text/plain");
        var response = await _httpClient.PostAsync(BaseUrl + path, content);
        response.EnsureSuccessStatusCode();
        
        return await response.Content.ReadFromJsonAsync<IEnumerable<Dictionary<string, string>>>() ?? new List<Dictionary<string, string>>();
    }

    public async Task<string> CreateReadingCsvFromCustomer(Customer customer)
    {
        const string path = "/createReadingCsvFromCustomer";
        string json = customer.ToJson(false);
        var content = new StringContent(json, Encoding.UTF8, "application/json");
        var response = await _httpClient.PostAsync(BaseUrl + path, content);
        response.EnsureSuccessStatusCode();
        
        return await response.Content.ReadAsStringAsync();
    }

    public async Task<string> CreateAllCustomersCsv()
    {
        const string path = "/createAllCustomersCsv";
        var response = await _httpClient.GetAsync(BaseUrl + path);
        response.EnsureSuccessStatusCode();
        
        return await response.Content.ReadAsStringAsync();
    }

    public async Task<string> CreateAllCustomersXml()
    {
        const string path = "/createAllCustomersXml";
        var response = await _httpClient.GetAsync(BaseUrl + path);
        response.EnsureSuccessStatusCode();
        
        return await response.Content.ReadAsStringAsync();
    }
    
    public async Task<string> CreateAllCustomersJson()
    {
        const string path = "/createAllCustomersJson";
        var response = await _httpClient.GetAsync(BaseUrl + path);
        response.EnsureSuccessStatusCode();
        
        return await response.Content.ReadAsStringAsync();
    }

    public async Task<string> CreateAllReadingsCsv()
    {
        const string path = "/createAllReadingsCsv";
        var response = await _httpClient.GetAsync(BaseUrl + path);
        response.EnsureSuccessStatusCode();
        
        return await response.Content.ReadAsStringAsync();
    }
    
    public async Task<string> CreateAllReadingsXml()
    {
        const string path = "/createAllReadingsXml";
        var response = await _httpClient.GetAsync(BaseUrl + path);
        response.EnsureSuccessStatusCode();
        
        return await response.Content.ReadAsStringAsync();
    }
    
    public async Task<string> CreateAllReadingsJson()
    {
        const string path = "/createAllReadingsJson";
        var response = await _httpClient.GetAsync(BaseUrl + path);
        response.EnsureSuccessStatusCode();
        
        return await response.Content.ReadAsStringAsync();
    }
    
}