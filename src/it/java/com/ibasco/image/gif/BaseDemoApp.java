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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

abstract public class BaseDemoApp {

    public static final String ANSI_RESET = "\u001B[0m";

    public static final String ANSI_BLACK = "\u001B[30m";

    public static final String ANSI_RED = "\u001B[31m";

    public static final String ANSI_GREEN = "\u001B[32m";

    public static final String ANSI_YELLOW = "\u001B[33m";

    public static final String ANSI_BLUE = "\u001B[34m";

    public static final String ANSI_PURPLE = "\u001B[35m";

    public static final String ANSI_CYAN = "\u001B[36m";

    public static final String ANSI_WHITE = "\u001B[37m";

    private final File[] files;

    private int totalFrames;

    private int frameCount;

    private int fileNameMaxLength;

    protected BaseDemoApp() {
        var samplesPath = Path.of(System.getProperty("user.dir"), "samples");
        //var samplesPath = Path.of("/home/ribasco/media/pictures");
        files = scanFilesFromPath(samplesPath);
    }

    protected File[] scanFilesFromPath(Path path) {
        final var directoryFile = path.toFile();
        return Arrays.stream(Objects.requireNonNull(directoryFile.list((dir1, name) -> name.toLowerCase().endsWith(".gif"))))
                     .map(s -> Path.of(directoryFile.getAbsolutePath(), s))
                     .map(Path::toFile)
                     .sorted()
                     .toArray(File[]::new);
    }

    protected void scanImageFiles(File... files) throws Exception {
        long scanStart = System.nanoTime();
        int maxLength = 0;
        for (var file : files) {
            int fileNameLength = file.getName().length();
            if (fileNameLength > maxLength)
                maxLength = fileNameLength;
            long readStart = System.nanoTime();
            int total;
            try (var reader = new GifImageReader(file)) {
                total = reader.getTotalFrames();
            }
            long readDuration = System.nanoTime() - readStart;
            logn("FILE: %s FRAMES: %d DURATION: %dms", String.format("%-69s", file.getName()), total, Duration.ofNanos(readDuration).toMillis());
        }
        long scanDuration = System.nanoTime() - scanStart;
        logn("Scan ended in %dms", Duration.ofNanos(scanDuration).toMillis());
    }

    protected void updateFrameCount() {
        updateFrameCount(1);
    }

    protected void updateFrameCount(int count) {
        this.frameCount += count;
        this.totalFrames += count;
    }

    private void printHeader(String text) {
        logn(colorize("=".repeat(104), ANSI_CYAN));
        logn("%s - %s", colorize(text.toUpperCase(), ANSI_CYAN), colorize(getClass().getSimpleName(), ANSI_BLUE));
        logn(colorize("=".repeat(104), ANSI_CYAN));
    }

    protected void runDemo() throws Exception {
        printHeader("START");
        int totalImages = 0;
        totalFrames = 0;
        long startNanos = System.nanoTime();
        File lastFileProcessed = null;
        var statusList = new ArrayList<Status>();
        try {
            for (var file : files) {
                frameCount = 0;
                lastFileProcessed = file;
                long startTime = System.nanoTime();
                Status status = null;
                try {
                    readFile(file);
                    status = new Status(file, frameCount, null, Duration.ofNanos(System.nanoTime() - startTime));
                } catch (Throwable ex) {
                    status = new Status(file, frameCount, ex, Duration.ofNanos(System.nanoTime() - startTime));
                    if (ex instanceof Error) {
                        break;
                    }
                } finally {
                    if (status != null) {
                        statusList.add(status);
                        String fileName = String.format("%-20s", file.getName());
                        logn("%s %s", field("FILE", fileName), field("STATUS", status.error == null ? colorize("PASS", ANSI_GREEN) : colorize("FAIL", ANSI_RED)));
                        //logn("File: {} Status: {}", fileName, status.error == null ? colorize("PASS", ANSI_GREEN) : colorize("FAIL", ANSI_RED));
                    }
                    totalImages++;
                }
            }
            if (!statusList.isEmpty()) {
                printHeader("SUMMARY");
                this.fileNameMaxLength = getMaxLength(statusList);
                for (var status : statusList) {
                    logn(status.toString());
                }
            }

            printHeader("END");
        } finally {
            long intervalNanos = System.nanoTime() - startNanos;
            logcn("Processed a total of %d frames from %d images (Took %d ms, Last File: %s)", ANSI_WHITE, totalFrames, totalImages, Duration.ofNanos(intervalNanos).toMillis(), (lastFileProcessed != null) ? lastFileProcessed.getName() : "N/A");
        }
    }

    protected static void logcn(String msg, String color, Object... params) {
        System.out.printf(color + (msg) + "%n" + ANSI_RESET, params);
    }

    protected static void logn(String msg, Object... params) {
        System.out.printf((msg) + "%n", params);
    }

    protected static String colorize(String text, String color) {
        return String.format("%s%s%s", color, text, ANSI_RESET);
    }

    private String field(String field, String value) {
        return field(field, value, 1);
    }

    private String field(String field, String value, int padding) {
        return String.format(ANSI_BLUE + "[%s]: " + ANSI_WHITE + "%-" + padding + "s" + ANSI_RESET, field, value);
    }

    protected int getMaxLength(List<Status> statusList) {
        return statusList.stream().map(Status::getFile).map(File::getName).map(String::length).max(Integer::compare).orElse(20);
    }

    abstract protected void readFile(File file) throws Exception;

    private class Status {

        File file;

        int frameCount;

        Throwable error;

        Duration duration;

        public Status(File file, int frameCount, Throwable error, Duration duration) {
            this.file = file;
            this.frameCount = frameCount;
            this.error = error;
            this.duration = duration;
        }

        private File getFile() {
            return file;
        }

        private String getErrorString() {
            String error;
            if (this.error != null) {
                if (this.error.getMessage() != null && !this.error.getMessage().isBlank()) {
                    error = this.error.getMessage();
                } else {
                    error = this.error.getClass().getName();
                }
            } else {
                error = "None";
            }
            return error;
        }

        @Override
        public String toString() {
            var errString = getErrorString();
            return String.format("%s %s %s %s",
                                 field("FILE", file.getName(), fileNameMaxLength),
                                 field("FRAMES", String.valueOf(frameCount), 5),
                                 field("DURATION", String.format("%d ms", duration.toMillis()), 10),
                                 field("ERROR", colorize(errString, "None".equalsIgnoreCase(errString) ? ANSI_WHITE : ANSI_RED))
            );
        }
    }
}
