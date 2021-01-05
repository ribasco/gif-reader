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

import com.ibasco.image.gif.enums.Block;
import com.ibasco.image.gif.enums.BlockIdentifier;
import com.ibasco.image.gif.enums.DisposalMethod;
import com.ibasco.image.gif.enums.ExtensionBlock;
import com.ibasco.image.gif.exceptions.InvalidSignatureException;
import com.ibasco.image.gif.exceptions.UnsupportedBlockException;
import com.ibasco.image.gif.util.ImageOps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.BiConsumer;

/**
 * A pure java implementation of the GIF89a Specification
 * <br />
 * <br />
 * <p><strong>Example 01:</strong></p>
 * <pre>
 * var file = new File("/home/user/example.gif");
 * try (var reader = new GifImageReader(file)) {
 *      while (reader.hasRemaining()) {
 *          GifFrame frame = reader.read();
 *          System.out.printf("Index: %d, Frame: %d x %d", frame.getIndex(), frame.getWidth(), frame.getHeight());
 *      }
 * }
 * </pre>
 *
 * <p><strong>Example 02:</strong></p>
 * <pre>
 * var file = new File("/home/user/example.gif");
 * try (var reader = new GifImageReader(file)) {
 *      GifFrame frame;
 *      while ((frame  = reader.read()) != null) {
 *          System.out.printf("Index: %d, Frame: %d x %d", frame.getIndex(), frame.getWidth(), frame.getHeight());
 *      }
 * }
 * </pre>
 *
 * @author Rafael Luis Ibasco
 * @see <a href="https://www.w3.org/Graphics/GIF/spec-gif89a.txt">GIF89a Specification</a>
 */
@SuppressWarnings("DuplicatedCode")
public final class GifImageReader implements AutoCloseable {

    //A simple filter that tells the processor to not skip the data blocks
    public static final BlockFilter DO_NOT_SKIP = (ident, data) -> false;

    public static final BlockFilter SKIP_ALL = (ident, data) -> true;

    /*
     * <p>Types of blocks</p>
     * <ol>
     * <li>
     * <strong>Control (0x80-0xF9 (128-249)):</strong> Control blocks, such as the Header, the Logical Screen
     * Descriptor, the Graphic Control Extension and the Trailer, contain information
     * used to control the process of the Data Stream or information  used in setting
     * hardware parameters
     * </li>
     * <li>
     * <strong>Graphic-Rendering (0x00-0x7F (0-127)):</strong> Graphic-Rendering blocks such as the Image Descriptor and the Plain Text Extension
     * contain information and data used to render a graphic on the display device. Special Purpose blocks such as the Comment Extension and
     * the Application Extension are neither used to control the process of the Data
     * Stream nor do they contain information or data used to render a graphic on the
     * display device. With the exception of the Logical Screen Descriptor and the
     * Global Color Table, whose scope is the entire Data Stream, all other Control
     * blocks have a limited scope, restricted to the Graphic-Rendering block that
     * follows them
     * </li>
     * <li>
     * <strong>Special Purpose (0xFA-0xFF (250-255)):</strong>
     * The labels used to identify labeled blocks fall
     * into three ranges : 0x00-0x7F (0-127) are the Graphic Rendering blocks,
     * excluding the Trailer (0x3B); 0x80-0xF9 (128-249) are the Control blocks;
     * 0xFA-0xFF (250-255) are the Special Purpose blocks.
     * </li>
     * </ol>
     */

    //<editor-fold desc="Constants">

    private static final Logger log = LoggerFactory.getLogger(GifImageReader.class);

    /**
     * A fixed sequence of bytes representing the GIF header signature
     */
    private static final byte[] HEADER_SIGNATURE = new byte[] {0x47, 0x49, 0x46};

    /**
     * A fixed sequence of bytes representing the 87a version of the GIF specification
     */
    private static final byte[] HEADER_VERSION_87A = new byte[] {0x38, 0x37, 0x61};

    /**
     * A fixed sequence of bytes representing the 89a version of the GIF specification
     */
    private static final byte[] HEADER_VERSION_89A = new byte[] {0x38, 0x39, 0x61};

    //Default byte order to use
    private static final ByteOrder BYTE_ORDER = ByteOrder.LITTLE_ENDIAN;

    //total number of bytes required to read the image descriptor size fields
    private static final int LENGTH_IMAGE_DESC_DIMENSIONS = 8;

    //block size of the graphic control extension block (excluding size and terminator bytes)
    private static final int LENGTH_GRAPHIC_BLOCK = 4;

    //number of bytes to read the logical screen descriptor's dimensions (2 bytes each)
    private static final int LENGTH_LOGICAL_SCREEN_DIMENSIONS = 4;

    //number of bytes to skip one plain text block
    private static final int LENGTH_PLAIN_TEXT_BLOCK = 12;

    //number of bytes for a block terminator
    private static final int LENGTH_TERIMINATOR = 1;

    //number of bytes for the data block size field
    private static final int LENGTH_BLOCK_SIZE = 1;

    //number of bytes for the application extension identifier field
    private static final int LENGTH_APP_IDENTIFIER = 8;

    //number of bytes for the application extension auth code field
    private static final int LENGTH_APP_AUTHCODE = 3;

    //number of bytes for the logical screen descriptor
    private static final int LENGTH_LOGICAL_SCREEN_DESC = 7;

    //<editor-fold desc="Private Members">
    private final ImageInputStream is;
    //</editor-fold>

    private GifMetaData metadata;

    private GifFrame currentFrame;

    private GifFrame lastFrame;

    private int frameIndex = 0;

    private boolean closed;

    private boolean initialized;

    //take note of the last image descriptor offset that was processed,
    private long lastImageDescriptorOffset;

    private long lastLogicalScreenDescriptorOffset;

    private BlockFilter filter = DO_NOT_SKIP;

    /**
     * Creates a new reader instance based on the source image {@link File} provided.
     *
     * @param imageFile
     *         The source image file
     *
     * @throws IOException
     *         When an I/O error occurs
     * @see #GifImageReader(ImageInputStream)
     */
    public GifImageReader(File imageFile) throws IOException {
        this(ImageIO.createImageInputStream(imageFile));
    }
    //</editor-fold>

    /**
     * Creates a new reader instance based on the {@link InputStream} provided.
     *
     * @param is
     *         The source {@link InputStream}
     *
     * @throws IOException
     *         When an I/O error occurs
     * @see #GifImageReader(ImageInputStream)
     */
    public GifImageReader(InputStream is) throws IOException {
        this(ImageIO.createImageInputStream(is));
    }

