
# Die Macher Sensor Processor Setup

This guide helps you get started with the Sensor Processor system, including EMQX MQTT broker, InfluxDB, Node-RED, and your Java application.

---

## 1. Create Environment Variables

Create a `.env` file in the same directory as the `docker-compose.yml` with the following content:

```env
# MQTT settings
MQTT_USERNAME=admin
MQTT_PASSWORD=admin

# InfluxDB credentials and config
INFLUXDB_TOKEN=token
INFLUXDB_ORG=org
INFLUXDB_BUCKET=bucket-data
INFLUXDB_USERNAME=admin
INFLUXDB_PASSWORD=admin
