package com.ibasco.gifdecoder.enums;

import java.util.Arrays;

/**
 * Indicates the way in which the graphic is to be treated after being displayed.
 *
 * @author Rafael Luis Ibasco
 */
public enum DisposalMethod {
    /**
     * No disposal specified. The decoder is not required to take any action.
     */
    NONE(0),
    /**
     * Do not dispose. The graphic is to be left in place.
     */
    DO_NOT_DISPOSE(1),
    /**
     * Restore to background color. The area used by the graphic must be restored to the background color.
     */
    RESTORE_TO_BACKGROUND(2),
    /**
     * Restore to previous. The decoder is required to restore the area overwritten by the graphic with what was there prior to rendering the graphic.
     */
    RESTORE_TO_PREVIOUS(3);

    private final int value;

    DisposalMethod(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static DisposalMethod get(int value) {
        for (var v : values()) {
            if (v.getValue() == value)
                return v;
        }
        throw new IllegalStateException("Invalid disposal method value");
    }
}
