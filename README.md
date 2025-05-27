# Setup Anleitung

Ein Property-Management System mit Java Backend und .NET Frontend.

## **Systemvoraussetzungen**
Stellen Sie sicher, dass folgende Software installiert ist:
- **MariaDB 11.7.2** oder höher
- **JDK 21** 
- **.NET 8.0** SDK
- **Git** für das Klonen des Repositories
- **Maven** (normalerweise mit JDK enthalten)

## **Projekt Setup**
## **1. Repository klonen**

```shell
git clone git@github.com:xkarox/P_3FA132_Gruppe_3.git 
cd P_3FA132_Gruppe_3
```
## **Datenbank Konfiguration**

## **2. MariaDB Datenbank erstellen**

Verbinden Sie sich mit Ihrem MariaDB Server und führen Sie folgende Befehle aus:
```sql
CREATE DATABASE homeautomation;
```
## **3. Datenbankbenutzer erstellen**

Erstellen Sie einen dedizierten Benutzer mit den notwendigen Rechten:
```sql
CREATE USER 'homeautomation_user'@'localhost' IDENTIFIED BY 'homeautomation_pass';
GRANT SELECT, INSERT, UPDATE, DELETE, DROP, CREATE ON homeautomation_test.* TO 'homeautomation_user'@'localhost';
FLUSH PRIVILEGES;
```
## **Backend Konfiguration**

## **4. Konfigurationsdatei anpassen**

Navigieren Sie zur Konfigurationsdatei:
```shell
cd Backend/config/
```
Öffnen Sie `properties.config` und passen Sie die Konfiguration an:
```ini
# Ersetzen Sie 'user' mit Ihrem Windows-Benutzernamen 
# (zu finden unter C:\Users\*ihr-username*) 
ihr-username.db.url=localhost:3306/homeautomation 
ihr-username.db.user=homeautomation_user 
ihr-username.db.pw=secure_password
```
**Beispiel:**
```ini
max-mustermann.db.url=localhost:3306/homeautomation 
max-mustermann.db.user=homeautomation_user 
max-mustermann.db.pw=secure_password
```
## **5. Backend kompilieren und starten**
```shell
cd Backend mvn clean package 
java -jar target/P_3FA132_Gruppe_3-0.0.1.jar
```
Das Backend startet auf **[http://localhost:8080](http://localhost:8080/)**
**Erfolgsmeldung:** Sie sollten "Ready for Requests...." in der Konsole sehen.
## **6. Datenbanktabellen erstellen**
**Wichtiger Schritt:** Nach dem Start des Backends müssen Sie die Datenbanktabellen erstellen. Da es sich um eine POST-Request handelt, können Sie diese **nicht direkt über den Browser** aufrufen.

**Verwendung mit curl:**
```shell
# Mit Authentifizierung (empfohlen für Produktionsumgebung) 
curl -X POST http://localhost:8080/setupDB?secure=true 
# Ohne Authentifizierung 
curl -X POST http://localhost:8080/setupDB?secure=false 
# Oder ohne Parameter (verhält sich wie secure=false) 
curl -X POST http://localhost:8080/setupDB
```

**Alternative Methoden:**
- **Postman:** Erstellen Sie eine neue POST-Request an `http://localhost:8080/setupDB?secure=true`
- **VS Code REST Client:** Mit der entsprechenden Extension

**Parameter-Optionen:**
- **`secure=true`**: Erstellt alle Tabellen inklusive Authentifizierungstabellen (empfohlen für Produktionsumgebung)
- **`secure=false`**: Erstellt nur die Grundtabellen ohne Authentifizierung
- **Ohne Parameter**: Verhält sich wie `secure=false`

**Erfolgreiche Ausführung:** Sie sollten eine JSON-Antwort mit einer Bestätigung erhalten, dass die Tabellen erfolgreich erstellt wurden.
## **Frontend Setup**

## **6. Frontend starten**
Öffnen Sie ein neues Terminal und navigieren Sie zum Frontend:
```shell
cd Frontend/P_3FA132_Gruppe_3_Frontend 
dotnet run
```
Das Frontend startet standardmäßig auf **[https://localhost:5001](https://localhost:5001/)** oder **[http://localhost:5000](http://localhost:5000/)**
## **Systemtest**

## **Überprüfung der Installation**
1. Öffnen Sie die angezeigte Frontend-URL
2. Sie sollten sich jetzt mit dem dem User anmelden koennen 

## **Häufige Probleme**
## **Datenbankverbindung fehlgeschlagen**
- Überprüfen Sie die MariaDB-Installation und Servicestatus
- Kontrollieren Sie Benutzername und Passwort in der `properties.config`
- Stellen Sie sicher, dass die Datenbank `homeautomation` existiert

## **Port bereits in Verwendung**
- Backend: Port 8080 bereits belegt → Beenden Sie andere Anwendungen oder ändern Sie den Port
- Frontend: Port 5000/5001 bereits belegt → .NET verwendet automatisch alternative Ports

## **Maven Build Fehler**
```shell
# Cleanup und erneuter Versuch 
mvn clean 
mvn compile 
mvn package
```

## **Weitere Schritte**

Nach erfolgreichem Setup können Sie:
- Kunden anlegen und verwalten
- Zählerablesungen erfassen
- CSV-Daten importieren/exportieren
- Benutzer und Berechtigungen verwalten


