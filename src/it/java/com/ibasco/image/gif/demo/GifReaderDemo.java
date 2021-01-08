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

package com.ibasco.image.gif.demo;

import com.ibasco.image.gif.GifImageReader;

import java.io.File;
import java.io.IOException;

public class GifReaderDemo extends BaseDemoApp {

    public static void main(String[] args) throws Exception {
        new GifReaderDemo().runDemo();
    }

    @Override
    protected void readFile(File file) throws IOException {
        try (var reader = new GifImageReader(file, true)) {
            //var metadata = reader.getMetadata();
            while (reader.hasRemaining()) {
                var frame = reader.read();
                if (frame == null)
                    continue;
                updateFrameCount();
            }
        }
    }
}
