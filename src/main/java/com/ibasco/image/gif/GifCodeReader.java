package com.ibasco.image.gif;

import java.io.Closeable;
import java.io.IOException;

public interface GifCodeReader extends Closeable {

    int read() throws IOException;

    int getCodeSize();

    int getCodeSizeOffset();

    void increaseCodeSizeOffset();

    void clearCodeSizeOffset();

    void close() throws IOException;
}
