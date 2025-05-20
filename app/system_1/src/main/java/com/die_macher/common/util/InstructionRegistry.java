package com.die_macher.common.util;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class InstructionRegistry {
    private final Map<String, List<Instruction>> instructionMap = new HashMap<>();

    public void register(String key, Instruction instruction) {
        instructionMap.computeIfAbsent(key, k -> new ArrayList<>()).add(instruction);
    }

    public void register(String key, List<Instruction> instructions) {
        instructions.forEach(instruction -> register(key, instruction));
    }

    public void execute(String key, Object... args) {
        List<Instruction> instructions = instructionMap.get(key);
        if (instructions == null) {
            throw new IllegalArgumentException("No instructions registered for key: " + key);
        }
        for (Instruction instruction : instructions) {
            instruction.execute(args);
        }
    }
}
