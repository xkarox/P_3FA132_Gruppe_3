using System.Net.Http.Headers;
using Microsoft.AspNetCore.Components.WebAssembly.Http;

public class JwtAuthorizationMessageHandler : DelegatingHandler
{
    protected override Task<HttpResponseMessage> SendAsync(HttpRequestMessage request, CancellationToken cancellationToken)
    {
        request.SetBrowserRequestCredentials(BrowserRequestCredentials.Include);
        return base.SendAsync(request, cancellationToken);
    }
}