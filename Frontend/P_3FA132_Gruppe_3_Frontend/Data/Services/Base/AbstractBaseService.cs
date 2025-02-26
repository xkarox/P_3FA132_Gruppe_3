using System.Text;
using P_3FA132_Gruppe_3_Frontend.Data.Models;
using P_3FA132_Gruppe_3_Frontend.Data.Models.Classes;


namespace P_3FA132_Gruppe_3_Frontend.Data.Services.Base
{
    public abstract class AbstractBaseService
    {
        private readonly string _baseUrl = "http://localhost:8080";
        protected readonly Uri _endpointUrl;
        protected readonly HttpClient _httpClient;

        public AbstractBaseService(HttpClient httpClient, string endpointUrl)
        {
            _httpClient = httpClient;
            _endpointUrl = BuildUri(endpointUrl);
        }

        protected Uri BuildUri(string relativePath)
        {
            var builder = new UriBuilder(_baseUrl)
            {
                Path = relativePath
            };
            return builder.Uri;
        }
        
        protected Uri BuildUri(string relativePath, IEnumerable<KeyValuePair<string, object>> queryParams)
        {
            var queryParameterString = BuildQueryParameters(queryParams);
            
            var builder = new UriBuilder(_baseUrl)
            {
                Path = relativePath,
                Query = queryParameterString
            };
            return builder.Uri;
        }

        private string BuildQueryParameters(
            IEnumerable<KeyValuePair<string, object>> queryParams)
        {
            var queryStringBuilder = new StringBuilder("");
            var index = 0;
            foreach (var (key, value) in queryParams)
            {
                if (index > 0)
                {
                    queryStringBuilder.Append("&");
                }
                if (value is KindOfMeter kindOfMeter)
                {
                    queryStringBuilder.Append($"kindOfMeter={(int)kindOfMeter}");
                }
                else if (value is DateOnly dateOnly)
                {
                    queryStringBuilder.Append($"{key.ToLower()}={dateOnly.ToString("yyyy-MM-dd")}");
                }
                else
                {
                    queryStringBuilder.Append($"{key.ToLower()}={value.ToString()}");
                }
                index++;
            }

            return queryStringBuilder.ToString();
        }
        protected enum HttpMethod
        {
            GET,
            GETALL,
            POST,
            PUT,
            DELETE
        }
    }
}
