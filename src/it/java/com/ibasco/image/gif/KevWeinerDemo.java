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

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class KevWeinerDemo {
    private static final Logger log = LoggerFactory.getLogger(KevWeinerDemo.class);

    public static void main(String[] args) throws Exception {
        File samplesDir = Path.of(System.getProperty("user.dir"), "samples").toFile();
        //var samplesDir = new File("/home/ribasco/media/pictures");
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

                try {
                    var decoder = new KevWeinerGifDecoder();
                    decoder.read(new FileInputStream(file));
                    int maxFrames = decoder.getFrameCount();

                    for (int index = 0; index < maxFrames; index++) {
                        BufferedImage frame = decoder.getFrame(index);
                        totalFrames++;
                        frameCount++;
                    }
                    statusList.add(new Status(file, frameCount, null));
                } catch (Exception e) {
                    statusList.add(new Status(file, frameCount, e));
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
