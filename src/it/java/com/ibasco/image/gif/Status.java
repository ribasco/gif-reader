package com.ibasco.image.gif;

import java.io.File;

public class Status {

    File file;

    int frameCount;

    Exception error;

    Status(File file, int frameCount, Exception error) {
        this.file = file;
        this.frameCount = frameCount;
        this.error = error;
    }

    @Override
    public String toString() {
        return String.format("FILE: %-25s FRAMES: %-5d, ERROR: %s", file.getName(), frameCount, error != null ? error.getMessage() : "None");
    }
}
