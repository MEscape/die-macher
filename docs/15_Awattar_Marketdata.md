# Awattar Marktdaten Integration

## Einleitung

Diese Dokumentation beschreibt die Integration der Awattar Market Data API für die Strompreisoptimierung im Projekt "Die Macher". Die API liefert stündliche Strompreisdaten für die nächsten 24 Stunden und ermöglicht eine energiekostenoptimierte Produktionsplanung.

## Externe API-Spezifikation

### Basis-Information
- **Anbieter**: [aWATTar Österreich](https://www.awattar.at/)
- **API-Version**: v1
- **Datenformat**: JSON
- **Authentifizierung**: Nicht erforderlich

### Endpunkt
```http
GET https://api.awattar.at/v1/marketdata
```

### Parameter
| Parameter | Typ | Beschreibung | Format |
|-----------|-----|--------------|--------|
| start | number | Startzeitpunkt | Unix-Timestamp (ms) |

### Beispielanfrage

```bash
# Abfrage der Preisdaten ab einem bestimmten Zeitpunkt
curl "https://api.awattar.at/v1/marketdata?start=1561932000000"

# Abfrage der aktuellen Preisdaten
curl "https://api.awattar.at/v1/marketdata?start=$(date +%s)000"
```

### Antwortformat

```json
{
  "object": "list",
  "data": [
    {
      "start_timestamp": 1561932000000,
      "end_timestamp": 1561935600000,
      "marketprice": 28.98,
      "unit": "Eur/MWh"
    },
    {
      "start_timestamp": 1561935600000,
      "end_timestamp": 1561939200000,
      "marketprice": 26.23,
      "unit": "Eur/MWh"
    }
  ]
}
```

### Datenstruktur

| Feld | Typ | Beschreibung | Format |
|------|-----|--------------|--------|
| start_timestamp | number | Startzeitpunkt | Unix-Timestamp (ms) |
| end_timestamp | number | Endzeitpunkt | Unix-Timestamp (ms) |
| marketprice | number | Strompreis | EUR/MWh |
| unit | string | Preiseinheit | Immer "Eur/MWh" |

### Eigenschaften

#### Zeitliche Auflösung
- Stündliche Preisdaten
- 24-Stunden-Vorhersage
- UTC-Zeitstempel

#### Preisberechnung
- Basis: EUR/MWh
- Umrechnung in EUR/kWh: Wert × 0,001
- Beispiel: 28,98 EUR/MWh = 0,02898 EUR/kWh

#### Datenanalyse
- Trendanalyse über 24 Stunden
- Identifikation von Niedrigpreisphasen
- Berechnung von Durchschnittspreisen“
- **unit** : Einheit des Marktpreises (immer „Eur/MWh“)

## Produktionsoptimierung

### Energieverbrauchsmodell

```yaml
Bauteil:
  Energiebedarf: 0,2 kWh/Stück
  Produktionsrate: 5 Stück/Stunde

Produktionsauftrag:
  Menge: 15 Stück
  Dauer: 3 Stunden
  Gesamtenergie: 3 kWh
```

### Optimierungsziele
- Minimierung der Energiekosten
- Nutzung von Niedrigpreisphasen
- Kontinuierliche Produktion (3 zusammenhängende Stunden)

## Interne REST-API

### Basis-URL
```http
BASE_URL: /api/awattar
```

### 1. Morgige Strompreise
```http
GET /api/awattar/tomorrow-prices
```

#### Antwort (200 OK)
```json
{
  "object": "list",
  "data": [{
    "startTimestamp": 1747778400000,
    "endTimestamp": 1747782000000,
    "marketprice": 107.93,
    "unit": "Eur/MWh",
    "priceInEurPerKwh": 0.10793,
    "startTimeFormatted": "21.05.2025 00:00",
    "endTimeFormatted": "21.05.2025 01:00"
  }]
}
```

### 2. Aktueller Strompreis
```http
GET /api/awattar/current-price
```

#### Antwort (200 OK)
```json
{
  "startTimestamp": 1747749600000,
  "endTimestamp": 1747753200000,
  "marketprice": 70.05,
  "unit": "Eur/MWh",
  "priceInEurPerKwh": 0.07005,
  "startTimeFormatted": "20.05.2025 16:00",
  "endTimeFormatted": "20.05.2025 17:00"
}
```

### 3. Optimales Produktionsfenster
```http
GET /api/awattar/optimal-window
```

#### Antwort (200 OK)
```json
{
  "startTimestamp": 1747821600000,
  "endTimestamp": 1747832400000,
  "prices": [
    {
      "startTimestamp": 1747821600000,
      "endTimestamp": 1747825200000,
      "marketprice": -0.1,
      "unit": "Eur/MWh",
      "priceInEurPerKwh": -0.0001,
      "startTimeFormatted": "21.05.2025 12:00",
      "endTimeFormatted": "21.05.2025 13:00"
    }
  ],
  "totalCost": -0.00383,
  "startTimeFormatted": "21.05.2025 12:00",
  "endTimeFormatted": "21.05.2025 15:00"
}
```

## Datenmodelle

### MarketData
| Feld | Typ | Beschreibung |
|------|-----|-------------|
| object | string | Immer "list" |
| data | array | Liste von MarketPrice-Objekten |

### MarketPrice
| Feld | Typ | Beschreibung |
|------|-----|-------------|
| startTimestamp | number | Startzeitpunkt (ms) |
| endTimestamp | number | Endzeitpunkt (ms) |
| marketprice | number | Preis in EUR/MWh |
| unit | string | Preiseinheit |
| priceInEurPerKwh | number | Umgerechneter Preis |
| startTimeFormatted | string | Lesbare Startzeit |
| endTimeFormatted | string | Lesbare Endzeit |“** durch Multiplikation des Wertes mit 0,1.

#### Anwendungsfälle:

- Ermittlung der günstigsten Stunde für Stromverbrauch am Folgetag.
- Visualisierung von Preistrends über den Tag.


## 📋 Was brauchen wir?

- **Energiekostenberechnung** basierend auf aWATTar-Strompreisdaten
- **Analyse der optimalen Produktionszeit für morgen**

## ⚙️ Produktionsmodell
### Annahmen:
- Ein Bauteil benötigt 0,2 kWh Energie
- 5 Bauteile pro Stunde
- Morgen sollen 15 Bauteile produziert werden (= 3 Stunden Produktionszeit)

**Gesucht**: Das günstigste zusammenhängende Zeitfenster von 3 Stunden


# aWATTar REST API Dokumentation

## Basis-URL

`/api/awattar`

---

## Endpunkte

### 1. Strompreise für morgen abrufen

`**GET** /api/awattar/tomorrow-prices `

- **Beschreibung:**Gibt die Strompreisdaten für den gesamten morgigen Tag zurück.
- **Antwort:**
    - **200 OK:** Ein MarketData-Objekt mit einer Liste von Zeitfenstern und Preisen.
    - **404 Not Found:**Keine Daten verfügbar.

**Beispielantwort:**

{
  "object": "list",
  "data": [
    {
      "startTimestamp": 1747778400000,
      "endTimestamp": 1747782000000,
      "marketprice": 107.93,
      "unit": "Eur/MWh",
      "priceInEurPerKwh": 0.10793,
      "startTimeFormatted": "21.05.2025 00:00",
      "endTimeFormatted": "21.05.2025 01:00"
    }
  ]
}

---

### 2. Aktuellen Strompreis abrufen

`**GET** /api/awattar/current-price`

- **Beschreibung:** Gibt den aktuellen Strompreis (erstes Zeitfenster ab jetzt) zurück.
- **Antwort:**
    - **200 OK:** Ein MarketPrice-Objekt mit Zeitfenster und Preis.
    - **404 Not Found:** Keine Daten verfügbar.

**Beispielantwort:**

{
  "startTimestamp": 1747749600000,
  "endTimestamp": 1747753200000,
  "marketprice": 70.05,
  "unit": "Eur/MWh",
  "priceInEurPerKwh": 0.07005,
  "startTimeFormatted": "20.05.2025 16:00",
  "endTimeFormatted": "20.05.2025 17:00"
}

---

### 3. Optimales Produktionsfenster abrufen

`**GET** /api/awattar/optimal-window`

- **Beschreibung:** Gibt das optimale Produktionsfenster (z.B. 3 Stunden mit den niedrigsten Stromkosten) für die Produktion zurück.
- **Antwort:**
    - **200 OK:** Ein OptimalProductionWindow-Objekt mit Start-/Endzeit, Preisen und Gesamtkosten.
    - **404 Not Found:** Keine Daten verfügbar.

**Beispielantwort:**

{
  "startTimestamp": 1747821600000,
  "endTimestamp": 1747832400000,
  "prices": [
    {
      "startTimestamp": 1747821600000,
      "endTimestamp": 1747825200000,
      "marketprice": -0.1,
      "unit": "Eur/MWh",
      "priceInEurPerKwh": -0.0001,
      "startTimeFormatted": "21.05.2025 12:00",
      "endTimeFormatted": "21.05.2025 13:00"
    },
    {
      "startTimestamp": 1747825200000,
      "endTimestamp": 1747828800000,
      "marketprice": -2.2,
      "unit": "Eur/MWh",
      "priceInEurPerKwh": -0.0022,
      "startTimeFormatted": "21.05.2025 13:00",
      "endTimeFormatted": "21.05.2025 14:00"
    },
    {
      "startTimestamp": 1747828800000,
      "endTimestamp": 1747832400000,
      "marketprice": -1.53,
      "unit": "Eur/MWh",
      "priceInEurPerKwh": -0.00153,
      "startTimeFormatted": "21.05.2025 14:00",
      "endTimeFormatted": "21.05.2025 15:00"
    }
  ],
  "totalCost": -0.00383,
  "priceInEurPerKwh": 0,
  "startTimeFormatted": "21.05.2025 12:00",
  "endTimeFormatted": "21.05.2025 15:00"
}

---

## Datenmodelle

### MarketData

- object: String (z.B. "list")
- data: Liste von MarketPrice

### MarketPrice
- startTimestamp : Startzeitpunkt (Millisekunden seit Unix-Epoch)
- endTimestamp : Endzeitpunkt (Millisekunden seit Unix-Epoch)
- marketprice : Preis (z.B. in EUR/MWh)
- unit : Einheit ("EUR/MWh")
- priceInEurPerKwh : Preis in EUR/kWh (umgerechnet, z.B. 0.10793)
- startTimeFormatted : Startzeitpunkt als lesbare Zeichenkette (z.B. "21.05.2025 00:00")
- endTimeFormatted : Endzeitpunkt als lesbare Zeichenkette (z.B. "21.05.2025 01:00")
### OptimalProductionWindow
- startTimestamp : Startzeitpunkt des optimalen Fensters (Millisekunden seit Unix-Epoch)
- endTimestamp : Endzeitpunkt des optimalen Fensters (Millisekunden seit Unix-Epoch)
- prices : Liste der zugehörigen MarketPrice-Objekte
- totalCost : Gesamtkosten für die Produktion im optimalen Fenster (EUR)
- priceInEurPerKwh : Durchschnittlicher Preis im Fenster (EUR/kWh)
- startTimeFormatted : Startzeitpunkt als lesbare Zeichenkette
- endTimeFormatted : Endzeitpunkt als lesbare Zeichenkette

---

**Hinweise:**

- Alle Zeitangaben sind in Millisekunden seit dem 01.01.1970 (Unix-Epoch).
- Die Preise werden standardmäßig in EUR/MWh geliefert, das Feld priceInEurPerKwh ist bereits umgerechnet (1 EUR/MWh = 0,001 EUR/kWh).
- Die Felder startTimeFormatted und endTimeFormatted dienen der besseren Lesbarkeit und sind im ISO-Format oder als deutsches Datumsformat ausgegeben.
- Die Feldnamen sind in CamelCase vereinheitlicht.
Wenn du möchtest, kann ich dir auch direkt einen Patch-Vorschlag für die Datei machen.
