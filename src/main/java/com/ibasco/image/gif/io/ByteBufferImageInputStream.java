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

import org.apiguardian.api.API;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.InvalidMarkException;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * An {@link ImageInputStream} backed by a {@link ByteBuffer}
 *
 * @author Rafael Luis Ibasco
 */
public class ByteBufferImageInputStream implements ImageInputStream {

    private File file;

    private ByteBuffer buf;

    private final Deque<Integer> markStack = new ArrayDeque<>();

    public ByteBufferImageInputStream(File file) throws IOException {
        this(new FileInputStream(file));
        this.file = file;
    }

    public ByteBufferImageInputStream(InputStream is) throws IOException {
        this.buf = ByteBuffer.wrap(is.readAllBytes());
    }

    @Override
    public int read(byte[] buffer, int offset, int length) {
        buf.get(buffer, offset, length);
        return length;
    }

    @Override
    public int read(byte[] buffer) {
        buf.get(buffer);
        return buffer.length;
    }

    @Override
    public void mark() {
        markStack.push(buf.position());
    }

    @Override
    public void reset() throws IOException {
        if (this.markStack.isEmpty())
            throw new InvalidMarkException();
        buf.position(this.markStack.pop());
    }

    @Override
    public void setByteOrder(ByteOrder order) {
        buf.order(order);
    }

    @Override
    public int getStreamPosition() {
        return buf.position();
    }

    @Override
    public void seek(int pos) {
        buf.position(pos);
    }

    @Override
    public byte readByte() throws IOException {
        return buf.get();
    }

    @Override
    public short readShort() throws IOException {
        return buf.getShort();
    }

    @Override
    public int readUnsignedByte() throws IOException {
        return Byte.toUnsignedInt(buf.get());
    }

    @Override
    public long skipBytes(int count) throws IOException {
        buf.position(buf.position() + count);
        return count;
    }

    @API(status = API.Status.STABLE)
    public final File getFile() {
        return file;
    }

    @Override
    public void close() throws IOException {
        if (buf == null)
            throw new IOException("Already closed");
        buf = null;
    }
}
