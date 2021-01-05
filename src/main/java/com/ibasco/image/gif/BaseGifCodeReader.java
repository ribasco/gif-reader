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

package com.ibasco.image.gif;

import java.io.IOException;

/**
 * Base class for {@link GifCodeReader}
 *
 * @author Rafael Luis Ibasco
 */
abstract public class BaseGifCodeReader implements GifCodeReader {

    private final int codeSize;

    private int fromIndex;

    private int offset = 0;

    public BaseGifCodeReader(int codeSize) {
        this.codeSize = codeSize + 1;
        this.fromIndex = 0;
    }

    protected final int getToIndex() {
        return getFromIndex() + getCodeSize();
    }

    protected int getFromIndex() {
        return fromIndex;
    }

    protected void setFromIndex(int fromIndex) {
        this.fromIndex = fromIndex;
    }

    /**
     * Reads a single LZW encoded byte
     *
     * @return An unsigned 32-bit LZW encoded integer
     */
    @Override
    abstract public int read() throws IOException;

    @Override
    public int getCodeSize() {
        return this.codeSize + offset;
    }

    @Override
    public int getCodeSizeOffset() {
        return offset;
    }

    /**
     * Call this method if you need to increase the code size by 1. Please note that internally,
     * we are not directly manipulating the code size value, rather an offset's value is increased
     */
    @Override
    public void increaseCodeSizeOffset() {
        this.offset++;
    }

    /**
     * Clears the code size offset which then allows us to revert back to the original code size value.
     */
    @Override
    public void clearCodeSizeOffset() {
        this.offset = 0;
    }
}
