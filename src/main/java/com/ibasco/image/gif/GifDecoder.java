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

package com.ibasco.image.gif;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.nio.IntBuffer;

/**
 * A class responsible for decoding image data blocks (encoded in LZW algorithm)
 *
 * <p>
 * <br />
 * Note from spec regarding color tables:
 * <blockquote>
 * Both types of color tables are optional, making it
 * possible for a Data Stream to contain numerous graphics without a color table
 * at all. For this reason, it is recommended that the decoder save the last
 * Global Color Table used until another Global Color Table is encountered. In
 * this way, a Data Stream which does not contain either a Global Color Table or
 * a Local Color Table may be processed using the last Global Color Table saved.
 * If a Global Color Table from a previous Stream is used, that table becomes the
 * Global Color Table of the present Stream. This is intended to reduce the
 * overhead incurred by color tables.
 * </blockquote>
 *
 * @author Rafael Luis Ibasco
 * @implNote Both types of color tables are optional, making it
 * possible for a Data Stream to contain numerous graphics without a color table
 * at all. For this reason, it is recommended that the decoder save the last
 * Global Color Table used until another Global Color Table is encountered. In
 * this way, a Data Stream which does not contain either a Global Color Table or
 * a Local Color Table may be processed using the last Global Color Table saved.
 * If a Global Color Table from a previous Stream is used, that table becomes the
 * Global Color Table of the present Stream. This is intended to reduce the
 * overhead incurred by color tables.
 */
//TODO: Implement on-the-fly decoding
public class GifDecoder implements Closeable {

    private static final Logger log = LoggerFactory.getLogger(GifDecoder.class);

    private static final int MAX_CODE_VALUE = 4095;

    private static final int MAX_CODE_SIZE = 12;

    private IntBuffer output;

    private int codeSize;

    private int clearCode;

    private int eoiCode;

    /**
     * First available compression code index
     */
    private int firstCodeIndex;

    private int tableCodeOffset;

    private int currentCode;

    private int previousCode;

    private int tableCodeMaxValue;

    private IntBuffer colorTable;

    private int[][] codeTable;

    private boolean initializedFrame;

    private boolean initializedData;

    private boolean transparencyFlag;

    private int transparencyIndex;

    private GifCodeReader codeReader;

    public GifDecoder() {}

    /**
     * Creates a new GIF Decoder instance for the current {@link GifFrame}.
     *
     * @param frame
     *         The {@link GifFrame} to decode
     */
    public GifDecoder(final GifFrame frame) throws IOException {
        initializeFrame(frame);
    }

    private void initializeFrame(final GifFrame frame) throws IOException {
        if (initializedFrame)
            return;

        if (frame == null)
            throw new IllegalArgumentException("Frame must not be null");
        this.codeSize = frame.codeSize;
        this.clearCode = 1 << codeSize;
        this.eoiCode = clearCode + 1;
        this.firstCodeIndex = clearCode + 2;
        this.transparencyFlag = frame.transparencyFlag;
        this.transparencyIndex = frame.transparencyIndex;
        this.codeTable = new int[MAX_CODE_VALUE + 1][1];
        this.colorTable = IntBuffer.wrap(frame.getActiveColorTable());
        frame.data = new int[frame.width * frame.height];
        this.output = IntBuffer.wrap(frame.data);

        initializedFrame = true;
    }

    /**
     * Initialize code table and parameters
     *
     * @throws IOException
     *         When an I/O error occurs
     */
    private void initializeFrameData() throws IOException {
        if (initializedData)
            return;

        log.debug("initialize(): Code Size: {}", codeSize);
        log.debug("initialize(): Clear Code: {}", clearCode);
        log.debug("initialize(): EOI Code: {}", eoiCode);
        log.debug("initialize(): First Code Index: {}", firstCodeIndex);

        //Read the first code
        this.currentCode = codeReader.read();

        //initialize code table
        reset();

        //check if the first code read is a clear code
        if (this.currentCode == clearCode) {
            //get the next code after clear code
            this.currentCode = codeReader.read();
        }

        //output first code to index stream
        outputToImage(this.currentCode);

        initializedData = true;
    }

    public void decode(final GifFrame frame, byte[] data) throws IOException {
        try {
            initializeFrame(frame);
            decode(data);
        } finally {
            close();
        }
    }

