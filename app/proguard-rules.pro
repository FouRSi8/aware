-keep class com.aware.app.data.** { *; }
-keepattributes *Annotation*

# Apache POI's core jar exposes optional desktop drawing, OSGi, and build-time
# annotation types. aware uses only OLE encryption and HSSF cell reading.
-dontwarn aQute.bnd.annotation.**
-dontwarn edu.umd.cs.findbugs.annotations.**
-dontwarn java.awt.**
-dontwarn org.osgi.framework.**

