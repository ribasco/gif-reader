package com.ibasco.image.gif.util;

import com.ibasco.image.gif.GifFrame;

/**
 * Utility methods for GIF related operations
 *
 * @author Rafael Luis Ibasco
 */
public final class ImageOps {

    //borrowed from Dhyan Blum's GifDecoder implementation
    public static int[] deinterlace(final GifFrame frame) {
        final int[] src = frame.getData();
        final int w = frame.getWidth(), h = frame.getHeight(), wh = frame.getWidth() * frame.getHeight();
        final int[] dest = new int[src.length];
        // Interlaced images are organized in 4 sets of pixel lines
        final int set2Y = (h + 7) >>> 3; // Line no. = ceil(h/8.0)
        final int set3Y = set2Y + ((h + 3) >>> 3); // ceil(h-4/8.0)
        final int set4Y = set3Y + ((h + 1) >>> 2); // ceil(h-2/4.0)
        // Sets' start indices in source array
        final int set2 = w * set2Y, set3 = w * set3Y, set4 = w * set4Y;
        // Line skips in destination array
        final int w2 = w << 1, w4 = w2 << 1, w8 = w4 << 1;
        // Group 1 contains every 8th line starting from 0
        int from = 0, to = 0;
        for (; from < set2; from += w, to += w8) {
            System.arraycopy(src, from, dest, to, w);
        } // Group 2 contains every 8th line starting from 4
        for (to = w4; from < set3; from += w, to += w8) {
            System.arraycopy(src, from, dest, to, w);
        } // Group 3 contains every 4th line starting from 2
        for (to = w2; from < set4; from += w, to += w4) {
            System.arraycopy(src, from, dest, to, w);
        } // Group 4 contains every 2nd line starting from 1 (biggest group)
        for (to = w; from < wh; from += w, to += w2) {
            System.arraycopy(src, from, dest, to, w);
        }
        return dest; // All pixel lines have now been rearranged
    }
}
