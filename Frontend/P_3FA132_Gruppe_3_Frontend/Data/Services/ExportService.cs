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
    private const string CustomerUrl = "http://localhost:8080/customers";
    private const string ReadingUrl = "http://localhost:8080/readings";
    private const string ExportUrl = "http://localhost:8080/export";

    public ExportService(HttpClient httpClient)
    {
        _httpClient = httpClient;
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

    public async Task<string> ExportFile(string fileContent, string fileType)
    {
        const string path = "/exportFile";
        string contentType;

        switch (fileType.ToLower())
        {
            case ".json":
                contentType = "application/json";
                break;
            case ".xml":
                contentType = "application/xml";
                break;
            case ".csv":
                contentType = "text/plain";
                break;
            default:
                throw new ArgumentException("Unsupported file type. Allowed: json, xml, txt.");
        }
        var content = new StringContent(fileContent, Encoding.UTF8, contentType);
        var response = await _httpClient.PostAsync(ExportUrl + path, content);

        response.EnsureSuccessStatusCode();
        
        return await response.Content.ReadAsStringAsync();
    }
    
}