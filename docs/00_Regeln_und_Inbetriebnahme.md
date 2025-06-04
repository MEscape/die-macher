# Regeln und Inbetriebnahme

## Einleitung

Dieses Kapitel beschreibt die grundlegenden Projektregeln, Konventionen und erste Schritte zur Inbetriebnahme des Projekts auf einem lokalen Rechner. Es dient als verbindliche Richtlinie für das gesamte Projektteam und soll die Zusammenarbeit effizient, einheitlich und nachvollziehbar gestalten.

## Projektregeln & Konventionen

### Sprache

| Bereich | Sprache |
|---------|----------|
| Dokumentation | Deutsch |
| Code-Kommentare | Englisch |
| Git-Commits | Englisch |
| User Interfaces | Deutsch, Englisch |

### Verzeichnisstruktur

```bash
/  ├── ci/                # Continues Integration
/  ├── app/
/       ├── raspi/          # Raspberry Pi Code
/           ├── opc_ua/         # OPC UA Server
/           └── tcp_ip/         # TCP/IP Socket
/       ├── system_1/         # Spring Boot Backend
/       ├── system_2/         # MQTT & Database Server
/       ├── system_3/         # React Native Mobile App
/  ├── docs/               # Project Documentation
```

### Best Practices

#### Architektur

- **Modularität**
  - Trennung von Zuständigkeiten pro Subsystem
  - Klare Schnittstellendefinitionen

- **Schnittstellen**
  - Kommunikation ausschließlich über definierte APIs
  - Unterstützte Protokolle: REST, MQTT, OPC UA

- **Fehlertoleranz**
  - Robuste Fehlerbehandlung
  - Besonderer Fokus auf Netzwerk- und Hardware-Kommunikation

- **Testbarkeit**
  - Implementierung von Unit-Tests
  - Integrationstests mit Mock-Objekten für Hardware

#### Programmierung

##### Namenskonventionen

| Bereich | Konvention | Beispiel |
|---------|------------|----------|
| Ordner | snake_case | `data_processing/` |
| Docs-Dateien | snake_case | `system_architecture.md` |
| Code | camelCase, PascalCase | `processData()`, `DataProcessor` |
| Datenbank | snake_case | `sensor_data` |

##### Code-Qualität

- **Typisierung**
  - TypeScript für JavaScript
  - MyPy für Python
  - Strikte Typenprüfung

- **Formatierung**
  - JavaScript: Prettier
  - Python: Black
  - Java: Spotless

- **Linting**
  - JavaScript: ESLint
  - Java: Checkstyle
  - Python: PyLint

- **Logging**
  - Structured Logging mit SLF4J
  - Log-Level: INFO, DEBUG, ERROR
  - Kontextbezogene Logging-Informationen

- **Security**
  - Verschlüsselte Kommunikation
  - Authentifizierung für OPC-UA und MQTT
  - Sichere Speicherung von Credentials

#### Git-Workflow

##### Branch-Namenskonventionen

- `feature/` - Neue Funktionalitäten
- `bugfix/` - Fehlerbehebungen
- `hotfix/` - Kritische Fixes
- `refactor/` - Code-Verbesserungen

##### Commit-Konventionen

**Format:** `type(scope): subject`

- **Type:**
  - `feature`: Neue Funktionalität
  - `fix`: Fehlerbehebung
  - `docs`: Dokumentation
  - `style`: Formatierung
  - `refactor`: Code-Verbesserung
  - `test`: Tests
  - `chore`: Maintenance

- **Scope:** Userstory-ID (z.B. US1234)
- **Subject:** Kurze Beschreibung

**Beispiel:** `fix(US1234): temperature sensor calibration`

##### Pull Requests
- Verpflichtende Code-Reviews
- Automatisierte Tests müssen bestanden sein
- Dokumentation muss aktuell sein

## Inbetriebnahme

### Repository Setup

```bash
# Repository klonen
git clone https://github.com/MEscape/die-macher.git

# Push-Konfiguration
git config --global push.autosetupremote true

# In Projektverzeichnis wechseln
cd die-macher
```

### Entwicklungsumgebung

#### 1. Pre-Commit Setup
```bash
# Installation
pip install pre-commit commitizen

# Hooks aktivieren
pre-commit install --hook-type pre-commit --hook-type commit-msg
```

#### 2. VSCode Extensions
- Language Support for Java(TM) by Red Hat
- Markdown Preview Mermaid Support
- BasedPyright
- Python
- Docker

#### 3. Runtime-Umgebungen

##### Java
- JDK21
- Maven

##### Python
- Python 3.11 oder höher
- Virtuelle Umgebung empfohlen

#### 4. Hardware-Treiber

##### Dobot Magician
- [CP210x VCP Windows Treiber](https://www.silabs.com/developer-tools/usb-to-uart-bridge-vcp-drivers?tab=downloads)

## FAQ

**F: Wie wird die Code-Qualität sichergestellt?**
A: Durch automatisierte Tests, Linting, Code-Reviews und CI/CD-Pipeline.

**F: Welche IDE wird empfohlen?**
A: VSCode mit den angegebenen Extensions für optimale Entwicklungsunterstützung.

## Weiterführende Dokumentation

- [Projektübersicht](01_Projektübersicht.md)
- [DevOps und Betrieb](11_DevOps_und_Betrieb.md)

## Änderungshistorie

| Datum | Version | Änderungen | Autor |
|-------|----------|------------|--------|
| 2024-04 | 1.0 | Initiale Version | Team |
| 2024-05 | 1.1 | Ergänzung Git-Workflow | Team |
