import asyncio
import pytest
from asyncua import Client
import logging

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Server details - update with your actual server IP
SERVER_ENDPOINT = "opc.tcp://192.168.1.100:4840"
NAMESPACE = "Die-Macher"

# Path to client certificate and private key
CLIENT_CERT_PATH = "path/to/client-cert.pem"
CLIENT_KEY_PATH = "path/to/client-key.pem"

class TestOpcUaClientConnection:
    """Tests for OPC UA client connection to the server."""

    @pytest.mark.asyncio
    async def test_connect_to_server(self):
        """Test basic connection to the OPC UA server."""
        client = Client(url=SERVER_ENDPOINT)
        
        # Set security if needed
        # client.set_security_string(f"Basic256Sha256,SignAndEncrypt,{CLIENT_CERT_PATH},{CLIENT_KEY_PATH}")
        
        try:
            async with client:
                # If we get here, connection was successful
                logger.info("Successfully connected to server")
                
                # Get namespace index
                nsidx = await client.get_namespace_index(NAMESPACE)
                assert nsidx is not None, f"Namespace '{NAMESPACE}' not found"
                
                # Test if we can browse the server
                objects = client.nodes.objects
                assert objects is not None
                
                # Try to find our folder
                folder = await objects.get_child([f"{nsidx}:Raspi"])
                assert folder is not None
                
                # Try to find our device
                device = await folder.get_child([f"{nsidx}:FBS-Platine"])
                assert device is not None
                
                # Try to read temperature value
                temp_node = await device.get_child([f"{nsidx}:temperature"])
                temp_value = await temp_node.read_value()
                logger.info(f"Temperature: {temp_value}")
                assert isinstance(temp_value, (int, float))
                
                # Try to read humidity value
                hum_node = await device.get_child([f"{nsidx}:humidity"])
                hum_value = await hum_node.read_value()
                logger.info(f"Humidity: {hum_value}")
                assert isinstance(hum_value, (int, float))
                
                # Try to read time value
                time_node = await device.get_child([f"{nsidx}:time"])
                time_value = await time_node.read_value()
                logger.info(f"Time: {time_value}")
                assert time_value is not None
                
        except Exception as e:
            pytest.fail(f"Failed to connect to server: {e}")

    @pytest.mark.asyncio
    async def test_security_connection(self):
        """Test secure connection to the OPC UA server."""
        client = Client(url=SERVER_ENDPOINT)
        
        # Set security
        client.set_security_string(f"Basic256Sha256,SignAndEncrypt,{CLIENT_CERT_PATH},{CLIENT_KEY_PATH}")
        
        try:
            async with client:
                # If we get here, secure connection was successful
                logger.info("Successfully connected to server with security")
                
                # Get namespace index
                nsidx = await client.get_namespace_index(NAMESPACE)
                assert nsidx is not None
                
        except Exception as e:
            pytest.fail(f"Failed to connect securely to server: {e}")

    @pytest.mark.asyncio
    async def test_data_changes(self):
        """Test subscribing to data changes."""
        client = Client(url=SERVER_ENDPOINT)
        
        try:
            async with client:
                # Get namespace index
                nsidx = await client.get_namespace_index(NAMESPACE)
                
                # Find temperature node
                objects = client.nodes.objects
                folder = await objects.get_child([f"{nsidx}:Raspi"])
                device = await folder.get_child([f"{nsidx}:FBS-Platine"])
                temp_node = await device.get_child([f"{nsidx}:temperature"])
                
                # Create subscription
                subscription = await client.create_subscription(500, self)
                handle = await subscription.subscribe_data_change(temp_node)
                
                # Wait for some data changes
                await asyncio.sleep(10)
                
                # Clean up
                await subscription.unsubscribe(handle)
                await subscription.delete()
                
        except Exception as e:
            pytest.fail(f"Failed to subscribe to data changes: {e}")
    
    # Callback for data change notifications
    async def datachange_notification(self, node, val, data):
        logger.info(f"Data change: {node} = {val}")