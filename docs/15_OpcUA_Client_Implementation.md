


          
# OPC UA Client Implementierungsdokumentation

## 1. Überblick

Die OPC UA (OPC Unified Architecture) Client-Implementierung in unserer system_1 Anwendung bietet eine robuste Schnittstelle für die Kommunikation mit industriellen Automatisierungssystemen und IoT-Geräten. Dieses Dokument beschreibt die Architektur, Komponenten und Verwendung unserer OPC UA Client-Implementierung.

## 2. Architektur

Die OPC UA Client-Implementierung besteht aus mehreren Schlüsselkomponenten:

```
┌─────────────────────────┐
│ System1Application      │
└───────────┬─────────────┘
            │
            ▼
┌─────────────────────────┐
│ OpcuaClientApplication  │◄────┐
└───────────┬─────────────┘     │
            │                   │
            ▼                   │
┌─────────────────────────┐     │
│ OpcuaConnectionManager  │     │
└───────────┬─────────────┘     │
            │                   │
            ▼                   │
┌─────────────────────────┐     │
│ OPC UA Server           │     │
└─────────────────────────┘     │
                                │
┌─────────────────────────┐     │
│ OpcuaSensorDataService  ├─────┘
└─────────────────────────┘
```

## 3. Hauptkomponenten

### 3.1 OpcuaClientApplication

Der Haupteinstiegspunkt für OPC UA-Funktionalität, den andere Komponenten nutzen können. Es bietet eine High-Level-API für die Verbindung zu OPC UA-Servern und das Abrufen von Sensordaten.

Wichtige Methoden:
- `isConnected()` - Prüft, ob der Client mit dem Server verbunden ist
- `reconnect()` - Versucht, die Verbindung zum Server wiederherzustellen
- `getLatestSensorData()` - Ruft die neuesten Sensordaten ab

### 3.2 OpcuaConnectionManager

Verwaltet die Low-Level-Verbindung zum OPC UA-Server, einschließlich:
- Aufbau sicherer Verbindungen
- Verwaltung des Verbindungslebenszyklus
- Behandlung der Wiederverbindungslogik
- Bereitstellung des Zugriffs auf den zugrunde liegenden OPC UA-Client

### 3.3 OpcuaSensorDataService

Verantwortlich für das Lesen von Sensordaten vom OPC UA-Server und deren Speicherung für den Abruf:
- Geplante Datenerfassung in konfigurierbaren Intervallen
- Lesen von Temperatur- und Feuchtigkeitswerten
- Speichern von Messwerten mit eindeutigen IDs
- Bereitstellung des Zugriffs auf historische Messwerte

### 3.4 SensorData

Eine Modellklasse, die Sensormesswerte repräsentiert:
- Eindeutige ID für jede Messung
- Temperaturwert
- Feuchtigkeitswert

## 4. Integration mit Spring Boot

Der OPC UA-Client ist in die Dependency Injection und das Lifecycle-Management von Spring Boot integriert:

```java
@SpringBootApplication
public class System1Application {

    private final OpcuaClientApplication opcuaClient;

    @Autowired
    public System1Application(OpcuaClientApplication opcuaClient) {
        this.opcuaClient = opcuaClient;
    }
    
}
```

## 5. Verwendung des OPC UA-Clients

### 5.1 Dependency Injection

Injizieren Sie die OpcuaClientApplication in Ihre Spring-Komponenten:

```java
@Component
public class YourComponent {
    
    private final OpcuaClientApplication opcuaClient;
    
    @Autowired
    public YourComponent(OpcuaClientApplication opcuaClient) {
        this.opcuaClient = opcuaClient;
    }
    
}
```

### 5.2 Verbindungsstatus prüfen

Bevor Sie versuchen, Daten zu lesen, prüfen Sie, ob der Client verbunden ist:

```java
boolean isConnected = opcuaClient.isConnected();
if (!isConnected) {
    // Attempt to reconnect
    boolean reconnected = opcuaClient.reconnect();
    if (!reconnected) {
        // Handle connection failure
        logger.error("Failed to connect to OPC UA server");
        return;
    }
}
```

### 5.3 Sensordaten lesen

Rufen Sie die neuesten Sensordaten ab:

```java
Optional<SensorData> latestData = opcuaClient.getLatestSensorData();
if (latestData.isPresent()) {
    SensorData data = latestData.get();
    // Use the data
    double temperature = data.getTemperature();
    double humidity = data.getHumidity();
    String id = data.getId();
    
    // Process the data...
} else {
    // Handle missing data
    logger.warn("No sensor data available");
}
```

## 6. Konfiguration

Der OPC UA-Client kann über die Datei `application.properties` konfiguriert werden:

```properties
# OPC UA Server Configuration
opcua.server.url=opc.tcp://localhost:4840
opcua.server.namespace=http://example.org/UA/
opcua.server.node.temperature=ns=2;s=Temperature
opcua.server.node.humidity=ns=2;s=Humidity

# Security Configuration
opcua.security.mode=SignAndEncrypt
opcua.security.policy=Basic256Sha256
opcua.security.certificatePath=certificates/client.der
opcua.security.privateKeyPath=certificates/client.pfx
opcua.security.password=password

# Data Collection
opcua.data.collection.interval=30000
```

