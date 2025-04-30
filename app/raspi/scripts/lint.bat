@echo off
poetry run mypy .
poetry run pylint modules/tcp_ip/ modules/opc_ua/