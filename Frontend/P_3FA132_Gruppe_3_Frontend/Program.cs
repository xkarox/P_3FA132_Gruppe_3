using ApexCharts;
using Blazing.Mvvm;

using Microsoft.AspNetCore.Components.Web;
using Microsoft.AspNetCore.Components.WebAssembly.Hosting;
using P_3FA132_Gruppe_3_Frontend;
using P_3FA132_Gruppe_3_Frontend.Data.Services;

var builder = WebAssemblyHostBuilder.CreateDefault(args);
builder.RootComponents.Add<App>("#app");
builder.RootComponents.Add<HeadOutlet>("head::after");

builder.Services.AddScoped(sp => new HttpClient { BaseAddress = new Uri(builder.HostEnvironment.BaseAddress) });

// Register HttpClient
builder.Services.AddScoped(sp => new HttpClient { BaseAddress = new Uri(builder.HostEnvironment.BaseAddress) });

// Register Services for singelton use
builder.Services.AddScoped<DatabaseService>();
builder.Services.AddScoped<CustomerService>();
builder.Services.AddScoped<ReadingService>();

builder.Services.AddScoped<UtilityService>();

// Register MVVM 
builder.Services.AddMvvm(options =>
{ 
    options.HostingModelType = BlazorHostingModelType.WebAssembly;
});

builder.Services.AddApexCharts(e =>
{
    e.GlobalOptions = new ApexChartBaseOptions
    {
        Debug = true,
        Theme = new Theme { Palette = PaletteType.Palette10 }
    };
});
await builder.Build().RunAsync();
