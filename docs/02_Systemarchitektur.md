📄 02_Systemarchitektur.md
==========================

🏗️ Systemarchitektur
----------------------------------------------------------------------

Dieses Kapitel beschreibt die **Systemarchitektur** des Projekts "Die Macher". Es zeigt die Komponenten, deren Zusammenspiel und die Datenflüsse zwischen den verschiedenen Systemen. Die Architektur ist modular aufgebaut und folgt dem Prinzip der Trennung von Zuständigkeiten.

🧩 Gesamtübersicht
-----------------

```mermaid
flowchart TD
    %% Verbesserte Ausrichtung und Gruppierung
    subgraph RPI["Raspberry Pi (Python)"]
        direction TB
        GPIO["GPIO Sensors (Temperatur, Luftfeuchtigkeit)"]
        OPC_S["OPC UA Server (Verschlüsselt mit Zertifikaten)"]
        CAM["Kamera (Würfel-Erkennung)"]
        TCP_C["TCP Server (Custom Protocol)"]

        GPIO --> OPC_S
        CAM --> TCP_C
    end

    subgraph SB["System 1 (Spring Boot, PC)"]
        direction TB
        OPC_C["OPC UA Client (Zertifikats-Authentifizierung)"]
        TCP_S["TCP Client (Inbound + Outbound)"]
        IMG_A["Kamera-Analyse/Farbservice"]
        DOBOT["Dobot Steuerung (USB)"]
        AWATTAR["Stromkostenmodul/awattar"]
        FWD["Datenweiterleitung"]

        TCP_S --> IMG_A
        IMG_A --> DOBOT
        OPC_C ---> FWD
        IMG_A ---> FWD
        AWATTAR ---> FWD
    end

    subgraph EXT["Externe Systeme"]
        AWAT_API["awattar API (REST)"]
    end

    %% Verbesserte Verbindungen mit höherem Kontrast
    OPC_S ===o|"Sensordaten"| OPC_C
    TCP_C ====>|"Bilddaten"| TCP_S
    TCP_S ====>|"Bildanfrage"| TCP_C
    AWAT_API ====>|"Strompreise"| AWATTAR
    DOBOT ====>|"Pick & Place"| CAM

    %% Main Flow mit deutlicherer Darstellung
    MF[("Hauptprozess")]:::flow
    MF -.->|1| DOBOT
    MF -.->|2| CAM
    MF -.->|3| TCP_S
    MF -.->|4| IMG_A
    MF -.->|5| DOBOT
    MF -.->|6| OPC_C
    MF -.->|7| AWATTAR
    MF -.->|8| FWD

    %% Deutlich unterscheidbare Farben
    classDef raspberry fill:#FF6B6B,stroke:#333,stroke-width:2px,color:black
    classDef springboot fill:#4ECDC4,stroke:#333,stroke-width:2px,color:black
    classDef external fill:#FFE66D,stroke:#333,stroke-width:2px,color:black
    classDef flow fill:#F7FFF7,stroke:#1A535C,stroke-width:2px,color:#1A535C

    %% Verbindungen mit besserem Kontrast
    linkStyle 0,1,2,3,4 stroke:#1A535C,stroke-width:2px
    linkStyle 5,6,7,8,9 stroke:#FF6B6B,stroke-width:3px,stroke-dasharray: 5 5
    
    class RPI raspberry
    class SB springboot
    class EXT external
    class MF flow
```

🔄 Hauptprozesse
-----------------

