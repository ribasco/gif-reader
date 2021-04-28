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

package com.ibasco.image.gif.io;

import java.io.Closeable;
import java.io.IOException;

/**
 * The interface for GIF Code Readers
 *
 * @author Rafael Luis Ibasco
 */
public interface GifCodeReader extends Closeable {

    /**
     * Reads a single LZW encoded byte
     *
     * @return An unsigned 32-bit LZW encoded integer
     */
    int read() throws IOException;

    /**
     * @return The current code size value (with the offset applied)
     */
    int getCodeSize();

    /**
     * @return The current offset applied to the code size value
     */
    int getCodeSizeOffset();

    /**
     * Call this method if you need to increase the code size by 1. Please note that internally,
     * we are not directly manipulating the code size value, rather an offset's value is increased
     */
    void increaseCodeSizeOffset();

    /**
     * Clears the code size offset which then allows us to revert back to the original code size value.
     */
    void clearCodeSizeOffset();

    /**
     * Closes the underlying resource of this class
     *
     * @throws IOException
     *         When the resource cannot be closed
     */
    void close() throws IOException;
}
