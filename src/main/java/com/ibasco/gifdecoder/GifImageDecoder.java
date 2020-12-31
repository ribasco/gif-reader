package com.ibasco.gifdecoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Decode's LZW encoded image blocks
 *
 * @author Rafael Luis Ibasco
 * @implNote Parts of the code below have been borrowed from Dhyan Blum's GIF Decoder implementation
 */
class GifImageDecoder {

    private static final Logger log = LoggerFactory.getLogger(GifImageDecoder.class);

    private final GifCodeTable table = new GifCodeTable();

    public void decode(final GifFrame frame, byte[] data, int[] image) {
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
                System.arraycopy(prevVals, 0, prevValsAndK, 0, prevVals.length);

                // Code table contains code
                if (code < table.getNextCode()) {
                    // Output pixels
                    pixels = table.getPixels(code);
                    System.arraycopy(pixels, 0, image, outPos, pixels.length);
                    outPos += pixels.length;
                    prevValsAndK[prevVals.length] = table.getPixels(code)[0];
                } else {
                    prevValsAndK[prevVals.length] = prevVals[0]; // K
                    System.arraycopy(prevValsAndK, 0, image, outPos, prevValsAndK.length);
                    outPos += prevValsAndK.length;
                }

                // Previous indices + K
                table.addPrevious(prevValsAndK);
            }
        } catch (final ArrayIndexOutOfBoundsException ex) {
            log.error("Error", ex);
        } finally {
            table.release();
        }
    }
}
