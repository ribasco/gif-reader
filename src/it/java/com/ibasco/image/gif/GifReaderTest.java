package com.ibasco.image.gif;

import com.ibasco.image.gif.enums.Block;
import com.ibasco.image.gif.enums.BlockIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class GifReaderTest {

    private static final Logger log = LoggerFactory.getLogger(GifReaderTest.class);

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
                int frameCount = 0;
                lastFileProcessed = file;
                try (var reader = new GifImageReader(file)) {
                    var metadata = reader.getMetadata();
                    /*reader.setFilter(new GifImageReader.BlockFilter() {
                        private int count = 0;
                        @Override
                        public boolean filter(BlockIdentifier block, Object... data) {
                            if (Block.IMAGE_DATA_BLOCK.equals(block)) {
                                return count++ == 0;
                            }
                            return false;
                        }
                    });*/
                    while (reader.hasRemaining()) {
                        var frame = reader.read();
                        if (frame == null)
                            continue;
                        totalFrames++;
                        frameCount++;
                    }
                    statusList.add(new Status(file, frameCount, null));
                } catch (Exception ex) {
                    statusList.add(new Status(file, frameCount, ex));
                    ex.printStackTrace();
                } finally {
                    totalImages++;
                    log.info("Processed file: {} (Total frames: {})", file.getName(), frameCount);
                }
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
