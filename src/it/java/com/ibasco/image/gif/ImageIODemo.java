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

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class ImageIODemo extends BaseDemoApp {

    public static void main(String[] args) throws Exception {
        new ImageIODemo().runDemo();
    }

    @Override
    protected void readFile(File file) throws Exception {
        try (var is = ImageIO.createImageInputStream(file)) {
            var it = ImageIO.getImageReadersBySuffix("gif");
            if (!it.hasNext())
                throw new IOException("No reader found for gif");
            var reader = it.next();
            reader.setInput(is);
            int numOfFrames = reader.getNumImages(true);
            for (int frameIndex = 0; frameIndex < numOfFrames; frameIndex++) {
                reader.read(frameIndex);
                updateFrameCount();
            }
        }
    }
}
