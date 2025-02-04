
function CopyToClipboard(value)
{
    return navigator.clipboard.writeText(value).then(function() {
        return true;
    }).catch(function(error){
        return false;
    });
}


function ShowNotification(message, x, y) {
    const notification = document.createElement("div");
    notification.className = "notification";
    notification.innerText = message;
    notification.style.position = "absolute";
    
    const scrollX = window.scrollX;
    const scrollY = window.scrollY;
    notification.style.left = `${x + scrollX}px`;
    notification.style.top = `${y - 40 + scrollY}px`;
    
    document.body.appendChild(notification);
    setTimeout(() => {
        document.body.removeChild(notification);
    }, 500);
}


function ShowNotificationHover(message, x, y) {
    const notification = document.createElement("div");
    notification.className = "notification hover";
    notification.innerText = message;
    notification.style.position = "absolute";

    const scrollX = window.scrollX;
    const scrollY = window.scrollY;
    notification.style.left = `${x + scrollX}px`;
    notification.style.top = `${y - 40 + scrollY}px`;

    document.body.appendChild(notification);
}

function RemoveNotificationHover()
{
    const notification = document.getElementsByClassName("notification hover")[0]
    if(notification) {
        document.body.removeChild(notification)
    }
}