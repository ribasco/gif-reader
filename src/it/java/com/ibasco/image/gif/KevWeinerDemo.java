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

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;

public class KevWeinerDemo extends BaseDemoApp {

    public static void main(String[] args) throws Exception {
        new KevWeinerDemo().runDemo();
    }

    @Override
    protected void readFile(File file) throws Exception {
        var decoder = new KevWeinerGifDecoder();
        decoder.read(new FileInputStream(file));
        int maxFrames = decoder.getFrameCount();

        for (int index = 0; index < maxFrames; index++) {
            BufferedImage frame = decoder.getFrame(index);
            updateFrameCount();
        }
    }
}
