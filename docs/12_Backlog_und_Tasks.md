📄 12\_Backlog\_und\_Tasks.md
=============================

🔗 Detaillierte Planung in Notion: [Projektplan auf Notion](https://www.notion.so/1e03d7b9e497803283c7fc80e961011a?v=1e03d7b9e4978049b0b9000cc5239179&pvs=4)

🏁 Sprint 1 – Planung
---------------------

In Sprint 1 liegt der Fokus auf der **Einrichtung der Basisinfrastruktur**, der **Erfassung und Verarbeitung von Sensordaten** (Temperatur, Feuchtigkeit, RGB) sowie der **Steuerung des Dobot-Roboters**. Zusätzlich wird die Grundlage für die Berechnung von Energiekosten über eine REST API geschaffen. Ziel ist es, einen funktionsfähigen Prototypen für die Sensorik und Robotersteuerung aufzubauen, unterstützt durch eine dokumentierte Entwicklungsumgebung. Der Sprint dauert **drei Wochen**.

📋 Sprint-Ziel (Definition of Done)
-----------------------------------

*   Entwicklungsumgebung ist eingerichtet, dokumentiert und für alle Teammitglieder nutzbar (Git-Repository und Notion konfiguriert).
*   System 1 erfasst Temperatur- und Feuchtigkeitsdaten von einem Raspberry Pi über OPC UA und verarbeitet diese korrekt.
*   System 1 erfasst RGB-Daten über eine TCP/IP-Verbindung und verarbeitet diese korrekt.
*   Dobot-Roboter wird über USB von System 1 gesteuert, Pick-and-Place-Funktionalität ist implementiert und testbar.
*   Logging und Debug-Ausgaben sind für den Sensordatenfluss integriert.
*   Optional (Medium-Priorität): Energiekosten werden über die aWATTar REST API abgerufen, berechnet und angezeigt.

📦 Product-Backlog (Sprint 1 – Auswahl)
---------------------------------------

| ID  | User Story | Beschreibung                                                  | Priorität |
|-----|------------|---------------------------------------------------------------|-----------|
| 1   | 1.1        | Entwicklungsumgebung eingerichtet und dokumentiert            | High      |
| 2   | 1.2        | System 1 erfasst Temperatur- und Fäuchtigkeitsdaten von Raspberry Pi | High     |
| 3   | 1.3        | System 1 erfasst RGB Daten                                    | High      |
| 4   | 1.4        | Steuerung des Roboters durch System 1                         | High      |
| 5   | 1.5        | Energiekosten über REST/aWATTar                               | Medium    |

✅ Sprint-Backlog (Sprint 1)
---------------------------
> **Hinweis zur Aufwandsschätzung:** Ein Storypoint entspricht 1 Stunde und 15 Minuten (1h 15min).

## User Story 1.1: Entwicklungsumgebung eingerichtet und dokumentiert

| Task ID | Aufgabe                                                         | Storypoints | Zuständig         | Status       |
|---------|-----------------------------------------------------------------|-------------|-------------------|--------------|
| T1.1    | Git-Repository einrichten und Zugriffsrechte konfigurieren      | ---         | ---               | ⬜️ Offen     |
| T1.2    | Notion einrichten und Zugriffsrechte konfigurieren              | ---         | ---               | ⬜️ Offen     |
| T1.3    | Entwicklungsumgebung aufsetzen und dokumentieren                | ---         | ---               | ⬜️ Offen     |
| T1.4    | Initiale Dokumentations-Struktur erstellen                      | ---         | ---               | ⬜️ Offen     |

## User Story 1.2: System 1 erfasst Temperatur- und Fäuchtigkeitsdaten von Raspberry Pi

| Task ID | Aufgabe                                                         | Storypoints | Zuständig         | Status       |
|---------|-----------------------------------------------------------------|-------------|-------------------|--------------|
| T1.5    | OPC UA Server für Temperatursensor und Feuchtigkeit Sensor konfigurieren | --- | ---               | ⬜️ Offen    |
| T1.6    | Die Daten auf System 1 auslesen                                 | ---         | ---               | ⬜️ Offen     |
| T1.7    | Logging & Debug-Ausgaben für Sensordatenfluss integrieren       | ---         | ---               | ⬜️ Offen     |
| T1.8    | Unittests schreiben                                             | ---         | ---               | ⬜️ Offen     |

## User Story 1.3: System 1 erfasst RGB Daten

| Task ID | Aufgabe                                                         | Storypoints | Zuständig         | Status       |
|---------|-----------------------------------------------------------------|-------------|-------------------|--------------|
| T1.9    | TCP/IP Server konfigurieren und Daten an System 1 senden        | ---         | ---               | ⬜️ Offen     |
| T1.10   | Daten auf System 1 empfangen und verarbeiten                    | ---         | ---               | ⬜️ Offen     |
| T1.11   | Logging & Debug-Ausgaben für Sensordatenfluss integrieren       | ---         | ---               | ⬜️ Offen     |
| T1.12   | Unittests schreiben                                             | ---         | ---               | ⬜️ Offen     |

## User Story 1.4: Steuerung des Roboters durch System 1

| Task ID | Aufgabe                                                         | Storypoints | Zuständig         | Status       |
|---------|-----------------------------------------------------------------|-------------|-------------------|--------------|
| T1.13   | USB-Verbindung zum Dobot initialisieren                         | ---         | ---               | ⬜️ Offen     |
| T1.14   | Steuerungslogik für Pick-and-Place implementieren               | ---         | ---               | ⬜️ Offen     |

## User Story 1.5: Energiekosten über REST/aWATTar
| Task ID | Aufgabe                                                         | Storypoints | Zuständig         | Status       |
|---------|-----------------------------------------------------------------|-------------|-------------------|--------------|
| T1.15   | Erfolgreicher Abruf der aktuellen Energiekosten über den aWATTar REST API | ---         | ---               | ⬜️ Offen     |
| T1.16   | Der API-Response enthält die aktuellen Preise in kWh            | ---         | ---               | ⬜️ Offen     |
| T1.17   | Berechnung der Gesamtkosten (Preis pro kWh * verbrauchte Energie) für eine gegebene Energiemenge (z. B. 0,2 kWh) | ---         | ---               | ⬜️ Offen     |
| T1.18   | Das System zeigt die berechneten Energiekosten korrekt an       | ---         | ---               | ⬜️ Offen     |
| T1.19   | Unittests schreiben                                             | ---         | ---               | ⬜️ Offen     |

> 🔁 **Gesamter Sprintumfang: ca. --- Stunden** – ideal für ein 3-Personen-Team in Teilzeit/Unterrichtsumfeld

📎 Verknüpfte Kapitel
---------------------

*   [13\_Sprint\_Review\_Checkliste.md](13\_Sprint\_Review\_Checkliste.md)

*   [04\_Softwarekomponenten\_System1.md](04\_Softwarekomponenten\_System1.md)

*   [05\_System2\_Architektur\_und\_Setup.md](05\_System2\_Architektur\_und\_Setup.md)


⚠️ Abhängigkeiten & Risiken
---------------------------

*   Zugriff auf Dobot-Bibliothek muss frühzeitig geprüft werden

*   TCP/IP-Kommunikation im Schulnetzwerk evtl. eingeschränkt → Test im privaten Netzwerk vorbereiten

*   OPC UA benötigt Zertifikate – rechtzeitig generieren & konfigurieren


🏁 Sprintbeginn: \[29.04.2025\]
-------------------------------

**Nächstes Sprint-Planning:** \[20.05.2025\]