    /**
     * <p>
     * Creates a new reader instance based on the {@link ImageInputStream} provided. To start processing the image frames, please use {@link #read()} method.
     * </p>
     *
     * @param is
     *         The {@link ImageInputStream} contianing the GIF image data to be read and processed
     *
     * @throws IOException
     *         When an I/O error occurs
     */
    public GifImageReader(ImageInputStream is) throws IOException {
        this.is = is;
        this.metadata = initializeImage(is);
        this.metadata.totalFrames = scanFrameSize(is);
    }

    private static boolean readGlobalColorTableFlag(byte packedByte) {
        return (packedByte & 0b10000000) >>> 7 == 1;
    }

    private static int readGlobalColorTableSize(byte packedByte) {
        final int globColTblSizePower = (packedByte & 7) + 1; // Bits 0-2
        return 1 << globColTblSizePower;
    }

    private static boolean readLocalColorTableFlag(byte packedByte) {
        return ((packedByte & 0b10000000) >>> 7) == 1;
    }

    private static int readLocalColorTableSize(byte packedByte) {
        final int localColorTablePower = (packedByte & 0b00000111) + 1;
        return 1 << localColorTablePower; //2 ^ (N + 1) as per spec
    }

    /**
     * Reads a byte from the data stream and checks if the size value is empty.
     *
     * @param is
     *         The {@link ImageInputStream} containing the data to be processed
     *
     * @throws IOException
     *         If the size is less than or equals to 0. Or end of file has been reached.
     */
    private static void checkBlockSize(ImageInputStream is) throws IOException {
        int size = is.readUnsignedByte();
        if (size <= 0)
            throw new IOException("Empty block size");
        is.mark();
        int skipped = is.skipBytes(size);
        if (skipped != size)
            throw new IOException(String.format("The number of remaining bytes is not equals to the expected block size (Remaining: %d, Expected: %d)", skipped, size));
        is.reset();
    }

    /**
     * Advances/Skips all data blocks of variable length, starting from the current position of the data stream.
     *
     * @param is
     *         The {@link ImageInputStream} The {@link ImageInputStream} to read data from
     *
     * @return The total number of bytes that were skipped (inclusive of the zero-length terminator byte)
     *
     * @throws IOException
     *         When an I/O error occurs
     */
    private static int skipDataBlocks(final ImageInputStream is) throws IOException {
        int totalSkipped = 0;
        int blockSize; //Note from spec: This field contains the fixed value 11.
        while ((blockSize = is.readUnsignedByte()) > 0) {
            totalSkipped += is.skipBytes(blockSize);
        }
        return totalSkipped;
    }

    /**
     * <p>
     * Scans for all available data blocks starting from the current position of the buffer and computes it's total size (in bytes).
     * </p>
     *
     * @param is
     *         The {@link ImageInputStream} to scan
     *
     * @return The total number of bytes read or 0 if no blocks were read.
     *
     * @throws IOException
     *         When an I/O error occurs (usually when the end of file has been reached)
     * @implNote The original position of the buffer (prior to calling this method) will be restored after this method completes
     */
    private static int computeBlockSize(ImageInputStream is) throws IOException {
        int totalSize = 0;
        try {
            is.mark();
            int blockSize; //note: per spec, block size does not take into account the size byte
            while ((blockSize = is.readUnsignedByte()) > 0) {
                totalSize += is.skipBytes(blockSize);
            }
        } finally {
            is.reset();
        }
        return totalSize;
    }

    /**
     * Read and process available color table from the data stream and store them to the specified int array.
     *
     * @param is
     *         The {@link ImageInputStream} instance to read the data from
     * @param colors
     *         A pre-allocated integer buffer whose length determines the number of colors to be processed.
     *         Each color consists of 3 bytes (red, green, blue) and will be converted into an ARGB integer.
     *
     * @implNote The alpha property will always be 255 (opaque)
     */
    private static void readColorTable(ImageInputStream is, final int[] colors) throws IOException {
        if (colors == null || colors.length == 0)
            throw new IllegalArgumentException("The destination buffer is null or empty");
        for (int index = 0; index < colors.length; index++) {
            final int r = Byte.toUnsignedInt(is.readByte()); //red
            final int g = Byte.toUnsignedInt(is.readByte()); //green
            final int b = Byte.toUnsignedInt(is.readByte()); //blue
            colors[index] = toArgb(r, g, b);
        }
    }

    /**
     * Converts the individual R-G-B values to a single signed integer value in ARGB format.
     * Alpha/Opacity value is applied at 255 (Opaque) by default.
     *
     * @param red
     *         The red component value (0 - 255)
     * @param green
     *         The green component value (0 - 255)
     * @param blue
     *         The blue component value (0 - 255)
     *
     * @return A signed 32-bit ARGB value
     */
    private static int toArgb(int red, int green, int blue) {
        return toArgb(red, green, blue, 255);
    }

    /**
     * Converts the individual R-G-B values to a single signed integer value in ARGB format.
     * Alpha/Opacity value is applied at 255 (Opaque) by default.
     *
     * @param red
     *         The red component value (0 - 255)
     * @param green
     *         The green component value (0 - 255)
     * @param blue
     *         The blue component value (0 - 255)
     * @param alpha
     *         The alpha component value (0 - 255)
     *
     * @return A signed 32-bit ARGB value
     */
    public static int toArgb(int red, int green, int blue, int alpha) {
        return ((alpha & 0xff) << 24) | ((red & 0xff) << 16) | ((green & 0xff) << 8) | (blue & 0xff);
    }

    /**
     * Breaks down a 32-bit ARGB integer into a 4-byte array contianing ARGB color channels
     *
     * @param argb
     *         The 32-bit ARGB integer to convert
     *
     * @return A 4-byte array containing the ARGB colors. Color values range from 0 to 255.
     */
    public static int[] fromArgb(int argb) {
        int[] ar = new int[4];
        ar[0] = (argb >> 24) & 0xff; //alpha
        ar[1] = (argb >> 16) & 0xff; //red
        ar[2] = (argb >> 8) & 0xff; //green
        ar[3] = argb & 0xff; //blue
        return ar;
    }

    /**
     * Convert byte array to hex string
     *
     * @param data
     *         The byte array to convert
     *
     * @return A string of hexadecimal codes
     */
    public static String toHexString(byte... data) {
        StringBuilder sb = new StringBuilder(data.length * 2);
        for (byte b : data) {
            sb.append(String.format("%02x", b).toUpperCase());
            sb.append(" ");
        }
        return sb.toString();
    }

    public static String toHexString(int... data) {
        StringBuilder sb = new StringBuilder(data.length * 2);
        for (int b : data) {
            sb.append(String.format("%02x", b).toUpperCase());
            sb.append(" ");
        }
        return sb.toString();
    }

