using Newtonsoft.Json.Linq;
using System.Text.Json;
using System.Text.Json.Serialization;

namespace P_3FA132_Gruppe_3_Frontend.Data.Models.Classes
{
    public class Customer : IBaseClass<Customer>
    {
        public Guid Id { get; set; }
        public string FirstName { get; set; }
        public string LastName { get; set; }
        public DateOnly? DateOfBirth { get; set; }
        public Gender Gender { get; set; }


        public string ToJson(bool indent = true)
        {
            var options = new JsonSerializerOptions
            {
                DefaultIgnoreCondition = JsonIgnoreCondition.WhenWritingNull,
                WriteIndented = indent
            };

            var customer = new
            {
                customer = BuildFormate()
            };

            return JsonSerializer.Serialize(customer, options);
        }

        public object BuildFormate()
        {
            var customerJson = new
            {
                id = Id == Guid.Empty ? null : Id.ToString(),
                firstName = FirstName,
                lastName = LastName,
                birthDate = DateOfBirth == default ? null : DateOfBirth?.ToString("yyyy-MM-dd"),
                gender = Gender.ToString()
            };

            return customerJson;
        }

        public static Customer LoadJson(string jsonData, bool loadDefaultRoot = true)
        {
            using var document = JsonDocument.Parse(jsonData);
            JsonElement root;
            if (loadDefaultRoot)
                root = document.RootElement.GetProperty("customer");
            else
                root = document.RootElement;

            var customer = new Customer
            {
                Id = root.GetProperty("id").GetGuid(),
                FirstName = root.GetProperty("firstName").GetString(),
                LastName = root.GetProperty("lastName").GetString(),
                DateOfBirth = root.GetProperty("birthDate").ValueKind == JsonValueKind.Null ? null : DateOnly.Parse(root.GetProperty("birthDate").GetString()),
                Gender = Enum.Parse<Gender>(root.GetProperty("gender").GetString())
            };

            return customer;
        }

        public static IEnumerable<Customer> LoadJsonList(string jsonData)
        {
            List<Customer> customers = new List<Customer>();
            JObject jsonObject = JObject.Parse(jsonData);
            JArray customersArray = (JArray)jsonObject["customers"];
            foreach (var customerJson in customersArray)
            {
                string customerJsonString = customerJson.ToString();
                Customer newCustomer = LoadJson(customerJsonString, false);
                customers.Add(newCustomer);
            }

            return customers;
        }

        public Customer Copy()
        {
            return (Customer)this.MemberwiseClone();
        }
    }
}
