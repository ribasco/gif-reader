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

package com.ibasco.image.gif.util;

import com.ibasco.image.gif.GifFrame;

import java.util.Arrays;
import java.util.function.BiFunction;

/**
 * Utility methods for GIF related operations
 *
 * @author Rafael Luis Ibasco
 */
public final class ImageOps {

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
    public static int toArgb(int red, int green, int blue) {
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

    public static void clear(int[] data) {
        Arrays.fill(data, 0);
    }

    public static void clear(int x, int y, int width, int height, int stride, int[] data) {
        //copy current frame data to previous image buffer
        for (int dstY = 0; dstY < height; dstY++) {
            for (int dstX = 0; dstX < width; dstX++) {
                int x0 = dstX + x;
                int y0 = dstY + y;
                data[x0 + y0 * stride] = 0;
            }
        }
    }

    public static void copy(int srcWidth, int srceHeight, int[] src, int dstX, int dstY, int dstWidth, int dstHeight, int[] dst) {
        copy(srcWidth, srceHeight, src, dstX, dstY, dstWidth, dstHeight, dst, null);
    }

    public static void copy(int srcWidth, int srceHeight, int[] src, int dstX, int dstY, int dstWidth, int dstHeight, int[] dst, BiFunction<Integer, Integer, Integer> blender) {
        //copy current frame data to previous image buffer
        for (int srcY = 0; srcY < srceHeight; srcY++) {
            for (int srcX = 0; srcX < srcWidth; srcX++) {
                int srcIndex = srcX + srcY * srcWidth;
                int srcColor = src[srcIndex];
                int dX = dstX + srcX;
                int dY = dstY + srcY;
                int dstIndex = dX + dY * dstWidth;
                if ((dX > (dstWidth - 1)) || (dY > (dstHeight - 1)))
                    continue;
                int dstColor = dst[dstIndex];
                dst[dstIndex] = blender == null ? alphaBlend(srcColor, dstColor) : blender.apply(srcColor, dstColor);
            }
        }
    }

    public static int sourceBlend(int source, int desc) {
        return source;
    }

    public static int alphaBlend(int source, int dest) {
        //C = A(alpha / 255) + B(1 - (alpha / 255))
        // a = alpha component
        // A = source (fg)
        // B = dest (bg)
        // C = final output

        //break down each color into their respective
        // 4-channeel values (Alpha, Red, Gree, Blue)

        //source components
        int srcA = (source >> 24) & 0xff; //alpha
        int srcR = (source >> 16) & 0xff; //red
        int srcG = (source >> 8) & 0xff; //green
        int srcB = source & 0xff; //blue

        //dest components
        int dstA = (dest >> 24) & 0xff; //alpha
        int dstR = (dest >> 16) & 0xff; //red
        int dstG = (dest >> 8) & 0xff; //green
        int dstB = dest & 0xff; //blue

        float ratio = (float) srcA / 255f; //use the alpha component of source (foreground)
        int outA = (int) ((srcA * ratio) + (dstA * (1 - ratio)));
        int outR = (int) ((srcR * ratio) + (dstR * (1 - ratio)));
        int outG = (int) ((srcG * ratio) + (dstG * (1 - ratio)));
        int outB = (int) ((srcB * ratio) + (dstB * (1 - ratio)));

        return ((outA & 0xff) << 24) | ((outR & 0xff) << 16) | ((outG & 0xff) << 8) | (outB & 0xff);
    }

    //credits to Dhyan Blum's GifDecoder implementation for this bit
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
