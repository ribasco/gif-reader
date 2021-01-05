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

import java.io.File;

public class Status {

    File file;

    int frameCount;

    Exception error;

    Status(File file, int frameCount, Exception error) {
        this.file = file;
        this.frameCount = frameCount;
        this.error = error;
    }

    @Override
    public String toString() {
        return String.format("FILE: %-25s FRAMES: %-5d, ERROR: %s", file.getName(), frameCount, error != null ? error.getMessage() : "None");
    }
}
