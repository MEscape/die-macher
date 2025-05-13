📄 00\_Regeln\_und\_Inbetriebnahme.md
=====================================

Regeln & Inbetriebnahme
--------------------------------------

Dieses Kapitel beschreibt die grundlegenden **Projektregeln**, Konventionen und erste Schritte zur **Inbetriebnahme des Projekts** auf einem lokalen Rechner. Es dient als verbindliche Richtlinie für das gesamte Projektteam und soll die Zusammenarbeit effizient, einheitlich und nachvollziehbar gestalten.

✅ Projektregeln & Konventionen
------------------------------

### 🗣️ Sprache

* **Dokumentation** Deutsch

* **Code-Kommentare** Englisch

* **Git-Commits** Englisch

* **User Interfaces** Deutsch, Englisch

### 📂 Verzeichnisstruktur

```bash
/  ├── app/
/       ├── raspi/          # Raspberry Pi Code
/           ├── opc_ua/         # OPC UA Server
/           └── tcp_ip/         # TCP/IP Socket
/       ├── system_1/         # Spring Boot Backend
/       ├── system_2/         # MQTT & Database Server
/       ├── system_3/         # React Native Mobile App
/  ├── docs/               # Project Documentation
```

### 💡 Best Practices (Coding & Architektur)

#### 🔧 Architektur

*   **Modularität:** Trennung von Zuständigkeiten pro Subsystem
    
*   **Schnittstellenorientiert:** Kommunikation ausschließlich über definierte Schnittstellen (REST, MQTT, OPC UA)
    
*   **Fehlertoleranz:** Robuste Fehlerbehandlung, insbesondere bei Netzwerk- und Hardware-Kommunikation
    
*   **Testbarkeit:** Unit-Tests und Integrationstests (Mock für Roboter/Sensoren)
    

#### 💻 Programmierung

*   **Code Cleanliness:** 

    *   **Ordner**: snake_case

    *   **Docs-Datein**: snake_case
    
    *   **Code**: camelCase, PascalCase
    
    *   **DB**: snake_case
    
*   **Typensicherheit:** z.B. TypeScript (JS), MyPy (Python)

*   **Code Formatierung:** z.B. Prettier (JS), Black (Python), Spotless (Java)

*   **Linter verwenden:** z.B. ESLint (JS), Checkstyle (Java), PyLint (Python)
    
*   **Logging:** Structured Logging mit Levels (INFO, DEBUG, ERROR) – mit SLF4J
    
*   **Security:** Besonders bei OPC-UA und MQTT auf Authentifizierung & Verschlüsselung achten
    

#### 📦 Git-Konventionen

*   Branch-Namen: feature/, bugfix/, hotfix/, refactor/

*   Git-Commits: 

    *   **Type**: feature, fix, docs, style, refactor, test, chore
    
    *   **Scope**: Userstory ID

    *   **Subject**: Kurze Beschreibung des Commits

        *   **Struktur**: "Type(Scope): Subject"

        *   **Beispiel**: "fix(US1234): temperature sensor"
    
*   Pull-Requests mit Reviewpflicht
    
🛠️ Inbetriebnahme (lokale Einrichtung)
---------------------------------------

### 📥 Repository klonen

Um das Projekt lokal zu nutzen, muss das GitHub-Repository geklont werden:

```bash
git clone https://github.com/MEscape/die-macher.git
git config --global push.autosetupremote true

cd die-macher
```  

### 📦 Abhängigkeiten installieren

Nachdem das Repository geklont wurde, müssen die Abhängigkeiten installiert werden:

### 1. VSCode Extensions

*   **Language Support for Java(TM) by Red Hat**

*   **Markdown Preview Mermaid Support**

*   **BasedPyright**

*   **Python**

*   **Docker**

### 2. Java

*   **JDK21**

*   **Maven**

### 3. Python

*   **Python 3.11+**

### 4. Dobot Magician

*   **CP210x VCP Windows**: https://www.silabs.com/developer-tools/usb-to-uart-bridge-vcp-drivers?tab=downloads

📎 Verknüpfte Kapitel
---------------------

*   [01\_Projektübersicht.md](01\_Projektübersicht.md)
    
*   [11\_DevOps\_und\_Betrieb.md](11\_DevOps\_und\_Betrieb.md)