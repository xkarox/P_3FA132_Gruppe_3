namespace P_3FA132_Gruppe_3_Frontend.Data.Models.Classes
{
    public interface IBaseClass<T>
    {
        public Guid Id { get; set; }
        public string ToJson(bool indent = false);
        public object BuildFormate();
        public static abstract T LoadJson(string jsonData, bool loadDefaultroot = true);
        public static abstract IEnumerable<T> LoadJsonList(string jsonData);
    }
}
