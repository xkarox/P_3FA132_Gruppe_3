using BitzArt.Blazor.Cookies;
using Blazing.Mvvm;
using Microsoft.AspNetCore.Components.Authorization;
using Microsoft.AspNetCore.Components.Web;
using Microsoft.AspNetCore.Components.WebAssembly.Hosting;
using Microsoft.AspNetCore.Components.WebAssembly.Http;
using P_3FA132_Gruppe_3_Frontend;
using P_3FA132_Gruppe_3_Frontend.Data.Models.Authentication;
using P_3FA132_Gruppe_3_Frontend.Data.Services;

var builder = WebAssemblyHostBuilder.CreateDefault(args);
builder.RootComponents.Add<App>("#app");
builder.RootComponents.Add<HeadOutlet>("head::after");


// Register Services for singelton use
builder.Services.AddSingleton<AuthenticatedUserStorage>();

builder.Services.AddScoped<UserAuthService>();
builder.Services.AddScoped<AuthStateProvider>();
builder.Services.AddScoped<AuthenticationStateProvider>(sp => 
    sp.GetRequiredService<AuthStateProvider>());
builder.Services.AddScoped<JwtAuthorizationMessageHandler>();

builder.Services.AddScoped<DatabaseService>();
builder.Services.AddScoped<CustomerService>();
builder.Services.AddScoped<ReadingService>();

builder.Services.AddScoped<UtilityService>();

builder.Services.AddAuthorizationCore();
builder.Services.AddCascadingAuthenticationState();
// Register MVVM 
builder.Services.AddMvvm(options =>
{ 
    options.HostingModelType = BlazorHostingModelType.WebAssembly;
});

builder.AddBlazorCookies();

builder.Services.AddOptions();

const string baseURL = "http://localhost:8080/";

builder.Services.AddHttpClient("AuthApi", client => 
    {
        client.BaseAddress = new Uri(baseURL);
    })
    .AddHttpMessageHandler<JwtAuthorizationMessageHandler>();

builder.Services.AddScoped(sp => new HttpClient(new JwtAuthorizationMessageHandler()) { BaseAddress = new Uri(builder.HostEnvironment.BaseAddress) });

await builder.Build().RunAsync();
