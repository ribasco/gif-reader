package com.ibasco.image.gif.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ByteBufferInputStream extends InputStream {

    private final ByteBuffer buffer;

    public ByteBufferInputStream(byte[] buffer) {
        this(ByteBuffer.wrap(buffer));
    }

    public ByteBufferInputStream(ByteBuffer buffer) {
        this.buffer = buffer.order(ByteOrder.LITTLE_ENDIAN);
    }

    @Override
    public int read() throws IOException {
        return !this.buffer.hasRemaining() ? -1 : Byte.toUnsignedInt(this.buffer.get());
    }

    @Override
    public synchronized void mark(int readlimit) {
        buffer.mark();
    }

    @Override
    public synchronized void reset() {
        buffer.reset();
    }

    @Override
    public boolean markSupported() {
        return true;
    }
}
