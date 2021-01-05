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

import java.util.List;

/**
 * Plain text extension fields
 *
 * @author Rafael Luis Ibasco
 */
public class PlainText {

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
     * @return Column number, in pixels, of the left
     * edge of the text grid, with respect to the left edge of the Logical
     * Screen.
     */
    public int getLeftPos() {
        return leftPos;
    }

    /**
     * @return Row number, in pixels, of the top edge
     * of the text grid, with respect to the top edge of the Logical
     * Screen.
     */
    public int getTopPos() {
        return topPos;
    }

    /**
     * @return Width of the text grid in pixels.
     */
    public int getTextGridWidth() {
        return textGridWidth;
    }

    /**
     * @return Height of the text grid in pixels.
     */
    public int getTextGridHeight() {
        return textGridHeight;
    }

    /**
     * @return Width, in pixels, of each cell in the grid.
     */
    public int getCharCellWidth() {
        return charCellWidth;
    }

    /**
     * @return Height, in pixels, of each cell in the grid.
     */
    public int getCharCellHeight() {
        return charCellHeight;
    }

    /**
     * @return Index into the Global Color Table to be used to render the text foreground.
     */
    public int getTextForegroundColorIndex() {
        return textForegroundColorIndex;
    }

    /**
     * @return Index into the Global Color Table to be used to render the text background.
     */
    public int getTextBackgroundColorIndex() {
        return textBackgroundColorIndex;
    }

    /**
     * @return Sequence of sub-blocks, each of size at most
     * 255 bytes and at least 1 byte, with the size in a byte preceding
     * the data.  The end of the sequence is marked by the Block
     * Terminator.
     */
    public List<String> getPlainTextData() {
        return plainTextData;
    }
}
