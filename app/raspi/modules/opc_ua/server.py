import asyncio
import board
import time
from datetime import datetime
from cryptography import x509
from cryptography.hazmat.primitives import serialization


import netifaces as ni #used for getting the ip address

# Sensor-Library vom Hersteller Adafruit
import adafruit_dht

#GPIOs konfigurieren
import RPi.GPIO as GPIO

from asyncua import Server, ua
from asyncua.ua import ObjectIds

# Definition der GPIOs
sensorPIN = 4

# Zählweise der Pins festlegen
GPIO.setmode(GPIO.BCM)
GPIO.setwarnings(False)
#GPIO Eingänge festlegen
GPIO.setup(sensorPIN, GPIO.IN)


# Device für Sensor
# Initial the dht device, with data pin connected to:
dhtDevice = adafruit_dht.DHT22(board.D4, use_pulseio=False)

SERVER_CERT_PATH = "/home/pi/opcua_certs/server-cert.pem"
SERVER_KEY_PATH = "/home/pi/opcua_certs/server-key.pem"

async def main():
    server=Server()

    await server.load_certificate(SERVER_CERT_PATH)
    await server.load_private_key(SERVER_KEY_PATH)
    

    await server.init()
    #Get the ip address
    IPV4_Address = ni.ifaddresses('wlan0')[ni.AF_INET][0]['addr']
    url="opc.tcp://"+IPV4_Address+":4840"
    server.set_endpoint(url)
    
    # Securityeinstellung für Clients angeben
    server.set_security_policy([
        ua.SecurityPolicyType.Basic256Sha256_SignAndEncrypt,
        ua.SecurityPolicyType.Basic256Sha256_Sign,
        ua.SecurityPolicyType.NoSecurity 
    ])

    # set up our own namespace, not really necessary but should as spec
    name="Die-Macher"
    addspace=await server.register_namespace(name)

    # populating our address space
    # server.nodes, contains links to very common nodes like objects and root
    dev = await server.nodes.base_object_type.add_object_type(addspace, "FBS-Platine")
    mytemp = await (await dev.add_variable(addspace, "temperature", 1.0)).set_modelling_rule(True)
    myhum = await (await dev.add_variable(addspace, "humidity", 1.0)).set_modelling_rule(True)
    mytime = await (await dev.add_variable(addspace, "time", datetime.utcnow())).set_modelling_rule(True)
    
    #Folder to organize nodes 
    myfolder = await server.nodes.objects.add_folder(addspace, "Raspi")
    #instanciate one instance of our device
    mydevice = await myfolder.add_object(addspace, "FBS-Platine", dev)
    
    
    #get proxy to child-elements
    temp = await mydevice.get_child({f"{addspace}:temperature"})
    hum = await mydevice.get_child({f"{addspace}:humidity"})
    time_node = await mydevice.get_child({f"{addspace}:time"})

    
    print("Starting server at " + url)
    
    async with server:
        print("Server startet auf {}".format(url))

        temperature_c = 0
        humidity = 0
        
        while True:
            #Zeit ermitteln
            TIME = datetime.now()

            # Temperatur messen
            try:
                # Print the values to Console
                temperature_c = dhtDevice.temperature
                humidity = dhtDevice.humidity
                
                print(
                    "Temp: {:.1f} C    Humidity: {}%  Zeit:{:s}".format(
                         temperature_c, humidity, TIME.strftime("%d-%b-%Y (%H:%M:%S.%f)")
                    )
                )
                
                await temp.write_value(temperature_c)
                await hum.write_value(humidity)
                await time_node.write_value(TIME)
                
            except RuntimeError as error:
                print(error.args[0])
                await asyncio.sleep(2)
                continue
            except Exception as error:
                dhtDevice.exit()
                raise error        
            
            await asyncio.sleep(2)
            

                        

if __name__ == "__main__":
    asyncio.run(main())


