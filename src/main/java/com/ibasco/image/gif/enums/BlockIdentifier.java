package com.ibasco.image.gif.enums;

import com.ibasco.image.gif.exceptions.UnsupportedBlockException;

public interface BlockIdentifier {

    int getCodeInt();

    byte getCodeByte();

    String getName();

    default BlockCategory getCategory() throws UnsupportedBlockException {
        return BlockCategory.get(getCodeInt());
    }
}