```mermaid
flowchart TB
    %% Subflow 1: Dobot und Kamera-Zyklus
    subgraph SF1["Subflow 1: Dobot und Kamera-Zyklus"]
        A1["1. Dobot platziert Würfel vor Kamera"]:::sf1_step
        B1["2. Spring Boot sendet TCP-Anfrage"]:::sf1_step
        C1["3. Raspberry Pi schneidet Bild zu & sendet Byte-Response"]:::sf1_step
        D1["4. Spring Boot dekodiert Bild & analysiert Farbe"]:::sf1_step
        E1["5. Farbe bestimmt Dobot-Ablageposition"]:::sf1_step
        F1["6. Dobot kehrt zur Startposition zurück"]:::sf1_step
        
        A1 --> B1 --> C1 --> D1 --> E1 --> F1
    end

    %% Subflow 2: Sensorik & OPC UA
    subgraph SF2["Subflow 2: Sensorik & OPC UA"]
        A2["1. Raspberry Pi liest GPIO Sensoren aus"]:::sf2_step
        B2["2. Werte über OPC UA Server bereitgestellt"]:::sf2_step
        C2["3. Spring Boot greift als OPC UA Client auf Daten zu"]:::sf2_step
        D2["4. Temperatur- & Luftfeuchtigkeitsdaten weiterverarbeitet"]:::sf2_step
        
        A2 --> B2 --> C2 --> D2
    end

    %% Subflow 3: Stromkosten-Ermittlung
    subgraph SF3["Subflow 3: Stromkosten-Ermittlung"]
        A3["1. Spring Boot nutzt awattar REST API für Strompreise"]:::sf3_step
        B3["2. Stromkosten werden berechnet"]:::sf3_step 
        C3["3. Ergebnis wird gespeichert"]:::sf3_step
        
        A3 --> B3 --> C3
    end

    %% Verbesserte Struktur durch Platzierung
    SF1 --> SF2 --> SF3
    
    %% Verbessertes Styling mit deutlicheren Schritten
    classDef subflow1 fill:#F9844A,stroke:#1D3557,stroke-width:3px,color:black
    classDef subflow2 fill:#4D908E,stroke:#1D3557,stroke-width:3px,color:black
    classDef subflow3 fill:#90BE6D,stroke:#1D3557,stroke-width:3px,color:black
    
    classDef sf1_step fill:#FFBB98,stroke:#1D3557,stroke-width:2px,color:black,font-weight:bold
    classDef sf2_step fill:#A7D5D3,stroke:#1D3557,stroke-width:2px,color:black,font-weight:bold
    classDef sf3_step fill:#C8E1B9,stroke:#1D3557,stroke-width:2px,color:black,font-weight:bold
    
    class SF1 subflow1
    class SF2 subflow2
    class SF3 subflow3
```

📊 Datenflüsse
--------------

```mermaid
flowchart LR
    %% Klarere Struktur mit Betonung auf Datenflüssen
    
    %% Datenquellen als dedizierte Gruppe
    subgraph INPUT["Datenquellen"]
        SENS["Temperatur & Luftfeuchtigkeit"]:::source
        CAM["Kamera"]:::source
        AWAPI["awattar API"]:::source
    end
    
    %% Raspberry Pi Komponenten
    subgraph RPI["Raspberry Pi"]
        RPI_OPC["OPC UA Server (verschlüsselt)"]:::rpi
        RPI_TCP["TCP Server (Custom Protocol)"]:::rpi
        
        SENS --> RPI_OPC
        CAM --> RPI_TCP
    end
    
    %% Spring Boot Komponenten
    subgraph SB["Spring Boot"]
        %% Unterteilung in logische Module
        subgraph COMM["Kommunikation"]
            SB_OPC["OPC UA Client"]:::sb_comm
            SB_TCP["TCP Client"]:::sb_comm
        end
        
        subgraph PROC["Verarbeitung"]
            SB_IMG["Bildanalyse Farbservice"]:::sb_proc
            SB_COST["Stromkostenmodul"]:::sb_proc
        end
        
        subgraph CTRL["Steuerung"]
            SB_ROB["Dobot Controller"]:::sb_ctrl
        end
        
        subgraph DATA["Datenmanagement"]
            SB_FWD["Datenverarbeitung"]:::sb_data
        end
        
        %% Interne Verbindungen
        SB_TCP --> SB_IMG
        SB_IMG --> SB_ROB
        SB_IMG --> SB_FWD
        SB_OPC --> SB_FWD
        SB_COST --> SB_FWD
        
        %% awattar Verbindung
        AWAPI --> SB_COST
    end
    
    %% Externe Systeme
    DOBOT["Dobot Roboter"]:::external
    
    %% Hauptdatenflüsse zwischen Systemen
    RPI_OPC -->|"Sensorwerte"| SB_OPC
    RPI_TCP -->|"Bilddaten"| SB_TCP
    SB_TCP -->|"Anfrage"| RPI_TCP
    SB_ROB <-->|"Steuerung"| DOBOT
    
    %% Verbesserte Farbcodierung
    classDef source fill:#F8F9FA,stroke:#212529,stroke-width:2px,color:#212529
    classDef rpi fill:#FF6B6B,stroke:#212529,stroke-width:2px,color:white
    classDef sb_comm fill:#4CC9F0,stroke:#212529,stroke-width:2px,color:white
    classDef sb_proc fill:#4361EE,stroke:#212529,stroke-width:2px,color:white
    classDef sb_ctrl fill:#3F37C9,stroke:#212529,stroke-width:2px,color:white
    classDef sb_data fill:#3A0CA3,stroke:#212529,stroke-width:2px,color:white
    classDef external fill:#F72585,stroke:#212529,stroke-width:2px,color:white
    
    %% Linkstile für verschiedene Verbindungstypen
    linkStyle default stroke:#212529,stroke-width:1.5px
```

