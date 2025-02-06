using P_3FA132_Gruppe_3_Frontend.Data.Models.Classes;
using System.Text;

namespace P_3FA132_Gruppe_3_Frontend.Data.Services.Base
{
    public class AbstractCrudService<T> : AbstractBaseService<T> where T : IBaseClass<T>
    {
        public AbstractCrudService(HttpClient httpClient, string endpointUrl) : base(httpClient, endpointUrl)
        {
        }

        public async Task<T?> Add(T item)
        {
            return await GetObject(item, HttpMethod.POST);
        }

        public async Task<T?> Update(T item)
        {
            return await GetObject(item, HttpMethod.PUT);
        }

        public async Task<T?> Get(Guid id)
        {
            T item = (T)Activator.CreateInstance(typeof(T));
            item!.Id = id;
            return await GetObject(item!, HttpMethod.GET);
        }

        public async Task<IEnumerable<T>?> GetAll()
        {
            return await GetObjects(HttpMethod.GETALL);
        }

        public async Task<T?> Delete(Guid id)
        {
            T item = (T)Activator.CreateInstance(typeof(T));
            item!.Id = id;
            return await GetObject(item!, HttpMethod.DELETE);
        }

        private async Task<T?> GetObject(T item, HttpMethod methode)
        {
            HttpResponseMessage response;
            switch (methode)
            {
                case HttpMethod.POST:
                    response = await _httpClient.PostAsync(_endpointUrl, new ByteArrayContent(GetBytes(item)));
                    break;
                case HttpMethod.PUT:
                    response = await _httpClient.PutAsync(_endpointUrl, new ByteArrayContent(GetBytes(item)));
                    break;
                case HttpMethod.GET:
                    response = await _httpClient.GetAsync($"{_endpointUrl}/{item.Id}");
                    break;
                case HttpMethod.DELETE:
                    response = await _httpClient.DeleteAsync($"{_endpointUrl}/{item.Id}");
                    break;
                default:
                    return default;
            }

            if (!response.IsSuccessStatusCode)
                return default;
            var responseText = await response.Content.ReadAsStringAsync();
            if (methode != HttpMethod.PUT)
            {
                return T.LoadJson(responseText);
            }

            return default;
        }

        private async Task<IEnumerable<T>?> GetObjects(HttpMethod methode)
        {
            HttpResponseMessage response;
            switch (methode)
            {
                case HttpMethod.GETALL:
                    response = await _httpClient.GetAsync(_endpointUrl);
                    break;
                default:
                    return default;
            }
            if (!response.IsSuccessStatusCode)
                return default;
            var responseText = await response.Content.ReadAsStringAsync();
            return T.LoadJsonList(responseText);
        }

        private byte[] GetBytes(T item)
        {
            return Encoding.UTF8.GetBytes(item.ToJson());
        }
    }
}