    private static void printDebugHeader(String format, Object... params) {
        log.debug("==========================================================");
        log.debug(format.toUpperCase(), params);
        log.debug("==========================================================");
    }

    /**
     * Creates a new Image instance and reads the header fields (signature/version, logical screen descriptor and/or global color table)
     *
     * @param is
     *         The {@link ImageInputStream} containing the data to be processed
     *
     * @return The new {@link GifMetaData} instance
     *
     * @throws IOException
     *         When an I/O error occurs
     */
    private GifMetaData initializeImage(ImageInputStream is) throws IOException {
        is.setByteOrder(BYTE_ORDER);
        //Create a new  instance
        var metadata = new GifMetaData();
        //process all headers
        readImageHeaderFields(is, metadata, DO_NOT_SKIP);
        initialized = true;
        return metadata;
    }

    private void checkInit() {
        if (!initialized)
            throw new IllegalStateException("Reader not initialized");
    }

    /**
     * Checks if there are available frames left to process
     *
     * @return {@code true} if there are more blocks available to be processed, {@code false} if there are no more blocks available or we have reached either {@link Block#TRAILER} or end-of-file.
     *
     * @apiNote  Calling this method does not guarantee that the next read operation will be successful.
     * It simply checks if the next byte is a valid block identifier.
     */
    public boolean hasRemaining() {
        if (metadata == null)
            return false;
        return frameIndex < metadata.totalFrames;
    }

    /**
     * The block filter currently being used by the reader
     *
     * @return The current {@link BlockFilter} used by the reader
     */
    public BlockFilter getFilter() {
        return filter;
    }

    /**
     * The block filter to be used by the reader. If not present, the default filter is {@link #DO_NOT_SKIP}
     *
     * @param filter
     *         The {@link BlockFilter} callback function
     */
    public void setFilter(BlockFilter filter) {
        this.filter = filter;
    }

    /**
     * Reads a single GIF frame from the data stream
     *
     * @return A decoded {@link GifFrame} instance.
     *
     * @throws IOException
     *         When an I/O error occurs
     */
    public GifFrame read() throws IOException {
        checkInit();
        final BlockFilter filter = this.filter == null ? DO_NOT_SKIP : this.filter;
        Block block;
        //scan the next image descriptor block
        while (!Block.TRAILER.equals(block = readBlock(is, metadata, filter))) {
            //image frame usually starts with a descriptor
            if (Block.IMAGE_DESCRIPTOR.equals(block)) {
                var lastFrame = getLastFrame();
                if (lastFrame != null) {
                    return lastFrame;
                }
            }
        }
        if (!closed)
            reset();
        return null;
    }

    /**
     * @return Returns the metadata whose scope covers the entire data stream
     */
    public final GifMetaData getMetadata() {
        return metadata;
    }

    /**
     * Scans through the entire stream and count the total number of frames available for the current image. This is a shortcut for {@link #getMetadata()}.getTotalFrames()
     *
     * @return The total number of frames available in the current image or -1 if unable to get count
     */
    public int getTotalFrames() {
        try {
            if (metadata == null || is == null)
                return -1;
            if (metadata.totalFrames == -1)
                metadata.totalFrames = scanFrameSize(is);
            return metadata.totalFrames;
        } catch (IOException e) {
            log.debug("Error while trying to compute frame size", e);
            return -1;
        }
    }

    /**
     * Process all image header fields (signature, logical screen descriptor and global color table)
     *
     * @param is
     *         The {@link ImageInputStream} to read data from
     * @param metadata
     *         The {@link GifMetaData} instance where the processed data will be written to
     *
     * @throws IOException
     *         When an I/O error occurs (e.g. Invalid gif format)
     */
    private void readImageHeaderFields(final ImageInputStream is, final GifMetaData metadata, BlockFilter filter) throws IOException {
        printDebugHeader("START: IMAGE HEADER");
        //Process signature and version
        readSignature(is, metadata); //unskippable
        //process logical screen descriptor
        readLogicalScreenDescriptor(is, metadata, filter);
        //process global color table (if applicable)
        readGlobalColorTable(is, metadata, filter);
        printDebugHeader("END: IMAGE HEADER");
    }

    /**
     * The Header identifies the GIF Data Stream in context. The
     * Signature field marks the beginning of the Data Stream, and the Version
     * field identifies the set of capabilities required of a decoder to fully
     * process the Data Stream.  This block is REQUIRED; exactly one Header must
     * be present per Data Stream.
     *
     * @param is
     *         The {@link ImageInputStream} instance to read the data from
     * @param metadata
     *         THe {@link GifMetaData} instance where the processed data will be written to
     *
     * @throws InvalidSignatureException
     *         If the image signature is invalid
     * @throws IOException
     *         When an I/O error occurs (eof reached etc)
     * @apiNote Required Version not applicable. This block is not subject to a
     * version number. This block must appear at the beginning of every Data
     * Stream.
     */
    private void readSignature(ImageInputStream is, GifMetaData metadata) throws IOException {
        byte[] signature = new byte[3];
        byte[] version = new byte[3];
        is.read(signature);
        is.read(version);

        if (!Arrays.equals(signature, HEADER_SIGNATURE))
            throw new InvalidSignatureException(String.format("Invalid GIF signature (Expected: %s, Actual: %s)", toHexString(HEADER_SIGNATURE), toHexString(signature)));

        if (!Arrays.equals(version, HEADER_VERSION_87A) && !Arrays.equals(version, HEADER_VERSION_89A))
            throw new InvalidSignatureException("Invalid/Unsupported GIF version: " + new String(version));

        if (metadata != null) {
            metadata.signature = signature;
            metadata.version = version;
            log.debug("Signature/Version: {}{}", metadata.getSignature(), metadata.getVersion());
        }
    }

