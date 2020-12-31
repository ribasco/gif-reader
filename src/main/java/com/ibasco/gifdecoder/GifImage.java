package com.ibasco.gifdecoder;

import java.util.ArrayList;
import java.util.List;

public class GifImage {

    private final List<GifFrame> frames = new ArrayList<>();

    byte[] signature;

    byte[] version;

    int width;

    int height;

    boolean globalColorTableFlag;

    int colorResolution;

    boolean sortFlag;

    int backgroundColorIndex;

    int pixelAspectRatio;

    int[] globalColorTable;

    int globalColorTableSize;

    GifPlainText plainText;

    List<String> comments;

    int loopCount = 0;

    /**
     * The Comment Extension contains textual information which
     * is not part of the actual graphics in the GIF Data Stream. It is suitable
     * for including comments about the graphics, credits, descriptions or any
     * other type of non-control and non-graphic data.  The Comment Extension
     * may be ignored by the decoder, or it may be saved for later processing;
     * under no circumstances should a Comment Extension disrupt or interfere
     * with the processing of the Data Stream.
     */
    public List<String> getComments() {
        return comments;
    }

    /**
     * Indicates the number of iterations the animated GIF should be executed.
     *
     * @return The number of times the image should be looped or 0 for infinite number.
     */
    public int getLoopCount() {
        return loopCount;
    }

    /**
     * The Plain Text Extension contains textual data and the
     * parameters necessary to render that data as a graphic, in a simple form.
     * The textual data will be encoded with the 7-bit printable ASCII
     * characters.  Text data are rendered using a grid of character cells defined
     * by the parameters in the block fields. Each character is rendered
     * in an individual cell. The textual data in this block is to be rendered
     * as mono-spaced characters, one character per cell, with a best fitting
     * font and size. For further information, see the section on
     * Recommendations below.
     * <p>
     * The data characters are taken sequentially from
     * the data portion of the block and rendered within a cell, starting with
     * the upper left cell in the grid and proceeding from left to right and
     * from top to bottom. Text data is rendered until the end of data is
     * reached or the character grid is filled.  The Character Grid contains an
     * integral number of cells; in the case that the cell dimensions do not
     * allow for an integral number, fractional cells must be discarded; an
     * encoder must be careful to specify the grid dimensions accurately so that
     * this does not happen.
     * <p>
     * This block requires a Global Color Table to be
     * available; the colors used by this block reference the Global Color Table
     * in the Stream if there is one, or the Global Color Table from a previous
     * Stream, if one was saved. This block is a graphic rendering block,
     * therefore it may be modified by a Graphic Control Extension.  This block
     * is OPTIONAL; any number of them may appear in the Data Stream.
     */
    public GifPlainText getPlainText() {
        return plainText;
    }

    /**
     * This field identifies the beginning of the GIF Data
     * Stream; it is not intended to provide a unique signature for the
     * identification of the data. It is recommended that the GIF Data
     * Stream be identified externally by the application. (Refer to
     * Appendix G for on-line identification of the GIF Data Stream.)
     */
    public String getSignature() {
        return new String(signature);
    }

    /**
     * ENCODER : An encoder should use the earliest possible
     * version number that defines all the blocks used in the Data Stream.
     * When two or more Data Streams are combined, the latest of the
     * individual version numbers should be used for the resulting Data
     * Stream. DECODER : A decoder should attempt to process the data
     * stream to the best of its ability; if it encounters a version
     * number which it is not capable of processing fully, it should
     * nevertheless, attempt to process the data stream to the best of its
     * ability, perhaps after warning the user that the data may be
     * incomplete.
     */
    public String getVersion() {
        return new String(version);
    }

    /**
     * Width (in pixels) of the Logical Screen where the images will be rendered in the displaying device.
     *
     * @return The width in pixel unit
     */
    public int getWidth() {
        return width;
    }

