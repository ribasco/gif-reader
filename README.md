### GIF Decoding Library

A pure java implementation of the  [GIF89a specification](https://www.w3.org/Graphics/GIF/spec-gif89a.txt).

#### Motivation

After testing some GIF decoding libraries available (ImageIO, Apache Commons Imaging) for Java, I found that there were still cases where some animated images were not processed or handled properly, resulting in deformation on the output image or simply returning in error. This library was developed with the intention of addressing these issues (e.g. `Bad Code`, `ArrayIndexOutOfBoundsException`).

#### Minimum JDK Version

JDK 11+

#### Limitations

- Android support is not possible at the moment since this library utilizes the `ImageInputStream` class provided by `javax.imageio` package.

#### Usage:

*Basic usage:*

~~~java
var file=new File("/home/user/example.gif");
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
var source=new File("/home/user/example.gif");

try(var reader=new GifImageReader(source)){
    int index=0;
    //scans the entire image and counts the number of frames available (optional)
    int totalFrames=reader.getTotalFrames();
    while(reader.hasRemaining()){
        var frame=reader.read();
        var frameImage=toFxImage(frame);
        //do what you wan with frameImage
    }
}

private static WritableImage toFxImage(GifFrame frame){
    //Method #1: Using Pixel Writer 
    //img.getPixelWriter().setPixels(0, 0, frame.getWidth(), frame.getHeight(), WritablePixelFormat.getIntArgbInstance(), pixels, 0, frame.getWidth());

    //Method #2: Using PixelBuffer    
    return new WritableImage(new PixelBuffer<>(frame.getWidth(),frame.getHeight(),IntBuffer.wrap(frame.getData()),WritablePixelFormat.getIntArgbPreInstance()));
}
```

*Using Block Filters*

Block filters allows you to inspect the current block being processed on-the-fly and provides you an option to skip it.

~~~java
var file=new File("/home/user/example.gif");
try(var reader=new GifImageReader(file)){
    var metadata=reader.getMetadata();
    //This block filter lets you skip the first image frame
    reader.setFilter(new GifImageReader.BlockFilter(){
        private int count=0;
        @Override
        public boolean filter(BlockIdentifier block,Object...data){
            if(Block.IMAGE_DATA_BLOCK.equals(block)){
                return count++==0;
            }
            //do not skip the rest
            return false;
        }
    });
    int frameCount=0;
    while(reader.hasRemaining()){
        var frame=reader.read();
        if(frame==null)
            continue;
        frameCount++;
    }
    System.out.println("Total frames processed: "+frameCount);
}
~~~

### Test results and Comparison

All images used in this test can be found at the [samples](https://github.com/ribasco/gif-reader/tree/master/samples) directory

**GifReader Test**

Note: A buffer overflow warning is shown below to indicate that the output data was trimmed by the decoder.

```text
====================================================
START
====================================================
Processed file: sample09-nsfw.gif (Total frames: 20)
Processed file: sample03.gif (Total frames: 49)
Processed file: sample05.gif (Total frames: 101)
Processed file: sample06.gif (Total frames: 10)
Processed file: sample02.gif (Total frames: 61)
Processed file: sample04.gif (Total frames: 10)
13:08:48.629 [main] WARN  com.ibasco.image.gif.GifDecoder - Buffer overflow: There is not enough space to store 2 elements in the output buffer. Data will be trimmed (Remaining Bytes: 1)
13:08:48.648 [main] WARN  com.ibasco.image.gif.GifDecoder - Buffer overflow: There is not enough space to store 5 elements in the output buffer. Data will be trimmed (Remaining Bytes: 4)
Processed file: sample08-nsfw.gif (Total frames: 24)
Processed file: sample01.gif (Total frames: 30)
Processed file: sample07.gif (Total frames: 3)
====================================================
STATUS REPORT
====================================================
FILE: sample09-nsfw.gif         FRAMES: 20   , ERROR: None
FILE: sample03.gif              FRAMES: 49   , ERROR: None
FILE: sample05.gif              FRAMES: 101  , ERROR: None
FILE: sample06.gif              FRAMES: 10   , ERROR: None
FILE: sample02.gif              FRAMES: 61   , ERROR: None
FILE: sample04.gif              FRAMES: 10   , ERROR: None
FILE: sample08-nsfw.gif         FRAMES: 24   , ERROR: None
FILE: sample01.gif              FRAMES: 30   , ERROR: None
FILE: sample07.gif              FRAMES: 3    , ERROR: None
====================================================
END
====================================================
Processed a total of 308 frames from 9 images (Took 906 ms, Last File: sample07.gif)
```

**Javax ImageIO**

```text
====================================================
START
====================================================
Processed file: sample09-nsfw.gif (Total frames: 9)
Processed file: sample03.gif (Total frames: 0)
Processed file: sample05.gif (Total frames: 0)
Processed file: sample06.gif (Total frames: 0)
Processed file: sample02.gif (Total frames: 0)
Processed file: sample04.gif (Total frames: 0)
Processed file: sample08-nsfw.gif (Total frames: 24)
Processed file: sample01.gif (Total frames: 0)
Processed file: sample07.gif (Total frames: 3)
====================================================
STATUS REPORT
====================================================
FILE: sample09-nsfw.gif         FRAMES: 9    , ERROR: Index 4096 out of bounds for length 4096
FILE: sample03.gif              FRAMES: 0    , ERROR: Index 4096 out of bounds for length 4096
FILE: sample05.gif              FRAMES: 0    , ERROR: Index 4096 out of bounds for length 4096
FILE: sample06.gif              FRAMES: 0    , ERROR: Index 4096 out of bounds for length 4096
FILE: sample02.gif              FRAMES: 0    , ERROR: Index 4096 out of bounds for length 4096
FILE: sample04.gif              FRAMES: 0    , ERROR: Index 4096 out of bounds for length 4096
FILE: sample08-nsfw.gif         FRAMES: 24   , ERROR: None
FILE: sample01.gif              FRAMES: 0    , ERROR: Index 4096 out of bounds for length 4096
FILE: sample07.gif              FRAMES: 3    , ERROR: None
====================================================
END
====================================================
Processed a total of 36 frames from 9 images (Took 189 ms, Last File: sample07.gif)
```

**Apache Commons Imaging**

```text
====================================================
START
====================================================
Processed file: sample09-nsfw.gif (Total frames: 0)
Processed file: sample03.gif (Total frames: 49)
Processed file: sample05.gif (Total frames: 101)
Processed file: sample06.gif (Total frames: 10)
Processed file: sample02.gif (Total frames: 61)
Processed file: sample04.gif (Total frames: 10)
Processed file: sample08-nsfw.gif (Total frames: 0)
Processed file: sample01.gif (Total frames: 30)
Processed file: sample07.gif (Total frames: 0)
====================================================
STATUS REPORT
====================================================
FILE: sample09-nsfw.gif         FRAMES: 0    , ERROR: Bad Code: -1 codes: 258 code_size: 9, table: 4096
FILE: sample03.gif              FRAMES: 49   , ERROR: None
FILE: sample05.gif              FRAMES: 101  , ERROR: None
FILE: sample06.gif              FRAMES: 10   , ERROR: None
FILE: sample02.gif              FRAMES: 61   , ERROR: None
FILE: sample04.gif              FRAMES: 10   , ERROR: None
FILE: sample08-nsfw.gif         FRAMES: 0    , ERROR: Bad Code: -1 codes: 66 code_size: 7, table: 4096
FILE: sample01.gif              FRAMES: 30   , ERROR: None
FILE: sample07.gif              FRAMES: 0    , ERROR: Bad Code: -1 codes: 258 code_size: 9, table: 4096
====================================================
END
====================================================
Processed a total of 261 frames from 9 images (Took 1066 ms, Last File: sample07.gif)
```