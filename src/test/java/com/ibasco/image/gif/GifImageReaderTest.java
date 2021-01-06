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

import com.ibasco.image.gif.test.BaseTest;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Spy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;

class GifImageReaderTest extends BaseTest {

    private static final Logger log = LoggerFactory.getLogger(GifImageReaderTest.class);

    private static final int TEST002_TOTAL_SIZE = 200396;

    private static final int TEST002_TOTAL_FRAMES = 20;

    //class under test
    private GifImageReader reader;

    @Spy
    private final BufferedInputStream is = new BufferedInputStream(getClass().getResourceAsStream("/test002.gif"));

    @BeforeEach
    void setUp() throws Exception {
        is.mark(Integer.MAX_VALUE);
        assertEquals(TEST002_TOTAL_SIZE, is.available());
        reader = assertDoesNotThrow(() -> new GifImageReader(is));
        assertNotNull(reader);
        assertNotNull(reader.getMetadata());
    }

    @AfterEach
    void tearDown() {
        assertDoesNotThrow(is::reset);
    }

    @Test
    @DisplayName("Test Total Frames")
    void testGetTotalFrames() {
        assertEquals(TEST002_TOTAL_FRAMES, reader.getTotalFrames());
    }

    @Test
    @DisplayName("Test GIF Signature and version")
    void testSignature() {
        assertEquals("GIF", reader.getMetadata().getSignature());
        assertEquals("89a", reader.getMetadata().getVersion());
    }

    @Test
    @DisplayName("Test Logical Screen Descriptor")
    void testLogicalScreenDescriptor() {
        assertEquals(210, reader.getMetadata().getWidth());
        assertEquals(210, reader.getMetadata().getHeight());
        assertEquals(0, reader.getMetadata().getPixelAspectRatio());
        assertEquals(0, reader.getMetadata().getBackgroundColorIndex());
        assertEquals(256, reader.getMetadata().getGlobalColorTableSize());
        assertEquals(256, reader.getMetadata().getGlobalColorTable().length);
        assertEquals(256, reader.getMetadata().getColorResolution());
        assertTrue(reader.getMetadata().getBackgroundColor() != 0);
        assertTrue(reader.getMetadata().hasGlobalColorTable());
        //assertNotNull();
        log.info("Data: {}", reader.getMetadata().getComments());
    }

    @Test
    @DisplayName("Test Read First Frame")
    void testReadFrame() {
        assertTrue(reader.hasRemaining());
        var frame = assertDoesNotThrow(() -> reader.read());
        assertNotNull(frame);
        assertEquals(0, frame.getIndex());
        assertNotNull(frame.getData());
        assertEquals(frame.getWidth() * frame.getHeight(), frame.getData().length);
    }

    @Test
    @DisplayName("Test Read All Frames")
    void testReadAllFrames() {
        assertDoesNotThrow(() -> {
            int frameCount = 0;
            while (reader.hasRemaining()) {
                var frame = reader.read();
                frameCount++;
                assertNotNull(frame);
            }
            assertEquals(TEST002_TOTAL_FRAMES, frameCount);
        });
        assertDoesNotThrow(() -> assertEquals(0, is.available()));
    }
}
