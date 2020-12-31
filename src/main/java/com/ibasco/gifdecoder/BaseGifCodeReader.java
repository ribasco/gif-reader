package com.ibasco.gifdecoder;

abstract public class BaseGifCodeReader implements GifCodeReader {

    private final int codeSize;

    private int fromIndex;

    private int offset = 0;

    public BaseGifCodeReader(int codeSize, byte[] data) {
        this.codeSize = codeSize + 1;
        this.fromIndex = 0;
    }

    protected int getFromIndex() {
        return fromIndex;
    }

    protected void setFromIndex(int fromIndex) {
        this.fromIndex = fromIndex;
    }

    protected final int getToIndex() {
        return getFromIndex() + getCodeSize();
    }

    /**
     * Reads a single LZW encoded byte
     *
     * @return An unsigned 32-bit LZW encoded integer
     */
    @Override
    abstract public int read();

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
