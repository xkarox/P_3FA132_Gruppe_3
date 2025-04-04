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

    public ExportService(HttpClient httpClient)
    {
        _httpClient = httpClient;
    }

    public async Task<string> CreateAllCustomers(string fileType)
    {
        const string path = "/exportCustomers";
        var url = $"{CustomerUrl}{path}?fileType={fileType.ToLower()}";

        var response = await _httpClient.GetAsync(url);
        response.EnsureSuccessStatusCode();

        return await response.Content.ReadAsStringAsync();
    }

    public async Task<string> CreateAllReadings(KindOfMeter kindOfMeter, string fileType)
    {
        const string path = "/exportReadings";
        var url = $"{ReadingUrl}{path}?kindOfMeter={kindOfMeter}&fileType={fileType.ToLower()}";

        var response = await _httpClient.GetAsync(url);
        response.EnsureSuccessStatusCode();

        return await response.Content.ReadAsStringAsync();
    }

    public async Task<string> ImportCustomer(string fileContent, string fileType)
    {
        const string path = "/validateCustomers";
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
                throw new ArgumentException("Unsupported file type. Allowed: json, xml, csv.");
        }

        var content = new StringContent(fileContent, Encoding.UTF8, contentType);
        var response = await _httpClient.PostAsync(CustomerUrl + path, content);

        response.EnsureSuccessStatusCode();

        return await response.Content.ReadAsStringAsync();
    }

    public async Task<string> ImportReading(string fileContent, string fileType)
    {
        const string path = "/validateReadings";
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
                throw new ArgumentException("Unsupported file type. Allowed: json, xml, csv.");
        }

        var content = new StringContent(fileContent, Encoding.UTF8, contentType);
        var response = await _httpClient.PostAsync(ReadingUrl + path, content);

        response.EnsureSuccessStatusCode();

        return await response.Content.ReadAsStringAsync();
    }
}