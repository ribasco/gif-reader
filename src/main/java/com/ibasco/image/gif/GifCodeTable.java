package com.ibasco.image.gif;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * LZW code dictionary class
 *
 * @author Rafael Luis Ibasco
 */
public final class GifCodeTable {

    private static final Logger log = LoggerFactory.getLogger(GifCodeTable.class);

    private static final int MAX_CODE_VALUE = 4095;

    private static final int CODE_SIZE_LIMIT = 12;

    private final int[][] table; // Maps codes to lists of colors (max size: 4096)

    private int firstCodeOffset; // Number of colors +2 for CLEAR + EOI

    private int initCodeSize; // Initial code size

    private int initCodeLimit; // First code limit

    private int codeSize; // Current code size, maximum is 12 bits

    private int nextCode; // Next available code for a new entry

    private int nextCodeLimit; // Increase codeSize when nextCode == limit

    private GifCodeReader reader;

    private GifFrame frame;

    public GifCodeTable() {
        this.table = new int[MAX_CODE_VALUE + 1][1];
    }

    public void acquire(final GifFrame frame, final GifCodeReader reader) {
        if (getFrame(true) != null || getReader(true) != null)
            throw new IllegalStateException(String.format("Acquired resources have not been released yet (Frame: %s, Reader: %s)", frame, reader));
        this.reader = reader;
        this.frame = frame;

        //initialize properties
        initCodeSize = frame.getCodeSize() + 1;
        initCodeLimit = (1 << initCodeSize) - 1; // 2 ^ initCodeSize - 1
        log.debug("END OF INFO + 1 = {}, CLEAR CODE + 2 = {}", frame.getEndOfInfoCode() + 1, frame.getClearCode() + 2);
        firstCodeOffset = frame.getClearCode() + 2; //The first available compression code value per spec (see appendix f)

        //Clear entries
        clearTable();

        //reset/initialize properties
        reset();

        populateColorTable(frame);
    }

    public void addEntry(final int[] indices) {
        //min: 0, max: 4095 = 4096 entries
        if (nextCode > MAX_CODE_VALUE) {
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
            codeSize++; // Max code size is 12
            getReader().increaseCodeSizeOffset();
            nextCodeLimit = (1 << codeSize) - 1; // 2^codeSize - 1
        }
        table[nextCode++] = indices;

        //printIntArray("CODE: " + (nextCode - 1), table[nextCode - 1]);

    }

    private void printIntArray(String header, final int[] array) {
        var str = Arrays.stream(array).mapToObj(Integer::toHexString).map(String::toUpperCase).collect(Collectors.joining(", "));
        log.debug("{} = {}", header, str);
    }

    private void clearTable() {
        for (int x = 0; x < table.length; x++) {
            if (table[x].length == 1) {
                table[x][0] = 0;
            } else {
                table[x] = new int[1];
            }
        }
    }

    public void reset() {
        if (getFrame(true) == null || getReader(true) == null)
            throw new IllegalStateException("Frame or code reader instance is null");
        codeSize = initCodeSize;
        nextCodeLimit = initCodeLimit;
        nextCode = firstCodeOffset;
        getReader().clearCodeSizeOffset();

        log.debug("Reset: Code Size = {}", codeSize);
        log.debug("Reset: Next Code Limit = {}", nextCodeLimit);
        log.debug("Reset: Next Code = {}", nextCode);
        log.debug("Reset: Clear Offset = {}", getReader().getCodeSize());
    }

    public void release() {
        if (getReader(true) == null && getFrame(true) == null)
            throw new IllegalStateException("Resources have not been aquired");
        reset();
        this.reader = null;
        this.frame = null;
    }

    public int getNextCode() {
        return nextCode;
    }

    public int[] getPixels(int code) {
        return table[code];
    }

    public int[][] getTable() {
        return table;
    }

    private void populateColorTable(GifFrame frame) {
        int[] activeColorTable = frame.getActiveColorTable();
        int numColors = activeColorTable.length;
        final int clearCode = frame.getClearCode();
        final int eoiCode = frame.getEndOfInfoCode();

        //copy the frame's acive color table to the internal color table
        for (int colorIndex = numColors - 1; colorIndex >= 0; colorIndex--) {
            table[colorIndex][0] = activeColorTable[colorIndex]; // Translated color
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
        if (!unchecked && reader == null)
            throw new IllegalStateException("Reader not assigned");
        return reader;
    }

    private GifFrame getFrame() {
        return getFrame(false);
    }

    private GifFrame getFrame(boolean unchecked) {
        if (!unchecked && frame == null)
            throw new IllegalStateException("Frame not assigned");
        return frame;
    }
}
