package com.ibasco.gifdecoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;

/**
 * LZW code dictionary class
 *
 * @author Rafael Luis Ibasco
 */
public final class GifCodeTable {

    private static final Logger log = LoggerFactory.getLogger(GifCodeTable.class);

    private static final int MAX_CODE_VALUE = 4095;

    private static final int CODE_SIZE_LIMIT = 12;

    private int[][] table; // Maps codes to lists of colors (max size: 4096)

    private int firstCodeOffset; // Number of colors +2 for CLEAR + EOI

    private int initCodeSize; // Initial code size

    private int initCodeLimit; // First code limit

    private int codeSize; // Current code size, maximum is 12 bits

    private int nextCode; // Next available code for a new entry

    private int nextCodeLimit; // Increase codeSize when nextCode == limit

    private WeakReference<GifCodeReader> readerRef;

    private WeakReference<GifFrame> frameRef;

    public GifCodeTable() {
        this.table = new int[MAX_CODE_VALUE][1];
    }

    public void acquire(final GifFrame frame, final GifCodeReader reader) {
        if (getFrame(true) != null || getReader(true) != null)
            throw new IllegalStateException(String.format("Acquired resources have not been released yet (Frame: %s, Reader: %s)", frame, reader));

        this.table = new int[MAX_CODE_VALUE][1];
        this.readerRef = new WeakReference<>(reader);
        this.frameRef = new WeakReference<>(frame);

        //initialize properties
        initCodeSize = frame.getCodeSize() + 1;
        initCodeLimit = (1 << initCodeSize) - 1; // 2 ^ initCodeSize - 1
        firstCodeOffset = frame.getClearCode() + 2; //The first available compression code value per spec (see appendix f)

        codeSize = initCodeSize;
        nextCodeLimit = initCodeLimit;
        nextCode = firstCodeOffset;
        getReader().clearCodeSizeOffset();

        //reset/initialize properties
        //reset();

        populateColorTable(frame);
    }

    public void addPrevious(final int[] indices) {
        //min: 0, max: 4094
        if (nextCode > (MAX_CODE_VALUE - 1)) {
            //log.debug("Next code reached limit (Next code: {}, Limit: {}", nextCode, 4095);
            return;
        }

        /*
         * The output codes are of variable length, starting at <code size>+1 bits per
         * code, up to 12 bits per code. This defines a maximum code value of 4095
         * (0xFFF). Whenever the LZW code value would exceed the current code length, the
         * code length is increased by one. The packing/unpacking of these codes must then
         * be altered to reflect the new code length.
         */
        if ((nextCode >= nextCodeLimit) && (codeSize < CODE_SIZE_LIMIT)) {
            //log.debug("The Next code has reached it's limit (Next code: {}, Next code limit: {}, Code Size: {})", nextCode, nextCodeLimit, codeSize);
            codeSize++; // Max code size is 12
            getReader().increaseCodeSizeOffset();
            nextCodeLimit = (1 << codeSize) - 1; // 2^codeSize - 1
        }

        table[nextCode] = indices;

        //advance next code
        nextCode++;
    }

    public void reset() {
        if (getFrame(true) == null || getReader(true) == null)
            throw new IllegalStateException("Frame or code reader instance is null");
        codeSize = initCodeSize;
        nextCodeLimit = initCodeLimit;
        nextCode = firstCodeOffset;
        getReader().clearCodeSizeOffset();
    }

    public void release() {
        if (getReader(true) == null && getFrame(true) == null)
            throw new IllegalStateException("Resources have not been aquired");
        reset();
        this.readerRef = null;
        this.frameRef = null;
    }

    public int getNextCode() {
        return nextCode;
    }

    public int[] getPixels(int code) {
        return table[code];
    }

    private void populateColorTable(GifFrame frame) {
        int[] activeColorTable = frame.getActiveColorTable();
        int numColors = activeColorTable.length;
        final int clearCode = frame.getClearCode();
        final int eoiCode = frame.getEndOfInfoCode();

        //copy the frame's acive color table to the internal color table
        for (int colorIndex = numColors - 1; colorIndex >= 0; colorIndex--) {
            table[colorIndex][0] = activeColorTable[colorIndex]; // Translated color
            //log.debug("Code Table: Index: {} = {} (Length: {})", colorIndex, GifImageReader.toHexString(table[colorIndex][0]), table[colorIndex].length);
        } // A gap may follow with no colors assigned if numCols < CLEAR

        //add the last two special codes, per spec
        table[clearCode] = new int[] {clearCode}; // CLEAR
        table[eoiCode] = new int[] {eoiCode}; // EOI

        // Locate transparent color in code table and set to 0
        if (frame.isTransparencySupported() && (frame.getTransparencyIndex() < numColors)) {
            table[frame.getTransparencyIndex()][0] = 0;
        }
    }

    private GifCodeReader getReader() {
        return getReader(false);
    }

    private GifCodeReader getReader(boolean unchecked) {
        var reader = readerRef != null && readerRef.get() != null ? readerRef.get() : null;
        if (!unchecked && reader == null)
            throw new IllegalStateException("Reader not assigned");
        return reader;
    }

    private GifFrame getFrame() {
        return getFrame(false);
    }

    private GifFrame getFrame(boolean unchecked) {
        var frame = frameRef != null && frameRef.get() != null ? frameRef.get() : null;
        if (!unchecked && frame == null)
            throw new IllegalStateException("Frame not assigned");
        return frame;
    }
}
