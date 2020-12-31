### GIF Decoding Library

A pure java implementation of the  [GIF89a specification](https://www.w3.org/Graphics/GIF/spec-gif89a.txt). 

#### Minimum version required

JDK 11+

#### Usage:

Basic usage:

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

Using with JavaFX (Converting each frame to WritableImage)

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
    return builder.build();
}

private static WritableImage toFxImage(GifFrame frame) {
    //Method #1: Using Pixel Writer 
    //img.getPixelWriter().setPixels(0, 0, frame.getWidth(), frame.getHeight(), WritablePixelFormat.getIntArgbInstance(), pixels, 0, frame.getWidth());
        
    //Method #2: Using PixelBuffer    
    return new WritableImage(new PixelBuffer<>(frame.getWidth(), frame.getHeight(), IntBuffer.wrap(frame.getData()), WritablePixelFormat.getIntArgbPreInstance()));
}
```