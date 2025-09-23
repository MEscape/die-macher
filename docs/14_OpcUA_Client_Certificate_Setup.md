
# OPC UA Client Zertifikats-Setup

## Überblick

Um eine sichere Verbindung zwischen `system_1` (OPC UA Client) und dem Raspberry Pi (OPC UA Server) herzustellen, benötigt jede Client-Maschine ein eigenes Zertifikat und einen eigenen privaten Schlüssel. Diese Dateien dürfen **niemals** im Git-Repository gespeichert werden.

## Schritte zur Einrichtung

### 1. Zertifikat und privaten Schlüssel auf jedem Client erzeugen

Auf jeder Maschine, auf der `system_1` läuft, führe im Terminal folgenden Befehl aus:

```bash
openssl req -x509 -newkey rsa:2048 -keyout client-key.pem -out client-cert.pem -days 365 -nodes -subj "/CN=system_1_client"
```

- Dies erzeugt `client-cert.pem` (Zertifikat) und `client-key.pem` (privater Schlüssel).
- Lege diese Dateien in ein Verzeichnis, das **nicht** von git verfolgt wird (z.B. `certs/`).

### 2. `.gitignore` anpassen

Stelle sicher, dass Zertifikats- und Schlüsseldateien ignoriert werden:

```
# Zertifikate und Schlüssel ignorieren
*.pem
certs/
client-cert.pem
client-key.pem
```

### 3. Anwendung konfigurieren

Trage die Pfade zu Zertifikat und Schlüssel in der `application.properties` ein:

```
opcua.certificate.path=certs/client-cert.pem
opcua.privatekey.path=certs/client-key.pem
```

Alternativ kannst du Umgebungsvariablen verwenden:

```
opcua.certificate.path=${CERT_PATH:./certs/client-cert.pem}
opcua.privatekey.path=${KEY_PATH:./certs/client-key.pem}
```

### 4. Server-Zertifikat vertrauen

- Falls der Server ein selbstsigniertes Zertifikat verwendet, kopiere das **öffentliche Server-Zertifikat** auf den Client und konfiguriere den Client so, dass er dieses vertraut.
- **Kopiere niemals den privaten Schlüssel des Servers auf den Client!**

### 5. Client-Zertifikat auf dem Server registrieren

- Kopiere das erzeugte `client-cert.pem` von jedem Client in das Verzeichnis für vertrauenswürdige Zertifikate auf dem Raspberry Pi Server.
- So erkennt und vertraut der Server dem Client.

### 6. Niemals Zertifikate ins Git-Repository einchecken

- Zertifikate und private Schlüssel dürfen **niemals** ins Repository gelangen.
- Nur die **Pfade** werden in Konfigurationsdateien gespeichert.

## Zusammenfassung

| Maschine      | Privater Schlüssel benötigt? | Eigenes Zertifikat benötigt? | Server-Zertifikat benötigt? (öffentlich) | Andere Client-Zertifikate benötigt? |
|---------------|-----------------------------|------------------------------|------------------------------------------|-------------------------------------|
| system_1      | Ja                          | Ja                           | Ja                                       | Nein                                |
| Raspberry Pi  | Ja                          | Ja                           | Nein                                     | Ja (öffentlich)                     |

## Fehlerbehebung

- Bei Verbindungsproblemen prüfe die Pfade und Dateiberechtigungen.
- Stelle sicher, dass der Server das Client-Zertifikat vertraut und umgekehrt.

## Weitere Informationen

- [Eclipse Milo OPC UA Dokumentation](https://github.com/eclipse/milo)
- [OpenSSL Dokumentation](https://www.openssl.org/docs/manmaster/man1/openssl-req.html)
```
Damit ist die sichere Handhabung und Einrichtung der Zertifikate für deinen OPC UA Client ausführlich dokumentiert.

        