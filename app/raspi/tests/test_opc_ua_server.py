import asyncio
import pytest
import unittest.mock as mock
from datetime import datetime, timezone
from unittest.mock import patch, MagicMock, AsyncMock

from asyncua import Server, Node
from asyncua.ua import SecurityPolicyType

# Import the module to test
from app.raspi.modules.opc_ua.server import OpcuaServer


class TestOpcUaServer:
    """Tests for the OPC UA server functionality."""

    @pytest.fixture
    def mock_server(self):
        """Create a mock Server instance."""
        server_mock = AsyncMock(spec=Server)
        server_mock.__aenter__.return_value = server_mock
        server_mock.__aexit__.return_value = None
        server_mock.nodes = MagicMock()
        server_mock.nodes.base_object_type = MagicMock()
        server_mock.nodes.objects = MagicMock()
        return server_mock

    @pytest.fixture
    def mock_node(self):
        """Create a mock Node instance."""
        node_mock = AsyncMock(spec=Node)
        return node_mock

    @pytest.fixture
    def mock_dht_device(self):
        """Create a mock DHT device."""
        dht_mock = MagicMock()
        dht_mock.temperature = 22.5
        dht_mock.humidity = 45.0
        dht_mock.exit = MagicMock()
        return dht_mock

    @pytest.fixture
    def mock_gpio(self):
        """Create a mock GPIO module."""
        gpio_mock = MagicMock()
        return gpio_mock

    @pytest.fixture
    def opcua_server(self, mock_dht_device, mock_gpio):
        """Create an OpcuaServer instance with mocked dependencies."""
        with patch('app.raspi.modules.opc_ua.server.adafruit_dht.DHT22', return_value=mock_dht_device), \
                patch('app.raspi.modules.opc_ua.server.GPIO', mock_gpio), \
                patch('app.raspi.modules.opc_ua.server.board'), \
                patch('app.raspi.modules.opc_ua.server.Server') as mock_server_class:

            mock_server_instance = MagicMock()
            mock_server_class.return_value = mock_server_instance

            server = OpcuaServer(
                cert_path="/test/cert.pem",
                key_path="/test/key.pem",
                interface="wlan0"
            )
            server.server = AsyncMock(spec=Server)
            return server

    @pytest.mark.asyncio
    async def test_server_initialization(self, opcua_server, mock_dht_device, mock_gpio):
        """Test server initialization."""
        # Verify server was initialized with correct parameters
        assert opcua_server.cert_path == "/test/cert.pem"
        assert opcua_server.key_path == "/test/key.pem"
        assert opcua_server.interface == "wlan0"
        assert opcua_server.dht_device == mock_dht_device

        # Verify GPIO setup was called
        mock_gpio.setmode.assert_called_once_with(mock_gpio.BCM)
        mock_gpio.setwarnings.assert_called_once_with(False)
        mock_gpio.setup.assert_called_once_with(4, mock_gpio.IN)

    @pytest.mark.asyncio
    async def test_setup_server(self, opcua_server):
        """Test server setup and configuration."""
        with patch('app.raspi.modules.opc_ua.server.ni.ifaddresses') as mock_ifaddresses:
            # Mock network interface
            mock_ifaddresses.return_value = {
                2: [{'addr': '192.168.1.100'}]
            }

            # Mock server nodes and methods
            mock_namespace = 2
            opcua_server.server.register_namespace = AsyncMock(return_value=mock_namespace)
            opcua_server.server.nodes.base_object_type.add_object_type = AsyncMock()
            opcua_server.server.nodes.objects.add_folder = AsyncMock()

            # Mock variable and object creation
            mock_dev_type = AsyncMock()
            mock_variable = AsyncMock()
            mock_variable.set_modelling_rule = AsyncMock(return_value=mock_variable)
            mock_dev_type.add_variable = AsyncMock(return_value=mock_variable)
            opcua_server.server.nodes.base_object_type.add_object_type.return_value = mock_dev_type

            mock_folder = AsyncMock()
            mock_device = AsyncMock()
            opcua_server.server.nodes.objects.add_folder.return_value = mock_folder
            mock_folder.add_object = AsyncMock(return_value=mock_device)

            # Mock node retrieval
            temp_node = AsyncMock()
            hum_node = AsyncMock()
            time_node = AsyncMock()
            mock_device.get_child = AsyncMock(side_effect=[temp_node, hum_node, time_node])

            # Execute setup
            await opcua_server.setup_server()

            # Verify server initialization calls
            opcua_server.server.load_certificate.assert_called_once_with("/test/cert.pem")
            opcua_server.server.load_private_key.assert_called_once_with("/test/key.pem")
            opcua_server.server.init.assert_called_once()
            opcua_server.server.set_endpoint.assert_called_once_with("opc.tcp://192.168.1.100:4840")

            # Verify security policy setup
            opcua_server.server.set_security_policy.assert_called_once()
            security_policies = opcua_server.server.set_security_policy.call_args[0][0]
            assert SecurityPolicyType.Basic256Sha256_SignAndEncrypt in security_policies
            assert SecurityPolicyType.Basic256Sha256_Sign in security_policies
            assert SecurityPolicyType.NoSecurity in security_policies

            # Verify namespace registration
            opcua_server.server.register_namespace.assert_called_once_with("Die-Macher")

            # Verify object type creation
            opcua_server.server.nodes.base_object_type.add_object_type.assert_called_once_with(
                mock_namespace, "FBS-Platine"
            )

            # Verify variable creation (3 variables: temperature, humidity, time)
            assert mock_dev_type.add_variable.call_count == 3

            # Verify folder and device creation
            opcua_server.server.nodes.objects.add_folder.assert_called_once_with(mock_namespace, "Raspi")
            mock_folder.add_object.assert_called_once_with(mock_namespace, "FBS-Platine", mock_dev_type)

            # Verify nodes were assigned
            assert opcua_server.temp_node == temp_node
            assert opcua_server.hum_node == hum_node
            assert opcua_server.time_node == time_node

    @pytest.mark.asyncio
    async def test_start_server_success(self, opcua_server, mock_dht_device):
        """Test successful server start and sensor data loop."""
        # Set up mock nodes
        opcua_server.temp_node = AsyncMock()
        opcua_server.hum_node = AsyncMock()
        opcua_server.time_node = AsyncMock()

        # Mock sensor readings
        mock_dht_device.temperature = 22.5
        mock_dht_device.humidity = 65.0

        # Mock asyncio.sleep to exit after one iteration
        call_count = 0
        async def mock_sleep(seconds):
            nonlocal call_count
            call_count += 1
            if call_count >= 2:  # Exit after first successful read
                raise KeyboardInterrupt("Test exit")

        with patch('app.raspi.modules.opc_ua.server.asyncio.sleep', side_effect=mock_sleep), \
                patch('builtins.print') as mock_print:

            # Start server (will exit after one iteration)
            with pytest.raises(KeyboardInterrupt):
                await opcua_server.start()

            # Verify sensor data was written to nodes
            opcua_server.temp_node.write_value.assert_called_with(22.5)
            opcua_server.hum_node.write_value.assert_called_with(65.0)
            opcua_server.time_node.write_value.assert_called_once()

            # Verify sensor reading was printed
            mock_print.assert_called()

    @pytest.mark.asyncio
    async def test_start_server_runtime_error_handling(self, opcua_server, mock_dht_device):
        """Test handling of RuntimeError from DHT sensor."""
        # Set up mock nodes
        opcua_server.temp_node = AsyncMock()
        opcua_server.hum_node = AsyncMock()
        opcua_server.time_node = AsyncMock()

        # Mock sensor to raise RuntimeError
        type(mock_dht_device).temperature = mock.PropertyMock(
            side_effect=RuntimeError("Sensor timeout")
        )

        # Mock asyncio.sleep to exit after error handling
        call_count = 0
        async def mock_sleep(seconds):
            nonlocal call_count
            call_count += 1
            if call_count >= 2:  # Exit after error recovery sleep
                raise KeyboardInterrupt("Test exit")

        with patch('app.raspi.modules.opc_ua.server.asyncio.sleep', side_effect=mock_sleep), \
                patch('builtins.print') as mock_print:

            # Start server (will exit after error handling)
            with pytest.raises(KeyboardInterrupt):
                await opcua_server.start()

            # Verify error was printed
            mock_print.assert_any_call("Sensor Error:", "Sensor timeout")

            # Verify nodes were not written to due to error
            opcua_server.temp_node.write_value.assert_not_called()
            opcua_server.hum_node.write_value.assert_not_called()
            opcua_server.time_node.write_value.assert_not_called()

    @pytest.mark.asyncio
    async def test_start_server_general_exception_handling(self, opcua_server, mock_dht_device):
        """Test handling of general exceptions from DHT sensor."""
        # Set up mock nodes
        opcua_server.temp_node = AsyncMock()
        opcua_server.hum_node = AsyncMock()
        opcua_server.time_node = AsyncMock()

        # Mock sensor to raise general Exception
        type(mock_dht_device).temperature = mock.PropertyMock(
            side_effect=Exception("General sensor error")
        )

        with patch('builtins.print'):
            # Start server (should raise the exception after cleanup)
            with pytest.raises(Exception) as excinfo:
                await opcua_server.start()

            assert "General sensor error" in str(excinfo.value)

            # Verify DHT device exit was called
            mock_dht_device.exit.assert_called_once()

    @pytest.mark.asyncio
    async def test_start_server_with_none_nodes(self, opcua_server, mock_dht_device):
        """Test server start when nodes are None (not initialized)."""
        # Leave nodes as None (default state)
        opcua_server.temp_node = None
        opcua_server.hum_node = None
        opcua_server.time_node = None

        # Mock sensor readings
        mock_dht_device.temperature = 22.5
        mock_dht_device.humidity = 65.0

        # Mock asyncio.sleep to exit after one iteration
        async def mock_sleep(seconds):
            raise KeyboardInterrupt("Test exit")

        with patch('app.raspi.modules.opc_ua.server.asyncio.sleep', side_effect=mock_sleep):

            # Start server (will exit after one iteration)
            with pytest.raises(KeyboardInterrupt):
                await opcua_server.start()

            # Should not crash even with None nodes

    @pytest.mark.asyncio
    async def test_stop_server(self, opcua_server, mock_dht_device, mock_gpio):
        """Test server stop and cleanup."""
        with patch('builtins.print') as mock_print:
            # Stop the server
            await opcua_server.stop()

            # Verify server stop was called
            opcua_server.server.stop.assert_called_once()

            # Verify cleanup was performed
            mock_dht_device.exit.assert_called_once()
            mock_gpio.cleanup.assert_called_once()

            # Verify status messages were printed
            mock_print.assert_any_call("Stopping OPC UA Server...")
            mock_print.assert_any_call("OPC UA Server stopped.")
            mock_print.assert_any_call("GPIO cleaned up and DHT sensor released.")

    @pytest.mark.asyncio
    async def test_stop_server_with_exception(self, opcua_server, mock_dht_device, mock_gpio):
        """Test server stop when server.stop() raises an exception."""
        # Mock server.stop to raise an exception
        opcua_server.server.stop.side_effect = Exception("Stop error")

        with patch('builtins.print') as mock_print:
            # Stop the server
            await opcua_server.stop()

            # Verify error was handled and cleanup still performed
            mock_print.assert_any_call("Error while stopping server: Stop error")
            mock_dht_device.exit.assert_called_once()
            mock_gpio.cleanup.assert_called_once()

    def test_default_interface(self, mock_dht_device, mock_gpio):
        """Test server initialization with default interface."""
        with patch('app.raspi.modules.opc_ua.server.adafruit_dht.DHT22', return_value=mock_dht_device), \
                patch('app.raspi.modules.opc_ua.server.GPIO', mock_gpio), \
                patch('app.raspi.modules.opc_ua.server.board'), \
                patch('app.raspi.modules.opc_ua.server.Server'):

            server = OpcuaServer("/test/cert.pem", "/test/key.pem")
            assert server.interface == "wlan0"  # Default value

    def test_custom_interface(self, mock_dht_device, mock_gpio):
        """Test server initialization with custom interface."""
        with patch('app.raspi.modules.opc_ua.server.adafruit_dht.DHT22', return_value=mock_dht_device), \
                patch('app.raspi.modules.opc_ua.server.GPIO', mock_gpio), \
                patch('app.raspi.modules.opc_ua.server.board'), \
                patch('app.raspi.modules.opc_ua.server.Server'):

            server = OpcuaServer("/test/cert.pem", "/test/key.pem", interface="eth0")
            assert server.interface == "eth0"