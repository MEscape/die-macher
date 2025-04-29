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
/       ├── system_1/
/  ├── docs/
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
    
    *   **Code**: camelCase, PascalCase
    
    *   **DB**: snake_case
    
*   **Linter verwenden:** z.B. ESLint (JS), Checkstyle (Java), PyLint (Python)
    
*   **Logging:** Structured Logging mit Levels (INFO, DEBUG, ERROR) – mit SLF4J
    
*   **Security:** Besonders bei OPC-UA und MQTT auf Authentifizierung & Verschlüsselung achten
    

#### 📦 Git-Konventionen

*   Branch-Namen: feature/, bugfix/, hotfix/

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

> **Hinweis:** Der Zugriff wird über eure GitHub-Accounts geregelt. Stellt sicher, dass ihr eingeladen seid und Schreibrechte besitzt.

```bash
git clone https://github.com/MEscape/die-macher.git

cd die-macher
```  

### 📦 Abhängigkeiten installieren

Nachdem das Repository geklont wurde, müssen die Abhängigkeiten installiert werden:

### 1. VSCode Extensions

*   **Language Support for Java(TM) by Red Hat**

*   **Markdown Preview Mermaid Support**

📎 Verknüpfte Kapitel
---------------------

*   [01\_Projektübersicht.md](01\_Projektübersicht.md)
    
*   [11\_DevOps\_und\_Betrieb.md](11\_DevOps\_und\_Betrieb.md)