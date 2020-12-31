package com.ibasco.gifdecoder;

public interface GifCodeReader {

    int read();

    int getCodeSize();

    int getCodeSizeOffset();

    void increaseCodeSizeOffset();

    void clearCodeSizeOffset();
}
