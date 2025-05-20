package com.die_macher.common.util;

@FunctionalInterface
public interface Instruction {
    void execute(Object... args);
}
