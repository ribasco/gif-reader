package com.ibasco.gifdecoder.enums;

import com.ibasco.gifdecoder.exceptions.UnsupportedBlockException;

import java.util.Arrays;

public enum Block {
    EXTENSION(0x21, "Extension Block"),
    IMAGE_DESCRIPTOR(0x2C, "Image Descriptor"),
    TRAILER(0x3B, "Image Trailer");

    private final int code;

    private final String name;

    Block(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public BlockCategory getCategory() throws UnsupportedBlockException {
        return BlockCategory.get(code);
    }

    public int getCodeInt() {
        return code;
    }

    public byte getCodeByte() {
        return (byte) code;
    }

    public String getName() {
        return name;
    }

    public static Block get(int code) throws UnsupportedBlockException {
        return Arrays.stream(values()).filter(f -> f.code == code).findFirst().orElseThrow(UnsupportedBlockException::new);
    }

    public static boolean isValid(int code) {
        for (var b : values()) {
            if (b.getCodeInt() == code)
                return true;
        }
        return false;
    }
}
