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

    public static ExtensionBlock get(int code) {
        return Arrays.stream(values()).filter(f -> f.code == code).findFirst().orElse(UNKNOWN);
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
}
