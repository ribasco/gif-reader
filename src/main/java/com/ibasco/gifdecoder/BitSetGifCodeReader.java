package com.ibasco.gifdecoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.BitSet;

/**
 * Process LZW codes from the image data blocks. This internally makes use of the {@link BitSet} class to read through and process the data bits accordingly.
 * The number of bits processed are defined by the provided code size.
 *
 * @author Rafael Luis Ibasco
 */
public class BitSetGifCodeReader implements GifCodeReader {

    private static final Logger log = LoggerFactory.getLogger(BitSetGifCodeReader.class);

    private final BitSet codes;

    private final int codeSize;

    private int fromIndex;

    private int offset = 0;

    BitSetGifCodeReader(int codeSize, byte[] data) {
        this.codes = BitSet.valueOf(data);
        this.codeSize = codeSize + 1;
        this.fromIndex = 0;
    }

    /**
     * Reads the next LZW code available in the data stream
     *
     * @return The LZW code
     */
    @Override
    public int read() {
        int fromIndex = getFromIndex();
        int toIndex = getToIndex();
        if (fromIndex < 0 || toIndex < 0 || fromIndex > toIndex)
            throw new IndexOutOfBoundsException(String.format("Out of bounds (From: %d, To: %d, Code Size: %d, Offset: %d)", getFromIndex(), getToIndex(), getCodeSize(), getCodeSizeOffset()));
        var data = codes.get(fromIndex, toIndex).toLongArray();
        int code = data.length == 1 ? (int) data[0] : 0;
        this.fromIndex += getCodeSize();
        return code;
    }

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

    public final int getFromIndex() {
        return fromIndex;
    }

    public final int getToIndex() {
        return getFromIndex() + getCodeSize();
    }
}
