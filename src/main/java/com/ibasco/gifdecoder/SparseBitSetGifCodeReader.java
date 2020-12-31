package com.ibasco.gifdecoder;

import com.zaxxer.sparsebits.SparseBitSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SparseBitSetGifCodeReader extends BaseGifCodeReader {

    private static final Logger log = LoggerFactory.getLogger(SparseBitSetGifCodeReader.class);

    private final SparseBitSet codes;

    public SparseBitSetGifCodeReader(int codeSize, byte[] data) {
        super(codeSize, data);
        this.codes = toBitSet(data);
    }

    @Override
    public int read() {
        int fromIndex = getFromIndex();
        int toIndex = getToIndex();
        if (fromIndex < 0 || toIndex < 0 || fromIndex > toIndex)
            throw new IndexOutOfBoundsException(String.format("Out of bounds (From: %d, To: %d, Code Size: %d, Offset: %d)", getFromIndex(), getToIndex(), getCodeSize(), getCodeSizeOffset()));
        int code = 0;
        int bitIndex = getCodeSize() - 1;
        for (int i = getToIndex() - 1; i >= getFromIndex(); i--) {
            int bitValue = codes.get(i) ? 1 : 0;
            code |= bitValue << bitIndex--;
        }
        setFromIndex(getFromIndex() + getCodeSize());
        return code;
    }

    public static SparseBitSet toBitSet(byte[] bytes) {
        SparseBitSet sparseBitSet = new SparseBitSet(bytes.length * 8);
        int j = 0;
        for (byte b : bytes) {
            for (int mask = 0x01; mask != 0x100; mask <<= 1) {
                if ((b & mask) != 0)
                    sparseBitSet.set(j);
                j++;
            }
        }
        return sparseBitSet;
    }
}
