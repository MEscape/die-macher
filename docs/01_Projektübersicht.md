📄 01\_Projektübersicht.md
==========================

🧠 Projektname: Die Macher
----------------------------------------------------------------------

🎯 Projektziel
--------------

Ziel des Projekts ist die Entwicklung eines cyber-physischen Systems zur Präsentation vor Herr Stolz und Herr Vey. Das System zeigt exemplarisch die Komponenten moderner Industrie 4.0-Architekturen: Sensordatenerfassung, intelligente Steuerung, Datenhaltung, Analyse und mobile Visualisierung. Besucher sollen den vollständigen Kommunikationsfluss live nachvollziehen können.

👥 Projektteam
--------------

**Rollenbeschreibung**

**Fachinformatiker AE (2x)** Zuständig für Softwareentwicklung, App-Design, Backend/API

**Fachinformatiker SI (1x)** Zuständig für Netzwerkinfrastruktur, Server-Setup, MQTT

**Organisationsstruktur:**

*   **Projektmanagement:** SCRUM (3-Wochen-Sprints, Sprint-Planning & Review)
    
*   **Werkzeuge:** GitHub (Codeverwaltung), Notion (Dokumentation, Backlog)
    

🏗️ Projektstruktur
-------------------

Das System besteht aus drei verbundenen Hauptkomponenten:

SystemRolleTechnologien

**System 1** Zentrale Steuerung, Roboteranbindung, REST APIJava (Spring Boot), Python

**System 2** Datenbank- und MQTT-ServerUbuntu VM, PostgreSQL, EMQX, Ubuntu-VM

**System 3** Mobile Visualisierung der DatenReact Native (Expo), MQTT

🔧 Eingesetzte Technologien
---------------------------

*   **Java (Spring Boot):** Backend-Services, REST API, Energiekosten-Ermittlung
    
*   **Python:** Sensorsteuerung, Bildverarbeitung (Farberkennung)
    
*   **JavaScript (React Native):** Mobile Visualisierung
    
*   **PostgreSQL:** Datenhaltung mit Sharding-Strategie
    
*   **MQTT (EMQX):** Echtzeit-Datenverteilung
    
*   **OPC UA / TCP/IP / REST:** Industrielle Schnittstellen
    

📚 Fachlicher Kontext
---------------------

**Lernfeld 7 & 8:**

*   Vernetzung cyber-physischer Systeme
    
*   Systemübergreifende Datenbereitstellung
    

🧾 User-Stories (Auszug)
------------------------

1.  **Demonstration industrieller Kommunikationsschnittstellen**
    
2.  **Präsentation Pick-and-Place-System mit Farberkennung**
    
3.  **Live-Auswertung und Visualisierung von Sensordaten**
    
4.  **Echtzeit-Anzeige von Energieverbrauch und Stromkosten**
    

🔎 Erwartete Systemfunktionen
-----------------------------

*   Pick-and-Place-Steuerung durch Dobot-Roboter
    
*   Echtzeit-Messung von Temperatur, Feuchtigkeit und Farbe
    
*   Speicherung und Abfrage von Produktionsdaten (zeitlich & farblich)
    
*   Berechnung und Anzeige von Energiekosten pro Bauteil & Tag
    
*   Mobile App zur Visualisierung und Analyse
    

📅 Zeitplan (agil)
------------------

1.  Setup, erste Schnittstellen, Grundfunktionalität

2.  Integration Sensorik, erste Auswertungen

3.  Mobile Visualisierung, Abschluss, Review

📎 Verknüpfte Kapitel
---------------------

*   [02\_Systemarchitektur.md](02\_Systemarchitektur.md)
    
*   [12\_Backlog\_und\_Tasks.md](12\_Backlog\_und\_Tasks.md)
    
*   [13\_Sprint\_Review\_Checkliste.md](13\_Sprint\_Review\_Checkliste.md)