    /**
     * <p>
     * The Logical Screen Descriptor contains the parameters
     * necessary to define the area of the display device within which the
     * images will be rendered.  The coordinates in this block are given with
     * respect to the top-left corner of the virtual screen; they do not
     * necessarily refer to absolute coordinates on the display device.  This
     * implies that they could refer to window coordinates in a window-based
     * environment or printer coordinates when a printer is used.
     * </p>
     * <p>
     * This block is <strong>REQUIRED</strong>; exactly one Logical Screen Descriptor must be
     * present per Data Stream.
     *
     * @param is
     *         The {@link ImageInputStream} instance to read the data from
     * @param image
     *         The {@link GifMetaData} instance where the processed data will be written to
     *
     * @throws IOException
     *         When an I/O error occurs
     * @apiNote Required Version not applicable. This block is not subject to a version number.
     * This block must appear immediately after the Header.
     */
    private void readLogicalScreenDescriptor(final ImageInputStream is, final GifMetaData image, BlockFilter skip) throws IOException {
        lastLogicalScreenDescriptorOffset = is.getStreamPosition();
        if (skip.filter(Block.LOGICAL_SCREEN_DESCRIPTOR)) {
            is.skipBytes(LENGTH_LOGICAL_SCREEN_DESC);
            return;
        }

        image.width = is.readShort();
        image.height = is.readShort();

        //Process packed-byte (using ImagStreamInput api)
        byte packedFields = is.readByte();
        image.globalColorTableFlag = (packedFields & 0b10000000) >>> 7 == 1;
        final int colResPower = ((packedFields & 0b01110000) >>> 4) + 1;
        image.colorResolution = 1 << colResPower;
        image.sortFlag = (packedFields & 0b00001000) >>> 3 == 1;
        final int globColTblSizePower = (packedFields & 7) + 1; // Bits 0-2
        image.globalColorTableSize = 1 << globColTblSizePower; // 2^(N+1), see spec
        image.backgroundColorIndex = is.readUnsignedByte();
        image.pixelAspectRatio = is.readUnsignedByte();

        log.debug("Width: {}", image.width);
        log.debug("Height: {}", image.height);
        log.debug("Has global color table: {}", image.globalColorTableFlag);
        log.debug("Color resolution: {}", image.colorResolution);
        log.debug("Sort Flag: {}", image.sortFlag);
        log.debug("Global Color Table Size: {}", image.globalColorTableSize);
        log.debug("Background color index: {}", image.backgroundColorIndex);
        log.debug("Pixel aspect ratio: {}", image.pixelAspectRatio);
    }

    /**
     * Process all supported data blocks (extension, image descriptors etc) available in the data stream
     *
     * @param is
     *         The {@link ImageInputStream} instance to read and process the data from
     * @param image
     *         The {@link GifMetaData} instance where the processed data will be written to
     *
     * @throws IOException
     *         When an I/O error occurs during data processing
     */
    private void readBlocks(ImageInputStream is, GifMetaData image) throws IOException {
        //Process the remaining bytes
        Block block;
        while ((block = readBlock(is, image, null)) != null) {
            if (Block.TRAILER.equals(block))
                break;
        }
    }

    /**
     * Check if the next identifier is a valid block.
     *
     * @param is
     *         The {@link ImageInputStream} to check
     *
     * @return {@code true} if the data stream contains a valid block that is available for processing,
     * {@code false} if input stream is null/there are no more valid blocks to be processed/if we have reached {@link Block#TRAILER}.
     *
     * @throws IOException
     *         When an I/O error occurs
     */
    private boolean hasNextBlock(ImageInputStream is) throws IOException {
        if (is == null)
            return false;
        try {
            is.mark();
            int block = is.readUnsignedByte();
            if (Block.TRAILER.getCodeInt() == block)
                return false;
            return Block.isValid(block);
        } catch (EOFException e) {
            return false;
        } finally {
            is.reset();
        }
    }

    /**
     * Attempts to retrieve the current {@link GifFrame}. If the frame is null, a new instance will be created
     *
     * @param image
     *         The {@link GifMetaData} instance associated with the frame
     * @param skip
     *         {@code True} if we should skip initialization completely
     *
     * @return The {@link GifFrame} instance or null if not available
     */
    private GifFrame getOrInitializeFrame(GifMetaData image, boolean skip) {
        GifFrame frame = null;
        if (!skip) {
            frame = getCurrentFrame();
            if (frame == null)
                frame = beginImageFrame(image);
        }
        return frame;
    }

    /**
     * Process a single data block from the data stream (e.g. Extension Blocks, Image Descriptors, Trailer Blocks)
     *
     * @param is
     *         The {@link ImageInputStream} instance to read and process the data from
     * @param metadata
     *         The {@link GifMetaData} instance where the processed data will be written to
     *
     * @return The block code that has been processed successfully or {@code null} if we have reached the end of file
     *
     * @throws UnsupportedBlockException
     *         When the current block identifier is not supported/handled
     * @throws IOException
     *         When an I/O error occurs
     */
    private Block readBlock(final ImageInputStream is, final GifMetaData metadata, BlockFilter filter) throws IOException {
        //log.debug("readBlock() : ({}) Scanning for blocks...", is.getStreamPosition());
        try {
            var block = Block.get(is.readUnsignedByte());
            if (filter == null) {
                filter = DO_NOT_SKIP;
                log.debug("readBlock() : Found block (0x{} - {} - {})", toHexString(block.getCodeByte()), block.getName(), block.getCategory().name());
            }
            switch (block) {
                //Extension blocks
                case EXTENSION: {
                    //log.debug("readBlock() : Found an Extension Block (0x{} - {})", toHexString(block.getCodeByte()), block.getCategory().name());
                    readExtensionBlock(is, metadata, filter);
                    break;
                }
                //Image descriptor (process the image frames)
                case IMAGE_DESCRIPTOR: {
                    //log.debug("readBlock() : Found an Image Descriptor Block (0x{} - {})", toHexString(block.getCodeByte()), block.getCategory().name());
                    var frame = getOrInitializeFrame(metadata, filter.filter(Block.INITIALIZE));
                    //read image descriptor block
                    readImageDescriptorBlock(is, frame, filter.filter(block));
                    //(OPTIONAL) If local color table flag is set, process it
                    readLocalColorTable(is, frame, filter);
                    //Read image data
                    readImageDataBlocks(is, frame, filter);
                    //Reset current frame
                    endImageFrame();
                    break;
                }
                //Application Trailer (End of stream)
                case TRAILER: {
                    printDebugHeader("END OF IMAGE PROCESSING (Total frames: {}, 0x{} - {})", frameIndex, toHexString(block.getCodeByte()), block.getCategory().name());
                    if (getCurrentFrame() != null)
                        endImageFrame();
                    break;
                }
                //Other
                default: {
                    throw new IOException("Unrecognized block identifier: 0x" + toHexString(block.getCodeByte()));
                }
            }
            return block;
        } catch (EOFException e) {
            return null;
        }
    }

    //<editor-fold desc="Utility Methods">

