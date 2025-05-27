
using System.Security.Claims;
using Newtonsoft.Json.Linq;
using P_3FA132_Gruppe_3_Frontend.Data.Models.Enums;
using System.Text.Json;
using System.Text.Json.Serialization;

namespace P_3FA132_Gruppe_3_Frontend.Data.Models.Classes
{
    public class AuthUser : IBaseClass<AuthUser>
    {
        public Guid Id { get; set; }
        public string Username { get; set; }
        public string Role { get; set; }
        public List<UserPermissions>? Permissions { get; set; }

        public static AuthUser LoadJson(string jsonData, bool loadDefaultroot = true)
        {
            using var document = JsonDocument.Parse(jsonData);
            JsonElement root = document.RootElement;

            var authUser = new AuthUser
            {
                Id = root.GetProperty("id").GetGuid(),
                Username = root.GetProperty("username").GetString(),
                Role = root.GetProperty("role").GetString(),
                Permissions = root.GetProperty("permissions").EnumerateArray().Select(p => p.GetString().ToUserPermissions()).ToList()
            };

            return authUser;
        }

        public static IEnumerable<AuthUser> LoadJsonList(string jsonData)
        {
            List<AuthUser> authUsers = new List<AuthUser>();
            JObject jObject = JObject.Parse(jsonData);
            JArray jArray = (JArray)jObject[0]; //ToDo: Check if this is correct for the root element as none is specified in the endpoint
            foreach (var item in jArray)
            {
                authUsers.Add(LoadJson(item.ToString(), false));
            }

            return authUsers;
        }

        public object BuildFormate()
        {
            return new
            {
                id = Id == Guid.Empty ? null : Id.ToString(),
                username = Username,
                role = Role.ToString(),
                permissions = Permissions?.Select(p => p.ToString()).ToList()
            };
        }

        public string ToJson(bool indent = false)
        {
            var options = new JsonSerializerOptions
            {
                DefaultIgnoreCondition = JsonIgnoreCondition.WhenWritingNull,
                WriteIndented = indent
            };

            var authUser = new
            {
                authUser = BuildFormate()
            };

            return JsonSerializer.Serialize(authUser, options);
        }
        
        public static AuthUser GetDummyAuthUserForDisabledAuthentication()
        {
            return new AuthUser()
            {
                Id = Guid.Empty,
                Username = "User",
                Role = "ADMIN",
                Permissions = [UserPermissions.READ, 
                    UserPermissions.DELETE, 
                    UserPermissions.WRITE, 
                    UserPermissions.UPDATE]
            };
        }
    }
}
 