import asyncio
# import board
import time
from datetime import datetime
from cryptography import x509
from cryptography.hazmat.primitives import serialization

from asyncua import Server, ua

async def main():
    server=Server()
    
    await server.load_certificate("./opcua_certs/server-cert.pem")
    await server.load_private_key("./opcua_certs/server-key.pem")
    
    await server.init()
    url = "opc.tcp://localhost:4840"
    server.set_endpoint(url)
    
    server.set_security_policy([
        ua.SecurityPolicyType.Basic256Sha256_SignAndEncrypt,
        ua.SecurityPolicyType.Basic256Sha256_Sign,
        #ua.SecurityPolicyType.NoSecurity 
    ])

    name="Die-Macher"
    addspace=await server.register_namespace(name)

    dev = await server.nodes.base_object_type.add_object_type(addspace, "FBS-Platine")
    mytemp = await (await dev.add_variable(addspace, "temperature", 1.0)).set_modelling_rule(True)
    myhum = await (await dev.add_variable(addspace, "humidity", 1.0)).set_modelling_rule(True)
    mytime = await (await dev.add_variable(addspace, "time", datetime.utcnow())).set_modelling_rule(True)
    
    myfolder = await server.nodes.objects.add_folder(addspace, "Raspi")
    mydevice = await myfolder.add_object(addspace, "FBS-Platine", dev)
        
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
                temperature_c = 22.1 # dhtDevice.temperature
                humidity = 44.55 # dhtDevice.humidity
                
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
                await asyncio.sleep(3)
                continue
            except Exception as error:
                # dhtDevice.exit()
                raise error        
            
            await asyncio.sleep(3)
                                    

if __name__ == "__main__":
    asyncio.run(main())


