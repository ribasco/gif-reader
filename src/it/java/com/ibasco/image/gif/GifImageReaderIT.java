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

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;

public class GifImageReaderIT {

    private static final Logger log = LoggerFactory.getLogger(GifImageReaderIT.class);

    private static final Path samplesPath = Path.of(System.getProperty("user.dir"), "samples");

    private static File[] samples;

    @BeforeAll
    static void beforeAll() {
        samples = scanFilesFromPath();
    }

    @Test
    @DisplayName("Read unprocessed samples")
    void testReadSamplesUnprocessed() throws IOException {
        log.info("Testing unprocessed frames");
        for (var file : samples) {
            log.info("Running test for file: {}", file.getName());
            try (var reader = assertDoesNotThrow(() -> new GifImageReader(file))) {
                var metadata = reader.getMetadata();
                assertEquals(reader.getTotalFrames(), reader.getMetadata().getTotalFrames());
                assertTrue(reader.getTotalFrames() > 0);
                assertNotNull(metadata);
                assertTrue(reader.hasRemaining());

                int count = 0;
                while (reader.hasRemaining()) {
                    var frame = reader.read();
                    assertNotNull(frame);
                    assertFalse(frame.isRendered());
                    count++;
                }
                assertEquals(reader.getTotalFrames(), count);
            }
        }
    }

    @Test
    @DisplayName("Read processed samples")
    void testReadSamplesProcessed() throws IOException {
        log.info("Testing processed frames");
        for (var file : samples) {
            log.info("Running test for file: {}", file.getName());
            try (var reader = assertDoesNotThrow(() -> new GifImageReader(file, true))) {
                var metadata = reader.getMetadata();
                int logicalScreenWidth = metadata.getWidth();
                int logicalScreenHeight = metadata.getHeight();

                assertEquals(reader.getTotalFrames(), reader.getMetadata().getTotalFrames());
                assertTrue(reader.getTotalFrames() > 0);
                assertNotNull(metadata);
                assertTrue(reader.hasRemaining());

                int count = 0;
                while (reader.hasRemaining()) {
                    var frame = reader.read();
                    assertNotNull(frame);
                    assertTrue(frame.isRendered());
                    assertEquals(logicalScreenWidth, frame.getWidth());
                    assertEquals(logicalScreenHeight, frame.getHeight());
                    count++;
                }
                assertEquals(reader.getTotalFrames(), count);
            }
        }
    }

    private static File[] scanFilesFromPath() {
        final var directoryFile = GifImageReaderIT.samplesPath.toFile();
        return Arrays.stream(Objects.requireNonNull(directoryFile.list((dir1, name) -> name.toLowerCase().endsWith(".gif"))))
                     .map(s -> Path.of(directoryFile.getAbsolutePath(), s))
                     .map(Path::toFile)
                     .sorted()
                     .toArray(File[]::new);
    }
}