## 7. Sicherheit

Die OPC UA-Client-Implementierung umfasst robuste Sicherheitsfunktionen:

- **X.509-Zertifikate**: Verwendet für Authentifizierung und Verschlüsselung
- **Sicherheitsmodi**: Unterstützung für None, Sign und SignAndEncrypt
- **Sicherheitsrichtlinien**: Unterstützung für verschiedene Verschlüsselungsalgorithmen
- **Zertifikatsverwaltung**: Tools zur Generierung und Verwaltung von Zertifikaten

## 8. Fehlerbehandlung

Der OPC UA-Client umfasst eine umfassende Fehlerbehandlung:

- **Verbindungsfehler**: Automatische Wiederverbindungsversuche
- **Datenlesefehler**: Elegante Behandlung von Lesefehlern
- **Sicherheitsausnahmen**: Ordnungsgemäße Behandlung von sicherheitsbezogenen Problemen

## 9. Tests

Die OPC UA-Client-Implementierung umfasst umfassende Unit-Tests:

- **OpcuaSensorDataServiceTest**: Tests für das Lesen und Speichern von Sensordaten
- **OpcuaConnectionManagerTest**: Tests für die Verbindungsverwaltung
- **OpcuaClientApplicationTest**: Integrationstests für die Client-API

Beispiel-Testfall:

```java
@Test
void testReadAndStoreSensorDataSuccess() throws Exception {
    // Arrange
    int namespaceIndex = 2;
    
    // Mock temperature data value
    DataValue tempDataValue = mock(DataValue.class);
    Variant tempVariant = mock(Variant.class);
    when(tempVariant.getValue()).thenReturn(22.5);
    when(tempDataValue.getValue()).thenReturn(tempVariant);
    CompletableFuture<DataValue> tempFuture = CompletableFuture.completedFuture(tempDataValue);
    
    // Mock humidity data value
    DataValue humDataValue = mock(DataValue.class);
    Variant humVariant = mock(Variant.class);
    when(humVariant.getValue()).thenReturn(45.7);
    when(humDataValue.getValue()).thenReturn(humVariant);
    CompletableFuture<DataValue> humFuture = CompletableFuture.completedFuture(humDataValue);
    
    // Mock client behavior
    when(opcUaClient.readValue(anyDouble(), any(TimestampsToReturn.class), any(NodeId.class)))
        .thenReturn(tempFuture)
        .thenReturn(humFuture);
    
    // Act
    String id = sensorDataService.readAndStoreSensorData(opcUaClient, namespaceIndex);
    
    // Assert
    assertNotNull(id, "Should return a valid ID");
    
    // Verify the data was stored correctly
    Optional<SensorData> storedData = sensorDataService.getSensorData(id);
    assertTrue(storedData.isPresent(), "Sensor data should be stored");
    assertEquals(22.5, storedData.get().getTemperature(), 0.001, "Temperature should match");
    assertEquals(45.7, storedData.get().getHumidity(), 0.001, "Humidity should match");
}
```

## 10. Fehlerbehebung

Häufige Probleme und ihre Lösungen:

### 10.1 Verbindungsprobleme

**Problem**: Keine Verbindung zum OPC UA-Server möglich
**Lösungen**:
- Überprüfen Sie, ob die Server-URL korrekt ist
- Prüfen Sie, ob der Server läuft und erreichbar ist
- Stellen Sie sicher, dass die Netzwerkverbindung zwischen Client und Server besteht
- Überprüfen Sie, ob die Sicherheitseinstellungen mit den Serveranforderungen übereinstimmen

### 10.2 Sicherheitsprobleme

**Problem**: Sicherheitsbezogene Ausnahmen
**Lösungen**:
- Überprüfen Sie die Pfade für Zertifikate und private Schlüssel
- Stellen Sie sicher, dass Zertifikate ordnungsgemäß generiert und vertrauenswürdig sind
- Überprüfen Sie die Kompatibilität von Sicherheitsmodus und -richtlinie mit dem Server

### 10.3 Datenleseprobleme

**Problem**: Sensordaten können nicht gelesen werden
**Lösungen**:
- Überprüfen Sie die Konfiguration von Namespace und Node-ID
- Prüfen Sie, ob der Server die erwarteten Datenpunkte bereitstellt
- Stellen Sie die korrekte Datentypkonvertierung sicher

## 11. Fazit

Die OPC UA-Client-Implementierung bietet eine robuste Möglichkeit, mit industriellen Systemen und IoT-Geräten zu kommunizieren. Durch Befolgen dieser Dokumentation können Sie OPC UA-Funktionalität effektiv in Ihre Spring Boot-Anwendung integrieren.

## 12. Referenzen

- [Eclipse Milo OPC UA Dokumentation](https://github.com/eclipse/milo)
