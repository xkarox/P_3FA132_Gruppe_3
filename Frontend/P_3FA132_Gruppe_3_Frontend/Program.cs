using System.Net;
using BitzArt.Blazor.Cookies;
using Blazing.Mvvm;
using Blazored.LocalStorage;
using Microsoft.AspNetCore.Components.Authorization;
using Microsoft.AspNetCore.Components.Web;
using Microsoft.AspNetCore.Components.WebAssembly.Hosting;
using Microsoft.AspNetCore.Components.WebAssembly.Http;
using P_3FA132_Gruppe_3_Frontend;
using P_3FA132_Gruppe_3_Frontend.Data.Authentication;
using P_3FA132_Gruppe_3_Frontend.Data.MessageHandler;
using P_3FA132_Gruppe_3_Frontend.Data.Services;

var builder = WebAssemblyHostBuilder.CreateDefault(args);
builder.RootComponents.Add<App>("#app");
builder.RootComponents.Add<HeadOutlet>("head::after");


builder.Services.AddScoped<AuthenticationStateProvider, CustomAuthStateProvider>();
builder.Services.AddScoped<AuthService>();
builder.Services.AddAuthorizationCore(options =>
{
    options.AddPolicy("Admin", policy => policy.RequireRole("Admin"));
    options.AddPolicy("CanRead", policy => policy.RequireClaim("Permission", "READ"));
    options.AddPolicy("CanWrite", policy => policy.RequireClaim("Permission", "WRITE"));
    options.AddPolicy("CanDelete", policy => policy.RequireClaim("Permission", "DELETE"));
});
builder.Services.AddCascadingAuthenticationState();


builder.Services.AddScoped<DatabaseService>();
builder.Services.AddScoped<CustomerService>();
builder.Services.AddScoped<ReadingService>();

builder.Services.AddScoped<UtilityService>();


builder.Services.AddMvvm(options =>
{
    options.HostingModelType = BlazorHostingModelType.WebAssembly;
});

builder.AddBlazorCookies();
builder.Services.AddOptions();
builder.Services.AddScoped<AuthorizationMessageHandler>();
const string baseURL = "http://localhost:8080/";
builder.Services.AddHttpClient("AuthApi", client =>
{
    client.BaseAddress = new Uri(baseURL);
    client.DefaultRequestHeaders.Add("X-Requested-With", "JSONHttpRequest");
})
.AddHttpMessageHandler<AuthorizationMessageHandler>();

await builder.Build().RunAsync();
