import asyncio
import pytest
import unittest.mock as mock
from datetime import datetime
from unittest.mock import patch, MagicMock, AsyncMock

from asyncua import Server, ua
#from asyncua.ua import ObjectIds

# Import the module to test
from app.raspi.modules.opc_ua.server import main


class TestOpcUaServer:
    """Tests for the OPC UA server functionality."""

    @pytest.fixture
    def mock_server(self):
        """Create a mock Server instance."""
        server_mock = AsyncMock(spec=Server)
        server_mock.__aenter__.return_value = server_mock
        server_mock.__aexit__.return_value = None
        return server_mock

    @pytest.fixture
    def mock_node(self):
        """Create a mock Node instance."""
        node_mock = AsyncMock()
        return node_mock

    @pytest.fixture
    def mock_dht_device(self):
        """Create a mock DHT device."""
        dht_mock = MagicMock()
        dht_mock.temperature = 22.5
        dht_mock.humidity = 45.0
        return dht_mock

    @pytest.mark.asyncio
    async def test_server_initialization(self, mock_server):
        """Test server initialization and certificate loading."""
        with patch('app.raspi.modules.opc_ua.server.Server', return_value=mock_server), \
             patch('app.raspi.modules.opc_ua.server.ni.ifaddresses') as mock_ifaddresses, \
             patch('app.raspi.modules.opc_ua.server.asyncio.sleep'), \
             patch('app.raspi.modules.opc_ua.server.dhtDevice') as mock_dht:
            
            # Mock network interface
            mock_ifaddresses.return_value = {
                2: [{'addr': '192.168.1.100'}]
            }
            
            # Mock DHT device to raise exception after first loop to exit
            mock_dht.temperature = 22.5
            mock_dht.humidity = 45.0
            mock_dht.side_effect = [None, RuntimeError("Test exit")]
            
            # Start the server (will run until exception)
            try:
                await main()
            except RuntimeError:
                pass
            
            # Verify server initialization
            mock_server.load_certificate.assert_called_once()
            mock_server.load_private_key.assert_called_once()
            mock_server.init.assert_called_once()
            mock_server.set_endpoint.assert_called_once()
            mock_server.set_security_policy.assert_called_once()
            mock_server.register_namespace.assert_called_once_with("Die-Macher")

    @pytest.mark.asyncio
    async def test_namespace_registration(self, mock_server):
        """Test namespace registration."""
        with patch('app.raspi.modules.opc_ua.server.Server', return_value=mock_server), \
             patch('app.raspi.modules.opc_ua.server.ni.ifaddresses') as mock_ifaddresses, \
             patch('app.raspi.modules.opc_ua.server.asyncio.sleep'), \
             patch('app.raspi.modules.opc_ua.server.dhtDevice') as mock_dht:
            
            # Mock network interface
            mock_ifaddresses.return_value = {
                2: [{'addr': '192.168.1.100'}]
            }
            
            # Mock namespace registration
            mock_server.register_namespace.return_value = 2
            
            # Mock DHT device to raise exception after first loop to exit
            mock_dht.side_effect = RuntimeError("Test exit")
            
            # Start the server (will run until exception)
            try:
                await main()
            except RuntimeError:
                pass
            
            # Verify namespace registration
            mock_server.register_namespace.assert_called_once_with("Die-Macher")
            assert mock_server.register_namespace.return_value == 2

    @pytest.mark.asyncio
    async def test_node_creation(self, mock_server, mock_node):
        """Test node creation and structure."""
        with patch('app.raspi.modules.opc_ua.server.Server', return_value=mock_server), \
             patch('app.raspi.modules.opc_ua.server.ni.ifaddresses') as mock_ifaddresses, \
             patch('app.raspi.modules.opc_ua.server.asyncio.sleep'), \
             patch('app.raspi.modules.opc_ua.server.dhtDevice') as mock_dht:
            
            # Mock network interface
            mock_ifaddresses.return_value = {
                2: [{'addr': '192.168.1.100'}]
            }
            
            # Mock namespace registration
            mock_server.register_namespace.return_value = 2
            
            # Mock node creation
            mock_server.nodes = MagicMock()
            mock_server.nodes.base_object_type = MagicMock()
            mock_server.nodes.base_object_type.add_object_type = AsyncMock(return_value=mock_node)
            mock_server.nodes.objects = MagicMock()
            mock_server.nodes.objects.add_folder = AsyncMock(return_value=mock_node)
            
            # Mock variable creation
            variable_mock = AsyncMock()
            variable_mock.set_modelling_rule = AsyncMock(return_value=variable_mock)
            mock_node.add_variable = AsyncMock(return_value=variable_mock)
            mock_node.add_object = AsyncMock(return_value=mock_node)
            mock_node.get_child = AsyncMock(return_value=mock_node)
            
            # Mock DHT device to raise exception after first loop to exit
            mock_dht.side_effect = RuntimeError("Test exit")
            
            # Start the server (will run until exception)
            try:
                await main()
            except RuntimeError:
                pass
            
            # Verify object type creation
            mock_server.nodes.base_object_type.add_object_type.assert_called_once_with(2, "FBS-Platine")
            
            # Verify variable creation (3 variables: temperature, humidity, time)
            assert mock_node.add_variable.call_count == 3
            
            # Verify folder creation
            mock_server.nodes.objects.add_folder.assert_called_once_with(2, "Raspi")
            
            # Verify device instance creation
            mock_node.add_object.assert_called_once_with(2, "FBS-Platine", mock_node)

    @pytest.mark.asyncio
    async def test_sensor_data_reading(self, mock_server, mock_node, mock_dht_device):
        """Test sensor data reading and node value updates."""
        with patch('app.raspi.modules.opc_ua.server.Server', return_value=mock_server), \
             patch('app.raspi.modules.opc_ua.server.ni.ifaddresses') as mock_ifaddresses, \
             patch('app.raspi.modules.opc_ua.server.asyncio.sleep'), \
             patch('app.raspi.modules.opc_ua.server.dhtDevice', mock_dht_device):
            
            # Mock network interface
            mock_ifaddresses.return_value = {
                2: [{'addr': '192.168.1.100'}]
            }
            
            # Mock namespace registration
            mock_server.register_namespace.return_value = 2
            
            # Mock node creation
            mock_server.nodes = MagicMock()
            mock_server.nodes.base_object_type = MagicMock()
            mock_server.nodes.base_object_type.add_object_type = AsyncMock(return_value=mock_node)
            mock_server.nodes.objects = MagicMock()
            mock_server.nodes.objects.add_folder = AsyncMock(return_value=mock_node)
            
            # Mock variable creation
            variable_mock = AsyncMock()
            variable_mock.set_modelling_rule = AsyncMock(return_value=variable_mock)
            mock_node.add_variable = AsyncMock(return_value=variable_mock)
            mock_node.add_object = AsyncMock(return_value=mock_node)
            
            # Mock child nodes
            temp_node = AsyncMock()
            hum_node = AsyncMock()
            time_node = AsyncMock()
            mock_node.get_child = AsyncMock(side_effect=[temp_node, hum_node, time_node])
            
            # Set up to exit after one iteration
            async def mock_sleep(seconds):
                raise RuntimeError("Test exit")
            
            with patch('app.raspi.modules.opc_ua.server.asyncio.sleep', side_effect=mock_sleep):
                # Start the server (will run until exception)
                try:
                    await main()
                except RuntimeError:
                    pass
            
            # Verify sensor data was read and written to nodes
            temp_node.write_value.assert_called_once_with(22.5)
            hum_node.write_value.assert_called_once_with(45.0)
            time_node.write_value.assert_called_once()

    @pytest.mark.asyncio
    async def test_sensor_runtime_error_handling(self, mock_server, mock_node):
        """Test handling of RuntimeError from DHT sensor."""
        with patch('app.raspi.modules.opc_ua.server.Server', return_value=mock_server), \
             patch('app.raspi.modules.opc_ua.server.ni.ifaddresses') as mock_ifaddresses, \
             patch('app.raspi.modules.opc_ua.server.asyncio.sleep') as mock_sleep, \
             patch('app.raspi.modules.opc_ua.server.dhtDevice') as mock_dht, \
             patch('builtins.print') as mock_print:
            
            # Mock network interface
            mock_ifaddresses.return_value = {
                2: [{'addr': '192.168.1.100'}]
            }
            
            # Mock namespace registration
            mock_server.register_namespace.return_value = 2
            
            # Mock node creation
            mock_server.nodes = MagicMock()
            mock_server.nodes.base_object_type = MagicMock()
            mock_server.nodes.base_object_type.add_object_type = AsyncMock(return_value=mock_node)
            mock_server.nodes.objects = MagicMock()
            mock_server.nodes.objects.add_folder = AsyncMock(return_value=mock_node)
            
            # Mock variable creation
            variable_mock = AsyncMock()
            variable_mock.set_modelling_rule = AsyncMock(return_value=variable_mock)
            mock_node.add_variable = AsyncMock(return_value=variable_mock)
            mock_node.add_object = AsyncMock(return_value=mock_node)
            mock_node.get_child = AsyncMock(return_value=mock_node)
            
            # Mock DHT device to raise RuntimeError
            mock_dht.temperature = property(side_effect=RuntimeError("Sensor error"))
            
            # Set up to exit after one sleep
            mock_sleep.side_effect = [None, RuntimeError("Test exit")]
            
            # Start the server (will run until exception)
            try:
                await main()
            except RuntimeError as e:
                assert str(e) == "Test exit"
            
            # Verify error was printed
            mock_print.assert_any_call("Sensor error")
            
            # Verify sleep was called for error recovery
            mock_sleep.assert_called_with(2)

    @pytest.mark.asyncio
    async def test_sensor_general_exception_handling(self, mock_server, mock_node):
        """Test handling of general exceptions from DHT sensor."""
        with patch('app.raspi.modules.opc_ua.server.Server', return_value=mock_server), \
             patch('app.raspi.modules.opc_ua.server.ni.ifaddresses') as mock_ifaddresses, \
             patch('app.raspi.modules.opc_ua.server.dhtDevice') as mock_dht:
            
            # Mock network interface
            mock_ifaddresses.return_value = {
                2: [{'addr': '192.168.1.100'}]
            }
            
            # Mock namespace registration
            mock_server.register_namespace.return_value = 2
            
            # Mock node creation
            mock_server.nodes = MagicMock()
            mock_server.nodes.base_object_type = MagicMock()
            mock_server.nodes.base_object_type.add_object_type = AsyncMock(return_value=mock_node)
            mock_server.nodes.objects = MagicMock()
            mock_server.nodes.objects.add_folder = AsyncMock(return_value=mock_node)
            
            # Mock variable creation
            variable_mock = AsyncMock()
            variable_mock.set_modelling_rule = AsyncMock(return_value=variable_mock)
            mock_node.add_variable = AsyncMock(return_value=variable_mock)
            mock_node.add_object = AsyncMock(return_value=mock_node)
            mock_node.get_child = AsyncMock(return_value=mock_node)
            
            # Mock DHT device to raise Exception
            mock_dht.temperature = property(side_effect=Exception("General error"))
            mock_dht.exit = MagicMock()
            
            # Start the server (will run until exception)
            with pytest.raises(Exception) as excinfo:
                await main()
            
            assert "General error" in str(excinfo.value)
            
            # Verify DHT device exit was called
            mock_dht.exit.assert_called_once()