    /**
     * Reads and processes a single supported extension blocks from the current data stream
     *
     * @param is
     *         The {@link ImageInputStream} to process and read data from
     *
     * @throws IOException
     *         When an I/O related error occurs
     */
    private void readExtensionBlock(ImageInputStream is, GifMetaData image, BlockFilter skip) throws IOException {
        var extBlock = ExtensionBlock.get(is.readUnsignedByte());
        boolean skipValue = skip.filter(extBlock);
        if (!skipValue) {
            log.debug("readExtensionBlock() : Found extension block ({} - {})", toHexString(extBlock.getCodeByte()), extBlock.getName());
        }
        switch (extBlock) {
            //Graphics control extension
            case GRAPHICS: {
                //log.debug("readExtensionBlocks() : Found graphics control extension block");
                var frame = getOrInitializeFrame(image, skipValue);
                readGraphicsControlExtensionBlock(is, frame, skipValue);
                break;
            }
            //Comment extension
            case COMMENT: {
                //log.debug("readExtensionBlocks() : Found comment extension block");
                readCommentExtensionBlock(is, image, skipValue);
                break;
            }
            //Plain text extension
            case PLAINTEXT: {
                //log.debug("readExtensionBlocks() : Found plain text extension block");
                readPlainTextExtensionBlock(is, image, skipValue);
                break;
            }
            //Application extension
            case APPLICATION: {
                //log.debug("readExtensionBlocks() : Found application extension block");
                readApplicationExtensionBlock(is, image, skipValue);
                break;
            }
            default: {
                //try skip unwanted/unsupported extension blocks
                if (skipValue)
                    log.debug("Found unsupported extension blocks (Skipped {} bytes)", skipDataBlocks(is));
                else
                    throw new UnsupportedBlockException("Unrecognized extension block");
            }
        }
    }

    /**
     * The image data for a table based image consists of a
     * sequence of sub-blocks, of size at most 255 bytes each, containing an
     * index into the active color table, for each pixel in the image.  Pixel
     * indices are in order of left to right and from top to bottom.  Each index
     * must be within the range of the size of the active color table, starting
     * at 0. The sequence of indices is encoded using the LZW Algorithm with
     * variable-length code, as described in Appendix F
     * <p>
     *
     * @param is
     *         The {@link ImageInputStream} containing the data to be processed
     * @param frame
     *         The {@link GifFrame} where the processed data would be written to
     *
     * @throws IOException
     *         When an I/O error occurs
     * @apiNote Required Version.  87a.
     */
    private void readImageDataBlocks(ImageInputStream is, GifFrame frame, BlockFilter skip) throws IOException {
        if (skip.filter(Block.IMAGE_DATA)) {
            //Read image data
            is.skipBytes(1); //image size
            skipDataBlocks(is); //skip image datta
            return;
        }

        printDebugHeader("START: Image Block");

        //Note: This byte determines the initial number of bits used for
        //LZW codes in the image data, as described in Appendix F.
        frame.codeSize = is.readUnsignedByte(); //LZW Minimum Code Size (bits per pixel) / Add 1 bit for CLEAR and EOI
        frame.clearCode = 1 << frame.codeSize; // CLEAR = 2^minCodeSize
        frame.endOfInfoCode = frame.clearCode + 1;

        log.debug("Min Code Size: {}", frame.codeSize);
        log.debug("Clear Code: {}", frame.clearCode);
        log.debug("End of info: {}", frame.endOfInfoCode);

        final int totalEncodedBytes = computeBlockSize(is);
        assert totalEncodedBytes > 0;

        //transfer all the LZW encoded bytes to a temporary buffer
        var encodedData = new byte[totalEncodedBytes];
        int blockSize, bytesRead = 0, bytesSkipped = 0;
        while ((blockSize = is.readUnsignedByte()) > 0) {
            if (skip.filter(Block.IMAGE_DATA_BLOCK, blockSize, is)) {
                bytesSkipped += is.skipBytes(blockSize);
                continue;
            }
            bytesRead += is.read(encodedData, bytesRead, blockSize);
        }

        assert bytesSkipped != 0 || totalEncodedBytes == bytesRead;

        //start decoding if we have no skipped bytes
        if (bytesSkipped == 0) {
            //Start decoding
            try (var decoder = new GifDecoder(frame)) {
                decoder.decode(encodedData);
            }
            //If interlace flag is set,
            if (frame.isInterlaced())
                frame.data = ImageOps.deinterlace(frame);
        } else {
            log.debug("{}) Image data blocks were skipped. Decoding and/or deinterlacing will not take placee", frame.index);
            frame.skipped = true;
            frame.data = null;
        }

        printDebugHeader("END: Image Block (Total size: {})", bytesRead);
    }

    /**
     * <p>
     * Each image in the Data Stream is composed of an Image
     * Descriptor, an optional Local Color Table, and the image data.  Each
     * image must fit within the boundaries of the Logical Screen, as defined
     * in the Logical Screen Descriptor.
     * <p>
     * The Image Descriptor contains the parameters necessary to process a table
     * based image. The coordinates given in this block refer to coordinates
     * within the Logical Screen, and are given in pixels. This block is a
     * Graphic-Rendering Block, optionally preceded by one or more Control
     * blocks such as the Graphic Control Extension, and may be optionally
     * followed by a Local Color Table; the Image Descriptor is always followed
     * by the image data.
     * <p>
     * This block is REQUIRED for an image.  Exactly one Image Descriptor must
     * be present per image in the Data Stream.  An unlimited number of images
     * may be present per Data Stream.
     *
     * @param is
     *         The {@link ImageInputStream} containing the data to be processed
     * @param frame
     *         The GifFrame currently being processed. This is not required (set to null) if skip flag is true.
     * @param skip
     *         {@code True} if we should skip this block.
     *
     * @apiNote Required Version. 87a.
     * @implNote This skip featture does not include skipping the local color table and image data blocks
     * @see #readLocalColorTable(ImageInputStream, GifFrame, BlockFilter)
     * @see #readImageDataBlocks(ImageInputStream, GifFrame, BlockFilter)
     */
    private void readImageDescriptorBlock(ImageInputStream is, GifFrame frame, boolean skip) throws IOException {
        //save the start position of this block for future reference
        lastImageDescriptorOffset = is.getStreamPosition();

        if (skip) {
            is.skipBytes(LENGTH_IMAGE_DESC_DIMENSIONS); //skip the dimensions
            is.skipBytes(1); //skip the packed byte
            return;
        }

        printDebugHeader("Image Descriptor Block (Frame: {})", frame.getIndex());

        frame.leftPos = is.readShort();
        frame.topPos = is.readShort();
        frame.width = is.readShort();
        frame.height = is.readShort();

        byte fields = is.readByte(); //packed byte
        frame.localColorTableFlag = ((fields & 0b10000000) >>> 7) == 1;
        frame.interlaceFlag = ((fields & 0b01000000) >>> 6) == 1;
        frame.sortFlag = ((fields & 0b00100000) >>> 5) == 1;
        final int localColorTablePower = (fields & 0b00000111) + 1;
        frame.localColorTableSize = 1 << localColorTablePower; // 2 ^ (N + 1)
        int bytesRead = (int) (is.getStreamPosition() - lastImageDescriptorOffset);

        log.debug("Left Pos: {}", frame.leftPos);
        log.debug("Top Pos: {}", frame.topPos);
        log.debug("Width: {}", frame.width);
        log.debug("Height: {}", frame.height);
        log.debug("Local Color Table Flag: {}", frame.localColorTableFlag);
        log.debug("Interlace Flag: {}", frame.interlaceFlag);
        log.debug("Sort Flag: {}", frame.sortFlag);
        log.debug("Local Color Table Size: {}", frame.localColorTableSize);

        assert bytesRead == 9;

        log.debug("Total bytes read from image descriptor: {}", bytesRead);
    }

