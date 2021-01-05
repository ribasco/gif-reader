### GIF Decoding Library

A pure java implementation of the  [GIF89a specification](https://www.w3.org/Graphics/GIF/spec-gif89a.txt). 

#### Motivation

After testing some GIF decoding libraries available (ImageIO, Apache Commons Imaging, DhyanB) for Java, I found that there were still cases where some animated images were not processed or handled properly, resulting in deformation on the output image or simply returning in error. This library was developed with the intention of addressing these issues (e.g. `Bad Code`, `ArrayIndexOutOfBoundsException`).  

#### Minimum JDK Version

JDK 11+

#### Limitations

- Android support is not possible at the moment since this library utilizes the `ImageInputStream` class provided by `javax.imageio` package. 

#### Usage:

*Basic usage:*

~~~java
var file = new File("/home/user/example.gif");
try (var reader = new GifImageReader(file)) {
     while (reader.hasRemaining()) {
         GifFrame frame = reader.read();
         //note: Use frame.getData() to extract the raw image data (in INT ARGB format)   
         System.out.printf("Index: %d, Frame: %d x %d", frame.getIndex(), frame.getWidth(), frame.getHeight());
     }
}
~~~

*Using with JavaFX (Converting each frame to WritableImage)*

```java
var source = new File("/home/user/example.gif");

try (var reader = new GifImageReader(source)) {
    int index = 0;
    //scans the entire image and counts the number of frames available (optional)
    int totalFrames = reader.getTotalFrames(); 
    while (reader.hasRemaining()) {
        var frame = reader.read();
        var frameImage = toFxImage(frame);
        //do what you wan with frameImage
    }
}

private static WritableImage toFxImage(GifFrame frame) {
    //Method #1: Using Pixel Writer 
    //img.getPixelWriter().setPixels(0, 0, frame.getWidth(), frame.getHeight(), WritablePixelFormat.getIntArgbInstance(), pixels, 0, frame.getWidth());
        
    //Method #2: Using PixelBuffer    
    return new WritableImage(new PixelBuffer<>(frame.getWidth(), frame.getHeight(), IntBuffer.wrap(frame.getData()), WritablePixelFormat.getIntArgbPreInstance()));
}
```

*Using Block Filters*

Block filters allows you to inspect the current block being processed on-the-fly and provides you an option to skip it. 

~~~java
var file = new File("/home/user/example.gif");
try (var reader = new GifImageReader(file)) {
    var metadata = reader.getMetadata();
    //This block filter lets you skip the first image frame
    reader.setFilter(new GifImageReader.BlockFilter() {
        private int count = 0;
        @Override
        public boolean filter(BlockIdentifier block, Object... data) {
            if (Block.IMAGE_DATA_BLOCK.equals(block)) {
                return count++ == 0;
            }
            //do not skip the rest
            return false;
        }
    });
    int frameCount = 0;
    while (reader.hasRemaining()) {
        var frame = reader.read();
        if (frame == null)
            continue;
        frameCount++;
    }
    System.out.println("Total frames processed: " + frameCount);
}
~~~

### Test results and Comparison

All images used in this test can be found at the [samples](https://github.com/ribasco/gif-reader/tree/master/samples) directory

**GifReader Test**

Note: A buffer overflow warning is shown below to indicate that the output data was trimmed by the decoder. 

```text
13:08:47.846 [main] INFO  com.ibasco.image.gif.GifReaderTest - ====================================================
13:08:47.850 [main] INFO  com.ibasco.image.gif.GifReaderTest - START
13:08:47.850 [main] INFO  com.ibasco.image.gif.GifReaderTest - ====================================================
13:08:48.059 [main] INFO  com.ibasco.image.gif.GifReaderTest - Processed file: sample09-nsfw.gif (Total frames: 20)
13:08:48.245 [main] INFO  com.ibasco.image.gif.GifReaderTest - Processed file: sample03.gif (Total frames: 49)
13:08:48.478 [main] INFO  com.ibasco.image.gif.GifReaderTest - Processed file: sample05.gif (Total frames: 101)
13:08:48.488 [main] INFO  com.ibasco.image.gif.GifReaderTest - Processed file: sample06.gif (Total frames: 10)
13:08:48.591 [main] INFO  com.ibasco.image.gif.GifReaderTest - Processed file: sample02.gif (Total frames: 61)
13:08:48.622 [main] INFO  com.ibasco.image.gif.GifReaderTest - Processed file: sample04.gif (Total frames: 10)
13:08:48.629 [main] WARN  com.ibasco.image.gif.GifDecoder - Buffer overflow: There is not enough space to store 2 elements in the output buffer. Data will be trimmed (Remaining Bytes: 1)
13:08:48.648 [main] WARN  com.ibasco.image.gif.GifDecoder - Buffer overflow: There is not enough space to store 5 elements in the output buffer. Data will be trimmed (Remaining Bytes: 4)
13:08:48.684 [main] INFO  com.ibasco.image.gif.GifReaderTest - Processed file: sample08-nsfw.gif (Total frames: 24)
13:08:48.745 [main] INFO  com.ibasco.image.gif.GifReaderTest - Processed file: sample01.gif (Total frames: 30)
13:08:48.754 [main] INFO  com.ibasco.image.gif.GifReaderTest - Processed file: sample07.gif (Total frames: 3)
13:08:48.755 [main] INFO  com.ibasco.image.gif.GifReaderTest - ====================================================
13:08:48.755 [main] INFO  com.ibasco.image.gif.GifReaderTest - STATUS REPORT
13:08:48.755 [main] INFO  com.ibasco.image.gif.GifReaderTest - ====================================================
13:08:48.755 [main] INFO  com.ibasco.image.gif.GifReaderTest - FILE: sample09-nsfw.gif         FRAMES: 20   , ERROR: None
13:08:48.755 [main] INFO  com.ibasco.image.gif.GifReaderTest - FILE: sample03.gif              FRAMES: 49   , ERROR: None
13:08:48.755 [main] INFO  com.ibasco.image.gif.GifReaderTest - FILE: sample05.gif              FRAMES: 101  , ERROR: None
13:08:48.756 [main] INFO  com.ibasco.image.gif.GifReaderTest - FILE: sample06.gif              FRAMES: 10   , ERROR: None
13:08:48.756 [main] INFO  com.ibasco.image.gif.GifReaderTest - FILE: sample02.gif              FRAMES: 61   , ERROR: None
13:08:48.756 [main] INFO  com.ibasco.image.gif.GifReaderTest - FILE: sample04.gif              FRAMES: 10   , ERROR: None
13:08:48.756 [main] INFO  com.ibasco.image.gif.GifReaderTest - FILE: sample08-nsfw.gif         FRAMES: 24   , ERROR: None
13:08:48.756 [main] INFO  com.ibasco.image.gif.GifReaderTest - FILE: sample01.gif              FRAMES: 30   , ERROR: None
13:08:48.756 [main] INFO  com.ibasco.image.gif.GifReaderTest - FILE: sample07.gif              FRAMES: 3    , ERROR: None
13:08:48.756 [main] INFO  com.ibasco.image.gif.GifReaderTest - ====================================================
13:08:48.756 [main] INFO  com.ibasco.image.gif.GifReaderTest - END
13:08:48.756 [main] INFO  com.ibasco.image.gif.GifReaderTest - ====================================================
13:08:48.758 [main] INFO  com.ibasco.image.gif.GifReaderTest - Processed a total of 308 frames from 9 images (Took 906 ms, Last File: sample07.gif)

Process finished with exit code 0
```

**Javax ImageIO**

```text
13:09:30.501 [main] INFO  com.ibasco.image.gif.ImageIOTest - ====================================================
13:09:30.504 [main] INFO  com.ibasco.image.gif.ImageIOTest - START
13:09:30.504 [main] INFO  com.ibasco.image.gif.ImageIOTest - ====================================================
13:09:30.591 [main] INFO  com.ibasco.image.gif.ImageIOTest - Processed file: sample09-nsfw.gif (Total frames: 9)
13:09:30.613 [main] INFO  com.ibasco.image.gif.ImageIOTest - Processed file: sample03.gif (Total frames: 0)
13:09:30.636 [main] INFO  com.ibasco.image.gif.ImageIOTest - Processed file: sample05.gif (Total frames: 0)
13:09:30.639 [main] INFO  com.ibasco.image.gif.ImageIOTest - Processed file: sample06.gif (Total frames: 0)
13:09:30.650 [main] INFO  com.ibasco.image.gif.ImageIOTest - Processed file: sample02.gif (Total frames: 0)
13:09:30.657 [main] INFO  com.ibasco.image.gif.ImageIOTest - Processed file: sample04.gif (Total frames: 0)
13:09:30.670 [main] INFO  com.ibasco.image.gif.ImageIOTest - Processed file: sample08-nsfw.gif (Total frames: 24)
13:09:30.680 [main] INFO  com.ibasco.image.gif.ImageIOTest - Processed file: sample01.gif (Total frames: 0)
13:09:30.683 [main] INFO  com.ibasco.image.gif.ImageIOTest - Processed file: sample07.gif (Total frames: 3)
13:09:30.683 [main] INFO  com.ibasco.image.gif.ImageIOTest - ====================================================
13:09:30.683 [main] INFO  com.ibasco.image.gif.ImageIOTest - STATUS REPORT
13:09:30.683 [main] INFO  com.ibasco.image.gif.ImageIOTest - ====================================================
13:09:30.691 [main] INFO  com.ibasco.image.gif.ImageIOTest - FILE: sample09-nsfw.gif         FRAMES: 9    , ERROR: Index 4096 out of bounds for length 4096
13:09:30.692 [main] INFO  com.ibasco.image.gif.ImageIOTest - FILE: sample03.gif              FRAMES: 0    , ERROR: Index 4096 out of bounds for length 4096
13:09:30.692 [main] INFO  com.ibasco.image.gif.ImageIOTest - FILE: sample05.gif              FRAMES: 0    , ERROR: Index 4096 out of bounds for length 4096
13:09:30.692 [main] INFO  com.ibasco.image.gif.ImageIOTest - FILE: sample06.gif              FRAMES: 0    , ERROR: Index 4096 out of bounds for length 4096
13:09:30.692 [main] INFO  com.ibasco.image.gif.ImageIOTest - FILE: sample02.gif              FRAMES: 0    , ERROR: Index 4096 out of bounds for length 4096
13:09:30.693 [main] INFO  com.ibasco.image.gif.ImageIOTest - FILE: sample04.gif              FRAMES: 0    , ERROR: Index 4096 out of bounds for length 4096
13:09:30.693 [main] INFO  com.ibasco.image.gif.ImageIOTest - FILE: sample08-nsfw.gif         FRAMES: 24   , ERROR: None
13:09:30.693 [main] INFO  com.ibasco.image.gif.ImageIOTest - FILE: sample01.gif              FRAMES: 0    , ERROR: Index 4096 out of bounds for length 4096
13:09:30.693 [main] INFO  com.ibasco.image.gif.ImageIOTest - FILE: sample07.gif              FRAMES: 3    , ERROR: None
13:09:30.693 [main] INFO  com.ibasco.image.gif.ImageIOTest - ====================================================
13:09:30.693 [main] INFO  com.ibasco.image.gif.ImageIOTest - END
13:09:30.694 [main] INFO  com.ibasco.image.gif.ImageIOTest - ====================================================
13:09:30.696 [main] INFO  com.ibasco.image.gif.ImageIOTest - Processed a total of 36 frames from 9 images (Took 189 ms, Last File: sample07.gif)

Process finished with exit code 0
```

**Apache Commons Imaging**

```text
13:35:09.349 [main] INFO  c.ibasco.image.gif.ApacheImagingTest - ====================================================
13:35:09.353 [main] INFO  c.ibasco.image.gif.ApacheImagingTest - START
13:35:09.353 [main] INFO  c.ibasco.image.gif.ApacheImagingTest - ====================================================
13:35:09.431 [main] INFO  c.ibasco.image.gif.ApacheImagingTest - Processed file: sample09-nsfw.gif (Total frames: 0)
13:35:10.121 [main] INFO  c.ibasco.image.gif.ApacheImagingTest - Processed file: sample03.gif (Total frames: 49)
13:35:10.269 [main] INFO  c.ibasco.image.gif.ApacheImagingTest - Processed file: sample05.gif (Total frames: 101)
13:35:10.276 [main] INFO  c.ibasco.image.gif.ApacheImagingTest - Processed file: sample06.gif (Total frames: 10)
13:35:10.336 [main] INFO  c.ibasco.image.gif.ApacheImagingTest - Processed file: sample02.gif (Total frames: 61)
13:35:10.355 [main] INFO  c.ibasco.image.gif.ApacheImagingTest - Processed file: sample04.gif (Total frames: 10)
13:35:10.355 [main] INFO  c.ibasco.image.gif.ApacheImagingTest - Processed file: sample08-nsfw.gif (Total frames: 0)
13:35:10.411 [main] INFO  c.ibasco.image.gif.ApacheImagingTest - Processed file: sample01.gif (Total frames: 30)
13:35:10.412 [main] INFO  c.ibasco.image.gif.ApacheImagingTest - Processed file: sample07.gif (Total frames: 0)
13:35:10.412 [main] INFO  c.ibasco.image.gif.ApacheImagingTest - ====================================================
13:35:10.412 [main] INFO  c.ibasco.image.gif.ApacheImagingTest - STATUS REPORT
13:35:10.412 [main] INFO  c.ibasco.image.gif.ApacheImagingTest - ====================================================
13:35:10.418 [main] INFO  c.ibasco.image.gif.ApacheImagingTest - FILE: sample09-nsfw.gif         FRAMES: 0    , ERROR: Bad Code: -1 codes: 258 code_size: 9, table: 4096
13:35:10.419 [main] INFO  c.ibasco.image.gif.ApacheImagingTest - FILE: sample03.gif              FRAMES: 49   , ERROR: None
13:35:10.419 [main] INFO  c.ibasco.image.gif.ApacheImagingTest - FILE: sample05.gif              FRAMES: 101  , ERROR: None
13:35:10.419 [main] INFO  c.ibasco.image.gif.ApacheImagingTest - FILE: sample06.gif              FRAMES: 10   , ERROR: None
13:35:10.419 [main] INFO  c.ibasco.image.gif.ApacheImagingTest - FILE: sample02.gif              FRAMES: 61   , ERROR: None
13:35:10.419 [main] INFO  c.ibasco.image.gif.ApacheImagingTest - FILE: sample04.gif              FRAMES: 10   , ERROR: None
13:35:10.419 [main] INFO  c.ibasco.image.gif.ApacheImagingTest - FILE: sample08-nsfw.gif         FRAMES: 0    , ERROR: Bad Code: -1 codes: 66 code_size: 7, table: 4096
13:35:10.420 [main] INFO  c.ibasco.image.gif.ApacheImagingTest - FILE: sample01.gif              FRAMES: 30   , ERROR: None
13:35:10.420 [main] INFO  c.ibasco.image.gif.ApacheImagingTest - FILE: sample07.gif              FRAMES: 0    , ERROR: Bad Code: -1 codes: 258 code_size: 9, table: 4096
13:35:10.420 [main] INFO  c.ibasco.image.gif.ApacheImagingTest - ====================================================
13:35:10.420 [main] INFO  c.ibasco.image.gif.ApacheImagingTest - END
13:35:10.420 [main] INFO  c.ibasco.image.gif.ApacheImagingTest - ====================================================
13:35:10.422 [main] INFO  c.ibasco.image.gif.ApacheImagingTest - Processed a total of 261 frames from 9 images (Took 1066 ms, Last File: sample07.gif)

Process finished with exit code 0
```