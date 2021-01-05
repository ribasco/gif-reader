module com.ibasco.gifreader {
    exports com.ibasco.image.gif;
    exports com.ibasco.image.gif.enums;
    exports com.ibasco.image.gif.exceptions;
    exports com.ibasco.image.gif.util;

    requires org.slf4j;
    requires java.desktop;
    requires org.apache.commons.compress;
}