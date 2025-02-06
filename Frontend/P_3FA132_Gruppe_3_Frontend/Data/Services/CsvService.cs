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

    public async Task<IEnumerable<List<string>>> UploadCsvFileAsync(IBrowserFile file)
    {
        if (file == null)
            throw new ArgumentNullException(nameof(file));

        using var content = new MultipartFormDataContent();
        var fileStream = file.OpenReadStream();
        var streamContent = new StreamContent(fileStream);
        streamContent.Headers.ContentType = new MediaTypeHeaderValue("text/csv");

        content.Add(streamContent, "file", file.Name);

        var response = await _httpClient.PostAsync(_baseUrl, content);

        if (!response.IsSuccessStatusCode)
            throw new Exception($"Fehler beim Hochladen: {response.StatusCode}");

        var result = await response.Content.ReadFromJsonAsync<IEnumerable<List<string>>>();

        return result ?? Enumerable.Empty<List<string>>();
    }
}