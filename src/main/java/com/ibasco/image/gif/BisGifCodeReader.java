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

import org.apache.commons.compress.utils.BitInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteOrder;

/**
 * GIF Code Reader using {@link BitInputStream}
 *
 * @author Rafael Luis Ibasco
 */
public class BisGifCodeReader extends BaseGifCodeReader {

    private final BitInputStream bis;

    public BisGifCodeReader(int codeSize, byte[] data) {
        super(codeSize);
        this.bis = new BitInputStream(new ByteArrayInputStream(data), ByteOrder.nativeOrder());
    }

    @Override
    public int read() throws IOException {
        return (int) bis.readBits(getCodeSize());
    }

    @Override
    public void close() throws IOException {
        bis.close();
    }
}
