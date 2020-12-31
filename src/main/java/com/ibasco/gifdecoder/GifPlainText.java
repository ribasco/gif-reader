package com.ibasco.gifdecoder;

import java.util.List;

/**
 * Plain text extension fields
 *
 * @author Rafael Luis Ibasco
 */
public class GifPlainText {

    int leftPos;

    int topPos;

    int textGridWidth;

    int textGridHeight;

    int charCellWidth;

    int charCellHeight;

    int textForegroundColorIndex;

    int textBackgroundColorIndex;

    List<String> plainTextData;

    /**
     * Column number, in pixels, of the left
     * edge of the text grid, with respect to the left edge of the Logical
     * Screen.
     */
    public int getLeftPos() {
        return leftPos;
    }

    /**
     * Row number, in pixels, of the top edge
     * of the text grid, with respect to the top edge of the Logical
     * Screen.
     */
    public int getTopPos() {
        return topPos;
    }

    /**
     * Width of the text grid in pixels.
     */
    public int getTextGridWidth() {
        return textGridWidth;
    }

    /**
     * Height of the text grid in pixels.
     */
    public int getTextGridHeight() {
        return textGridHeight;
    }

    /**
     * Width, in pixels, of each cell in the grid.
     */
    public int getCharCellWidth() {
        return charCellWidth;
    }

    /**
     * Height, in pixels, of each cell in the grid.
     */
    public int getCharCellHeight() {
        return charCellHeight;
    }

    /**
     * Index into the Global Color Table to be used to render the text foreground.
     */
    public int getTextForegroundColorIndex() {
        return textForegroundColorIndex;
    }

    /**
     * Index into the Global Color Table to be used to render the text background.
     */
    public int getTextBackgroundColorIndex() {
        return textBackgroundColorIndex;
    }

    /**
     * Sequence of sub-blocks, each of size at most
     * 255 bytes and at least 1 byte, with the size in a byte preceding
     * the data.  The end of the sequence is marked by the Block
     * Terminator.
     */
    public List<String> getPlainTextData() {
        return plainTextData;
    }
}
