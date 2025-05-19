package com.die_macher.dobot.exception;

/**
 * Exception thrown when communication with the Dobot device fails.
 */
public class DobotCommunicationException extends Exception {

    public DobotCommunicationException(String message) {
        super(message);
    }
}