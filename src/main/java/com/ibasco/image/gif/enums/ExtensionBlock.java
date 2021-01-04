package com.ibasco.image.gif.enums;

import java.util.Arrays;

/**
 * Enumeration for the supported extension blocks in the GIF specification
 *
 * @author Rafael Luis Ibasco
 */
public enum ExtensionBlock implements BlockIdentifier {

    GRAPHICS(0xF9, "Graphics Extension Block"),
    COMMENT(0xFE, "Comment Extension Block"),
    PLAINTEXT(0x01, "PlainText Extension Block"),
    APPLICATION(0xFF, "Application Extension Block"),
    UNKNOWN(-1, "Unknown Extension Block");

    private final int code;

    private final String name;

    ExtensionBlock(int code, String name) {
        this.code = code;
        this.name = name;
    }

    @Override
    public int getCodeInt() {
        return code;
    }

    @Override
    public byte getCodeByte() {
        return (byte) code;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public static ExtensionBlock get(int code) {
        return Arrays.stream(values()).filter(f -> f.code == code).findFirst().orElse(UNKNOWN);
    }
}
