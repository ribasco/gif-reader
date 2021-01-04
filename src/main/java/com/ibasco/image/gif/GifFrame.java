package com.ibasco.image.gif;

import com.ibasco.image.gif.enums.DisposalMethod;

/**
 * Represents a single frame of a GIF Image
 *
 * @author Rafael Luis Ibasco
 */
public class GifFrame {

    final GifMetaData metadata;

    int[] data;

    DisposalMethod disposalMethod;

    boolean userInputFlag;

    boolean transparencyFlag;

    int delay;

    int transparencyIndex;

    //<editor-fold desc="Image Descriptor Fields">
    int leftPos;

    int topPos;

    int width;

    int height;

    boolean localColorTableFlag;

    boolean interlaceFlag;

    boolean sortFlag;

    int localColorTableSize;

    int[] localColorTable;
    //</editor-fold>

    int codeSize;

    int clearCode;

    int endOfInfoCode;

    int index;

    GifFrame(int index, GifMetaData metadata) {
        this.metadata = metadata;
        this.index = index;
    }

    /**
     * @return The frame index
     */
    public int getIndex() {
        return index;
    }

    /**
     * The active color table (Local or Global)
     *
     * @return The active color table containing an array of codes. The local color table will take priority if
     * the {@code localColorTableFlag} is set, otherwise the global color table will be used instead.
     */
    public int[] getActiveColorTable() {
        if (hasLocalColorTable()) {
            return localColorTable;
        } else {
            var image = getMetadata();
            if (image.hasGlobalColorTable()) {
                return image.getGlobalColorTable();
            }
            throw new IllegalStateException("Missing color table");
        }
    }

    /**
     * <p>
     * Indicates the way in which the graphic is to be treated after being displayed.
     * </p>
     * <br />
     * <table style="padding: 10px; border-collapse: collapse;" width="500">
     *      <tr>
     *          <th>Name</th>
     *          <th>Value</th>
     *          <th>Description</th>
     *      </tr>
     *      <tr>
     *         <td>NONE</td>
     *         <td>0</td>
     *         <td>No disposal specified. The decoder is not required to take any action.</td>
     *      </tr>
     *      <tr>
     *          <td>DO_NOT_DISPOSE</td>
     *          <td>1</td>
     *          <td>Do not dispose. The graphic is to be left in place.</td>
     *      </tr>
     *      <tr>
     *          <td>RESTORE_TO_BACKGROUND</td>
     *          <td>2</td>
     *          <td>Restore to background color. The area used by the graphic must be restored to the background color.</td>
     *      </tr>
     *      <tr>
     *          <td>RESTORE_TO_PREVIOUS</td>
     *          <td>3</td>
     *          <td>Restore to previous. The decoder is required to restore the area overwritten by the graphic with what was there prior to rendering the graphic.</td>
     *      </tr>
     * </table>
     */
    public DisposalMethod getDisposalMethod() {
        return disposalMethod;
    }

    /**
     * Indicates whether or not user input is
     * expected before continuing. If the flag is set, processing will
     * continue when user input is entered. The nature of the User input
     * is determined by the application (Carriage Return, Mouse Button Click, etc.)
     * <p>
     *
     * <pre>
     * false -   User input is not expected.
     * true -   User input is expected.
     * </pre>
     * <p>
     * When a Delay Time is used and the User Input Flag is set,
     * processing will continue when user input is received or when the
     * delay time expires, whichever occurs first.
     */
    public boolean isUserInputSupported() {
        return userInputFlag;
    }

    /**
     * Indicates whether a transparency index is given in the Transparent Index field. (This field is the least significant bit of the byte.)
     * <p>
     * <pre>
     * Values :  false - Transparent Index is not given.
     *           true -  Transparent Index is given.
     * </pre>
     */
    public boolean isTransparencySupported() {
        return transparencyFlag;
    }

    /**
     * If not 0, this field specifies the number of
     * hundredths (1/100) of a second to wait before continuing with the
     * processing of the Data Stream. The clock starts ticking immediately
     * after the graphic is rendered. This field may be used in
     * conjunction with the User Input Flag field.
     */
    public int getDelay() {
        return delay;
    }

    /**
     * The Transparency Index is such that when
     * encountered, the corresponding pixel of the display device is not
     * modified and processing goes on to the next pixel. The index is
     * present if and only if the Transparency Flag is set to 1.
     */
    public int getTransparencyIndex() {
        return transparencyIndex;
    }

