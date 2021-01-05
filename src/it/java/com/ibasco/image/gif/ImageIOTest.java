package com.ibasco.image.gif;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class ImageIOTest {

    private static final Logger log = LoggerFactory.getLogger(ImageIOTest.class);

    public static void main(String[] args) throws Exception {
        File samplesDir = Path.of(System.getProperty("user.dir"), "samples").toFile();
        var imageFiles = Arrays.stream(Objects.requireNonNull(samplesDir.list((dir1, name) -> name.toLowerCase().endsWith(".gif")))).map(s -> Path.of(samplesDir.getAbsolutePath(), s)).map(Path::toFile);
        var imageList = imageFiles.toArray(File[]::new);
        processImageFiles(imageList);
    }

    private static void processImageFiles(File... files) throws IOException {
        log.info("====================================================");
        log.info("START");
        log.info("====================================================");
        int totalImages = 0;
        int totalFrames = 0;
        long startNanos = System.nanoTime();
        File lastFileProcessed = null;
        var statusList = new ArrayList<Status>();
        try {
            for (var file : files) {
                lastFileProcessed = file;
                int frameCount = 0;
                try (var is = ImageIO.createImageInputStream(file)) {
                    var it = ImageIO.getImageReadersBySuffix("gif");
                    if (!it.hasNext())
                        throw new IOException("No reader found for gif");
                    var reader = it.next();
                    reader.setInput(is);
                    int numOfFrames = reader.getNumImages(true);
                    for (int frameIndex = 0; frameIndex < numOfFrames; frameIndex++) {
                        reader.read(frameIndex);
                        frameCount++;
                        totalFrames++;
                    }
                    statusList.add(new Status(file, frameCount, null));
                } catch (Exception ex) {
                    statusList.add(new Status(file, frameCount, ex));
                } finally {
                    log.info("Processed file: {} (Total frames: {})", file.getName(), frameCount);
                }
                totalImages++;
            }
            if (!statusList.isEmpty()) {
                log.info("====================================================");
                log.info("STATUS REPORT");
                log.info("====================================================");
                for (var status : statusList) {
                    log.info(status.toString());
                }
            }
            log.info("====================================================");
            log.info("END");
            log.info("====================================================");
        } finally {
            long intervalNanos = System.nanoTime() - startNanos;
            log.info("Processed a total of {} frames from {} images (Took {} ms, Last File: {})", totalFrames, totalImages, Duration.ofNanos(intervalNanos).toMillis(), (lastFileProcessed != null) ? lastFileProcessed.getName() : "N/A");
        }
    }
}
