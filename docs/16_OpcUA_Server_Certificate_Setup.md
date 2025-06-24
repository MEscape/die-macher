## Anleitung: Server-Zertifikat erzeugen und an Client übertragen


### 1. Zertifikat und privaten Schlüssel auf dem Server erzeugen
Führe auf dem Server (z.B. Raspberry Pi) folgenden Befehl im Terminal aus:

```
openssl req -x509 -newkey rsa:2048 -nodes

-keyout server-key.pem -out server-cert.

pem -days 365 -subj "/C=DE/ST=HE/L=Fulda/

O=DieMacher/CN=raspberrypi"
```

- server-key.pem : Privater Schlüssel (bleibt immer auf dem Server!)
- server-cert.pem : Öffentliches Zertifikat (darf an Clients verteilt werden)

### 2. Zertifikat und Schlüssel im Server-Code verwenden

Im Python-Servercode müssen die Zertifikat- und Schlüsseldateien wie folgt eingebunden werden:

```
SERVER_CERT_PATH = "/home/pi/opcua_certs/server-cert.pem"
SERVER_KEY_PATH = "/home/pi/opcua_certs/server-key.pem"

async def main():
    server = Server()
    await server.load_certificate(SERVER_CERT_PATH)
    await server.load_private_key(SERVER_KEY_PATH)
    # ... existing code ...
```
Stelle sicher, dass die Pfade zu den Zertifikatsdateien korrekt sind und die Dateien vorhanden sind.