    /**
     * Column number, in pixels, of the left edge of the image, with respect to the
     * left edge of the Logical Screen. Leftmost column of the Logical Screen is 0.
     */
    public int getLeftPos() {
        return leftPos;
    }

    /**
     * Row number, in pixels, of the top edge of the image with respect to the
     * top edge of the Logical Screen. Top row of the Logical Screen is 0.
     */
    public int getTopPos() {
        return topPos;
    }

    /**
     * Width of the image frame in pixels
     */
    public int getWidth() {
        return width;
    }

    /**
     * Height of the image frame in pixels
     */
    public int getHeight() {
        return height;
    }

    /**
     * Indicates the presence of a Local Color Table immediately
     * following this Image Descriptor. (This field is the most significant bit of the byte.)
     *
     * @return {@code True} - local color table present, and to follow immediately after this image descriptor. <p>
     * {@code False} - local color table is not present. Use global color table if available.
     */
    public boolean hasLocalColorTable() {
        return localColorTableFlag;
    }

    /**
     * Indicates if the image is interlaced. An image is interlaced
     * in a four-pass interlace pattern; see Appendix E for details.
     *
     * @return {@code True} - Image is interlaced
     */
    public boolean isInterlaced() {
        return interlaceFlag;
    }

    /**
     * <p>
     * Indicates whether the Local Color Table is sorted.  If the flag is set,
     * the Local Color Table is sorted, in order of decreasing importance.
     * Typically, the order would be decreasing frequency, with most frequent
     * color first. This assists a decoder, with fewer available colors, in
     * choosing the best subset of colors; the decoder may use an initial segment of the table to
     * render the graphic.
     * </p>
     *
     * @return {@code True} - Ordered by decreasing importance, most important color first.
     * <p>
     * {@code False} - Not ordered
     */
    public boolean isSorted() {
        return sortFlag;
    }

    /**
     * If the Local Color Table Flag is
     * set to 1, the value in this field is used to calculate the number
     * of bytes contained in the Local Color Table. To determine that
     * actual size of the color table, raise 2 to the value of the field
     * + 1. This value should be 0 if there is no Local Color Table
     * specified. (This field is made up of the 3 least significant bits
     * of the byte.)
     */
    public int getLocalColorTableSize() {
        return localColorTableSize;
    }

    /**
     * This block contains a color table, which is a sequence of
     * bytes representing red-green-blue color triplets. The Local Color Table
     * is used by the image that immediately follows. Its presence is marked by
     * the Local Color Table Flag being set to 1 in the Image Descriptor; if
     * present, the Local Color Table immediately follows the Image Descriptor
     * and contains a number of bytes equal to
     * 3x2^(Size of Local Color Table+1).
     * If present, this color table temporarily becomes the active color table
     * and the following image should be processed using it. This block is
     * OPTIONAL; at most one Local Color Table may be present per Image
     * Descriptor and its scope is the single image associated with the Image
     * Descriptor that precedes it.
     * <p>
     *
     * @apiNote Required Version:  87a.
     */
    public int[] getLocalColorTable() {
        return localColorTable;
    }

    /**
     * This byte determines the initial number of bits
     * used for LZW codes in the image data, as
     * described in Appendix F.
     *
     * @return The code value
     */
    public int getCodeSize() {
        return codeSize;
    }

    /**
     * A special Clear code is defined which resets all compression/decompression
     * parameters and tables to a start-up state. The value of this code is 2**<code
     * size>. For example if the code size indicated was 4 (image was 4 bits/pixel)
     * the Clear code value would be 16 (10000 binary). The Clear code can appear at
     * any point in the image data stream and therefore requires the LZW algorithm to
     * process succeeding codes as if a new data stream was starting. Encoders should
     * output a Clear code as the first code of each image data stream.
     *
     * @return The code value
     */
    public int getClearCode() {
        return clearCode;
    }

    /**
     * An End of Information code is defined that explicitly indicates	the
     * end  of	the image data stream.	LZW processing terminates when this
     * code is encountered.  It must be the last code output by the encoder
     * for an image.  The value of this code is <Clear code>+1.
     *
     * @return The code value
     */
    public int getEndOfInfoCode() {
        return endOfInfoCode;
    }

    /**
     * @return The decoded image data (ARGB format)
     */
    public int[] getData() {
        return data;
    }

    /**
     * @return The image metadata
     */
    public GifMetaData getMetadata() {
        return metadata;
    }
}
