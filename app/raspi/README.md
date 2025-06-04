# Raspberry Pi Camera and Sensor Module

This module handles TCP/IP camera streaming and OPC UA temperature & humidity monitoring on Raspberry Pi.

## 🚀 Quick Start

1. Install Poetry (Python package manager):
```bash
pip install poetry
```

2. Verify installation:
```bash
poetry --version
```

3. Install project dependencies:
```bash
poetry install
```

## 💻 Development

Format code and sort imports:
```bash
scripts/format
```

Type checking and linting:
```bash
scripts/lint
```

Tests with coverage:
```bash
scripts/test
```

## 📦 Adding Dependencies

Add production dependencies:
```bash
poetry add <package-name>
```

Add development dependencies:
```bash
poetry add --dev <package-name>
```

## 🔑 Executing the Entry Point

```bash
scripts/start
```

## 📄 Building the module

```bash
scripts/build
```
