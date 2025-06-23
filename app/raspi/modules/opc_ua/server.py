"""OPC UA Server module for Raspberry Pi sensor data."""

import asyncio
from datetime import datetime, timezone
from typing import Optional

import adafruit_dht
import board
import netifaces as ni
import RPi.GPIO as GPIO
from asyncua import Server, Node
from asyncua.ua import SecurityPolicyType


class OpcuaServer:
    """OPC UA Server for publishing Raspberry Pi sensor data."""

    def __init__(self, cert_path: str, key_path: str, interface: str = 'wlan0') -> None:
        """Initialize the OPC UA server.

        Args:
            cert_path: Path to the server certificate file
            key_path: Path to the server private key file
            interface: Network interface to use (default: 'wlan0')
        """
        self.cert_path = cert_path
        self.key_path = key_path
        self.interface = interface
        self.server = Server()
        self.dht_device = adafruit_dht.DHT22(board.D4, use_pulseio=False)
        self.temp_node: Optional[Node] = None
        self.hum_node: Optional[Node] = None
        self.time_node: Optional[Node] = None

        # GPIO setup
        sensor_pin = 4
        GPIO.setmode(GPIO.BCM)
        GPIO.setwarnings(False)
        GPIO.setup(sensor_pin, GPIO.IN)

    async def setup_server(self) -> None:
        """Set up the OPC UA server configuration and endpoints."""
        await self.server.load_certificate(self.cert_path)
        await self.server.load_private_key(self.key_path)
        await self.server.init()

        ipv4_address = ni.ifaddresses(self.interface)[ni.AF_INET][0]['addr']
        endpoint_url = f"opc.tcp://{ipv4_address}:4840"
        self.server.set_endpoint(endpoint_url)

        self.server.set_security_policy([
            SecurityPolicyType.Basic256Sha256_SignAndEncrypt,
            SecurityPolicyType.Basic256Sha256_Sign,
            SecurityPolicyType.NoSecurity
        ])

        namespace = await self.server.register_namespace("Die-Macher")
        dev_type = await self.server.nodes.base_object_type.add_object_type(
            namespace, "FBS-Platine"
        )

        # Create variable templates (these are used as templates, not actual variables)
        await (await dev_type.add_variable(
            namespace, "temperature", 1.0
        )).set_modelling_rule(True)
        await (await dev_type.add_variable(
            namespace, "humidity", 1.0
        )).set_modelling_rule(True)
        await (await dev_type.add_variable(
            namespace, "time", datetime.now(timezone.utc)
        )).set_modelling_rule(True)

        folder = await self.server.nodes.objects.add_folder(namespace, "Raspi")
        device = await folder.add_object(namespace, "FBS-Platine", dev_type)

        # Get the actual variable nodes that we'll write to
        self.temp_node = await device.get_child(f"{namespace}:temperature")
        self.hum_node = await device.get_child(f"{namespace}:humidity")
        self.time_node = await device.get_child(f"{namespace}:time")

        print(f"Server endpoint set at: {endpoint_url}")

    async def start(self) -> None:
        """Start the OPC UA server and begin sensor data collection loop."""
        async with self.server:
            print("OPC UA Server running...")
            while True:
                try:
                    temperature_c = self.dht_device.temperature
                    humidity = self.dht_device.humidity
                    current_time = datetime.now()

                    print(f"Temp: {temperature_c:.1f} C    "
                          f"Humidity: {humidity}%  "
                          f"Zeit: {current_time:%d-%b-%Y (%H:%M:%S.%f)}")

                    # Check if nodes are available before writing
                    if self.temp_node is not None:
                        await self.temp_node.write_value(temperature_c)
                    if self.hum_node is not None:
                        await self.hum_node.write_value(humidity)
                    if self.time_node is not None:
                        await self.time_node.write_value(current_time)

                except RuntimeError as error:
                    print("Sensor Error:", error.args[0])
                    await asyncio.sleep(2)
                    continue
                except Exception as error:
                    self.dht_device.exit()
                    raise error

                await asyncio.sleep(2)

    async def stop(self) -> None:
        """Stop the OPC UA server and clean up resources."""
        print("Stopping OPC UA Server...")
        try:
            await self.server.stop()
            print("OPC UA Server stopped.")
        except Exception as e:
            print(f"Error while stopping server: {e}")
        finally:
            self.dht_device.exit()
            GPIO.cleanup()
            print("GPIO cleaned up and DHT sensor released.")


# Usage:
# if __name__ == "__main__":
#     server = OpcuaServer(
#         "/home/pi/opcua_certs/server-cert.pem",
#         "/home/pi/opcua_certs/server-key.pem"
#     )
#     asyncio.run(server.setup_server())
#     asyncio.run(server.start())