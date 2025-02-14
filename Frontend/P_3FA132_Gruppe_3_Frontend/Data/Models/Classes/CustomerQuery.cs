namespace P_3FA132_Gruppe_3_Frontend.Data.Models.Classes;

public class CustomerQuery
{
    public string FirstName { get; set; }

    public string LastName { get; set; }
    
    public List<KeyValuePair<string, object>> QueryParameters
    {
        get
        {
            var type = this.GetType();
            var properties = type.GetProperties();
            var queryParams = new List<KeyValuePair<string, object>>();
            foreach (var property in properties)
            {
                var propName = property.Name;
                if (propName == "QueryParameters")
                {
                    continue;
                }
                var queryProp = property.GetValue(this);
                if (queryProp != null)
                {
                    queryParams.Add(
                        new KeyValuePair<string, object>(key: propName,
                            value: queryProp));
                }
            }

            return queryParams;
        }
    }
}