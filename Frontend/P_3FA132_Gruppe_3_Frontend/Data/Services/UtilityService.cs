using Microsoft.AspNetCore.Components;
using Microsoft.JSInterop;
using System.Threading.Tasks;

public class UtilityService(IJSRuntime jsRuntime)
{
    public async Task CopyToClipboard(string value, double x, double y)
    {
        var success =
            await jsRuntime.InvokeAsync<bool>("CopyToClipboard", value);
        if (success)
        {
            await jsRuntime.InvokeVoidAsync("ShowNotification", "Copied!", x, y);
        }
    }

    public async Task ShowNotificationHover(string value, double x, double y)
    {
        await jsRuntime.InvokeVoidAsync("ShowNotificationHover", value, x, y);
    }

    public async Task RemoveNotificationHover()
    {
        await jsRuntime.InvokeVoidAsync("RemoveNotificationHover");
    }
}