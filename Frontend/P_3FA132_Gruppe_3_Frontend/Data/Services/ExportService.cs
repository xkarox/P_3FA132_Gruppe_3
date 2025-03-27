using System.Net.Http.Headers;
using System.Net.Http.Json;
using System.Text;
using System.Text.Json;
using Microsoft.AspNetCore.Components.Forms;
using P_3FA132_Gruppe_3_Frontend.Data.Models;
using P_3FA132_Gruppe_3_Frontend.Data.Models.Classes;

namespace P_3FA132_Gruppe_3_Frontend.Data.Services;

public class ExportService
{
    private readonly HttpClient _httpClient;
    private const string CsvUrl = "http://localhost:8080/csv";
    private const string CustomerUrl = "http://localhost:8080/customers";
    private const string ReadingUrl = "http://localhost:8080/readings";

    public ExportService(HttpClient httpClient)
    {
        _httpClient = httpClient;
    }

    public async Task<IEnumerable<List<string>>> FormatReadingValues(string csvContent)
    {
        const string path = "/readingValues";
        var content = new StringContent(csvContent, Encoding.UTF8, "text/plain");

        var response = await _httpClient.PostAsync(CsvUrl + path, content);
        response.EnsureSuccessStatusCode();

        return await response.Content.ReadFromJsonAsync<IEnumerable<List<string>>>() ?? new List<List<string>>();
    }
    
    public async Task<IEnumerable<List<string>>> FormatCustomerValues(string csvContent)
    {
        const string path = "/customerValues";
        var content = new StringContent(csvContent, Encoding.UTF8, "text/plain");

        var response = await _httpClient.PostAsync(CsvUrl + path, content);
        response.EnsureSuccessStatusCode();

        return await response.Content.ReadFromJsonAsync<IEnumerable<List<string>>>() ?? new List<List<string>>();
    }

    public async Task<IEnumerable<string>> FormatHeader(string csvContent)
    {
        const string path = "/header";
        var content = new StringContent(csvContent, Encoding.UTF8, "text/plain");
        var response = await _httpClient.PostAsync(CsvUrl + path, content);
        response.EnsureSuccessStatusCode();
        
        return await response.Content.ReadFromJsonAsync<IEnumerable<string>>() ?? new List<string>();
    }

    public async Task<IEnumerable<Dictionary<string, string>>> FormatMetaData(string csvContent)
    {
        const string path = "/metaData";
        var content = new StringContent(csvContent, Encoding.UTF8, "text/plain");
        var response = await _httpClient.PostAsync(CsvUrl + path, content);
        response.EnsureSuccessStatusCode();
        
        return await response.Content.ReadFromJsonAsync<IEnumerable<Dictionary<string, string>>>() ?? new List<Dictionary<string, string>>();
    }

    public async Task<string> CreateReadingCsvFromCustomer(Customer customer)
    {
        const string path = "/createReadingCsvFromCustomer";
        string json = customer.ToJson(false);
        var content = new StringContent(json, Encoding.UTF8, "application/json");
        var response = await _httpClient.PostAsync(CsvUrl + path, content);
        response.EnsureSuccessStatusCode();
        
        return await response.Content.ReadAsStringAsync();
    }

    public async Task<string> CreateAllCustomersCsv()
    {
        const string path = "/createAllCustomersCsv";
        var response = await _httpClient.GetAsync(CustomerUrl + path);
        response.EnsureSuccessStatusCode();
        
        return await response.Content.ReadAsStringAsync();
    }

    public async Task<string> CreateAllCustomersXml()
    {
        const string path = "/createAllCustomersXml";
        var response = await _httpClient.GetAsync(CustomerUrl + path);
        response.EnsureSuccessStatusCode();
        
        return await response.Content.ReadAsStringAsync();
    }
    
    public async Task<string> CreateAllCustomersJson()
    {
        const string path = "/getCustomers";
        var response = await _httpClient.GetAsync(CustomerUrl + path);
        response.EnsureSuccessStatusCode();
        
        return await response.Content.ReadAsStringAsync();
    }

    public async Task<string> CreateAllReadingsCsv(KindOfMeter kindOfMeter)
    {
        const string path = "/createAllReadingsCsv";
        var url = $"{ReadingUrl}{path}?kindOfMeter={kindOfMeter}";
        var response = await _httpClient.GetAsync(url);
        response.EnsureSuccessStatusCode();
        
        return await response.Content.ReadAsStringAsync();
    }
    
    public async Task<string> CreateAllReadingsXml(KindOfMeter kindOfMeter)
    {
        const string path = "/createAllReadingsXml";
        var url = $"{ReadingUrl}{path}?kindOfMeter={kindOfMeter}";
        var response = await _httpClient.GetAsync(url);
        response.EnsureSuccessStatusCode();
        
        return await response.Content.ReadAsStringAsync();
    }
    
    public async Task<string> CreateAllReadingsJson(KindOfMeter kindOfMeter)
    {
        const string path = "/createAllReadingsJson";
        var url = $"{ReadingUrl}{path}?kindOfMeter={kindOfMeter}";
        var response = await _httpClient.GetAsync(url);
        response.EnsureSuccessStatusCode();
        
        return await response.Content.ReadAsStringAsync();
    }

    public async Task<string> ValidateCsv(string csvContent)
    {
        const string path = "/validateCsv";
        var content = new StringContent(csvContent, Encoding.UTF8, "text/plain");
        var response = await _httpClient.PostAsync(CsvUrl + path, content);
        response.EnsureSuccessStatusCode();
        return await response.Content.ReadAsStringAsync();
    }
    
}