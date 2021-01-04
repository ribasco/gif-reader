package com.ibasco.image.gif.enums;

import com.ibasco.image.gif.exceptions.UnsupportedBlockException;

public enum BlockCategory {
    CONTROL(0x80, 0xF9),
    GRAPHIC(0x00, 0x7F),
    SPECIAL(0xFA, 0xFF);

    private final int min;

    private final int max;

    private int value = -1;

    BlockCategory(int min, int max) {
        this.min = min;
        this.max = max;
    }

    int getMin() {
        return min;
    }

    int getMax() {
        return max;
    }

    int getValue() {
        return value;
    }

    public static BlockCategory get(int block) throws UnsupportedBlockException {
        for (var type : values()) {
            if (block >= type.min && block <= type.max) {
                type.value = block;
                return type;
            }
        }
        throw new UnsupportedBlockException("Invalid or unsupported block identifier");
    }
}