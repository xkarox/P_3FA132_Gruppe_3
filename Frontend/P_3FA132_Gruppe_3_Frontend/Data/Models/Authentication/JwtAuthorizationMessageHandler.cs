using System.Net.Http.Headers;
using Microsoft.AspNetCore.Components.WebAssembly.Http;

public class JwtAuthorizationMessageHandler : DelegatingHandler
{
    protected override Task<HttpResponseMessage> SendAsync(HttpRequestMessage request, CancellationToken cancellationToken)
    {
        Console.Write("request.RequestUri: ");
        Console.WriteLine(request.RequestUri);
        request.SetBrowserRequestCredentials(BrowserRequestCredentials.Include);
        return base.SendAsync(request, cancellationToken);
    }
}