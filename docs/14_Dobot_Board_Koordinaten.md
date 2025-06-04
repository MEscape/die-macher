# Dobot Koordinaten und Positionen

## Einleitung

Dieses Dokument definiert die kalibrierten Koordinaten für den Dobot Magician im Würfelsortierungsprozess. Alle Positionen sind in Millimetern angegeben und wurden für optimale Präzision und Sicherheit kalibriert.

### Koordinatensystem
- X: Position entlang der Längsachse (links/rechts)
- Y: Position entlang der Querachse (vorne/hinten)
- Z: Höhenposition (oben/unten)
- R: Rotation des Endeffektors in Grad

## Prozess-Positionen

### Würfel-Startposition
```yaml
Board-Position: 16-17, S
Koordinaten:
  X: 283.9522  # mm
  Y: 10.8680   # mm
  Z: -40.3899  # mm
  R: 55.7961   # Grad
```

### Kamera-Position
```yaml
Board-Position: >E 30-31
Koordinaten:
  X: 19.3342   # mm
  Y: 291.8189  # mm
  Z: -3.7983   # mm
  R: 52.2961   # Grad
```

## Ablage-Positionen

### Gelber Würfel
```yaml
Board-Position: J, K, 2, 3
Koordinaten:
  X: 139.1296  # mm
  Y: -267.4972 # mm
  Z: -39.6540  # mm
  R: -4.2960   # Grad
```

### Grüner Würfel
```yaml
Board-Position: G, H, 1, 2
Koordinaten:
  X: 80.9609   # mm
  Y: -290.1449 # mm
  Z: -44.0922  # mm
  R: -4.2960   # Grad
```

### Blauer Würfel
```yaml
Board-Position: D, E, 1, 2
Koordinaten:
  X: 19.8350   # mm
  Y: -294.8379 # mm
  Z: -39.8903  # mm
  R: -4.2960   # Grad
```

### Roter Würfel
```yaml
Board-Position: A, B, 1, 2
Koordinaten:
  X: -42.1087  # mm
  Y: -297.6579 # mm
  Z: -39.8903  # mm
  R: -4.2960   # Grad
```

## Hinweise
- Alle Koordinaten wurden für optimale Greif- und Ablagepositionen kalibriert
- Die Z-Werte berücksichtigen die Würfelhöhe und Greifer-Geometrie
- R-Werte sind für kollisionsfreie Bewegungen optimiert
