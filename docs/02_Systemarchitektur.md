📄 02_Systemarchitektur.md
==========================

🔧 Systemübersicht
----------------

Die Systemarchitektur unseres Industrie 4.0-Projekts besteht aus drei Hauptkomponenten, die über verschiedene Kommunikationsprotokolle miteinander verbunden sind. Das System demonstriert moderne industrielle Kommunikationsstandards und ermöglicht eine durchgängige Datenkette von der Sensorik bis zur Visualisierung.

📊 Systemkomponenten-Flow
------------------------

```mermaid
flowchart TD
  subgraph "Raspberry Pi - Python"
    A1[GPIO Sensor<br/>Temp & Feuchtigkeit]
    A2[OPC UA Server]
    A3[Farberkennung Kamera]
    A4[TCP/IP Socket Server]
    
    A1 --> A2
    A3 --> A4
  end

  subgraph "System 1 - PC Spring Boot"
    B1[OPC UA Client<br/>liest Temperatur & Feuchtigkeit]
    B2[TCP/IP Client<br/>empfängt RGB-Daten]
    B3[Farbanalyse & Sortierlogik]
    B4[Dobot Steuerung<br/>USB]
    B5[Stromkostenmodul]
    B6[REST Client → awattar]
  end

  subgraph Extern
    C1[awattar API<br/>REST]
  end

  A2 --> B1
  A4 --> B2
  B2 --> B3
  B1 --> B3
  B5 --> B3
  B3 --> B4
  C1 --> B6
  B6 --> B5
```

🔍 Hauptkomponenten
-----------------

### 1. Raspberry Pi (Python)
- **Sensordatenerfassung:** Temperatur- und Feuchtigkeitsmessung über GPIO
- **Bildverarbeitung:** Farberkennung mittels Kamera
- **Kommunikation:** OPC UA Server für Sensordaten, TCP/IP Socket Server für Bilddaten

### 2. System 1 - PC (Spring Boot)
- **Datenempfang:** OPC UA Client und TCP/IP Client
- **Verarbeitung:** Farbanalyse und Sortierlogik
- **Steuerung:** Dobot-Roboter via USB
- **Energiemanagement:** Stromkostenberechnung mit aWATTar-Integration

### 3. Externe Systeme
- **aWATTar API:** REST-Schnittstelle für Energiepreisdaten

🔌 Kommunikationsprotokolle
-------------------------

- **OPC UA:** Industriestandard für Sensordatenübertragung
- **TCP/IP:** Netzwerkkommunikation für Bilddaten
- **REST:** HTTP-basierte API-Kommunikation
- **USB:** Direkte Hardwaresteuerung des Roboters

📎 Verknüpfte Kapitel
---------------------

- [03_Datenfluss_und_Kommunikation.md](03_Datenfluss_und_Kommunikation.md)
- [04_Softwarekomponenten_System1.md](04_Softwarekomponenten_System1.md)
- [08_MQTT_Nachrichtenmodell.md](08_MQTT_Nachrichtenmodell.md)