package com.ibasco.gifdecoder;

import org.apache.commons.compress.utils.BitInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteOrder;

public class BitInputStreamGifCodeReader extends BaseGifCodeReader {

    private static final Logger log = LoggerFactory.getLogger(BitInputStreamGifCodeReader.class);

    private final BitInputStream bis;

    public BitInputStreamGifCodeReader(int codeSize, byte[] data) {
        super(codeSize, data);
        this.bis = new BitInputStream(new ByteArrayInputStream(data), ByteOrder.nativeOrder());
    }

    @Override
    public int read() {
        try {
            return (int) bis.readBits(getCodeSize());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
