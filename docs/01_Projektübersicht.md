# Die Macher - Projektübersicht

## Einleitung

Dieses Dokument bietet einen umfassenden Überblick über das Projekt "Die Macher", ein cyber-physisches System zur Demonstration moderner Industrie 4.0-Technologien. Es dient als Einstiegspunkt für alle Projektbeteiligten und interessierte Stakeholder.

## Projektziel

Unser Ziel ist die Entwicklung eines cyber-physischen Systems, das die wesentlichen Komponenten moderner Industrie 4.0-Architekturen demonstriert:

- Sensordatenerfassung und -verarbeitung
- Intelligente Robotersteuerung
- Zentrale Datenhaltung und -analyse
- Echtzeit-Visualisierung auf mobilen Endgeräten

Das System wird vor den Prüfern (Herr Stolz und Herr Vey) präsentiert und ermöglicht Besuchern, den vollständigen Kommunikationsfluss live nachzuvollziehen.

## Projektteam und Organisation

### Rollen und Verantwortlichkeiten

| Rolle | Anzahl | Hauptverantwortlichkeiten |
|-------|--------|---------------------------|
| Fachinformatiker AE | 2 | - Softwareentwicklung<br>- App-Design<br>- Backend/API-Entwicklung |
| Fachinformatiker SI | 1 | - Netzwerkinfrastruktur<br>- Server-Setup<br>- MQTT-Konfiguration |

### Organisationsstruktur

- **Projektmanagement:** SCRUM mit 3-Wochen-Sprints
- **Werkzeuge:**
  - GitHub: Codeverwaltung und Versionierung
  - Notion: Dokumentation und Backlog-Verwaltung

## Systemarchitektur

### Hauptkomponenten

1. **Raspberry Pi**
   - Sensordatenerfassung
   - Bildverarbeitung (Python)

2. **Dobot Magician**
   - Robotersteuerung
   - Pick-and-Place-Operationen

3. **System 1**
   - Zentrale Steuerung
   - Datenverarbeitung
   - Roboteranbindung (Java Spring Boot)

4. **System 2**
   - Datenbank-Server (PostgreSQL)
   - MQTT-Broker (EMQX)
   - Ubuntu-VM als Hostingsystem

5. **System 3**
   - Visualisierung
   - NextJS mit Shadcn UI
   - MQTT-Client-Integration

## Technologie-Stack

### Backend und Verarbeitung
- Java Spring Boot: Backend-Services, Energiekosten-Berechnung
- Python: Sensorsteuerung, Bildverarbeitung
- InfluxDB: Zeitreihendatenbank mit Sharding

### Kommunikation
- MQTT (EMQX): Echtzeit-Datenverteilung
- OPC UA: Industrielle Kommunikation
- REST: API-Schnittstellen
- TCP/IP: Netzwerkkommunikation

### Frontend
- NextJS: Frontend-Framework
- Shadcn UI: UI-Komponentenbibliothek

## Fachlicher Kontext

### Lernfelder
- **LF 7:** Vernetzung cyber-physischer Systeme
- **LF 8:** Systemübergreifende Datenbereitstellung

## Kernfunktionen

### User Stories
1. Demonstration industrieller Kommunikationsschnittstellen
2. Pick-and-Place-System mit Farberkennung
3. Live-Auswertung von Sensordaten
4. Echtzeit-Energiekostenberechnung

### Systemfunktionen im Detail
- Robotergesteuerte Pick-and-Place-Operationen
- Echtzeit-Sensorik (Temperatur, Feuchtigkeit, Farbe)
- Datenbankbasierte Produktionsdatenerfassung
- Energiekostenberechnung pro Bauteil und Tag
- Mobile Visualisierung und Analyse

## Projektplanung

### Agiler Zeitplan
1. **Sprint 1:** Setup und Grundfunktionalität
   - Systemaufbau
   - Erste Schnittstellen

2. **Sprint 2:** Integration und Auswertung
   - Sensorikintegration
   - Erste Datenauswertungen

3. **Sprint 3:** Finalisierung
   - Mobile Visualisierung
   - Systemintegration
   - Abschlussreview

## Weiterführende Dokumentation

- [Systemarchitektur](02_Systemarchitektur.md)
- [Backlog und Tasks](12_Backlog_und_Tasks.md)
- [Sprint Review Checkliste](13_Sprint_Review_Checkliste.md)

## FAQ

**F: Wie wird die Datensicherheit gewährleistet?**
A: Durch verschlüsselte Kommunikation und Authentifizierung bei MQTT und OPC UA.

**F: Welche Skalierungsmöglichkeiten bietet das System?**
A: InfluxDB unterstützt Sharding, die Microservice-Architektur ermöglicht horizontale Skalierung.

## Änderungshistorie

| Datum | Version | Änderungen | Autor |
|-------|----------|------------|--------|
| 2025-06 | 1.0 | Initiale Version | Team |
