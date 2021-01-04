package com.ibasco.image.gif.exceptions;

import java.io.IOException;

public class InvalidSignatureException extends IOException {

    public InvalidSignatureException(String message) {
        super(message);
    }
}
