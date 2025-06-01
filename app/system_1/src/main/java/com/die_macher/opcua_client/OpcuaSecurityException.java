package com.die_macher.opcua_client;

public class OpcuaSecurityException extends Exception {
    public OpcuaSecurityException(String message) {
        super(message);
    }
    public OpcuaSecurityException(String message, Throwable cause) {
        super(message, cause);
    }
}