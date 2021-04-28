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
import java.nio.ByteOrder;

/**
 * Interface containing operations for reading from an Image file
 *
 * @author Rafael Luis Ibasco
 */
public interface ImageInputStream extends Closeable {

    int read(byte[] buffer, int offset, int length);

    int read(byte[] buffer);

    void mark();

    void reset() throws IOException;

    void setByteOrder(ByteOrder order);

    int getStreamPosition();

    void seek(int pos);

    byte readByte() throws IOException;

    short readShort() throws IOException;

    int readUnsignedByte() throws IOException;

    long skipBytes(int count) throws IOException;
}
