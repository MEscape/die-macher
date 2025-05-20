# Awattar Marktdaten API – Stündliche Preisliste

Die Awattar Market Data API liefert Strompreisdaten von jetzt bis zu 24 Stunden in die Zukunft.

**API-Endpunkt:**  

Wir nutzen die öffentliche API von [aWATTar Österreich](https://www.awattar.at/) für stündliche Strompreisdaten.

`https://api.awattar.at/v1/marketdata?start=<timestamp>`

- `start` ist ein Zeitstempel in Millisekunden seit dem 01.01.1970 (UTC).
- Rückgabe: Strompreise stundenweise für die nächsten 24 Stunden.

**Beispielaufruf:**
```bash
curl "https://api.awattar.at/v1/marketdata?start=1561932000000"
```

## Antwortstruktur

**Beispiel:**
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
```

 Die Antwort besteht aus einer Liste von Objekten mit folgenden Feldern:

- **start_timestamp** : Startzeitpunkt der Stunde (Millisekunden seit dem 01.01.1970, UTC)
- **end_timestamp** : Endzeitpunkt der Stunde (Millisekunden seit dem 01.01.1970, UTC)
- **marketprice** : Strommarktpreis für die jeweilige Stunde, angegeben in „Eur/MWh“
- **unit** : Einheit des Marktpreises (immer „Eur/MWh“)
 
#### Eigenschaften:

- **Stündliche Auflösung der Preise** ermöglicht detaillierte Analyse von Preistrends im Tagesverlauf.
- **Zeitbasierte Filterung** durch Vergleich der Zeitstempel, z.B. für heutige oder morgige Preise.
- **Automatisierte Auswertung möglich**, z.B. zur Bestimmung des niedrigsten, höchsten oder durchschnittlichen Preises für einen bestimmten Zeitraum.
- **Umrechnung in „Cent/kWh“** durch Multiplikation des Wertes mit 0,1.

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
      "start_timestamp": 1747778400000,
      "end_timestamp": 1747782000000,
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
  "start_timestamp": 1747749600000,
  "end_timestamp": 1747753200000,
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
      "start_timestamp": 1747821600000,
      "end_timestamp": 1747825200000,
      "marketprice": -0.1,
      "unit": "Eur/MWh",
      "priceInEurPerKwh": -0.0001,
      "startTimeFormatted": "21.05.2025 12:00",
      "endTimeFormatted": "21.05.2025 13:00"
    },
    {
      "start_timestamp": 1747825200000,
      "end_timestamp": 1747828800000,
      "marketprice": -2.2,
      "unit": "Eur/MWh",
      "priceInEurPerKwh": -0.0022,
      "startTimeFormatted": "21.05.2025 13:00",
      "endTimeFormatted": "21.05.2025 14:00"
    },
    {
      "start_timestamp": 1747828800000,
      "end_timestamp": 1747832400000,
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

- **start_timestamp**: Startzeitpunkt (Millisekunden seit Unix-Epoch)
- **end_timestamp**: Endzeitpunkt (Millisekunden seit Unix-Epoch)
- **marketprice**: Preis (z.B. in EUR/MWh)
- **unit**: Einheit (z.B. "EUR/MWh")

### OptimalProductionWindow

- **startTimestamp**: Startzeitpunkt des optimalen Fensters
- **endTimestamp**: Endzeitpunkt des optimalen Fensters
- **prices**: Liste der zugehörigen MarketPrice-Objekte
- **totalCost**: Gesamtkosten für die Produktion im optimalen Fenster (EUR)

---

**Hinweis:**

Alle Zeitangaben sind in Millisekunden seit dem 01.01.1970 (Unix-Epoch). 
Die Preise werden standardmäßig in EUR/MWh geliefert, können aber im Code auch in EUR/kWh umgerechnet werden.