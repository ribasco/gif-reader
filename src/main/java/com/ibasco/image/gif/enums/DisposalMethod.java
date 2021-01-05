/*
 * Copyright 2021 Rafael Luis L. Ibasco
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ibasco.image.gif.enums;

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

    public static DisposalMethod get(int value) {
        for (var v : values()) {
            if (v.getValue() == value)
                return v;
        }
        throw new IllegalStateException("Invalid disposal method value");
    }

    public int getValue() {
        return value;
    }
}
