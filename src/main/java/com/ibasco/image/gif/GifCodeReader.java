package com.ibasco.image.gif;

import java.io.IOException;

public interface GifCodeReader {

    int read() throws IOException;

    int getCodeSize();

    int getCodeSizeOffset();

    void increaseCodeSizeOffset();

    void clearCodeSizeOffset();

    void close() throws IOException;
}