    /**
     * The Plain Text Extension contains textual data and the
     * parameters necessary to render that data as a graphic, in a simple form.
     * The textual data will be encoded with the 7-bit printable ASCII
     * characters.  Text data are rendered using a grid of character cells
     * defined by the parameters in the block fields. Each character is rendered
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
     * this does not happen. This block requires a Global Color Table to be
     * available; the colors used by this block reference the Global Color Table
     * in the Stream if there is one, or the Global Color Table from a previous
     * Stream, if one was saved. This block is a graphic rendering block,
     * therefore it may be modified by a Graphic Control Extension.  This block
     * is OPTIONAL; any number of them may appear in the Data Stream.
     * <p>
     *
     * @param is
     *         The {@link ImageInputStream} contianing the data to be processed
     *
     * @throws IOException
     *         WHen an I/O error occurs
     * @apiNote Required Version 89a.
     * @implSpec <ol>
     * <li><strong>Extensions and Scope</strong> - The scope of this block is the Plain Text Data
     * Block contained in it. This block may be modified by the Graphic Control
     * Extension.</li>
     *
     * <li><strong>Recommendations</strong> - The data in the Plain Text Extension is assumed to be
     * preformatted. The selection of font and size is left to the discretion of
     * the decoder.  If characters less than 0x20 or greater than 0xf7 are
     * encountered, it is recommended that the decoder display a Space character
     * (0x20). The encoder should use grid and cell dimensions such that an
     * integral number of cells fit in the grid both horizontally as well as
     * vertically.  For broadest compatibility, character cell dimensions should
     * be around 8x8 or 8x16 (width x height); consider an image for unusual
     * sized text.</li>
     * </ol>
     */
    private void readPlainTextExtensionBlock(ImageInputStream is, final GifMetaData image, boolean skip) throws IOException {
        if (skip) {
            is.skipBytes(LENGTH_PLAIN_TEXT_BLOCK + LENGTH_BLOCK_SIZE); //+1 to include size byte in the count
            skipDataBlocks(is); //skip variable-length data blocks
            is.skipBytes(LENGTH_TERIMINATOR);  //skip terminator byte
            return;
        }

        printDebugHeader("Plain Text Extension Block");

        int extBlockSize = is.readUnsignedByte(); //extension block size
        if (extBlockSize <= 0)
            throw new IOException("Empty plain text block");

        final var plainText = new PlainText();
        image.plainText = plainText;
        plainText.leftPos = is.readShort();
        plainText.topPos = is.readShort();
        plainText.textGridWidth = is.readShort();
        plainText.textGridHeight = is.readShort();
        plainText.charCellWidth = is.readUnsignedByte();
        plainText.charCellHeight = is.readUnsignedByte();
        plainText.textForegroundColorIndex = is.readUnsignedByte();
        plainText.textBackgroundColorIndex = is.readUnsignedByte();
        plainText.plainTextData = new ArrayList<>();

        log.debug("Extension Block Size: {}", extBlockSize);
        log.debug("Text Grid Left Pos: {}", plainText.leftPos);
        log.debug("Text Grid Top Pos: {}", plainText.topPos);
        log.debug("Text Grid Width: {}", plainText.textGridWidth);
        log.debug("Text Grid Height: {}", plainText.textGridHeight);
        log.debug("Char Cell Width: {}", plainText.charCellWidth);
        log.debug("Char Cell Height: {}", plainText.charCellHeight);
        log.debug("Text FG Color Index: {}", plainText.textForegroundColorIndex);
        log.debug("Text BG Color Index: {}", plainText.textBackgroundColorIndex);

        readDataBlocks(is, (data, blockSize) -> {
            var label = new String(data.array());
            plainText.plainTextData.add(label);
            log.debug("Plain text label: {}", label);
        });

        log.debug("==========================================================");
    }

    /**
     * The Comment Extension contains textual information which
     * is not part of the actual graphics in the GIF Data Stream. It is suitable
     * for including comments about the graphics, credits, descriptions or any
     * other type of non-control and non-graphic data.  The Comment Extension
     * may be ignored by the decoder, or it may be saved for later processing;
     * under no circumstances should a Comment Extension disrupt or interfere
     * with the processing of the Data Stream.
     * <p>
     * This block is OPTIONAL; any number of them may appear in the Data Stream.
     *
     * @param is
     *         The {@link ImageInputStream} containing the data to be processed
     *
     * @throws IOException
     *         When an I/O error occurs
     */
    private void readCommentExtensionBlock(ImageInputStream is, final GifMetaData image, boolean skip) throws IOException {
        if (skip) {
            ///assuming we have read the introducer and label bytes
            // we should proceed with skipping the data blocks
            skipDataBlocks(is);
            return;
        }
        image.comments = new ArrayList<>();
        readDataBlocks(is, (data, blockSize) -> {
            log.debug("COMMENT: {}", new String(data.array()));
            image.comments.add(new String(data.array()));
        });
    }