    /**
     * Height (in pixels) of the Logical Screen where the images will be rendered in the displaying device.
     *
     * @return The height in pixel unit
     */
    public int getHeight() {
        return height;
    }

    /**
     * Flag indicating the presence of a
     * Global Color Table; if the flag is set, the Global Color Table will
     * immediately follow the Logical Screen Descriptor. This flag also
     * selects the interpretation of the Background Color Index; if the
     * flag is set, the value of the Background Color Index field should
     * be used as the table index of the background color. (This field is
     * the most significant bit of the byte.)
     */
    boolean hasGlobalColorTable() {
        return globalColorTableFlag;
    }

    /**
     * Number of bits per primary color available
     * to the original image, minus 1. This value represents the size of
     * the entire palette from which the colors in the graphic were
     * selected, not the number of colors actually used in the graphic.
     * For example, if the value in this field is 3, then the palette of
     * the original image had 4 bits per primary color available to create
     * the image.  This value should be set to indicate the richness of
     * the original palette, even if not every color from the whole
     * palette is available on the source machine.
     */
    public int getColorResolution() {
        return colorResolution;
    }

    /**
     * Indicates whether the Global Color Table is sorted.
     * If the flag is set, the Global Color Table is sorted, in order of
     * decreasing importance. Typically, the order would be decreasing
     * frequency, with most frequent color first. This assists a decoder,
     * with fewer available colors, in choosing the best subset of colors;
     * the decoder may use an initial segment of the table to render the
     * graphic.
     */
    public boolean isSorted() {
        return sortFlag;
    }

    /**
     * Index into the Global Color Table for the Background Color. The Background Color is the color used for
     * those pixels on the screen that are not covered by an image. If the
     * Global Color Table Flag is set to (zero), this field should be zero
     * and should be ignored.
     */
    public int getBackgroundColorIndex() {
        return backgroundColorIndex;
    }

    /**
     * The Background Color used for those pixels on the screen that are not covered by an image. If the
     * Global Color Table Flag is set to (zero), this field should be zero and should be ignored.
     *
     * @return The background color in int ARGB format
     */
    public int getBackgroundColor() {
        if (hasGlobalColorTable() && globalColorTableSize > 0) {
            return globalColorTable[backgroundColorIndex];
        }
        return -1;
    }

    /**
     * Factor used to compute an approximation
     * of the aspect ratio of the pixel in the original image.  If the
     * value of the field is not 0, this approximation of the aspect ratio
     * is computed based on the formula:
     * <p>
     * Aspect Ratio = (Pixel Aspect Ratio + 15) / 64
     * <p>
     * The Pixel Aspect Ratio is defined to be the quotient of the pixel's
     * width over its height.  The value range in this field allows
     * specification of the widest pixel of 4:1 to the tallest pixel of
     * 1:4 in increments of 1/64th.
     * <p>
     * Values :        0 -   No aspect ratio information is given.
     * 1..255 -   Value used in the computation.
     */
    public int getPixelAspectRatio() {
        return pixelAspectRatio;
    }

    /**
     * The global color table (in ARGB format)
     */
    int[] getGlobalColorTable() {
        return globalColorTable;
    }

    /**
     * If the Global Color Table Flag is
     * set to 1, the value in this field is used to calculate the number
     * of bytes contained in the Global Color Table. To determine that
     * actual size of the color table, raise 2 to [the value of the field
     * + 1].  Even if there is no Global Color Table specified, set this
     * field according to the above formula so that decoders can choose
     * the best graphics mode to display the stream in.  (This field is
     * made up of the 3 least significant bits of the byte.)
     */
    int getGlobalColorTableSize() {
        return globalColorTableSize;
    }

    /**
     * @return A list of all available frames in this image
     */
    public List<GifFrame> getFrames() {
        return frames;
    }

    @Override
    public String toString() {
        return String.format("%s%s (%d x %d)", getSignature(), getVersion(), getWidth(), getHeight());
    }
}
