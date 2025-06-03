package com.die_macher.pick_and_place.dobot.protocol.api;

import lombok.Getter;

@Getter
public enum PTPModes {
    JUMP_XYZ(0), // JUMP mode, (x,y,z,r) is the target point in Cartesian coordinate system
    MOVJ_XYZ(1), // MOVJ mode, (x,y,z,r) is the target point in Cartesian coordinate system
    MOVL_XYZ(2), //MOVL mode, (x,y,z,r) is the target point in Cartesian coordinate system
    JUMP_ANGLE(3), // JUMP mode, (x,y,z,r) is the target point in Joint coordinate system
    MOVJ_ANGLE(4), // MOVJ mode, (x,y,z,r) is the target point in Joint coordinate system
    MOVL_ANGLE(5), // MOVL mode, (x,y,z,r) is the target point in Joint coordinate system
    MOVJ_INC(6), // MOVJ mode, (x,y,z,r) is the angle increment in Joint coordinate system
    MOVL_INC(7), // MOVL mode, (x,y,z,r) is the Cartesian coordinate increment in Joint coordinate system
    MOVJ_XYZ_INC(8); // MOVJ mode, (x,y,z,r) is the Cartesian coordinate increment in Cartesian coordinate

    private final int value;

    PTPModes(int value) {
        this.value = value;
    }
}