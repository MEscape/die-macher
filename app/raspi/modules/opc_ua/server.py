import asyncio
import board
import time
from datetime import datetime
import netifaces as ni
import adafruit_dht
import RPi.GPIO as GPIO

from asyncua import Server, ua
from asyncua.ua import ObjectIds

class OpcuaServer:
    def __init__(self, cert_path, key_path, interface='wlan0'):
        self.cert_path = cert_path
        self.key_path = key_path
        self.interface = interface
        self.server = Server()
        self.dht_device = adafruit_dht.DHT22(board.D4, use_pulseio=False)
        self.temp_node = None
        self.hum_node = None
        self.time_node = None

        # GPIO setup
        sensor_pin = 4
        GPIO.setmode(GPIO.BCM)
        GPIO.setwarnings(False)
        GPIO.setup(sensor_pin, GPIO.IN)

    async def setup_server(self):
        await self.server.load_certificate(self.cert_path)
        await self.server.load_private_key(self.key_path)
        await self.server.init()

        ipv4_address = ni.ifaddresses(self.interface)[ni.AF_INET][0]['addr']
        endpoint_url = f"opc.tcp://{ipv4_address}:4840"
        self.server.set_endpoint(endpoint_url)

        self.server.set_security_policy([
            ua.SecurityPolicyType.Basic256Sha256_SignAndEncrypt,
            ua.SecurityPolicyType.Basic256Sha256_Sign,
            ua.SecurityPolicyType.NoSecurity
        ])

        namespace = await self.server.register_namespace("Die-Macher")
        dev_type = await self.server.nodes.base_object_type.add_object_type(namespace, "FBS-Platine")
        temp_var = await (await dev_type.add_variable(namespace, "temperature", 1.0)).set_modelling_rule(True)
        hum_var = await (await dev_type.add_variable(namespace, "humidity", 1.0)).set_modelling_rule(True)
        time_var = await (await dev_type.add_variable(namespace, "time", datetime.utcnow())).set_modelling_rule(True)

        folder = await self.server.nodes.objects.add_folder(namespace, "Raspi")
        device = await folder.add_object(namespace, "FBS-Platine", dev_type)

        self.temp_node = await device.get_child({f"{namespace}:temperature"})
        self.hum_node = await device.get_child({f"{namespace}:humidity"})
        self.time_node = await device.get_child({f"{namespace}:time"})

        print("Server endpoint set at:", endpoint_url)

    async def start(self):
        async with self.server:
            print("OPC UA Server running...")
            while True:
                try:
                    temperature_c = self.dht_device.temperature
                    humidity = self.dht_device.humidity
                    current_time = datetime.now()

                    print(f"Temp: {temperature_c:.1f} C    Humidity: {humidity}%  Zeit: {current_time:%d-%b-%Y (%H:%M:%S.%f)}")

                    await self.temp_node.write_value(temperature_c)
                    await self.hum_node.write_value(humidity)
                    await self.time_node.write_value(current_time)

                except RuntimeError as error:
                    print("Sensor Error:", error.args[0])
                    await asyncio.sleep(2)
                    continue
                except Exception as error:
                    self.dht_device.exit()
                    raise error

                await asyncio.sleep(2)


    async def stop(self):
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
#     server = OpcuaServer("/home/pi/opcua_certs/server-cert.pem", "/home/pi/opcua_certs/server-key.pem")
#     asyncio.run(server.setup_server())
#     asyncio.run(server.start())




