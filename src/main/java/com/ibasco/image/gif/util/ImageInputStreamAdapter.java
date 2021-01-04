package com.ibasco.image.gif.util;

import javax.imageio.stream.ImageInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public class ImageInputStreamAdapter extends InputStream {

    private final ImageInputStream iis;

    public ImageInputStreamAdapter(ImageInputStream iis) {
        this.iis = iis;
    }

    @Override
    public int read() throws IOException {
        try {
            return (iis.getStreamPosition() < iis.length()) ? iis.readUnsignedByte() : -1;
        } catch (EOFException e) {
            return -1;
        }
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        try {
            return iis.read(b, off, len);
        } catch (EOFException e) {
            return -1;
        }
    }

    @Override
    public byte[] readAllBytes() throws IOException {
        byte[] tmp = new byte[(int) (iis.length() - iis.getStreamPosition())];
        iis.readFully(tmp);
        return tmp;
    }

    @Override
    public synchronized void mark(int readlimit) {
        iis.mark();
    }

    @Override
    public synchronized void reset() throws IOException {
        iis.reset();
    }
}
