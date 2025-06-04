@echo off
poetry run mypy .
poetry run pylint modules
