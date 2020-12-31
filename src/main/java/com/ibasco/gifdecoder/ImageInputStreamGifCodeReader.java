package com.ibasco.gifdecoder;

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteOrder;

public class ImageInputStreamGifCodeReader implements GifCodeReader {

    private final ImageInputStream is;

    private final int codeSize;

    private int offset = 0;

    ImageInputStreamGifCodeReader(int codeSize, byte[] data) {
        this.is = new MemoryCacheImageInputStream(new ByteArrayInputStream(data));
        this.is.setByteOrder(ByteOrder.nativeOrder());
        this.codeSize = codeSize;
    }

    @Override
    public int read() {
        try {
            return (int) is.readBits(getCodeSize());
        } catch (EOFException e) {
            return 0;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public int getCodeSize() {
        return this.codeSize + offset;
    }

    @Override
    public int getCodeSizeOffset() {
        return offset;
    }

    @Override
    public void increaseCodeSizeOffset() {
        this.offset++;
    }

    @Override
    public void clearCodeSizeOffset() {
        this.offset = 0;
    }
}
