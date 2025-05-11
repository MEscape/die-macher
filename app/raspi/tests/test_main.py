import asyncio
from unittest.mock import patch

import pytest

from modules.tcp_ip.server import serve_webcam_stream


@pytest.mark.asyncio
async def test_run_tcp_server_success():
    """Test successful TCP server execution"""
    with patch("asyncio.to_thread") as mock_to_thread:
        mock_to_thread.return_value = None
        from main import run_tcp_server

        task = asyncio.create_task(run_tcp_server())
        await asyncio.sleep(0.1)
        task.cancel()
        try:
            await task
        except asyncio.CancelledError:
            pass
        mock_to_thread.assert_called_once_with(
            serve_webcam_stream, host="localhost", port=8000
        )


@pytest.mark.asyncio
async def test_run_tcp_server_error():
    """Test TCP server error handling"""
    with patch("asyncio.to_thread") as mock_to_thread, patch(
        "sys.stderr"
    ) as mock_stderr:
        mock_to_thread.side_effect = Exception("Test error")
        from main import run_tcp_server

        await run_tcp_server()
        assert any(
            "Error in TCP/IP server: Test error" in str(call_args)
            for call_args in mock_stderr.write.call_args_list
        )


@pytest.mark.asyncio
async def test_main_success():
    """Test successful main function execution"""
    with patch("main.run_tcp_server") as mock_run_tcp:
        mock_run_tcp.return_value = None

        from main import main

        task = asyncio.create_task(main())
        await asyncio.sleep(0.1)
        task.cancel()
        try:
            await task
        except asyncio.CancelledError:
            pass

        mock_run_tcp.assert_called_once()


@pytest.mark.asyncio
async def test_main_general_exception():
    """Test main function with general exception"""
    with patch("main.run_tcp_server") as mock_run_tcp, patch(
        "sys.stderr"
    ) as mock_stderr:
        mock_run_tcp.side_effect = Exception("Test error")

        from main import main

        await main()

        assert any(
            "Error: Test error" in str(call_args)
            for call_args in mock_stderr.write.call_args_list
        )
