package com.ibasco.image.gif.enums;

import com.ibasco.image.gif.exceptions.UnsupportedBlockException;

import java.util.Arrays;

/**
 * Enumeration describing the types of data blocks contained in a GIF file.
 *
 * @author Rafael Luis Ibasco
 */
public enum Block implements BlockIdentifier {

    EXTENSION(0x21, "Extension"),

    IMAGE_DESCRIPTOR(0x2C, "Image Descriptor"),

    TRAILER(0x3B, "Trailer"),

    /* All non-official spec codes are > 0x3B */

    LOGICAL_SCREEN_DESCRIPTOR(0x3C, "Logical Screen Descriptor"), //non-official spec

    LOCAL_COLOR_TABLE(0x3D, "Global Color Table"), //non-official spec

    GLOBAL_COLOR_TABLE(0x3E, "Local Color Table"), //non-official spec

    IMAGE_DATA(0x3F, "Image Data"), //non-official spec

    IMAGE_DATA_BLOCK(0x41, "Image Data Block"), //non-official spec

    INITIALIZE(0x40, "Initialize"); //non-official spec

    private final int code;

    private final String name;

    Block(int code, String name) {
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