    /**
     * <p>
     * The Graphic Control Extension contains parameters used
     * when processing a graphic rendering block. The scope of this extension is
     * the first graphic rendering block to follow. The extension contains only
     * one data sub-block.
     * </p>
     * <p>
     * This block is OPTIONAL; at most one Graphic Control Extension may precede
     * a graphic rendering block. This is the only limit to the number of
     * Graphic Control Extensions that may be contained in a Data Stream.
     * </p>
     * <br />
     * <p><strong>Packed Field Layout</strong></p>
     *
     * <table>
     *    <tr>
     *        <th>Field</th>
     *        <th>Num of Bits</th>
     *    </tr>
     *    <tr>
     *        <td>Reserved</td>
     *        <td>3 Bits</td>
     *    </tr>
     *    <tr>
     *        <td>Disposal Method</td>
     *        <td>3 Bits</td>
     *    </tr>
     *    <tr>
     *         <td>User Input Flag</td>
     *         <td>1 Bit</td>
     *    </tr>
     *    <tr>
     *         <td>Transparent Color Flag</td>
     *         <td>1 Bit</td>
     *    </tr>
     * </table>
     *
     * @param is
     *         {@link InputStream} data
     *
     * @throws IOException
     *         when an I/O related error occurs
     * @apiNote <ol>
     * <li><u>Extensions and Scope</u></li>
     * <p>
     * The scope of this Extension is the graphic
     * rendering block that follows it; it is possible for other extensions to
     * be present between this block and its target. This block can modify the
     * Image Descriptor Block and the Plain Text Extension.
     * </p>
     * <li><u>Recommendations</u></li>
     *
     * <ol>
     * <li>
     * <strong>Disposal Method</strong> - The mode <strong><u><em>Restore To Previous</em></u></strong> is intended to be
     * used in small sections of the graphic; the use of this mode imposes
     * severe demands on the decoder to store the section of the graphic
     * that needs to be saved. For this reason, this mode should be used
     * sparingly.  This mode is not intended to save an entire graphic or
     * large areas of a graphic; when this is the case, the encoder should
     * make every attempt to make the sections of the graphic to be
     * restored be separate graphics in the data stream. In the case where
     * a decoder is not capable of saving an area of a graphic marked as
     * Restore To Previous, it is recommended that a decoder restore to
     * the background color.
     * </li>
     *
     * <li><strong>User Input Flag</strong> - When the flag is set, indicating that user
     * input is expected, the decoder may sound the bell (0x07) to alert
     * the user that input is being expected.  In the absence of a
     * specified Delay Time, the decoder should wait for user input
     * indefinitely.  It is recommended that the encoder not set the User
     * Input Flag without a Delay Time specified.
     * </li>
     * </ol>
     * </ol>
     */
    private void readGraphicsControlExtensionBlock(ImageInputStream is, GifFrame frame, boolean skip) throws IOException {
        if (skip) {
            is.skipBytes(LENGTH_GRAPHIC_BLOCK + LENGTH_BLOCK_SIZE + LENGTH_TERIMINATOR); //+2 to include the size and terminator blocks
            return;
        }

        printDebugHeader("START: Graphics Control Extension Block");
        checkBlockSize(is);

        var packedField = is.readByte();
        frame.disposalMethod = DisposalMethod.get((packedField & 0b00011100) >>> 2); //from bit index: 4 to 2
        frame.userInputFlag = ((packedField & 0b00000010) >>> 1) == 1; //0 - User input is not expected, 1 - User input is expected (bit index: 1)
        frame.transparencyFlag = (packedField & 0b00000001) == 1; //0 - Transparent Index is not given, 1 - Transparent Index is given. (bit index: 0)
        frame.delay = is.readShort();
        frame.transparencyIndex = Byte.toUnsignedInt(is.readByte());

        log.debug("Disposal Method : {}", frame.disposalMethod);
        log.debug("User Input Flag: {}", frame.userInputFlag);
        log.debug("Transparency Flag : {}", frame.userInputFlag);
        log.debug("Delay : {}", frame.delay);
        log.debug("Transparency Index : {}", frame.transparencyIndex);

        //read terminating byte
        byte term = is.readByte();
        assert term == 0;

        printDebugHeader("END: Graphics Control Extension Block");
    }

    /**
     * The Application Extension contains application-specific
     * information; it conforms with the extension block syntax, as described
     * below, and its block label is 0xFF.
     *
     * @param is
     *         The {@link ImageInputStream} instance where the data will be read from
     *
     * @throws IOException
     *         When an I/O error occurs
     * @apiNote Required Version 89a
     */
    private void readApplicationExtensionBlock(ImageInputStream is, final GifMetaData image, boolean skip) throws IOException {
        if (skip) {
            is.skipBytes(LENGTH_APP_IDENTIFIER + LENGTH_APP_AUTHCODE + LENGTH_BLOCK_SIZE);
            skipDataBlocks(is); //note: this also reads the terminator block, so no need to explicitly skip the terminator block after this call
            return;
        }

        checkBlockSize(is); //Note from spec: This field contains the fixed value 11.

        byte[] authCode = new byte[LENGTH_APP_AUTHCODE];
        byte[] ident = new byte[LENGTH_APP_IDENTIFIER];

        //String identifier = readString(is, 8);
        is.read(ident); //application identifier (8-bytes)
        is.read(authCode); //application auth code (3-bytes)

        printDebugHeader("START: Application extension block");

        //log.debug("Extension Block size: {}", extBlockSize);
        log.debug("Identifier: {}", new String(ident));
        log.debug("Auth code: {}", new String(authCode));

        //read all available extensions
        readDataBlocks(is, (data, blockSize) -> {
            //primarily intended for netscape application looping extension,
            // but there are other extensions that follows the same format
            //so we just check the blocksize.
            // so far this is the only application extension we will support
            if (blockSize == 3 && image.loopCount == 0) {
                int subBlockId = Byte.toUnsignedInt(data.get());
                image.loopCount = data.getShort();
                log.debug("ID: {}, LOOP COUNT: {}", subBlockId, image.loopCount);
            }
        });

        printDebugHeader("END: Application extension block");
    }

    /**
     * Reads and process local color table of an image frame
     *
     * @param is
     *         The {@link ImageInputStream} instance to read the data from
     * @param frame
     *         The {@link GifFrame} instance where the local table will be stored
     * @param filter
     *         Optional block filter. If true, descriptor parameter is required.
     *
     * @see #readColorTable(ImageInputStream, int[])
     */
    private int readLocalColorTable(ImageInputStream is, GifFrame frame, BlockFilter filter) throws IOException {
        long startPos = is.getStreamPosition();

        //Skip local color table
        if (filter.filter(Block.LOCAL_COLOR_TABLE)) {
            //since we know that a local color table is preceded with an image descriptor block,
            // we will need to extract the previous bytes to get some information pertaining to the local color table
            if (lastImageDescriptorOffset <= 0)
                throw new IOException("No image descriptor found");

            //go back to image descriptor
            is.mark();
            is.seek(lastImageDescriptorOffset);
            is.skipBytes(LENGTH_IMAGE_DESC_DIMENSIONS);
            byte packedField = is.readByte();
            boolean hasLocalColorTable = readLocalColorTableFlag(packedField);
            int localColorTableSize = readLocalColorTableSize(packedField);
            //reset to previously marked position
            is.reset();

            //proceed skipping the local color table (if applicable)
            if (hasLocalColorTable && localColorTableSize > 0) {
                //multiply by 3 (rgb bytes) to compute the total number of bytes to be skipped
                is.skipBytes(localColorTableSize * 3);
            }

            return (int) (is.getStreamPosition() - startPos);
        }

        //process local color table only if flag is set
        if (!frame.hasLocalColorTable()) {
            log.debug("Local color table not set, skipping");
            return (int) (is.getStreamPosition() - startPos);
        }

        log.debug("Reading local color table (Size: {})", frame.getLocalColorTableSize());
        frame.localColorTable = new int[frame.localColorTableSize];
        readColorTable(is, frame.localColorTable);

        return (int) (is.getStreamPosition() - startPos); //bytes read
    }

