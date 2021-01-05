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

    public static BlockCategory get(int block) throws UnsupportedBlockException {
        for (var type : values()) {
            if (block >= type.min && block <= type.max) {
                type.value = block;
                return type;
            }
        }
        throw new UnsupportedBlockException("Invalid or unsupported block identifier");
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
}