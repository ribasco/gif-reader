package com.ibasco.gifdecoder.enums;

import java.util.Arrays;

/**
 * Enumeration for the supported extension blocks in the GIF specification
 *
 * @author Rafael Luis Ibasco
 */
public enum ExtensionBlock {
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

    String getName() {
        return name;
    }

    int getCode() {
        return code;
    }

    public static ExtensionBlock get(int code) {
        return Arrays.stream(values()).filter(f -> f.code == code).findFirst().orElse(UNKNOWN);
    }
}
