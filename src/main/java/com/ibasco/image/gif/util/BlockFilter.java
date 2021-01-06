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

package com.ibasco.image.gif.util;

import com.ibasco.image.gif.enums.BlockIdentifier;

/**
 * Interface for on-the-fly filtering of sub/data-blocks during image processing
 *
 * @author Rafael Luis Ibasco
 */
@FunctionalInterface
public interface BlockFilter {

    boolean filter(BlockIdentifier block, Object... data);
}
