namespace P_3FA132_Gruppe_3_Frontend.Data.Models.Authentication;

public interface IAuth
{
    Task LoginAsync(string username, string password);
    void Logout();
}