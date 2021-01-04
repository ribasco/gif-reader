package com.ibasco.image.gif.exceptions;

import java.io.IOException;

public class UnsupportedBlockException extends IOException {

    public UnsupportedBlockException() {
    }

    public UnsupportedBlockException(String message) {
        super(message);
    }

    public UnsupportedBlockException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnsupportedBlockException(Throwable cause) {
        super(cause);
    }
}
