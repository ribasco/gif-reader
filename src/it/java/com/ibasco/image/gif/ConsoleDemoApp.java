package com.ibasco.image.gif;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

public class ConsoleDemoApp {

    private static final Logger log = LoggerFactory.getLogger(ConsoleDemoApp.class);

    public static void main(String[] args) throws Exception {



        var imageDirectory = new File("/home/ribasco/media/pictures/");

        var imageFiles = Arrays.stream(Objects.requireNonNull(imageDirectory.list((dir1, name) -> name.toLowerCase().endsWith(".gif")))).map(s -> Path.of(imageDirectory.getAbsolutePath(), s)).map(Path::toFile);
        var imageList = imageFiles.toArray(File[]::new);
        /*for (int i=0; i < 100; i++)
            processImageFiles(imageList);*/
        processImageFiles(imageList);
    }

    private static void processImageFiles(File... files) throws IOException {
        int totalImages = 0;
        int totalFrames = 0;
        long startNanos = System.nanoTime();
        File lastFileProcessed = null;
        var status = new HashMap<File, Exception>();
        try {
            for (var file : files) {
                lastFileProcessed = file;
                try (var reader = new GifImageReader(file)) {
                    var metadata = reader.getMetadata();
                    log.debug("==========================================================");
                    log.debug("PROCESSING IMAGE FILE: {}", file.getName());
                    log.debug("Frame count: {}", reader.getTotalFrames());

                    while (reader.hasRemaining()) {
                        var frame = reader.read();
                        if (frame == null)
                            return;
                        log.debug("{}) Frame: {} x {} (Total Frames: {})", frame.getIndex(), frame.getWidth(), frame.getHeight(), reader.getTotalFrames());
                        totalFrames++;
                    }
                    log.debug("There were {} total frames extracted from image '{}'", reader.getTotalFrames(), file.getName());
                    status.put(file, null);
                } catch (Exception ex) {
                    status.put(file, ex);
                    ex.printStackTrace();
                } finally {
                    totalImages++;
                }
            }
            if (!status.isEmpty()) {
                log.debug("====================================================");
                log.debug("STATUS REPORT");
                log.debug("====================================================");
                for (var entry : status.entrySet()) {
                    log.error("File: {}, Error: {}", entry.getKey().getName(), entry.getValue() != null ? entry.getValue().getMessage() : "NONE");
                }
            }
        } finally {
            long intervalNanos = System.nanoTime() - startNanos;
            log.debug("Processed a total of {} frames from {} images (Tool {} seconds, Last File: {})", totalFrames, totalImages, Duration.ofNanos(intervalNanos).toSeconds(), (lastFileProcessed != null) ? lastFileProcessed.getName() : "N/A");
        }
    }
}
