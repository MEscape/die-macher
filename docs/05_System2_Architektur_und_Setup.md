
# System 2: Architektur und Setup

## Einleitung

Dieses Dokument beschreibt die Architektur und Konfiguration von System 2, das als Datenbank- und Analysesystem für das Projekt "Die Macher" dient. Es basiert auf einer virtualisierten Ubuntu-Umgebung mit InfluxDB als Zeitreihendatenbank.

## Systemarchitektur

### Basisinformationen
```yaml
VM-IP-Adresse: 10.100.20.180
Betriebssystem: Ubuntu Server LTS
InfluxDB-Version: 2.7.11
Installationsdatum: 20.05.2025
```

### Netzwerkkonfiguration
```yaml
HTTP-Port: 8086
Firewall:
  - Port 8086 (TCP) für InfluxDB HTTP-API
  - UFW-Regel aktiv
Zugriff:
  - Intern: Vollzugriff
  - Extern: Nur HTTP-API
```

## Datenbankkonfiguration

### InfluxDB-Setup
```yaml
Organisation: MACHER
Bucket:
  Name: robot_sensor_data
  Retention: Unbegrenzt
Admin:
  Benutzername: admin
  Passwort: Macherpwd  # In Produktionsumgebung durch sicheres Passwort ersetzen
```

### Systemverzeichnisse
```yaml
Datenverzeichnis: /home/fbs/.influxdbv2/
Konfiguration: /etc/default/influxdb2
Token-Speicherort: /home/username/.influxdbv2/configs
```

## Betrieb und Wartung

### Start und Stop
```bash
# Systemd-Service (empfohlen)
sudo systemctl start influxdb
sudo systemctl stop influxdb
sudo systemctl restart influxdb

# Status überprüfen
sudo systemctl status influxdb
```

### Monitoring
- Regelmäßige Überprüfung der Systemauslastung
- Monitoring der Datenbankgröße
- Überwachung der API-Verfügbarkeit

### Backup-Strategie
- Tägliche automatische Backups
- Backup-Verzeichnis: `/backup/influxdb`
- Retention: 30 Tage