    /**
     * Decodes a block of frame data containing LZW encoded byte(s)
     *
     * @param data
     *         The byte array containing the LZW encoded bytes of the frame
     */
    public void decode(final byte[] data) throws IOException {
        try {
            codeReader = new BisGifCodeReader(codeSize, data);
            initializeFrameData();

            while (true) {
                //store previous code
                previousCode = currentCode;
                //get next code
                currentCode = codeReader.read();
                //check if we have clear code or eoi code
                if (currentCode == clearCode) {
                    //reset table and parameters
                    reset();
                    currentCode = codeReader.read();
                    outputToImage(getCodeValue(currentCode));
                    continue;
                } else if (currentCode == eoiCode) {
                    break;
                }

                //check to see if this value is in our code table
                if (exists(currentCode)) {
                    //output {CODE} to index stream
                    //let K be the first index in {CODE}
                    //add {CODE-1}+K to the code table
                    final int[] currentCodeValue = getCodeValue(currentCode);
                    //output to index stream
                    outputToImage(currentCodeValue);
                    //add to table entry
                    final int[] entry = combineToPrevious(currentCodeValue);
                    addTableEntry(entry);
                } else {
                    //let K be the first index of {CODE-1}
                    //output {CODE-1}+K to index stream
                    //add {CODE-1}+K to code table
                    final int[] previousCodeValue = getCodeValue(previousCode);
                    //copy the previous code value to the new table entry array = {CODE-1}+K
                    final int[] entry = combineToPrevious(previousCodeValue);
                    //output new table entry to index stream
                    outputToImage(entry);
                    //add new entry to code table
                    addTableEntry(entry);
                }
            }
        } finally {
            if (codeReader != null)
                codeReader.close();
        }
    }

    /**
     * (Re)Initialize code table. This can be safely called at any time
     */
    private void initializeTable() {
        colorTable.clear();
        while (colorTable.hasRemaining()) {
            int index = colorTable.position();
            //(re)initialize each color table cell to single length
            if (codeTable[index] == null || codeTable[index].length > 1)
                codeTable[index] = new int[1];
            codeTable[index][0] = colorTable.get();
        }
        //add clear code and eoicode right after the color table entries
        codeTable[clearCode] = new int[] {clearCode};
        codeTable[eoiCode] = new int[] {eoiCode};

        //Initialize the rest to null values
        //NOTE: this is REQUIRED  for us to determine later which slot has no value assigned yet
        for (int idx = eoiCode + 1; idx < codeTable.length; idx++) {
            codeTable[idx] = null;
        }

        //Apply transparency index
        if (transparencyFlag) {
            codeTable[transparencyIndex] = new int[] {0};
        }
    }

    private void reset() {
        tableCodeOffset = firstCodeIndex;
        codeReader.clearCodeSizeOffset();
        tableCodeMaxValue = (1 << codeReader.getCodeSize()) - 1;
        //initialize code table. Populate it with the initial
        // values provided by the ACTIVE color table
        initializeTable();
    }

    /**
     * Concatenates the first index of the provided code value to the previous code value entry
     *
     * @param codeValue
     *         An array of values to be combined to the previous. Only the first index of this value will be used.
     *
     * @return A new integer array containing the combined result of the operation
     */
    private int[] combineToPrevious(int[] codeValue) {
        if (codeValue == null)
            throw new IllegalArgumentException("Code value cannot be null");
        final int[] previousCodeValue = getCodeValue(this.previousCode);
        final int[] newTableEntry = new int[previousCodeValue.length + 1];
        //copy previousCodeValue to newTableEntry
        System.arraycopy(previousCodeValue, 0, newTableEntry, 0, previousCodeValue.length); //{CODE - 1}
        newTableEntry[previousCodeValue.length] = codeValue[0]; //+ K
        return newTableEntry;
    }

    /**
     * Translate the provided code value and store it to the output image buffer
     *
     * @param code
     *         The code value that will be used as a lookup key in the code table
     */
    private void outputToImage(int code) {
        outputToImage(getCodeValue(code));
    }

    private void outputToImage(int[] values) {
        if (output.remaining() < values.length) {
            log.warn("Buffer overflow: There is not enough space to store {} elements in the output buffer. Data will be trimmed (Remaining Bytes: {})", values.length, output.remaining());
            output.put(values, 0, output.remaining());
            return;
        }
        output.put(values);
    }

    private void addTableEntry(int[] entry) {
        //is new offset within bounds?
        if (tableCodeOffset > MAX_CODE_VALUE) {
            return;
        }
        //adjust code size if necessary
        if ((tableCodeOffset >= tableCodeMaxValue) && (codeReader.getCodeSize() < MAX_CODE_SIZE)) {
            codeReader.increaseCodeSizeOffset();
            tableCodeMaxValue = (1 << codeReader.getCodeSize()) - 1; // 2^codeSize - 1
        }
        codeTable[tableCodeOffset++] = entry;
    }

    /**
     * The value associated with the specified code
     *
     * @param code
     *         The code index to lookup on the table
     *
     * @return An array of color pixels associated with the code
     */
    private int[] getCodeValue(int code) {
        if (code < 0 || code > codeTable.length) {
            throw new IllegalArgumentException(String.format("Invalid code number: %d (Code Size: %d, Code Table Size: %d)", code, codeReader.getCodeSize(), codeTable.length));
        }
        return codeTable[code];
    }

    /**
     * Check if the code contains a value in our code table
     *
     * @param code
     *         The code to search
     *
     * @return {@code true} if the code has an existing entry in the table
     */
    private boolean exists(int code) {
        if (code < 0 || code > codeTable.length) {
            log.warn("exists() : Invalid code number {}", code);
            return false;
        }
        return codeTable[code] != null;
    }

    @Override
    public void close() throws IOException {
        codeReader.close();
        codeReader = null;
        initializedFrame = false;
        initializedData = false;
    }
}