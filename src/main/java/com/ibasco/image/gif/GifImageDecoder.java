package com.ibasco.image.gif;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Decode's LZW encoded image blocks
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
 * @implNote Parts of the code below have been borrowed from Dhyan Blum's GIF Decoder implementation
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
public final class GifImageDecoder {

    private static final Logger log = LoggerFactory.getLogger(GifImageDecoder.class);

    private final GifCodeTable table = new GifCodeTable();

    private IntBuffer output;

    public GifImageDecoder() {
    }

    public void decode(final GifFrame frame, byte[] data, int[] image) throws IOException {
        final var reader = new BitInputStreamGifCodeReader(frame.getCodeSize(), data);

        //Re-initialize code table for the current frame
        table.acquire(frame, reader);

        //get clear and eoi codes
        final int clearCode = frame.getClearCode();
        final int eoiCode = frame.getEndOfInfoCode();

        // Next pixel position in the output image array
        int outPos = 0;

        //Read first code (usually its a clear code)
        //Note: Apparently not all GIF image starts with a clear code,
        //so make sure we still initialize the necessary parameters in the code table
        int code = reader.read();
        if (code == clearCode) {
            table.reset();
            code = reader.read();
        }

        //Always output the first code immediately
        // to the image/output buffer
        int[] pixels = table.getPixels(code);
        System.arraycopy(pixels, 0, image, outPos, pixels.length);
        outPos += pixels.length;

        //note (color indices = index in active color table):
        //compress: index stream -> code stream (color indices to code entries)
        //decompress: code stream -> index stream (code entries to color indices)

        try {
            while (true) {
                final int prevCode = code;
                //Get the next code from the stream
                code = reader.read();

                //check if we have a clear code
                if (code == clearCode) {
                    table.reset();

                    // After a CLEAR table, there is no previous code, we need to read  a new one
                    code = reader.read();

                    // Output pixels
                    pixels = table.getPixels(code);
                    System.arraycopy(pixels, 0, image, outPos, pixels.length);
                    outPos += pixels.length;

                    continue; // Back to the loop with a valid previous code
                }
                //break once we receive an "end of information" code.
                // This marks the end of the image stream
                else if (code == eoiCode) {
                    break;
                }

                final int[] prevVals = table.getPixels(prevCode);
                final int[] prevValsAndK = new int[prevVals.length + 1];

                //copy previous code value to prevValsAndK
                System.arraycopy(prevVals, 0, prevValsAndK, 0, prevVals.length);

                // Code table contains code
                if (code < table.getNextCode()) {
                    // Output pixels
                    pixels = table.getPixels(code);
                    //output to image
                    System.arraycopy(pixels, 0, image, outPos, pixels.length);
                    outPos += pixels.length;
                    //add to table entry
                    prevValsAndK[prevVals.length] = pixels[0];
                } else {
                    prevValsAndK[prevVals.length] = prevVals[0]; // K
                    System.arraycopy(prevValsAndK, 0, image, outPos, prevValsAndK.length);
                    outPos += prevValsAndK.length;
                }

                // Previous indices + K
                table.addEntry(prevValsAndK);
            }
        } finally {
            table.release();
            reader.close();
            //printCodeTable(table.getTable());
        }
    }

    private void printCodeTable(final int[][] codeTable) {
        for (int codeIndex = 0; codeIndex < codeTable.length; codeIndex++) {
            var valueString = Arrays.stream(codeTable[codeIndex]).mapToObj(Integer::toHexString).map(String::toUpperCase).collect(Collectors.joining(", "));
            String num = String.format("%4d", codeIndex).replace(' ', '0');
            log.debug("[CODE]: {} = {}", num, valueString);
        }
    }
}