📱 Komponenten im Detail
-----------------------

### 🔴 Raspberry Pi (Python)

* **GPIO-Platine**
  * Sensoren für Temperatur und Luftfeuchtigkeit
  * Datenübermittlung über OPC UA Server
  * Verschlüsselte Kommunikation mit Zertifikaten

* **Kamera**
  * Erkennung farbiger Würfel
  * Zugeschnittene Bilder des Würfels (zentraler Bildausschnitt)
  * Reagiert auf TCP-Anfragen von System 1
  * Sendet Byte-Array (Bilddaten) im Custom Protocol Format
  * Agiert als TCP-Server

### 🔵 System 1 (Spring Boot, PC)

* **TCP-Client (Inbound + Outbound)**
  * Empfängt Nachrichten vom Raspberry Pi
  * Stellt gezielte Bildanfragen an den Raspberry Pi
  * Verarbeitet Byte-Response mit Custom Header

* **Kamera-Analyse / Farbservice**
  * Wandelt Byte-Response in Bild um
  * Extrahiert dominante Farbe (Rot, Grün, Gelb, Blau)
  * Übergibt Farbinformation an die Sortierlogik

* **Dobot Steuerung (USB)**
  * Pick-and-Place Prozess:
    * Greift Würfel an fester Position
    * Platziert ihn vor Kamera
    * Sortiert nach Farbklassifikation
    * Kehrt zur Ausgangsposition zurück

* **OPC UA Client**
  * Verbindung zum OPC UA Server auf dem Raspberry Pi
  * Zertifikats-basierte Authentifizierung
  * Empfängt Temperatur- & Feuchtigkeitsdaten

* **Stromkostenmodul / awattar**
  * Abruf von Strompreisdaten über REST API
  * Berechnung der Stromkosten
  * Weiterleitung der Ergebnisse an System 2

* **Datenweiterleitung**
  * Übermittlung aller relevanten Daten an System 2 via TCP

### 🟢 Externe Systeme

* **awattar API (REST)**
  * Liefert aktuelle Strompreise
  * Basis für Stromkostenberechnung

📎 Verknüpfte Kapitel
---------------------

* [01_Projektübersicht.md](01_Projektübersicht.md)
* [03_Datenfluss_und_Kommunikation.md](03_Datenfluss_und_Kommunikation.md)
* [04_Komponenten_System1.md](04_Komponenten_System1.md)