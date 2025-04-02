using System.Globalization;
using System.Text;
using System.Text.Json;
using Newtonsoft.Json.Linq;
using System.Text.Json.Serialization;

namespace P_3FA132_Gruppe_3_Frontend.Data.Models.Classes
{
    public class Reading : IBaseClass<Reading>
    {
        public Guid Id { get; set; }
        public string? Comment { get; set; }
        public Guid? CustomerId { get; set; }
        public Customer? Customer { get; set; }
        public DateOnly? DateOfReading { get; set; }
        public KindOfMeter KindOfMeter { get; set; }
        public double MeterCount { get; set; }
        public string MeterId { get; set; }
        public bool Substitute { get; set; }
        public string CustomerName =>
            new StringBuilder(Customer?.FirstName).Append(' ')
                .Append(Customer?.LastName).ToString();
        public string FormattedDate =>
            DateOfReading?.ToString("yyyy-MM-dd") ?? new DateOnly().ToString("yyyy-MM-dd");
        public string MeterCountWithUnit =>
            new StringBuilder(MeterCount.ToString(CultureInfo.InvariantCulture))
                .Append(' ').Append(KindOfMeter.GetUnit()).ToString();

        public string ToJson(bool indent = true)
        {
            var options = new JsonSerializerOptions
            {
                DefaultIgnoreCondition = JsonIgnoreCondition.WhenWritingNull,
                WriteIndented = indent
            };
            var reading = new
            {
                reading = BuildFormate()
            };
            return JsonSerializer.Serialize(reading, options);
        }

        public object BuildFormate()
        {
            var readingJson = new
            {
                id = Id == Guid.Empty ? null : Id.ToString(),
                comment = Comment,
                customer = Customer == null ? null : Customer.BuildFormate(),
                customerId = CustomerId,
                dateOfReading = DateOfReading == default ? null : DateOfReading?.ToString("yyyy-MM-dd"),
                kindOfMeter = KindOfMeter.ToString(),
                meterCount = MeterCount,
                meterId = MeterId,
                substitute = Substitute
            };

            return readingJson;
        }

        public static Reading LoadJson(string jsonData, bool loadDefaultRoot = true)
        {
            using var document = JsonDocument.Parse(jsonData);

            var root = document.RootElement;
            JsonElement readingElement;

            if (root.TryGetProperty("reading", out readingElement))
            {
                try
                {
                    var reading = new Reading
                    {
                        Id = root.GetProperty("id").GetGuid(),
                        Comment = root.GetProperty("comment").ValueKind == JsonValueKind.Null ? null : root.GetProperty("comment").GetString(),
                        CustomerId = root.GetProperty("customerId").ValueKind == JsonValueKind.Null ? null : root.GetProperty("customerId").GetGuid(),
                        Customer = root.GetProperty("customer").ValueKind == JsonValueKind.Null ? null : Customer.LoadJson(root.ToString()),
                        DateOfReading = root.GetProperty("dateOfReading").ValueKind == JsonValueKind.Null ? null : DateOnly.Parse(root.GetProperty("dateOfReading").GetString()),
                        KindOfMeter = Enum.Parse<KindOfMeter>(root.GetProperty("kindOfMeter").GetString()),
                        MeterCount = root.GetProperty("meterCount").GetDouble(),
                        MeterId = root.GetProperty("meterId").GetString(),
                        Substitute = root.GetProperty("substitute").GetBoolean()
                    };

                    return reading;
                }
                catch (Exception ex)
                {
                    Console.WriteLine($"Fehler: {ex.Message}");
                    Console.WriteLine($"Stacktrace: {ex.StackTrace}");
                }

            }
                try
                {
                    var reading = new Reading
                    {
                        Id = root.GetProperty("id").ValueKind == JsonValueKind.Null ? Guid.Empty : root.GetProperty("id").GetGuid(),
                        Comment = root.GetProperty("comment").ValueKind == JsonValueKind.Null ? null : root.GetProperty("comment").GetString(),
                        Customer = root.GetProperty("customer").ValueKind == JsonValueKind.Null ? null : Customer.LoadJson(root.ToString()),
                        CustomerId = root.GetProperty("customerId").ValueKind == JsonValueKind.Null ? root.GetProperty("customer").GetProperty("id").GetGuid() : root.GetProperty("customerId").GetGuid(),
                        DateOfReading = root.GetProperty("dateOfReading").ValueKind == JsonValueKind.Null ? null : DateOnly.Parse(root.GetProperty("dateOfReading").GetString()),
                        KindOfMeter = Enum.Parse<KindOfMeter>(root.GetProperty("kindOfMeter").GetString()),
                        MeterCount = root.GetProperty("meterCount").GetDouble(),
                        MeterId = root.GetProperty("meterId").GetString(),
                        Substitute = root.GetProperty("substitute").GetBoolean()
                    };

                    return reading;
                }
                catch (Exception ex)
                {
                    // Logge den Fehler, um herauszufinden, was schiefgelaufen ist
                    Console.WriteLine($"Fehler: {ex.Message}");
                    Console.WriteLine($"Stacktrace: {ex.StackTrace}");
                    // Hier kannst du auch den Typ der Ausnahme prüfen (z.B. InvalidCastException, FormatException, etc.)
                }


            return null;

        }

        public Reading Copy()
        {
            return (Reading)this.MemberwiseClone();
        }

        public static IEnumerable<Reading> LoadJsonList(string jsonData)
        {
            List<Reading> readings = new List<Reading>();
            JObject jsonObject = JObject.Parse(jsonData);
            JArray readingsArray = (JArray)jsonObject["readings"];
            foreach (var readingJson in readingsArray)
            {
                string readingJsonString = readingJson.ToString();
                Reading newReading = LoadJson(readingJsonString, false);
                readings.Add(newReading);
            }

            return readings;
        }
    }
}