    /**
     * Reads and process the global color table of an image. This table will be shared and used by each available frame if no local color table is present.
     *
     * @param is
     *         The {@link ImageInputStream} instance to read the data from
     * @param image
     *         The {@link GifMetaData} instance where global color table will be stored
     *
     * @see #readColorTable(ImageInputStream, int[])
     */
    private void readGlobalColorTable(final ImageInputStream is, final GifMetaData image, BlockFilter filter) throws IOException {
        //Skip local color table
        if (filter.filter(Block.GLOBAL_COLOR_TABLE)) {
            //since we know tha a local color table is preceded with an image descriptor block,
            // we will need to extract the previous bytes to get some information pertaining to the local color table
            if (lastLogicalScreenDescriptorOffset <= 0)
                throw new IOException("No logical screen descriptor found");
            //go back to image descriptor
            is.mark();
            is.seek(lastLogicalScreenDescriptorOffset);
            is.skipBytes(LENGTH_LOGICAL_SCREEN_DIMENSIONS);
            final byte packedField = is.readByte();
            final boolean hasGlobalColorTable = readGlobalColorTableFlag(packedField);
            final int globalColorTableSize = readGlobalColorTableSize(packedField);
            //reset to previously marked position
            is.reset();
            //proceed skipping the local color table (if applicable)
            if (hasGlobalColorTable && globalColorTableSize > 0) {
                //multiply by 3 (rgb bytes) to compute the total number of bytes to be skipped
                is.skipBytes(globalColorTableSize * 3);
            }
            return;
        }

        //process global color table (if applicable)
        if (!image.hasGlobalColorTable() || image.getGlobalColorTableSize() < 0)
            return;

        log.debug("Reading global color table (Size: {})", image.globalColorTableSize);
        image.globalColorTable = new int[image.globalColorTableSize];
        readColorTable(is, image.globalColorTable);
    }

    /**
     * Marks the start of the processing of a new {@link GifFrame}
     *
     * @param image
     *         The {@link GifMetaData} associated with the new frame
     *
     * @return A fresh {@link GifFrame} instance
     */
    private GifFrame beginImageFrame(GifMetaData image) {
        /*if (this.currentFrame != null && !image.getFrames().contains(currentFrame))
            throw new IllegalStateException("beginImageFrame() : Call to beginImageFrame() but another frame is still currently being processed");*/
        log.debug("beginImageFrame() : Creating new image frame");
        currentFrame = new GifFrame(frameIndex++, image);
        /*if (this.is == null)
            image.getFrames().add(currentFrame);*/
        return currentFrame;
    }

    /**
     * Marks the end of processing for the Image Frame
     */
    private void endImageFrame() {
        this.lastFrame = (this.currentFrame != null && !this.currentFrame.skipped) ? this.currentFrame : null;
        this.currentFrame = null;
    }

    /**
     * @return The last image frame that was successfully processed, or null if none
     */
    private GifFrame getLastFrame() {
        return lastFrame;
    }

    /**
     * @return The current frame being processed or null if none.
     */
    private GifFrame getCurrentFrame() {
        return this.currentFrame;
    }

    /**
     * Scans the entire data stream and counts the number of image frames available. This is safe to be called at any position in the data stream.
     *
     * @param is
     *         The {@link ImageInputStream} to scan
     *
     * @return The total number of frames available in this image
     *
     * @throws IOException
     *         When an I/O error occurs
     */
    private int scanFrameSize(ImageInputStream is) throws IOException {
        //Required blocks
        // - Signauture
        // - Logical Screen Descriptor (One per data stream)
        // - Image Descriptor
        final int[] count = {0};
        try {
            is.mark();
            is.seek(0);

            final BlockFilter counter = (blockIdentifier, data) -> {
                if (Block.IMAGE_DESCRIPTOR.equals(blockIdentifier))
                    count[0]++;
                return true;
            };

            //process/skip headers
            readImageHeaderFields(is, null, SKIP_ALL);

            //process/skip the rest of the available blocks
            Block block;
            do {
                block = readBlock(is, metadata, counter);
            } while (block != null && !Block.TRAILER.equals(block));
        } finally {
            is.reset();
        }
        return count[0];
    }

    /**
     * Reads contiguous stream of sub-data blocks from the data stream.
     *
     * @param is
     *         The {@link ByteBuffer} to read the data from
     * @param offset
     *         The starting offset in the destination buffer, the point which data will start to be written.
     * @param dst
     *         A pre-allocated array of bytes where the processed data will be written to
     *
     * @return The total number of bytes read or 0 if no bytes have been read
     */
    private int readDataBlocks(ImageInputStream is, int offset, byte[] dst) throws IOException {
        if (dst == null)
            throw new IllegalArgumentException("Destination buffer must not be null");
        if (dst.length == 0)
            return 0;
        //note: per spec, block size does not take into account the size byte
        int blockSize;
        int dstOffset = offset;
        int bytesRead = 0;
        while ((blockSize = is.readUnsignedByte()) > 0) {
            is.read(dst, dstOffset, blockSize);
            bytesRead += blockSize;
            dstOffset += blockSize;
        }
        return bytesRead;
    }

    /**
     * Reads contiguous stream of sub-data blocks from the data stream.
     *
     * @param is
     *         The {@link ByteBuffer} instance to read the data from
     * @param callback
     *         A callback responsible for processing each found data-block
     *
     * @return The total number of bytes processed (total data block size)
     */
    private int readDataBlocks(ImageInputStream is, BiConsumer<ByteBuffer, Integer> callback) throws IOException {
        int totalSize = 0;
        //note: per spec, block size does not take into account the size byte
        int blockSize;
        while ((blockSize = is.readUnsignedByte()) > 0) {
            var data = new byte[blockSize];
            int bytesRead = is.read(data);
            totalSize += bytesRead;
            //create new bytebuffer instance but share the same content of the source
            if (callback != null)
                callback.accept(ByteBuffer.wrap(data), bytesRead);
        }
        return totalSize;
    }

    @Override
    public void close() throws IOException {
        if (!closed && this.is != null) {
            this.is.close();
            reset();
            closed = true;
            log.debug("Successfully closed the underlying image input stream");
        }
    }

    private void reset() {
        this.lastFrame = null;
        this.currentFrame = null;
        this.metadata = null;
        this.frameIndex = 0;
    }

    /**
     * Interface for on-the-fly filtering of sub/data-blocks during image processing
     *
     * @author Rafael Luis Ibasco
     */
    @FunctionalInterface
    public interface BlockFilter {

        boolean filter(BlockIdentifier block, Object... data);
    }
    //</editor-fold>
}
